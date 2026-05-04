package com.aioa.im.controller;

import com.aioa.im.dto.ConversationCreateDTO;
import com.aioa.im.dto.ReadConfirmDTO;
import com.aioa.im.dto.SendMessageDTO;
import com.aioa.im.service.ConversationService;
import com.aioa.im.service.MessageService;
import com.aioa.im.vo.ConversationVO;
import com.aioa.im.vo.MessageVO;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * ImController 单元测试
 * 测试即时通讯模块 REST API
 */
@WebMvcTest(controllers = ImController.class)
@Import(ImControllerTest.TestConfig.class)
@AutoConfigureMockMvc(addFilters = false)
@DisplayName("ImControllerTest 即时通讯控制器测试")
class ImControllerTest {

    @Configuration
    static class TestConfig {
        @Bean
        public ObjectMapper objectMapper() {
            return new ObjectMapper();
        }
    }

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private ConversationService conversationService;

    @MockBean
    private MessageService messageService;

    // ==================== Conversation APIs ====================

    @Test
    @DisplayName("获取会话列表 - 成功")
    void getConversationList_success() throws Exception {
        // given
        ConversationVO vo = createConversationVO("conv-001", "私聊会话", 1);
        when(conversationService.getConversationList(anyString(), isNull(), anyInt(), anyInt()))
                .thenReturn(List.of(vo));

        // when & then
        mockMvc.perform(get("/api/v1/im/conversations")
                        .requestAttr("userId", "user-001")
                        .param("page", "1")
                        .param("size", "20"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data[0].id").value("conv-001"));

        verify(conversationService, times(1)).getConversationList(eq("user-001"), isNull(), eq(1), eq(20));
    }

    @Test
    @DisplayName("获取会话列表 - 按类型筛选")
    void getConversationList_byType() throws Exception {
        // given
        ConversationVO groupConv = createConversationVO("conv-002", "群聊", 2);
        when(conversationService.getConversationList(anyString(), eq(2), anyInt(), anyInt()))
                .thenReturn(List.of(groupConv));

        // when & then
        mockMvc.perform(get("/api/v1/im/conversations")
                        .requestAttr("userId", "user-001")
                        .param("type", "2")
                        .param("page", "1")
                        .param("size", "20"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data[0].type").value(2));

        verify(conversationService, times(1)).getConversationList(eq("user-001"), eq(2), eq(1), eq(20));
    }

    @Test
    @DisplayName("获取会话列表 - 空结果")
    void getConversationList_empty() throws Exception {
        // given
        when(conversationService.getConversationList(anyString(), isNull(), anyInt(), anyInt()))
                .thenReturn(List.of());

        // when & then
        mockMvc.perform(get("/api/v1/im/conversations")
                        .requestAttr("userId", "user-001"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.length()").value(0));
    }

    @Test
    @DisplayName("获取会话详情 - 成功")
    void getConversationById_success() throws Exception {
        // given
        ConversationVO vo = createConversationVO("conv-001", "测试会话", 1);
        when(conversationService.getConversationById("conv-001", "user-001"))
                .thenReturn(vo);

        // when & then
        mockMvc.perform(get("/api/v1/im/conversations/conv-001")
                        .requestAttr("userId", "user-001"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.id").value("conv-001"));

        verify(conversationService, times(1)).getConversationById("conv-001", "user-001");
    }

    @Test
    @DisplayName("获取会话详情 - 不存在")
    void getConversationById_notFound() throws Exception {
        // given
        when(conversationService.getConversationById("nonexist", "user-001"))
                .thenReturn(null);

        // when & then
        mockMvc.perform(get("/api/v1/im/conversations/nonexist")
                        .requestAttr("userId", "user-001"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data").isEmpty());
    }

    @Test
    @DisplayName("创建会话 - 成功")
    void createConversation_success() throws Exception {
        // given
        ConversationCreateDTO dto = new ConversationCreateDTO();
        dto.setType(2);
        dto.setName("新群聊");
        dto.setMemberIds(List.of("user-002", "user-003"));

        ConversationVO vo = createConversationVO("conv-new", "新群聊", 2);
        when(conversationService.createConversation(eq("user-001"), any(ConversationCreateDTO.class)))
                .thenReturn(vo);

        // when & then
        mockMvc.perform(post("/api/v1/im/conversations")
                        .requestAttr("userId", "user-001")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.id").value("conv-new"));

        verify(conversationService, times(1)).createConversation(eq("user-001"), any(ConversationCreateDTO.class));
    }

    @Test
    @DisplayName("获取或创建私聊会话 - 成功")
    void getOrCreatePrivateConversation_success() throws Exception {
        // given
        ConversationVO vo = createConversationVO("conv-private", "私聊", 1);
        when(conversationService.getOrCreatePrivateConversation("user-001", "user-002"))
                .thenReturn(vo);

        // when & then
        mockMvc.perform(get("/api/v1/im/conversations/private")
                        .requestAttr("userId", "user-001")
                        .param("userId", "user-002"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.id").value("conv-private"));

        verify(conversationService, times(1)).getOrCreatePrivateConversation("user-001", "user-002");
    }

    @Test
    @DisplayName("删除会话 - 成功")
    void deleteConversation_success() throws Exception {
        // given
        when(conversationService.deleteConversation("conv-001", "user-001"))
                .thenReturn(true);

        // when & then
        mockMvc.perform(delete("/api/v1/im/conversations/conv-001")
                        .requestAttr("userId", "user-001"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));

        verify(conversationService, times(1)).deleteConversation("conv-001", "user-001");
    }

    @Test
    @DisplayName("删除会话 - 失败")
    void deleteConversation_failure() throws Exception {
        // given
        when(conversationService.deleteConversation("conv-001", "user-001"))
                .thenReturn(false);

        // when & then
        mockMvc.perform(delete("/api/v1/im/conversations/conv-001")
                        .requestAttr("userId", "user-001"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(500));
    }

    @Test
    @DisplayName("禁言会话 - 成功")
    void muteConversation_success() throws Exception {
        // given
        when(conversationService.muteConversation("conv-001", "user-001", 1))
                .thenReturn(true);

        // when & then
        mockMvc.perform(put("/api/v1/im/conversations/conv-001/mute")
                        .requestAttr("userId", "user-001")
                        .param("muteStatus", "1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));

        verify(conversationService, times(1)).muteConversation("conv-001", "user-001", 1);
    }

    @Test
    @DisplayName("置顶会话 - 成功")
    void topConversation_success() throws Exception {
        // given
        when(conversationService.topConversation("conv-001", "user-001", 1))
                .thenReturn(true);

        // when & then
        mockMvc.perform(put("/api/v1/im/conversations/conv-001/top")
                        .requestAttr("userId", "user-001")
                        .param("topStatus", "1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));

        verify(conversationService, times(1)).topConversation("conv-001", "user-001", 1);
    }

    @Test
    @DisplayName("更新会话信息 - 成功")
    void updateConversationInfo_success() throws Exception {
        // given
        when(conversationService.updateConversationInfo(eq("conv-001"), eq("user-001"), eq("新名称"), isNull()))
                .thenReturn(true);

        // when & then
        mockMvc.perform(put("/api/v1/im/conversations/conv-001")
                        .requestAttr("userId", "user-001")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"新名称\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));

        verify(conversationService, times(1)).updateConversationInfo("conv-001", "user-001", "新名称", null);
    }

    // ==================== Message APIs ====================

    @Test
    @DisplayName("获取消息列表 - 成功")
    void getMessageList_success() throws Exception {
        // given
        MessageVO msg = createMessageVO("msg-001", "conv-001", "user-sender", "发送者", 1, "你好");
        when(messageService.getMessageList(eq("conv-001"), eq("user-001"), isNull(), anyInt(), anyInt()))
                .thenReturn(List.of(msg));

        // when & then
        mockMvc.perform(get("/api/v1/im/conversations/conv-001/messages")
                        .requestAttr("userId", "user-001")
                        .param("page", "1")
                        .param("size", "20"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data[0].id").value("msg-001"))
                .andExpect(jsonPath("$.data[0].senderNickname").value("发送者"));

        verify(messageService, times(1)).getMessageList("conv-001", "user-001", null, 1, 20);
    }

    @Test
    @DisplayName("获取消息列表 - 翻页")
    void getMessageList_pagination() throws Exception {
        // given
        MessageVO msg2 = createMessageVO("msg-002", "conv-001", "user-sender", "发送者", 1, "第二条消息");
        when(messageService.getMessageList(eq("conv-001"), eq("user-001"), eq("msg-001"), eq(2), eq(10)))
                .thenReturn(List.of(msg2));

        // when & then
        mockMvc.perform(get("/api/v1/im/conversations/conv-001/messages")
                        .requestAttr("userId", "user-001")
                        .param("beforeMsgId", "msg-001")
                        .param("page", "2")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));

        verify(messageService, times(1)).getMessageList("conv-001", "user-001", "msg-001", 2, 10);
    }

    @Test
    @DisplayName("获取消息列表 - 空结果")
    void getMessageList_empty() throws Exception {
        // given
        when(messageService.getMessageList(anyString(), anyString(), isNull(), anyInt(), anyInt()))
                .thenReturn(List.of());

        // when & then
        mockMvc.perform(get("/api/v1/im/conversations/conv-001/messages")
                        .requestAttr("userId", "user-001"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.length()").value(0));
    }

    @Test
    @DisplayName("发送消息 - 成功")
    void sendMessage_success() throws Exception {
        // given
        SendMessageDTO dto = new SendMessageDTO();
        dto.setConversationId("conv-001");
        dto.setType(1);
        dto.setContent("这是一条测试消息");

        MessageVO vo = createMessageVO("msg-new", "conv-001", "user-001", "当前用户", 1, "这是一条测试消息");
        when(messageService.sendMessage(eq("user-001"), any(SendMessageDTO.class)))
                .thenReturn(vo);

        // when & then
        mockMvc.perform(post("/api/v1/im/messages")
                        .requestAttr("userId", "user-001")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.id").value("msg-new"))
                .andExpect(jsonPath("$.data.senderNickname").value("当前用户"));

        verify(messageService, times(1)).sendMessage(eq("user-001"), any(SendMessageDTO.class));
    }

    @Test
    @DisplayName("发送消息 - 图片类型")
    void sendMessage_image() throws Exception {
        // given
        SendMessageDTO dto = new SendMessageDTO();
        dto.setConversationId("conv-001");
        dto.setType(2);
        dto.setContent("[图片]");

        MessageVO vo = createMessageVO("msg-img", "conv-001", "user-001", "当前用户", 2, "[图片]");
        when(messageService.sendMessage(eq("user-001"), any(SendMessageDTO.class)))
                .thenReturn(vo);

        // when & then
        mockMvc.perform(post("/api/v1/im/messages")
                        .requestAttr("userId", "user-001")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.type").value(2));

        verify(messageService, times(1)).sendMessage(eq("user-001"), any(SendMessageDTO.class));
    }

    @Test
    @DisplayName("撤回消息 - 成功")
    void recallMessage_success() throws Exception {
        // given
        when(messageService.recallMessage("msg-001", "user-001"))
                .thenReturn(true);

        // when & then
        mockMvc.perform(put("/api/v1/im/messages/msg-001/recall")
                        .requestAttr("userId", "user-001"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));

        verify(messageService, times(1)).recallMessage("msg-001", "user-001");
    }

    @Test
    @DisplayName("撤回消息 - 失败(超时)")
    void recallMessage_failure() throws Exception {
        // given
        when(messageService.recallMessage("msg-001", "user-001"))
                .thenReturn(false);

        // when & then
        mockMvc.perform(put("/api/v1/im/messages/msg-001/recall")
                        .requestAttr("userId", "user-001"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(500));

        verify(messageService, times(1)).recallMessage("msg-001", "user-001");
    }

    @Test
    @DisplayName("删除消息 - 成功")
    void deleteMessage_success() throws Exception {
        // given
        when(messageService.deleteMessage("msg-001", "user-001"))
                .thenReturn(true);

        // when & then
        mockMvc.perform(delete("/api/v1/im/messages/msg-001")
                        .requestAttr("userId", "user-001"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));

        verify(messageService, times(1)).deleteMessage("msg-001", "user-001");
    }

    @Test
    @DisplayName("删除消息 - 失败")
    void deleteMessage_failure() throws Exception {
        // given
        when(messageService.deleteMessage("msg-001", "user-001"))
                .thenReturn(false);

        // when & then
        mockMvc.perform(delete("/api/v1/im/messages/msg-001")
                        .requestAttr("userId", "user-001"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(500));
    }

    @Test
    @DisplayName("根据ID获取消息 - 成功")
    void getMessageById_success() throws Exception {
        // given
        MessageVO vo = createMessageVO("msg-001", "conv-001", "user-sender", "发送者", 1, "消息内容");
        when(messageService.getMessageById("msg-001"))
                .thenReturn(vo);

        // when & then
        mockMvc.perform(get("/api/v1/im/messages/msg-001")
                        .requestAttr("userId", "user-001"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.id").value("msg-001"));

        verify(messageService, times(1)).getMessageById("msg-001");
    }

    // ==================== Read Status APIs ====================

    @Test
    @DisplayName("标记消息已读 - 成功")
    void markAsRead_success() throws Exception {
        // given
        ReadConfirmDTO dto = new ReadConfirmDTO();
        dto.setLastReadMsgId("msg-001");

        when(conversationService.markAsRead("conv-001", "user-001", "msg-001"))
                .thenReturn(true);

        // when & then
        mockMvc.perform(post("/api/v1/im/conversations/conv-001/read")
                        .requestAttr("userId", "user-001")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));

        verify(conversationService, times(1)).markAsRead("conv-001", "user-001", "msg-001");
    }

    @Test
    @DisplayName("标记消息已读 - 失败")
    void markAsRead_failure() throws Exception {
        // given
        ReadConfirmDTO dto = new ReadConfirmDTO();
        dto.setLastReadMsgId("msg-001");

        when(conversationService.markAsRead("conv-001", "user-001", "msg-001"))
                .thenReturn(false);

        // when & then
        mockMvc.perform(post("/api/v1/im/conversations/conv-001/read")
                        .requestAttr("userId", "user-001")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(500));
    }

    @Test
    @DisplayName("获取总未读消息数 - 成功")
    void getTotalUnreadCount_success() throws Exception {
        // given
        when(messageService.getTotalUnreadCount("user-001"))
                .thenReturn(5L);

        // when & then
        mockMvc.perform(get("/api/v1/im/unread/count")
                        .requestAttr("userId", "user-001"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data").value(5));

        verify(messageService, times(1)).getTotalUnreadCount("user-001");
    }

    @Test
    @DisplayName("获取总未读消息数 - 零")
    void getTotalUnreadCount_zero() throws Exception {
        // given
        when(messageService.getTotalUnreadCount("user-001"))
                .thenReturn(0L);

        // when & then
        mockMvc.perform(get("/api/v1/im/unread/count")
                        .requestAttr("userId", "user-001"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data").value(0));
    }

    @Test
    @DisplayName("获取会话未读数 - 成功")
    void getConversationUnreadCount_success() throws Exception {
        // given
        when(messageService.getConversationUnreadCount("conv-001", "user-001"))
                .thenReturn(3L);

        // when & then
        mockMvc.perform(get("/api/v1/im/conversations/conv-001/unread")
                        .requestAttr("userId", "user-001"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data").value(3));

        verify(messageService, times(1)).getConversationUnreadCount("conv-001", "user-001");
    }

    // ==================== Helper Methods ====================

    private ConversationVO createConversationVO(String id, String name, Integer type) {
        ConversationVO vo = new ConversationVO();
        vo.setId(id);
        vo.setName(name);
        vo.setType(type);
        vo.setAvatar("http://example.com/avatar.png");
        vo.setOwnerId("user-001");
        vo.setLastMessageContent("最后一条消息");
        vo.setLastMessageTime("2026-05-04 10:00:00");
        vo.setLastMessageSender("发送者");
        vo.setUnreadCount(0);
        vo.setMuteStatus(0);
        vo.setTopStatus(0);
        vo.setMemberCount(2);
        vo.setCreateTime(LocalDateTime.now());
        vo.setUpdateTime(LocalDateTime.now());
        return vo;
    }

    private MessageVO createMessageVO(String id, String conversationId, String senderId,
                                      String senderNickname, Integer type, String content) {
        MessageVO vo = new MessageVO();
        vo.setId(id);
        vo.setConversationId(conversationId);
        vo.setSenderId(senderId);
        vo.setSenderNickname(senderNickname);
        vo.setSenderAvatar("http://example.com/avatar.png");
        vo.setType(type);
        vo.setContent(content);
        vo.setExtra("{}");
        vo.setReactionCount(0);
        vo.setReactions("[]");
        vo.setRecallStatus(0);
        vo.setMsgStatus(1);
        vo.setAtAll(0);
        vo.setAtUserIds("");
        vo.setIsSelf(true);
        vo.setCreateTime(LocalDateTime.now());
        return vo;
    }
}
