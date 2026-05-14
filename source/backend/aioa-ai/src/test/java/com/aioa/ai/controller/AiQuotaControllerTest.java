package com.aioa.ai.controller;

import com.aioa.ai.service.AiQuotaService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.HashMap;
import java.util.Map;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * AiQuotaController 单元测试
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("AiQuotaControllerTest AI配额控制器测试")
class AiQuotaControllerTest {

    private MockMvc mockMvc;

    @Mock
    private AiQuotaService aiQuotaService;

    @InjectMocks
    private AiQuotaController aiQuotaController;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(aiQuotaController).build();
    }

    // ==================== 检查配额 ====================

    @Test
    @DisplayName("检查配额 - 用户有配额")
    void checkQuota_hasQuota() throws Exception {
        when(aiQuotaService.checkQuota("user123", "gpt-4o")).thenReturn(true);

        mockMvc.perform(get("/api/ai/quota/check/user123/gpt-4o"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data").value("User has quota available"));

        verify(aiQuotaService, times(1)).checkQuota("user123", "gpt-4o");
    }

    @Test
    @DisplayName("检查配额 - 用户无配额")
    void checkQuota_noQuota() throws Exception {
        when(aiQuotaService.checkQuota("user456", "gpt-4o")).thenReturn(false);

        mockMvc.perform(get("/api/ai/quota/check/user456/gpt-4o"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(500))
                .andExpect(jsonPath("$.message").value("User quota exceeded or not available"));

        verify(aiQuotaService, times(1)).checkQuota("user456", "gpt-4o");
    }

    @Test
    @DisplayName("检查配额 - 默认用户")
    void checkQuota_defaultUser() throws Exception {
        when(aiQuotaService.checkQuota("default", "gpt-4o")).thenReturn(true);

        mockMvc.perform(get("/api/ai/quota/check/default/gpt-4o"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));
    }

    // ==================== 使用配额 ====================

    @Test
    @DisplayName("使用配额 - 成功")
    void useQuota_success() throws Exception {
        when(aiQuotaService.useQuota("user123", "gpt-4o", 100)).thenReturn(true);

        mockMvc.perform(post("/api/ai/quota/use/user123/gpt-4o/100"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data").value("100 tokens used successfully"));

        verify(aiQuotaService, times(1)).useQuota("user123", "gpt-4o", 100);
    }

    @Test
    @DisplayName("使用配额 - 失败（配额不足）")
    void useQuota_failed() throws Exception {
        when(aiQuotaService.useQuota("user456", "gpt-4o", 500)).thenReturn(false);

        mockMvc.perform(post("/api/ai/quota/use/user456/gpt-4o/500"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(500))
                .andExpect(jsonPath("$.message").value("Failed to use quota: insufficient quota or quota exceeded"));

        verify(aiQuotaService, times(1)).useQuota("user456", "gpt-4o", 500);
    }

    @Test
    @DisplayName("使用配额 - token数为0返回错误")
    void useQuota_zeroTokens() throws Exception {
        mockMvc.perform(post("/api/ai/quota/use/user123/gpt-4o/0"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(500))
                .andExpect(jsonPath("$.message").value("Token count must be positive"));

        verify(aiQuotaService, never()).useQuota(anyString(), anyString(), anyInt());
    }

    @Test
    @DisplayName("使用配额 - token数为负返回错误")
    void useQuota_negativeTokens() throws Exception {
        mockMvc.perform(post("/api/ai/quota/use/user123/gpt-4o/-100"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(500))
                .andExpect(jsonPath("$.message").value("Token count must be positive"));

        verify(aiQuotaService, never()).useQuota(anyString(), anyString(), anyInt());
    }

    // ==================== 获取用户配额信息 ====================

    @Test
    @DisplayName("获取用户配额 - 成功")
    void getUserQuota_success() throws Exception {
        Map<String, Object> quotaInfo = new HashMap<>();
        quotaInfo.put("userId", "user123");
        quotaInfo.put("dailyLimit", 1000);
        quotaInfo.put("used", 200);
        quotaInfo.put("remaining", 800);
        quotaInfo.put("models", Map.of("gpt-4o", 800, "claude-3.5", 1000));

        when(aiQuotaService.getUserQuota("user123")).thenReturn(quotaInfo);

        mockMvc.perform(get("/api/ai/quota/user/user123"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.userId").value("user123"))
                .andExpect(jsonPath("$.data.remaining").value(800));

        verify(aiQuotaService, times(1)).getUserQuota("user123");
    }

    @Test
    @DisplayName("获取用户配额 - 空配额")
    void getUserQuota_empty() throws Exception {
        Map<String, Object> emptyQuota = new HashMap<>();
        emptyQuota.put("userId", "newuser");
        emptyQuota.put("dailyLimit", 0);
        emptyQuota.put("used", 0);
        emptyQuota.put("remaining", 0);

        when(aiQuotaService.getUserQuota("newuser")).thenReturn(emptyQuota);

        mockMvc.perform(get("/api/ai/quota/user/newuser"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.remaining").value(0));
    }

    // ==================== 重置每日配额 ====================

    @Test
    @DisplayName("重置每日配额 - 成功")
    void resetDailyQuota_success() throws Exception {
        doNothing().when(aiQuotaService).resetDailyQuota();

        mockMvc.perform(post("/api/ai/quota/reset-daily"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data").value("Daily quota reset successfully"));

        verify(aiQuotaService, times(1)).resetDailyQuota();
    }
}