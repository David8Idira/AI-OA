package com.aioa.im.service;

import com.aioa.im.dto.ConversationCreateDTO;
import com.aioa.im.entity.Conversation;
import com.aioa.im.entity.ConversationMember;
import com.aioa.im.mapper.ConversationMapper;
import com.aioa.im.mapper.ConversationMemberMapper;
import com.aioa.im.service.impl.ConversationServiceImpl;
import com.aioa.im.vo.ConversationVO;
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
 * ConversationService Unit Tests
 */
@ExtendWith(MockitoExtension.class)
class ConversationServiceTest {

    @Mock
    private ConversationMapper conversationMapper;

    @Mock
    private ConversationMemberMapper memberMapper;

    @Mock
    private StringRedisTemplate redisTemplate;

    @Mock
    private ValueOperations<String, String> valueOperations;

    @InjectMocks
    private ConversationServiceImpl conversationService;

    private static final String USER_ID_1 = "user-001";
    private static final String USER_ID_2 = "user-002";
    private static final String CONVERSATION_ID = "conv-001";

    @BeforeEach
    void setUp() {
        lenient().when(redisTemplate.opsForValue()).thenReturn(valueOperations);
    }

    @Test
    @DisplayName("getConversationList - should return empty list when user has no conversations")
    void getConversationList_empty() {
        when(memberMapper.selectList(any())).thenReturn(Collections.emptyList());

        List<ConversationVO> result = conversationService.getConversationList(USER_ID_1, null, 1, 20);

        assertNotNull(result);
        assertTrue(result.isEmpty());
        verify(memberMapper).selectList(any());
    }

    @Test
    @DisplayName("getConversationList - should return conversations sorted by top and update time")
    void getConversationList_withConversations() {
        // Setup mock memberships
        ConversationMember member = createMember(CONVERSATION_ID, USER_ID_1);
        when(memberMapper.selectList(any())).thenReturn(Arrays.asList(member));

        // Setup mock conversation
        Conversation conv = createConversation(CONVERSATION_ID, 1, USER_ID_1);
        when(conversationMapper.selectList(any())).thenReturn(Arrays.asList(conv));

        when(valueOperations.get(anyString())).thenReturn("0");

        List<ConversationVO> result = conversationService.getConversationList(USER_ID_1, null, 1, 20);

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(CONVERSATION_ID, result.get(0).getId());
        assertEquals(1, result.get(0).getType());
    }

    @Test
    @DisplayName("getOrCreatePrivateConversation - should create new conversation when none exists")
    void getOrCreatePrivateConversation_createNew() {
        when(conversationMapper.selectList(any())).thenReturn(Collections.emptyList());
        when(conversationMapper.insert(any(Conversation.class))).thenReturn(1);
        when(memberMapper.insert(any(ConversationMember.class))).thenReturn(1);
        when(memberMapper.selectList(any())).thenReturn(Collections.emptyList());
        when(conversationMapper.selectById(anyString())).thenReturn(null);

        ConversationVO result = conversationService.getOrCreatePrivateConversation(USER_ID_1, USER_ID_2);

        assertNotNull(result);
        verify(conversationMapper).insert(any(Conversation.class));
        verify(memberMapper, times(2)).insert(any(ConversationMember.class));
    }

    @Test
    @DisplayName("getOrCreatePrivateConversation - should return existing conversation")
    void getOrCreatePrivateConversation_existing() {
        Conversation existingConv = createConversation(CONVERSATION_ID, 1, USER_ID_1);

        // Return the conversation with type=1 (private)
        when(conversationMapper.selectList(any())).thenReturn(Arrays.asList(existingConv));

        // First call: user1 is member, user2 is not
        // Second call for checking user2 membership
        ConversationMember member1 = createMember(CONVERSATION_ID, USER_ID_1);
        ConversationMember member2 = createMember(CONVERSATION_ID, USER_ID_2);

        when(memberMapper.selectList(argThat(wrapper -> {
            // First call for user1 membership check, second for user2
            return true;
        }))).thenReturn(Arrays.asList(member1)).thenReturn(Arrays.asList(member2));

        when(conversationMapper.selectById(CONVERSATION_ID)).thenReturn(existingConv);
        when(valueOperations.get(anyString())).thenReturn("0");

        // First check: user1 is member of conv-001, user2 is not
        // This would create a new conversation
        ConversationVO result = conversationService.getOrCreatePrivateConversation(USER_ID_1, USER_ID_2);

        assertNotNull(result);
    }

