package com.aioa.im.service;

import com.aioa.im.dto.SendMessageDTO;
import com.aioa.im.entity.Conversation;
import com.aioa.im.entity.ConversationMember;
import com.aioa.im.entity.Message;
import com.aioa.im.mapper.ConversationMapper;
import com.aioa.im.mapper.ConversationMemberMapper;
import com.aioa.im.mapper.MessageMapper;
import com.aioa.im.service.impl.MessageServiceImpl;
import com.aioa.im.vo.MessageVO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * MessageService Unit Tests
 */
@ExtendWith(MockitoExtension.class)
class MessageServiceTest {

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

    @InjectMocks
    private MessageServiceImpl messageService;

    private static final String USER_ID_1 = "user-001";
    private static final String USER_ID_2 = "user-002";
    private static final String CONVERSATION_ID = "conv-001";
    private static final String MESSAGE_ID = "msg-001";

    @BeforeEach
    void setUp() {
        lenient().when(redisTemplate.opsForValue()).thenReturn(valueOperations);
    }

    @Test
    @DisplayName("getMessageList - should return messages in chronological order")
    void getMessageList_success() {
        ConversationMember member = createMember(CONVERSATION_ID, USER_ID_1);
        when(memberMapper.selectList(any())).thenReturn(Arrays.asList(member));

        Message msg1 = createMessage("msg-001", CONVERSATION_ID, USER_ID_1, "Hello");
        Message msg2 = createMessage("msg-002", CONVERSATION_ID, USER_ID_2, "Hi there");
        when(messageMapper.selectList(any())).thenReturn(Arrays.asList(msg1, msg2));

        List<MessageVO> result = messageService.getMessageList(CONVERSATION_ID, USER_ID_1, null, 1, 20);

        assertNotNull(result);
        assertEquals(2, result.size());
        // Should be reversed to chronological order
        assertEquals("Hello", result.get(1).getContent());
    }

    @Test
    @DisplayName("getMessageList - should reject non-member")
    void getMessageList_notMember() {
        when(memberMapper.selectList(any())).thenReturn(Collections.emptyList());

        assertThrows(Exception.class, () -> {
            messageService.getMessageList(CONVERSATION_ID, USER_ID_1, null, 1, 20);
        });
    }

    @Test
    @DisplayName("sendMessage - should create and return message")
    void sendMessage_success() {
        ConversationMember member = createMember(CONVERSATION_ID, USER_ID_1);
        when(memberMapper.selectList(any())).thenReturn(Arrays.asList(member));

        Conversation conv = createConversation(CONVERSATION_ID, 2, USER_ID_1);
        when(conversationMapper.selectById(CONVERSATION_ID)).thenReturn(conv);

        when(messageMapper.insert(any(Message.class))).thenReturn(1);
        when(conversationMapper.update(any(), any())).thenReturn(1);

        SendMessageDTO dto = new SendMessageDTO();
        dto.setConversationId(CONVERSATION_ID);
        dto.setType(1);
        dto.setContent("Test message");

        MessageVO result = messageService.sendMessage(USER_ID_1, dto);

        assertNotNull(result);
        verify(messageMapper).insert(any(Message.class));
        verify(conversationMapper).update(any(), any());
    }

    @Test
    @DisplayName("sendMessage - should reject empty content for text message")
    void sendMessage_emptyContent() {
        ConversationMember member = createMember(CONVERSATION_ID, USER_ID_1);
        when(memberMapper.selectList(any())).thenReturn(Arrays.asList(member));

        Conversation conv = createConversation(CONVERSATION_ID, 2, USER_ID_1);
        when(conversationMapper.selectById(CONVERSATION_ID)).thenReturn(conv);

        SendMessageDTO dto = new SendMessageDTO();
        dto.setConversationId(CONVERSATION_ID);
        dto.setType(1);
        dto.setContent(""); // empty

        assertThrows(Exception.class, () -> {
            messageService.sendMessage(USER_ID_1, dto);
        });
    }

    @Test
    @DisplayName("sendMessage - should increment unread for other members")
    void sendMessage_incrementUnread() {
        ConversationMember member1 = createMember(CONVERSATION_ID, USER_ID_1);
        ConversationMember member2 = createMember(CONVERSATION_ID, USER_ID_2);
        when(memberMapper.selectList(any())).thenReturn(Arrays.asList(member1, member2));

        Conversation conv = createConversation(CONVERSATION_ID, 2, USER_ID_1);
        when(conversationMapper.selectById(CONVERSATION_ID)).thenReturn(conv);

        when(messageMapper.insert(any(Message.class))).thenReturn(1);
        when(conversationMapper.update(any(), any())).thenReturn(1);
        when(memberMapper.update(any(), any())).thenReturn(1);
        when(valueOperations.increment(anyString())).thenReturn(1L);

        SendMessageDTO dto = new SendMessageDTO();
        dto.setConversationId(CONVERSATION_ID);
        dto.setType(1);
        dto.setContent("Test message");

        messageService.sendMessage(USER_ID_1, dto);

        // Should increment unread for user2 (exclude sender user1)
        verify(valueOperations, atLeastOnce()).increment(anyString());
    }

    @Test
    @DisplayName("recallMessage - should allow sender to recall within 2 minutes")
    void recallMessage_bySender() {
        Message message = createMessage(MESSAGE_ID, CONVERSATION_ID, USER_ID_1, "Test");
        message.setCreateTime(LocalDateTime.now()); // just now
        when(messageMapper.selectById(MESSAGE_ID)).thenReturn(message);
        when(messageMapper.update(any(), any())).thenReturn(true);

        boolean result = messageService.recallMessage(MESSAGE_ID, USER_ID_1);

        assertTrue(result);
        verify(messageMapper).update(any(), any());
    }

