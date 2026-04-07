package com.aioa.im.controller;

import com.aioa.im.dto.ConversationCreateDTO;
import com.aioa.im.dto.SendMessageDTO;
import com.aioa.im.service.ConversationService;
import com.aioa.im.service.MessageService;
import com.aioa.im.vo.ConversationVO;
import com.aioa.im.vo.MessageVO;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * ImController Unit Tests
 */
@WebMvcTest(ImController.class)
class ImControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private ConversationService conversationService;

    @MockBean
    private MessageService messageService;

    @Autowired
    private ObjectMapper objectMapper;

    private static final String USER_ID = "user-001";
    private static final String CONVERSATION_ID = "conv-001";
    private static final String MESSAGE_ID = "msg-001";

    @Test
    @DisplayName("GET /api/v1/im/conversations - should return conversation list")
    void getConversationList_success() throws Exception {
        ConversationVO conv1 = createConversationVO(CONVERSATION_ID, 1, "Test Chat");
        ConversationVO conv2 = createConversationVO("conv-002", 2, "Test Group");
        when(conversationService.getConversationList(eq(USER_ID), isNull(), eq(1), eq(20)))
                .thenReturn(Arrays.asList(conv1, conv2));

        mockMvc.perform(get("/api/v1/im/conversations")
                        .requestAttr("userId", USER_ID)
                        .param("page", "1")
                        .param("size", "20"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data").isArray())
                .andExpect(jsonPath("$.data.length()").value(2));
    }

    @Test
    @DisplayName("GET /api/v1/im/conversations/{id} - should return conversation detail")
    void getConversationById_success() throws Exception {
        ConversationVO vo = createConversationVO(CONVERSATION_ID, 1, "Test Chat");
        when(conversationService.getConversationById(CONVERSATION_ID, USER_ID)).thenReturn(vo);

        mockMvc.perform(get("/api/v1/im/conversations/{id}", CONVERSATION_ID)
                        .requestAttr("userId", USER_ID))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.id").value(CONVERSATION_ID));
    }

    @Test
    @DisplayName("POST /api/v1/im/conversations - should create conversation")
    void createConversation_success() throws Exception {
        ConversationCreateDTO dto = new ConversationCreateDTO();
        dto.setType(2);
        dto.setName("New Group");

        ConversationVO vo = createConversationVO("conv-new", 2, "New Group");
        when(conversationService.createConversation(eq(USER_ID), any(ConversationCreateDTO.class)))
                .thenReturn(vo);

        mockMvc.perform(post("/api/v1/im/conversations")
                        .requestAttr("userId", USER_ID)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.id").value("conv-new"));
    }

    @Test
    @DisplayName("GET /api/v1/im/conversations/private - should get or create private chat")
    void getOrCreatePrivateConversation_success() throws Exception {
        ConversationVO vo = createConversationVO(CONVERSATION_ID, 1, "Private Chat");
        when(conversationService.getOrCreatePrivateConversation(USER_ID, "user-002")).thenReturn(vo);

        mockMvc.perform(get("/api/v1/im/conversations/private")
                        .requestAttr("userId", USER_ID)
                        .param("userId", "user-002"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));
    }

    @Test
    @DisplayName("DELETE /api/v1/im/conversations/{id} - should leave conversation")
    void deleteConversation_success() throws Exception {
        when(conversationService.deleteConversation(CONVERSATION_ID, USER_ID)).thenReturn(true);

        mockMvc.perform(delete("/api/v1/im/conversations/{id}", CONVERSATION_ID)
                        .requestAttr("userId", USER_ID))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));
    }

    @Test
    @DisplayName("PUT /api/v1/im/conversations/{id}/mute - should toggle mute")
    void muteConversation_success() throws Exception {
        when(conversationService.muteConversation(CONVERSATION_ID, USER_ID, 1)).thenReturn(true);

        mockMvc.perform(put("/api/v1/im/conversations/{id}/mute", CONVERSATION_ID)
                        .requestAttr("userId", USER_ID)
                        .param("muteStatus", "1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));
    }

    @Test
    @DisplayName("GET /api/v1/im/conversations/{id}/messages - should return message list")
    void getMessageList_success() throws Exception {
        MessageVO msg1 = createMessageVO(MESSAGE_ID, CONVERSATION_ID, USER_ID, "Hello");
        MessageVO msg2 = createMessageVO("msg-002", CONVERSATION_ID, "user-002", "Hi");
        when(messageService.getMessageList(eq(CONVERSATION_ID), eq(USER_ID), isNull(), eq(1), eq(20)))
                .thenReturn(Arrays.asList(msg1, msg2));

        mockMvc.perform(get("/api/v1/im/conversations/{id}/messages", CONVERSATION_ID)
                        .requestAttr("userId", USER_ID)
                        .param("page", "1")
                        .param("size", "20"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data").isArray())
                .andExpect(jsonPath("$.data.length()").value(2));
    }

    @Test
    @DisplayName("POST /api/v1/im/messages - should send message")
    void sendMessage_success() throws Exception {
        SendMessageDTO dto = new SendMessageDTO();
        dto.setConversationId(CONVERSATION_ID);
        dto.setType(1);
        dto.setContent("Hello, world!");

        MessageVO vo = createMessageVO(MESSAGE_ID, CONVERSATION_ID, USER_ID, "Hello, world!");
        when(messageService.sendMessage(eq(USER_ID), any(SendMessageDTO.class))).thenReturn(vo);

        mockMvc.perform(post("/api/v1/im/messages")
                        .requestAttr("userId", USER_ID)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.id").value(MESSAGE_ID));
    }

    @Test
    @DisplayName("PUT /api/v1/im/messages/{id}/recall - should recall message")
    void recallMessage_success() throws Exception {
        when(messageService.recallMessage(MESSAGE_ID, USER_ID)).thenReturn(true);

        mockMvc.perform(put("/api/v1/im/messages/{id}/recall", MESSAGE_ID)
                        .requestAttr("userId", USER_ID))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));
    }

    @Test
    @DisplayName("DELETE /api/v1/im/messages/{id} - should delete message")
    void deleteMessage_success() throws Exception {
        when(messageService.deleteMessage(MESSAGE_ID, USER_ID)).thenReturn(true);

        mockMvc.perform(delete("/api/v1/im/messages/{id}", MESSAGE_ID)
                        .requestAttr("userId", USER_ID))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));
    }

    @Test
    @DisplayName("GET /api/v1/im/unread/count - should return total unread count")
    void getTotalUnreadCount_success() throws Exception {
        when(messageService.getTotalUnreadCount(USER_ID)).thenReturn(15L);

        mockMvc.perform(get("/api/v1/im/unread/count")
                        .requestAttr("userId", USER_ID))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data").value(15));
    }

    @Test
    @DisplayName("GET /api/v1/im/messages/{id} - should return message by ID")
    void getMessageById_success() throws Exception {
        MessageVO vo = createMessageVO(MESSAGE_ID, CONVERSATION_ID, USER_ID, "Test");
        when(messageService.getMessageById(MESSAGE_ID)).thenReturn(vo);

        mockMvc.perform(get("/api/v1/im/messages/{id}", MESSAGE_ID))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.id").value(MESSAGE_ID));
    }

    // ==================== Helper Methods ====================

    private ConversationVO createConversationVO(String id, int type, String name) {
        ConversationVO vo = new ConversationVO();
        vo.setId(id);
        vo.setType(type);
        vo.setName(name);
        vo.setOwnerId(USER_ID);
        vo.setUnreadCount(0);
        vo.setMuteStatus(0);
        vo.setTopStatus(0);
        vo.setMemberCount(2);
        vo.setCreateTime(LocalDateTime.now());
        vo.setUpdateTime(LocalDateTime.now());
        return vo;
    }

    private MessageVO createMessageVO(String id, String conversationId, String senderId, String content) {
        MessageVO vo = new MessageVO();
        vo.setId(id);
        vo.setConversationId(conversationId);
        vo.setSenderId(senderId);
        vo.setSenderNickname("User");
        vo.setType(1);
        vo.setContent(content);
        vo.setMsgStatus(1);
        vo.setRecallStatus(0);
        vo.setReactionCount(0);
        vo.setAtAll(0);
        vo.setIsSelf(senderId.equals(USER_ID));
        vo.setCreateTime(LocalDateTime.now());
        return vo;
    }
}
