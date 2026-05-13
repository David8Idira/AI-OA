package com.aioa.ai.controller;

import com.aioa.ai.dto.ChatRequestDTO;
import com.aioa.ai.dto.ChatResponseDTO;
import com.aioa.ai.service.AiChatService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * AiController 单元测试 - 使用MockMvcBuilders手动配置
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("AiControllerTest AI聊天控制器测试")
class AiControllerTest {

    private MockMvc mockMvc;
    private ObjectMapper objectMapper = new ObjectMapper();

    @Mock
    private AiChatService aiChatService;

    @InjectMocks
    private AiController aiController;

    private ChatRequestDTO chatRequest;
    private ChatResponseDTO chatResponse;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(aiController).build();
        chatRequest = new ChatRequestDTO();
        chatRequest.setMessage("Hello AI");
        chatRequest.setModelCode("gpt-4o");

        chatResponse = new ChatResponseDTO();
        chatResponse.setReply("Hello! How can I help you?");
        chatResponse.setModelCode("gpt-4o");
        chatResponse.setTokens(100);
    }

    // ==================== AI对话 ====================

    @Test
    @DisplayName("AI对话成功")
    void chat_success() throws Exception {
        when(aiChatService.chat(any(ChatRequestDTO.class)))
                .thenReturn(chatResponse);

        mockMvc.perform(post("/api/ai/chat")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(chatRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.reply").value("Hello! How can I help you?"))
                .andExpect(jsonPath("$.data.modelCode").value("gpt-4o"))
                .andExpect(jsonPath("$.code").value(200));

        verify(aiChatService, times(1)).chat(any(ChatRequestDTO.class));
    }

    @Test
    @DisplayName("AI对话返回空消息")
    void chat_emptyMessage() throws Exception {
        ChatRequestDTO emptyRequest = new ChatRequestDTO();
        emptyRequest.setMessage("");
        emptyRequest.setModelCode("gpt-4o");

        ChatResponseDTO emptyResponse = new ChatResponseDTO();
        emptyResponse.setReply("");
        emptyResponse.setModelCode("gpt-4o");
        emptyResponse.setTokens(0);

        when(aiChatService.chat(any(ChatRequestDTO.class)))
                .thenReturn(emptyResponse);

        mockMvc.perform(post("/api/ai/chat")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(emptyRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.reply").value(""));
    }

    // ==================== 获取模型列表 ====================

    @Test
    @DisplayName("获取模型列表成功")
    void getModels_success() throws Exception {
        Map<String, String> model1 = new HashMap<>();
        model1.put("modelCode", "gpt-4o");
        model1.put("modelName", "GPT-4o");
        Map<String, String> model2 = new HashMap<>();
        model2.put("modelCode", "claude-3.5");
        model2.put("modelName", "Claude-3.5");

        when(aiChatService.getAvailableModels())
                .thenReturn(Arrays.asList(model1, model2));

        mockMvc.perform(get("/api/ai/models"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.length()").value(2))
                .andExpect(jsonPath("$.code").value(200));

        verify(aiChatService, times(1)).getAvailableModels();
    }

    @Test
    @DisplayName("获取模型列表为空")
    void getModels_empty() throws Exception {
        when(aiChatService.getAvailableModels())
                .thenReturn(Arrays.asList());

        mockMvc.perform(get("/api/ai/models"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.length()").value(0));
    }

    // ==================== 健康检查 ====================

    @Test
    @DisplayName("健康检查成功")
    void health_success() throws Exception {
        mockMvc.perform(get("/api/ai/health"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data").value("AI服务正常"))
                .andExpect(jsonPath("$.code").value(200));
    }

    // ==================== 获取用户配额 ====================

    @Test
    @DisplayName("获取用户配额成功-默认用户")
    void getQuota_defaultUser() throws Exception {
        Map<String, Object> quota = new HashMap<>();
        quota.put("userId", "default");
        quota.put("dailyLimit", 1000);
        quota.put("used", 50);
        quota.put("remaining", 950);

        when(aiChatService.getUserQuota("default"))
                .thenReturn(quota);

        mockMvc.perform(get("/api/ai/quota"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.userId").value("default"))
                .andExpect(jsonPath("$.data.remaining").value(950))
                .andExpect(jsonPath("$.code").value(200));

        verify(aiChatService, times(1)).getUserQuota("default");
    }

    @Test
    @DisplayName("获取用户配额成功-指定用户")
    void getQuota_specificUser() throws Exception {
        Map<String, Object> quota = new HashMap<>();
        quota.put("userId", "user123");
        quota.put("dailyLimit", 5000);
        quota.put("used", 1200);
        quota.put("remaining", 3800);

        when(aiChatService.getUserQuota("user123"))
                .thenReturn(quota);

        mockMvc.perform(get("/api/ai/quota")
                        .param("userId", "user123"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.userId").value("user123"))
                .andExpect(jsonPath("$.data.remaining").value(3800));

        verify(aiChatService, times(1)).getUserQuota("user123");
    }

    // ==================== 异常场景 ====================

    @Test
    @DisplayName("无效JSON格式返回400")
    void invalidJson() throws Exception {
        // Standalone MockMvc会检测无效JSON并返回400
        mockMvc.perform(post("/api/ai/chat")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{invalid json}"))
                .andExpect(status().isBadRequest());


        verify(aiChatService, never()).chat(any(ChatRequestDTO.class));
    }
}