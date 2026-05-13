package com.aioa.im.service;

import com.aioa.common.exception.BusinessException;
import com.aioa.im.dto.SendMessageDTO;
import com.aioa.im.entity.Conversation;
import com.aioa.im.entity.ConversationMember;
import com.aioa.im.entity.Message;
import com.aioa.im.mapper.ConversationMapper;
import com.aioa.im.mapper.ConversationMemberMapper;
import com.aioa.im.mapper.MessageMapper;
import com.aioa.im.service.impl.MessageServiceImpl;
import com.aioa.im.vo.ConversationVO;
import com.aioa.im.vo.MessageVO;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.metadata.TableInfoHelper;
import org.apache.ibatis.session.Configuration;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * MessageServiceImpl 单元测试
 * 覆盖消息发送、已读标记、撤回（2分钟内可撤回）、删除等核心功能
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("MessageServiceImplTest 消息服务测试")
class MessageServiceImplTest {

    @Mock
    private MessageMapper messageMapper;

    @Mock
    private ConversationMapper conversationMapper;

    @Mock
    private ConversationMemberMapper memberMapper;

    @Mock
    private ConversationService conversationService;

    @Mock
    private StringRedisTemplate redisTemplate;

    @Mock
    private ValueOperations<String, String> valueOperations;

    private MessageServiceImpl messageService;

    private static final String USER_ID = "user-001";
    private static final String OTHER_USER_ID = "user-002";
    private static final String CONV_ID = "conv-001";
    private static final String MSG_ID = "msg-001";

    @BeforeEach
    void setUp() throws Exception {
        messageService = new MessageServiceImpl(
                conversationMapper, memberMapper, conversationService, redisTemplate);
        // ServiceImpl 的 baseMapper 字段在 Spring 环境外为 null，需要手动注入 mock
        injectBaseMapper(messageService, messageMapper);
        // Pre-warm MyBatis-Plus lambda cache so LambdaUpdateWrapper doesn't fail
        warmLambdaCache(ConversationMember.class, "com.aioa.im.mapper.ConversationMemberMapper");
        warmLambdaCache(Message.class, "com.aioa.im.mapper.MessageMapper");
        warmLambdaCache(Conversation.class, "com.aioa.im.mapper.ConversationMapper");
    }

    // 预热 MyBatis-Plus lambda 缓存
    private void warmLambdaCache(Class<?> entityClass, String mapperNamespace) throws Exception {
        Method initMethod = TableInfoHelper.class.getDeclaredMethod(
                "initTableInfo", Configuration.class, String.class, Class.class);
        initMethod.setAccessible(true);
        initMethod.invoke(null, new Configuration(), mapperNamespace, entityClass);
    }

    /**
     * 通过反射将 mock mapper 注入 ServiceImpl 的 baseMapper 字段
     */
    @SuppressWarnings("unchecked")
    private void injectBaseMapper(Object service, Object mapper) throws Exception {
        Class<?> clazz = service.getClass();
        while (clazz != null) {
            for (Field field : clazz.getDeclaredFields()) {
                if ("baseMapper".equals(field.getName())) {
                    field.setAccessible(true);
                    field.set(service, mapper);
                    return;
                }
            }
            clazz = clazz.getSuperclass();
        }
        throw new IllegalStateException("baseMapper field not found in class hierarchy");
    }

    // 通用反射设置字段值工具
    private void setField(Object target, String fieldName, Object value) throws Exception {
        Class<?> clazz = target.getClass();
        while (clazz != null) {
            for (java.lang.reflect.Field f : clazz.getDeclaredFields()) {
                if (f.getName().equals(fieldName)) {
                    f.setAccessible(true);
                    f.set(target, value);
                    return;
                }
            }
            clazz = clazz.getSuperclass();
        }
        throw new IllegalStateException("Field '" + fieldName + "' not found");
    }

    // ==================== getMessageList ====================

