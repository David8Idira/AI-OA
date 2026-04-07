package com.aioa.im.service.impl;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.StrUtil;
import com.aioa.common.exception.BusinessException;
import com.aioa.common.result.ResultCode;
import com.aioa.im.dto.ConversationCreateDTO;
import com.aioa.im.entity.Conversation;
import com.aioa.im.entity.ConversationMember;
import com.aioa.im.entity.Message;
import com.aioa.im.mapper.ConversationMapper;
import com.aioa.im.mapper.ConversationMemberMapper;
import com.aioa.im.mapper.MessageMapper;
import com.aioa.im.service.ConversationService;
import com.aioa.im.service.ConversationServiceImplSingleton;
import com.aioa.im.vo.ConversationVO;
import com.aioa.im.vo.MemberVO;
import com.aioa.im.vo.MessageVO;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Conversation Service Implementation
 */
@Service
@RequiredArgsConstructor
public class ConversationServiceImpl extends ServiceImpl<ConversationMapper, Conversation>
        implements ConversationService {

    private final ConversationMemberMapper memberMapper;
    private final MessageMapper messageMapper;
    private final StringRedisTemplate redisTemplate;

    private static final String UNREAD_KEY_PREFIX = "aioa:im:unread:";

    @PostConstruct
    public void init() {
        // Register singleton for WebSocket handler circular dependency
        ConversationServiceImplSingleton.INSTANCE = this;
    }

    @Override
    public List<ConversationVO> getConversationList(String userId, Integer type, Integer page, Integer size) {
        // Get user's conversation IDs
        LambdaQueryWrapper<ConversationMember> memberWrapper = new LambdaQueryWrapper<>();
        memberWrapper.eq(ConversationMember::getUserId, userId);
        memberWrapper.eq(ConversationMember::getStatus, 1);
        List<ConversationMember> memberships = memberMapper.selectList(memberWrapper);

        if (CollUtil.isEmpty(memberships)) {
            return new ArrayList<>();
        }

        List<String> conversationIds = memberships.stream()
                .map(ConversationMember::getConversationId)
                .collect(Collectors.toList());

        // Query conversations
        LambdaQueryWrapper<Conversation> wrapper = new LambdaQueryWrapper<>();
        wrapper.in(Conversation::getId, conversationIds);
        wrapper.eq(Conversation::getStatus, 1);
        if (type != null) {
            wrapper.eq(Conversation::getType, type);
        }
        wrapper.orderByDesc(Conversation::getTopStatus)
                .orderByDesc(Conversation::getUpdateTime);

        // Build conversation VO list
        List<Conversation> conversations = this.list(wrapper);
        Map<String, Integer> unreadMap = getUnreadCounts(userId, conversationIds);
        Map<String, ConversationMember> membershipMap = memberships.stream()
                .collect(Collectors.toMap(ConversationMember::getConversationId, m -> m));

        List<ConversationVO> voList = new ArrayList<>();
        for (Conversation conv : conversations) {
            ConversationVO vo = toConversationVO(conv);
            ConversationMember membership = membershipMap.get(conv.getId());
            if (membership != null) {
                vo.setUnreadCount(unreadMap.getOrDefault(conv.getId(), 0));
                vo.setMuteStatus(membership.getMuteStatus());
                vo.setTopStatus(membership.getTopStatus());
            }
            voList.add(vo);
        }

        // Sort: pinned first, then by last message time
        voList.sort(Comparator
                .comparing(ConversationVO::getTopStatus, Comparator.reverseOrder())
                .thenComparing(ConversationVO::getLastMessageTime, Comparator.nullsLast(Comparator.reverseOrder())));

        // Pagination
        int start = (page - 1) * size;
        int end = Math.min(start + size, voList.size());
        if (start >= voList.size()) {
            return new ArrayList<>();
        }
        return voList.subList(start, end);
    }

    @Override
    public ConversationVO getConversationById(String conversationId, String userId) {
        Conversation conv = this.getById(conversationId);
        if (conv == null) {
            throw new BusinessException(ResultCode.NOT_FOUND, "Conversation not found");
        }

        // Verify user is a member
        if (!isMember(conversationId, userId)) {
            throw new BusinessException(ResultCode.FORBIDDEN, "You are not a member of this conversation");
        }

        ConversationVO vo = toConversationVO(conv);
        vo.setUnreadCount(getConversationUnreadCountRedis(conversationId, userId));

        // Get members
        List<MemberVO> members = getConversationMembers(conversationId);
        vo.setMembers(members);
        vo.setMemberCount(members.size());

        return vo;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ConversationVO createConversation(String userId, ConversationCreateDTO dto) {
        // Validate
        if (dto.getType() == 1) {
            // Private chat - should use getOrCreatePrivateConversation
            throw new BusinessException(ResultCode.BAD_REQUEST, "For private chat, use the private chat API");
        }
        if (dto.getType() == 2 || dto.getType() == 3) {
            if (StrUtil.isBlank(dto.getName())) {
                throw new BusinessException(ResultCode.BAD_REQUEST, "Group/Channel name is required");
            }
        }

        // Create conversation
        Conversation conv = new Conversation();
        conv.setType(dto.getType());
        conv.setName(dto.getName());
        conv.setAvatar(dto.getAvatar());
        conv.setOwnerId(userId);
        conv.setDescription(dto.getDescription());
        conv.setMaxMembers(dto.getMaxMembers() != null ? dto.getMaxMembers() : -1);
        conv.setStatus(1);
        conv.setUnreadCount(0);
        conv.setMuteStatus(0);
        conv.setTopStatus(0);
        conv.setArchiveStatus(0);
        this.save(conv);

        // Add creator as member (owner)
        addMember(conv.getId(), userId, userId, 1);

        // Add other members
        if (CollUtil.isNotEmpty(dto.getMemberIds())) {
            for (String memberId : dto.getMemberIds()) {
                if (!memberId.equals(userId)) {
                    addMember(conv.getId(), memberId, userId, 3);
                }
            }
        }

        return getConversationById(conv.getId(), userId);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ConversationVO getOrCreatePrivateConversation(String userId, String otherUserId) {
        if (StrUtil.isBlank(otherUserId)) {
            throw new BusinessException(ResultCode.BAD_REQUEST, "Other user ID is required");
        }

        // Find existing private conversation
        LambdaQueryWrapper<Conversation> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Conversation::getType, 1)
                .eq(Conversation::getStatus, 1);
        List<Conversation> conversations = this.list(wrapper);

        for (Conversation conv : conversations) {
            if (isMember(conv.getId(), userId) && isMember(conv.getId(), otherUserId)) {
                return getConversationById(conv.getId(), userId);
            }
        }

        // Create new private conversation
        Conversation conv = new Conversation();
        conv.setType(1);
        conv.setOwnerId(userId);
        conv.setStatus(1);
        conv.setUnreadCount(0);
        conv.setMuteStatus(0);
        conv.setTopStatus(0);
        conv.setArchiveStatus(0);
        this.save(conv);

        // Add both users as members
        addMember(conv.getId(), userId, userId, 1);
        addMember(conv.getId(), otherUserId, userId, 1);

        return getConversationById(conv.getId(), userId);
    }

    @Override
    public boolean markAsRead(String conversationId, String userId, String lastReadMsgId) {
        if (!isMember(conversationId, userId)) {
            throw new BusinessException(ResultCode.FORBIDDEN, "You are not a member of this conversation");
        }

        // Update member's last read
        LambdaUpdateWrapper<ConversationMember> updateWrapper = new LambdaUpdateWrapper<>();
        updateWrapper.eq(ConversationMember::getConversationId, conversationId)
                .eq(ConversationMember::getUserId, userId)
                .set(ConversationMember::getLastReadMsgId, lastReadMsgId)
                .set(ConversationMember::getLastReadTime, LocalDateTime.now())
                .set(ConversationMember::getUnreadCount, 0);
        memberMapper.update(null, updateWrapper);

        // Clear Redis unread count
        redisTemplate.delete(UNREAD_KEY_PREFIX + conversationId + ":" + userId);

        return true;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean deleteConversation(String conversationId, String userId) {
        Conversation conv = this.getById(conversationId);
        if (conv == null) {
            throw new BusinessException(ResultCode.NOT_FOUND, "Conversation not found");
        }

        // Remove member
        LambdaUpdateWrapper<ConversationMember> updateWrapper = new LambdaUpdateWrapper<>();
        updateWrapper.eq(ConversationMember::getConversationId, conversationId)
                .eq(ConversationMember::getUserId, userId)
                .set(ConversationMember::getStatus, 0)
                .set(ConversationMember::getLeaveTime, LocalDateTime.now());
        memberMapper.update(null, updateWrapper);

        // If private chat or last member, delete the conversation
        LambdaQueryWrapper<ConversationMember> memberWrapper = new LambdaQueryWrapper<>();
        memberWrapper.eq(ConversationMember::getConversationId, conversationId)
                .eq(ConversationMember::getStatus, 1);
        long activeMembers = memberMapper.selectCount(memberWrapper);

        if (conv.getType() == 1 || activeMembers == 0) {
            this.removeById(conversationId);
        }

        return true;
    }

    @Override
    public boolean muteConversation(String conversationId, String userId, Integer muteStatus) {
        if (!isMember(conversationId, userId)) {
            throw new BusinessException(ResultCode.FORBIDDEN, "You are not a member of this conversation");
        }

        LambdaUpdateWrapper<ConversationMember> updateWrapper = new LambdaUpdateWrapper<>();
        updateWrapper.eq(ConversationMember::getConversationId, conversationId)
                .eq(ConversationMember::getUserId, userId)
                .set(ConversationMember::getMuteStatus, muteStatus);
        memberMapper.update(null, updateWrapper);

        return true;
    }

    @Override
    public boolean topConversation(String conversationId, String userId, Integer topStatus) {
        if (!isMember(conversationId, userId)) {
            throw new BusinessException(ResultCode.FORBIDDEN, "You are not a member of this conversation");
        }

        LambdaUpdateWrapper<ConversationMember> updateWrapper = new LambdaUpdateWrapper<>();
        updateWrapper.eq(ConversationMember::getConversationId, conversationId)
                .eq(ConversationMember::getUserId, userId)
                .set(ConversationMember::getTopStatus, topStatus);
        memberMapper.update(null, updateWrapper);

        return true;
    }

    @Override
    public boolean updateConversationInfo(String conversationId, String userId, String name, String avatar) {
        Conversation conv = this.getById(conversationId);
        if (conv == null) {
            throw new BusinessException(ResultCode.NOT_FOUND, "Conversation not found");
        }

        // Only owner or admin can update
        ConversationMember membership = getMembership(conversationId, userId);
        if (membership == null) {
            throw new BusinessException(ResultCode.FORBIDDEN, "You are not a member of this conversation");
        }
        if (!conv.getOwnerId().equals(userId) && membership.getRole() > 2) {
            throw new BusinessException(ResultCode.FORBIDDEN, "Only owner or admin can update");
        }

        if (StrUtil.isNotBlank(name)) {
            conv.setName(name);
        }
        if (avatar != null) {
            conv.setAvatar(avatar);
        }
        return this.updateById(conv);
    }

    // ==================== Helper Methods ====================

    private boolean isMember(String conversationId, String userId) {
        return getMembership(conversationId, userId) != null;
    }

    private ConversationMember getMembership(String conversationId, String userId) {
        LambdaQueryWrapper<ConversationMember> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ConversationMember::getConversationId, conversationId)
                .eq(ConversationMember::getUserId, userId)
                .eq(ConversationMember::getStatus, 1);
        return memberMapper.selectOne(wrapper);
    }

    private void addMember(String conversationId, String userId, String inviterId, Integer role) {
        ConversationMember member = new ConversationMember();
        member.setConversationId(conversationId);
        member.setUserId(userId);
        member.setRole(role);
        member.setJoinTime(LocalDateTime.now());
        member.setInviterId(inviterId);
        member.setUnreadCount(0);
        member.setMuteStatus(0);
        member.setTopStatus(0);
        member.setSortOrder(0);
        member.setStatus(1);
        memberMapper.insert(member);
    }

    private List<MemberVO> getConversationMembers(String conversationId) {
        LambdaQueryWrapper<ConversationMember> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ConversationMember::getConversationId, conversationId)
                .eq(ConversationMember::getStatus, 1)
                .orderByAsc(ConversationMember::getRole)
                .orderByAsc(ConversationMember::getJoinTime);
        List<ConversationMember> members = memberMapper.selectList(wrapper);

        return members.stream().map(m -> {
            MemberVO vo = new MemberVO();
            vo.setUserId(m.getUserId());
            vo.setNickname(m.getNickname());
            vo.setAvatar(m.getAvatar());
            vo.setRole(m.getRole());
            vo.setJoinTime(m.getJoinTime());
            vo.setMuteStatus(m.getMuteStatus());
            return vo;
        }).collect(Collectors.toList());
    }

    private Map<String, Integer> getUnreadCounts(String userId, List<String> conversationIds) {
        LambdaQueryWrapper<ConversationMember> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ConversationMember::getUserId, userId)
                .eq(ConversationMember::getStatus, 1)
                .in(ConversationMember::getConversationId, conversationIds);
        List<ConversationMember> members = memberMapper.selectList(wrapper);

        return members.stream()
                .collect(Collectors.toMap(
                        ConversationMember::getConversationId,
                        ConversationMember::getUnreadCount,
                        (a, b) -> a
                ));
    }

    private Integer getConversationUnreadCountRedis(String conversationId, String userId) {
        String key = UNREAD_KEY_PREFIX + conversationId + ":" + userId;
        String count = redisTemplate.opsForValue().get(key);
        return count != null ? Integer.parseInt(count) : 0;
    }

    private ConversationVO toConversationVO(Conversation conv) {
        ConversationVO vo = new ConversationVO();
        vo.setId(conv.getId());
        vo.setType(conv.getType());
        vo.setName(conv.getName());
        vo.setAvatar(conv.getAvatar());
        vo.setOwnerId(conv.getOwnerId());
        vo.setLastMessageContent(conv.getLastMessageContent());
        vo.setLastMessageTime(conv.getLastMessageTime());
        vo.setCreateTime(conv.getCreateTime());
        vo.setUpdateTime(conv.getUpdateTime());
        return vo;
    }
}
