package com.aioa.im.service.impl;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.StrUtil;
import com.aioa.common.exception.BusinessException;
import com.aioa.common.result.ResultCode;
import com.aioa.im.dto.SendMessageDTO;
import com.aioa.im.entity.Conversation;
import com.aioa.im.entity.ConversationMember;
import com.aioa.im.entity.Message;
import com.aioa.im.mapper.ConversationMapper;
import com.aioa.im.mapper.ConversationMemberMapper;
import com.aioa.im.mapper.MessageMapper;
import com.aioa.im.service.ConversationService;
import com.aioa.im.service.MessageService;
import com.aioa.im.vo.ConversationVO;
import com.aioa.im.vo.MessageVO;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Message Service Implementation
 */
@Service
@RequiredArgsConstructor
public class MessageServiceImpl extends ServiceImpl<MessageMapper, Message>
        implements MessageService {

    private final ConversationMapper conversationMapper;
    private final ConversationMemberMapper memberMapper;
    private final ConversationService conversationService;
    private final StringRedisTemplate redisTemplate;

    private static final String UNREAD_KEY_PREFIX = "aioa:im:unread:";

    @Override
    public List<MessageVO> getMessageList(String conversationId, String userId, String beforeMsgId,
                                          Integer page, Integer size) {
        // Verify user is a member
        if (!isMember(conversationId, userId)) {
            throw new BusinessException(ResultCode.FORBIDDEN, "You are not a member of this conversation");
        }

        LambdaQueryWrapper<Message> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Message::getConversationId, conversationId)
                .eq(Message::getSenderDeleted, 0)
                .eq(Message::getRecallStatus, 0);

        if (StrUtil.isNotBlank(beforeMsgId)) {
            // Load messages before a specific message
            Message beforeMsg = this.getById(beforeMsgId);
            if (beforeMsg != null) {
                wrapper.lt(Message::getCreateTime, beforeMsg.getCreateTime());
            }
        }

        wrapper.orderByDesc(Message::getCreateTime);
        wrapper.last("LIMIT " + size);

        List<Message> messages = this.list(wrapper);

        // Convert to VO and reverse to chronological order
        List<MessageVO> voList = new ArrayList<>();
        for (Message msg : messages) {
            MessageVO vo = toMessageVO(msg, userId);
            // Load reply if exists
            if (StrUtil.isNotBlank(msg.getReplyId())) {
                Message reply = this.getById(msg.getReplyId());
                if (reply != null) {
                    vo.setReply(toMessageVO(reply, userId));
                }
            }
            voList.add(0, vo);
        }

        return voList;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public MessageVO sendMessage(String userId, SendMessageDTO dto) {
        String conversationId = dto.getConversationId();

        // Get or create conversation for private chat
        if (StrUtil.isBlank(conversationId)) {
            if (StrUtil.isNotBlank(dto.getReceiverId())) {
                ConversationVO privateConv = conversationService
                        .getOrCreatePrivateConversation(userId, dto.getReceiverId());
                conversationId = privateConv.getId();
            } else {
                throw new BusinessException(ResultCode.BAD_REQUEST, "Conversation ID or receiver ID is required");
            }
        }

        // Verify user is a member
        if (!isMember(conversationId, userId)) {
            throw new BusinessException(ResultCode.FORBIDDEN, "You are not a member of this conversation");
        }

        // Validate content
        if (dto.getType() == 1 && StrUtil.isBlank(dto.getContent())) {
            throw new BusinessException(ResultCode.BAD_REQUEST, "Text message content is required");
        }

        // Get conversation info
        Conversation conversation = conversationMapper.selectById(conversationId);
        if (conversation == null) {
            throw new BusinessException(ResultCode.NOT_FOUND, "Conversation not found");
        }

        // Create message
        Message message = new Message();
        message.setConversationId(conversationId);
        message.setSenderId(userId);
        message.setType(dto.getType());
        message.setContent(dto.getContent());
        message.setExtra(dto.getExtra());
        message.setReplyId(dto.getReplyId());
        message.setForwardId(dto.getForwardId());
        message.setAtUserIds(dto.getAtUserIds());
        message.setAtAll(dto.getAtAll() != null ? dto.getAtAll() : 0);
        message.setReadStatus(0);
        message.setRecallStatus(0);
        message.setSenderDeleted(0);
        message.setMsgStatus(1); // sent
        message.setReactionCount(0);

        // Set reply content preview
        if (StrUtil.isNotBlank(dto.getReplyId())) {
            Message replyMsg = this.getById(dto.getReplyId());
            if (replyMsg != null) {
                message.setReplyContent(StrUtil.sub(replyMsg.getContent(), 0, 100));
            }
        }

        this.save(message);

        // Update conversation's last message
        String preview = StrUtil.sub(dto.getContent(), 0, 199);
        LambdaUpdateWrapper<Conversation> updateWrapper = new LambdaUpdateWrapper<>();
        updateWrapper.eq(Conversation::getId, conversationId)
                .set(Conversation::getLastMessageId, message.getId())
                .set(Conversation::getLastMessageContent, preview)
                .set(Conversation::getLastMessageTime, LocalDateTime.now().toString());
        conversationMapper.update(null, updateWrapper);

        // Increment unread count for other members (via Redis)
        incrementUnreadForMembers(conversationId, userId, message.getId());

        return toMessageVO(message, userId);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean recallMessage(String messageId, String userId) {
        Message message = this.getById(messageId);
        if (message == null) {
            throw new BusinessException(ResultCode.NOT_FOUND, "Message not found");
        }

        // Only sender can recall
        if (!message.getSenderId().equals(userId)) {
            throw new BusinessException(ResultCode.FORBIDDEN, "Only sender can recall this message");
        }

        // Can only recall within 2 minutes (business rule)
        if (message.getCreateTime() != null) {
            LocalDateTime twoMinutesAgo = LocalDateTime.now().minusMinutes(2);
            if (message.getCreateTime().isBefore(twoMinutesAgo)) {
                throw new BusinessException(ResultCode.BAD_REQUEST, "Can only recall message within 2 minutes");
            }
        }

        LambdaUpdateWrapper<Message> updateWrapper = new LambdaUpdateWrapper<>();
        updateWrapper.eq(Message::getId, messageId)
                .set(Message::getRecallStatus, 1)
                .set(Message::getRecallTime, LocalDateTime.now());
        return this.update(updateWrapper);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean deleteMessage(String messageId, String userId) {
        Message message = this.getById(messageId);
        if (message == null) {
            throw new BusinessException(ResultCode.NOT_FOUND, "Message not found");
        }

        // Only sender can delete
        if (!message.getSenderId().equals(userId)) {
            throw new BusinessException(ResultCode.FORBIDDEN, "Only sender can delete this message");
        }

        LambdaUpdateWrapper<Message> updateWrapper = new LambdaUpdateWrapper<>();
        updateWrapper.eq(Message::getId, messageId)
                .set(Message::getSenderDeleted, 1);
        return this.update(updateWrapper);
    }

    @Override
    public MessageVO getMessageById(String messageId) {
        Message message = this.getById(messageId);
        if (message == null) {
            throw new BusinessException(ResultCode.NOT_FOUND, "Message not found");
        }
        return toMessageVO(message, message.getSenderId());
    }

    @Override
    public boolean markMessageRead(String conversationId, String userId, String messageId) {
        if (!isMember(conversationId, userId)) {
            throw new BusinessException(ResultCode.FORBIDDEN, "You are not a member of this conversation");
        }

        // Update member's last read
        LambdaUpdateWrapper<ConversationMember> updateWrapper = new LambdaUpdateWrapper<>();
        updateWrapper.eq(ConversationMember::getConversationId, conversationId)
                .eq(ConversationMember::getUserId, userId)
                .set(ConversationMember::getLastReadMsgId, messageId)
                .set(ConversationMember::getLastReadTime, LocalDateTime.now())
                .set(ConversationMember::getUnreadCount, 0);
        memberMapper.update(null, updateWrapper);

        // Clear Redis unread
        redisTemplate.delete(UNREAD_KEY_PREFIX + conversationId + ":" + userId);

        return true;
    }

    @Override
    public long getTotalUnreadCount(String userId) {
        LambdaQueryWrapper<ConversationMember> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ConversationMember::getUserId, userId)
                .eq(ConversationMember::getStatus, 1);
        List<ConversationMember> members = memberMapper.selectList(wrapper);

        long total = 0;
        for (ConversationMember member : members) {
            String key = UNREAD_KEY_PREFIX + member.getConversationId() + ":" + userId;
            String count = redisTemplate.opsForValue().get(key);
            if (count != null) {
                total += Long.parseLong(count);
            } else {
                total += member.getUnreadCount() != null ? member.getUnreadCount() : 0;
            }
        }
        return total;
    }

    @Override
    public long getConversationUnreadCount(String conversationId, String userId) {
        String key = UNREAD_KEY_PREFIX + conversationId + ":" + userId;
        String count = redisTemplate.opsForValue().get(key);
        if (count != null) {
            return Long.parseLong(count);
        }

        ConversationMember member = getMembership(conversationId, userId);
        return member != null && member.getUnreadCount() != null ? member.getUnreadCount() : 0;
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

    private void incrementUnreadForMembers(String conversationId, String excludeUserId, String messageId) {
        LambdaQueryWrapper<ConversationMember> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ConversationMember::getConversationId, conversationId)
                .eq(ConversationMember::getStatus, 1)
                .ne(ConversationMember::getUserId, excludeUserId);
        List<ConversationMember> members = memberMapper.selectList(wrapper);

        for (ConversationMember member : members) {
            String key = UNREAD_KEY_PREFIX + conversationId + ":" + member.getUserId();
            redisTemplate.opsForValue().increment(key);

            // Also update DB periodically (async would be better in production)
            LambdaUpdateWrapper<ConversationMember> updateWrapper = new LambdaUpdateWrapper<>();
            updateWrapper.eq(ConversationMember::getId, member.getId())
                    .setSql("unread_count = unread_count + 1");
            memberMapper.update(null, updateWrapper);
        }
    }

    private MessageVO toMessageVO(Message message, String currentUserId) {
        MessageVO vo = new MessageVO();
        vo.setId(message.getId());
        vo.setConversationId(message.getConversationId());
        vo.setSenderId(message.getSenderId());
        vo.setSenderNickname(message.getSenderNickname());
        vo.setSenderAvatar(message.getSenderAvatar());
        vo.setType(message.getType());
        vo.setContent(message.getContent());
        vo.setExtra(message.getExtra());
        vo.setAtUserIds(message.getAtUserIds());
        vo.setAtAll(message.getAtAll());
        vo.setReactionCount(message.getReactionCount());
        vo.setReactions(message.getReactions());
        vo.setRecallStatus(message.getRecallStatus());
        vo.setMsgStatus(message.getMsgStatus());
        vo.setCreateTime(message.getCreateTime());
        vo.setIsSelf(message.getSenderId() != null && message.getSenderId().equals(currentUserId));
        return vo;
    }
}