    @Test
    @DisplayName("recallMessage - should reject non-sender")
    void recallMessage_byNonSender() {
        Message message = createMessage(MESSAGE_ID, CONVERSATION_ID, USER_ID_1, "Test");
        message.setCreateTime(LocalDateTime.now());
        when(messageMapper.selectById(MESSAGE_ID)).thenReturn(message);

        assertThrows(Exception.class, () -> {
            messageService.recallMessage(MESSAGE_ID, USER_ID_2);
        });
    }

    @Test
    @DisplayName("deleteMessage - should mark as sender deleted")
    void deleteMessage_success() {
        Message message = createMessage(MESSAGE_ID, CONVERSATION_ID, USER_ID_1, "Test");
        when(messageMapper.selectById(MESSAGE_ID)).thenReturn(message);
        when(messageMapper.update(any(), any())).thenReturn(true);

        boolean result = messageService.deleteMessage(MESSAGE_ID, USER_ID_1);

        assertTrue(result);
        verify(messageMapper).update(any(), any());
    }

    @Test
    @DisplayName("deleteMessage - should reject non-sender")
    void deleteMessage_byNonSender() {
        Message message = createMessage(MESSAGE_ID, CONVERSATION_ID, USER_ID_1, "Test");
        when(messageMapper.selectById(MESSAGE_ID)).thenReturn(message);

        assertThrows(Exception.class, () -> {
            messageService.deleteMessage(MESSAGE_ID, USER_ID_2);
        });
    }

    @Test
    @DisplayName("getMessageById - should return message VO")
    void getMessageById_success() {
        Message message = createMessage(MESSAGE_ID, CONVERSATION_ID, USER_ID_1, "Test");
        when(messageMapper.selectById(MESSAGE_ID)).thenReturn(message);

        MessageVO result = messageService.getMessageById(MESSAGE_ID);

        assertNotNull(result);
        assertEquals(MESSAGE_ID, result.getId());
        assertEquals("Test", result.getContent());
    }

    @Test
    @DisplayName("getMessageById - should throw when not found")
    void getMessageById_notFound() {
        when(messageMapper.selectById(MESSAGE_ID)).thenReturn(null);

        assertThrows(Exception.class, () -> {
            messageService.getMessageById(MESSAGE_ID);
        });
    }

    @Test
    @DisplayName("markMessageRead - should update member last read info")
    void markMessageRead_success() {
        ConversationMember member = createMember(CONVERSATION_ID, USER_ID_1);
        when(memberMapper.selectList(any())).thenReturn(Arrays.asList(member));
        when(memberMapper.update(any(), any())).thenReturn(1);
        when(redisTemplate.delete(anyString())).thenReturn(true);

        boolean result = messageService.markMessageRead(CONVERSATION_ID, USER_ID_1, MESSAGE_ID);

        assertTrue(result);
        verify(memberMapper).update(any(), any());
        verify(redisTemplate).delete(anyString());
    }

    @Test
    @DisplayName("getTotalUnreadCount - should sum all unread counts")
    void getTotalUnreadCount_success() {
        ConversationMember member1 = createMember(CONVERSATION_ID, USER_ID_1);
        member1.setUnreadCount(5);
        when(memberMapper.selectList(any())).thenReturn(Arrays.asList(member1));
        when(valueOperations.get(anyString())).thenReturn(null); // Redis miss, use DB

        long result = messageService.getTotalUnreadCount(USER_ID_1);

        assertEquals(5, result);
    }

    @Test
    @DisplayName("getConversationUnreadCount - should return from Redis if available")
    void getConversationUnreadCount_redis() {
        when(valueOperations.get("aioa:im:unread:" + CONVERSATION_ID + ":" + USER_ID_1))
                .thenReturn("10");

        long result = messageService.getConversationUnreadCount(CONVERSATION_ID, USER_ID_1);

        assertEquals(10, result);
    }

    // ==================== Helper Methods ====================

    private Conversation createConversation(String id, int type, String ownerId) {
        Conversation conv = new Conversation();
        conv.setId(id);
        conv.setType(type);
        conv.setName("Test Conversation");
        conv.setOwnerId(ownerId);
        conv.setStatus(1);
        conv.setUnreadCount(0);
        conv.setCreateTime(LocalDateTime.now());
        conv.setUpdateTime(LocalDateTime.now());
        return conv;
    }

    private ConversationMember createMember(String conversationId, String userId) {
        ConversationMember member = new ConversationMember();
        member.setId("member-" + userId);
        member.setConversationId(conversationId);
        member.setUserId(userId);
        member.setRole(1);
        member.setJoinTime(LocalDateTime.now());
        member.setUnreadCount(0);
        member.setStatus(1);
        return member;
    }

    private Message createMessage(String id, String conversationId, String senderId, String content) {
        Message message = new Message();
        message.setId(id);
        message.setConversationId(conversationId);
        message.setSenderId(senderId);
        message.setType(1);
        message.setContent(content);
        message.setReadStatus(0);
        message.setRecallStatus(0);
        message.setSenderDeleted(0);
        message.setMsgStatus(1);
        message.setReactionCount(0);
        message.setCreateTime(LocalDateTime.now());
        return message;
    }
}