    @Test
    @DisplayName("获取消息列表 - 用户非会话成员抛出异常")
    void getMessageList_notMember() {
        when(memberMapper.selectOne(any(LambdaQueryWrapper.class))).thenReturn(null);

        BusinessException ex = assertThrows(BusinessException.class,
                () -> messageService.getMessageList(CONV_ID, USER_ID, null, 1, 20));
        assertTrue(ex.getMessage().contains("not a member"));
    }

    @Test
    @DisplayName("获取消息列表 - 空结果")
    void getMessageList_empty() {
        ConversationMember member = createConversationMember(CONV_ID, USER_ID, 1);
        when(memberMapper.selectOne(any(LambdaQueryWrapper.class))).thenReturn(member);
        when(messageMapper.selectList(any(LambdaQueryWrapper.class))).thenReturn(Collections.emptyList());

        List<MessageVO> result = messageService.getMessageList(CONV_ID, USER_ID, null, 1, 20);

        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    @Test
    @DisplayName("获取消息列表 - 成功返回消息列表（倒序转正序）")
    void getMessageList_success() {
        // given
        ConversationMember member = createConversationMember(CONV_ID, USER_ID, 1);
        when(memberMapper.selectOne(any(LambdaQueryWrapper.class))).thenReturn(member);

        Message msg = createMessage(MSG_ID, CONV_ID, USER_ID, 1, "你好", LocalDateTime.now());
        when(messageMapper.selectList(any(LambdaQueryWrapper.class))).thenReturn(List.of(msg));

        // when
        List<MessageVO> result = messageService.getMessageList(CONV_ID, USER_ID, null, 1, 20);

        // then
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(MSG_ID, result.get(0).getId());
        assertEquals("你好", result.get(0).getContent());
    }

    @Test
    @DisplayName("获取消息列表 - 按 beforeMsgId 翻页")
    void getMessageList_pagination() {
        // given
        ConversationMember member = createConversationMember(CONV_ID, USER_ID, 1);
        when(memberMapper.selectOne(any(LambdaQueryWrapper.class))).thenReturn(member);

        Message beforeMsg = createMessage("msg-old", CONV_ID, USER_ID, 1, "旧消息", LocalDateTime.now().minusHours(1));
        when(messageMapper.selectById("msg-old")).thenReturn(beforeMsg);

        Message newMsg = createMessage(MSG_ID, CONV_ID, USER_ID, 1, "新消息", LocalDateTime.now());
        when(messageMapper.selectList(any(LambdaQueryWrapper.class))).thenReturn(List.of(newMsg));

        // when
        List<MessageVO> result = messageService.getMessageList(CONV_ID, USER_ID, "msg-old", 2, 20);

        // then
        assertNotNull(result);
        assertEquals(1, result.size());
        // 验证按 beforeMsgId 翻页时，查询了 beforeMsg 的时间
        verify(messageMapper, times(1)).selectById("msg-old");
    }

    // ==================== sendMessage ====================

    @Test
    @DisplayName("发送消息 - 会话ID和接收者ID都为空抛出异常")
    void sendMessage_bothIdsBlank() {
        SendMessageDTO dto = new SendMessageDTO();
        dto.setType(1);
        dto.setContent("测试消息");
        // conversationId 和 receiverId 都为空

        BusinessException ex = assertThrows(BusinessException.class,
                () -> messageService.sendMessage(USER_ID, dto));
        assertTrue(ex.getMessage().contains("required"));
    }

    @Test
    @DisplayName("发送消息 - 用户非会话成员抛出异常")
    void sendMessage_notMember() {
        SendMessageDTO dto = new SendMessageDTO();
        dto.setConversationId(CONV_ID);
        dto.setType(1);
        dto.setContent("测试消息");

        when(memberMapper.selectOne(any(LambdaQueryWrapper.class))).thenReturn(null);

        BusinessException ex = assertThrows(BusinessException.class,
                () -> messageService.sendMessage(USER_ID, dto));
        assertTrue(ex.getMessage().contains("not a member"));
    }

    @Test
    @DisplayName("发送消息 - 文字消息内容为空抛出异常")
    void sendMessage_textContentRequired() {
        SendMessageDTO dto = new SendMessageDTO();
        dto.setConversationId(CONV_ID);
        dto.setType(1);
        // content 为空

        ConversationMember member = createConversationMember(CONV_ID, USER_ID, 1);
        when(memberMapper.selectOne(any(LambdaQueryWrapper.class))).thenReturn(member);

        BusinessException ex = assertThrows(BusinessException.class,
                () -> messageService.sendMessage(USER_ID, dto));
        assertTrue(ex.getMessage().contains("content"));
    }

    @Test
    @DisplayName("发送消息 - 成功发送文字消息")
    void sendMessage_success() {
        // given
        SendMessageDTO dto = new SendMessageDTO();
        dto.setConversationId(CONV_ID);
        dto.setType(1);
        dto.setContent("你好，这是测试消息");

        ConversationMember member = createConversationMember(CONV_ID, USER_ID, 1);
        when(memberMapper.selectOne(any(LambdaQueryWrapper.class))).thenReturn(member);

        Conversation conv = createConversation(CONV_ID, 1, null);
        when(conversationMapper.selectById(CONV_ID)).thenReturn(conv);

        when(messageMapper.insert(any(Message.class))).thenAnswer(inv -> {
            Message m = inv.getArgument(0);
            if (m.getId() == null) {
                setField(m, "id", MSG_ID);
            }
            return 1;
        });

        ConversationMember otherMember = createConversationMember(CONV_ID, OTHER_USER_ID, 1);
        when(memberMapper.selectList(any(LambdaQueryWrapper.class)))
                .thenReturn(List.of(otherMember));

        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        when(valueOperations.increment(anyString())).thenReturn(1L);

        // when
        MessageVO result = messageService.sendMessage(USER_ID, dto);

        // then
        assertNotNull(result);
        assertEquals(CONV_ID, result.getConversationId());
        assertEquals(USER_ID, result.getSenderId());
        assertEquals(1, result.getType());
        verify(messageMapper, times(1)).insert(any(Message.class));
        // 对方未读数增加
        verify(memberMapper, times(1)).update(any(), any(LambdaUpdateWrapper.class));
    }

    @Test
    @DisplayName("发送消息 - 私聊通过 receiverId 创建或获取会话")
    void sendMessage_privateChatWithReceiverId() {
        // given
        SendMessageDTO dto = new SendMessageDTO();
        dto.setReceiverId(OTHER_USER_ID); // 无 conversationId，通过 receiverId 发送
        dto.setType(1);
        dto.setContent("私聊消息");

        ConversationVO privateConv = new ConversationVO();
        privateConv.setId(CONV_ID);
        privateConv.setType(1);
        when(conversationService.getOrCreatePrivateConversation(USER_ID, OTHER_USER_ID))
                .thenReturn(privateConv);

        Conversation conv = createConversation(CONV_ID, 1, null);
        when(conversationMapper.selectById(CONV_ID)).thenReturn(conv);

        ConversationMember member = createConversationMember(CONV_ID, USER_ID, 1);
        when(memberMapper.selectOne(any(LambdaQueryWrapper.class))).thenReturn(member);

        when(messageMapper.insert(any(Message.class))).thenReturn(1);
        when(conversationMapper.update(any(), any())).thenReturn(1);
        when(memberMapper.selectList(any(LambdaQueryWrapper.class))).thenReturn(Collections.emptyList());

        // when
        MessageVO result = messageService.sendMessage(USER_ID, dto);

        // then
        assertNotNull(result);
        assertEquals(CONV_ID, result.getConversationId());
        verify(conversationService, times(1)).getOrCreatePrivateConversation(USER_ID, OTHER_USER_ID);
    }

    // ==================== recallMessage ====================

    @Test
    @DisplayName("撤回消息 - 消息不存在")
    void recallMessage_notFound() {
        when(messageMapper.selectById(MSG_ID)).thenReturn(null);

        BusinessException ex = assertThrows(BusinessException.class,
                () -> messageService.recallMessage(MSG_ID, USER_ID));
        assertTrue(ex.getMessage().contains("not found"));
    }

    @Test
    @DisplayName("撤回消息 - 非发送者不能撤回")
    void recallMessage_notSender() {
        Message msg = createMessage(MSG_ID, CONV_ID, OTHER_USER_ID, 1, "对方消息", LocalDateTime.now());
        when(messageMapper.selectById(MSG_ID)).thenReturn(msg);

        BusinessException ex = assertThrows(BusinessException.class,
                () -> messageService.recallMessage(MSG_ID, USER_ID));
        assertTrue(ex.getMessage().contains("Only sender"));
    }

    @Test
    @DisplayName("撤回消息 - 超过2分钟不可撤回")
    void recallMessage_timeout() {
        // 消息创建于3分钟前
        Message msg = createMessage(MSG_ID, CONV_ID, USER_ID, 1, "旧消息", LocalDateTime.now().minusMinutes(3));
        when(messageMapper.selectById(MSG_ID)).thenReturn(msg);

        BusinessException ex = assertThrows(BusinessException.class,
                () -> messageService.recallMessage(MSG_ID, USER_ID));
        assertTrue(ex.getMessage().contains("2 minutes"));
    }

    @Test
    @DisplayName("撤回消息 - 2分钟内可撤回")
    void recallMessage_within2Minutes() {
        // 消息创建于1分钟前（可撤回）
        Message msg = createMessage(MSG_ID, CONV_ID, USER_ID, 1, "新消息", LocalDateTime.now().minusMinutes(1));
        doReturn(msg).when(messageMapper).selectById(anyString());
        when(messageMapper.update(any(), any())).thenReturn(1);

        boolean result = messageService.recallMessage(MSG_ID, USER_ID);

        assertTrue(result);
        verify(messageMapper, times(1)).update(any(), any());
    }

    // ==================== deleteMessage ====================

    @Test
    @DisplayName("删除消息 - 非发送者不能删除")
    void deleteMessage_notSender() {
        Message msg = createMessage(MSG_ID, CONV_ID, OTHER_USER_ID, 1, "对方消息", LocalDateTime.now());
        when(messageMapper.selectById(MSG_ID)).thenReturn(msg);

        BusinessException ex = assertThrows(BusinessException.class,
                () -> messageService.deleteMessage(MSG_ID, USER_ID));
        assertTrue(ex.getMessage().contains("Only sender"));
    }

    @Test
    @DisplayName("删除消息 - 发送者成功删除（标记senderDeleted=1）")
    void deleteMessage_success() {
        Message msg = createMessage(MSG_ID, CONV_ID, USER_ID, 1, "我的消息", LocalDateTime.now());
        doReturn(msg).when(messageMapper).selectById(anyString());
        when(messageMapper.update(any(), any())).thenReturn(1);

        boolean result = messageService.deleteMessage(MSG_ID, USER_ID);

        assertTrue(result);
        verify(messageMapper, times(1)).update(any(), any());
    }

    // ==================== markMessageRead ====================

    @Test
    @DisplayName("标记消息已读 - 非成员抛出异常")
    void markMessageRead_notMember() {
        when(memberMapper.selectOne(any(LambdaQueryWrapper.class))).thenReturn(null);

        BusinessException ex = assertThrows(BusinessException.class,
                () -> messageService.markMessageRead(CONV_ID, USER_ID, MSG_ID));
        assertTrue(ex.getMessage().contains("not a member"));
    }

    @Test
    @DisplayName("标记消息已读 - 成功")
    void markMessageRead_success() {
        ConversationMember member = createConversationMember(CONV_ID, USER_ID, 1);
        when(memberMapper.selectOne(any(LambdaQueryWrapper.class))).thenReturn(member);
        when(memberMapper.update(any(), any(LambdaUpdateWrapper.class))).thenReturn(1);
        when(redisTemplate.delete(anyString())).thenReturn(true);

        boolean result = messageService.markMessageRead(CONV_ID, USER_ID, MSG_ID);

        assertTrue(result);
        verify(redisTemplate, times(1)).delete(contains(CONV_ID + ":" + USER_ID));
    }

    // ==================== getTotalUnreadCount ====================

    @Test
    @DisplayName("获取总未读数 - 无会话返回0")
    void getTotalUnreadCount_noConversations() {
        when(memberMapper.selectList(any(LambdaQueryWrapper.class))).thenReturn(Collections.emptyList());

        long result = messageService.getTotalUnreadCount(USER_ID);

        assertEquals(0, result);
    }

    @Test
    @DisplayName("获取总未读数 - 从Redis获取未读数")
    void getTotalUnreadCount_fromRedis() {
        ConversationMember m1 = createConversationMember("conv-1", USER_ID, 1);
        ConversationMember m2 = createConversationMember("conv-2", USER_ID, 1);
        when(memberMapper.selectList(any(LambdaQueryWrapper.class)))
                .thenReturn(List.of(m1, m2));

        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        when(valueOperations.get(contains("conv-1"))).thenReturn("3");
        when(valueOperations.get(contains("conv-2"))).thenReturn("5");

        long result = messageService.getTotalUnreadCount(USER_ID);

        assertEquals(8, result);
    }

    // ==================== getConversationUnreadCount ====================

    @Test
    @DisplayName("获取会话未读数 - Redis有值则直接返回")
    void getConversationUnreadCount_fromRedis() {
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        when(valueOperations.get(contains(CONV_ID))).thenReturn("7");

        long result = messageService.getConversationUnreadCount(CONV_ID, USER_ID);

        assertEquals(7, result);
    }

    @Test
    @DisplayName("获取会话未读数 - Redis无值则查DB")
    void getConversationUnreadCount_fromDb() {
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        when(valueOperations.get(anyString())).thenReturn(null);

        ConversationMember member = createConversationMember(CONV_ID, USER_ID, 1);
        member.setUnreadCount(4);
        when(memberMapper.selectOne(any(LambdaQueryWrapper.class))).thenReturn(member);

        long result = messageService.getConversationUnreadCount(CONV_ID, USER_ID);

        assertEquals(4, result);
    }

    // ==================== Helper Methods ====================

    private ConversationMember createConversationMember(String conversationId, String userId, Integer role) {
        ConversationMember member = new ConversationMember();
        member.setId("member-" + conversationId + "-" + userId);
        member.setConversationId(conversationId);
        member.setUserId(userId);
        member.setRole(role);
        member.setJoinTime(LocalDateTime.now());
        member.setInviterId(userId);
        member.setUnreadCount(0);
        member.setMuteStatus(0);
        member.setTopStatus(0);
        member.setSortOrder(0);
        member.setStatus(1);
        return member;
    }

    private Message createMessage(String id, String conversationId, String senderId,
                                   Integer type, String content, LocalDateTime createTime) {
        Message msg = new Message();
        msg.setId(id);
        msg.setConversationId(conversationId);
        msg.setSenderId(senderId);
        msg.setSenderNickname("用户");
        msg.setSenderAvatar("http://example.com/avatar.png");
        msg.setType(type);
        msg.setContent(content);
        msg.setReadStatus(0);
        msg.setRecallStatus(0);
        msg.setSenderDeleted(0);
        msg.setMsgStatus(1);
        msg.setReactionCount(0);
        msg.setAtAll(0);
        msg.setCreateTime(createTime);
        return msg;
    }

    private Conversation createConversation(String id, Integer type, String name) {
        Conversation conv = new Conversation();
        conv.setId(id);
        conv.setType(type);
        conv.setName(name);
        conv.setOwnerId(USER_ID);
        conv.setStatus(1);
        conv.setUnreadCount(0);
        conv.setMuteStatus(0);
        conv.setTopStatus(0);
        conv.setArchiveStatus(0);
        conv.setMaxMembers(-1);
        conv.setCreateTime(LocalDateTime.now());
        conv.setUpdateTime(LocalDateTime.now());
        return conv;
    }
}