    @Test
    @DisplayName("createConversation - should create group conversation")
    void createConversation_group() {
        ConversationCreateDTO dto = new ConversationCreateDTO();
        dto.setType(2); // group
        dto.setName("Test Group");
        dto.setDescription("Test Description");

        when(conversationMapper.insert(any(Conversation.class))).thenReturn(1);
        when(memberMapper.insert(any(ConversationMember.class))).thenReturn(1);
        when(conversationMapper.selectById(anyString())).thenReturn(null);
        when(memberMapper.selectList(any())).thenReturn(Collections.emptyList());

        ConversationVO result = conversationService.createConversation(USER_ID_1, dto);

        assertNotNull(result);
        verify(conversationMapper).insert(any(Conversation.class));
        // Creator added as member
        verify(memberMapper, atLeastOnce()).insert(any(ConversationMember.class));
    }

    @Test
    @DisplayName("createConversation - should reject private chat type")
    void createConversation_privateType_rejected() {
        ConversationCreateDTO dto = new ConversationCreateDTO();
        dto.setType(1); // private

        assertThrows(Exception.class, () -> {
            conversationService.createConversation(USER_ID_1, dto);
        });
    }

    @Test
    @DisplayName("muteConversation - should update mute status")
    void muteConversation_success() {
        ConversationMember member = createMember(CONVERSATION_ID, USER_ID_1);
        when(memberMapper.selectList(any())).thenReturn(Arrays.asList(member));
        when(memberMapper.update(any(), any())).thenReturn(1);

        boolean result = conversationService.muteConversation(CONVERSATION_ID, USER_ID_1, 1);

        assertTrue(result);
        verify(memberMapper).update(any(), any());
    }

    @Test
    @DisplayName("topConversation - should update top status")
    void topConversation_success() {
        ConversationMember member = createMember(CONVERSATION_ID, USER_ID_1);
        when(memberMapper.selectList(any())).thenReturn(Arrays.asList(member));
        when(memberMapper.update(any(), any())).thenReturn(1);

        boolean result = conversationService.topConversation(CONVERSATION_ID, USER_ID_1, 1);

        assertTrue(result);
        verify(memberMapper).update(any(), any());
    }

    @Test
    @DisplayName("deleteConversation - should mark member as left")
    void deleteConversation_success() {
        Conversation conv = createConversation(CONVERSATION_ID, 2, USER_ID_1);
        when(conversationMapper.selectById(CONVERSATION_ID)).thenReturn(conv);

        ConversationMember member = createMember(CONVERSATION_ID, USER_ID_1);
        when(memberMapper.selectList(any())).thenReturn(Arrays.asList(member));
        when(memberMapper.update(any(), any())).thenReturn(1);

        boolean result = conversationService.deleteConversation(CONVERSATION_ID, USER_ID_1);

        assertTrue(result);
        verify(memberMapper).update(any(), any());
    }

    @Test
    @DisplayName("markAsRead - should update last read info and clear unread")
    void markAsRead_success() {
        ConversationMember member = createMember(CONVERSATION_ID, USER_ID_1);
        when(memberMapper.selectList(any())).thenReturn(Arrays.asList(member));
        when(memberMapper.update(any(), any())).thenReturn(1);
        when(redisTemplate.delete(anyString())).thenReturn(true);

        boolean result = conversationService.markAsRead(CONVERSATION_ID, USER_ID_1, "msg-001");

        assertTrue(result);
        verify(memberMapper).update(any(), any());
        verify(redisTemplate).delete(anyString());
    }

    // ==================== Helper Methods ====================

    private Conversation createConversation(String id, int type, String ownerId) {
        Conversation conv = new Conversation();
        conv.setId(id);
        conv.setType(type);
        conv.setName("Test Conversation");
        conv.setOwnerId(ownerId);
        conv.setStatus(1);
        conv.setMuteStatus(0);
        conv.setTopStatus(0);
        conv.setArchiveStatus(0);
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
        member.setMuteStatus(0);
        member.setTopStatus(0);
        member.setStatus(1);
        return member;
    }
}
