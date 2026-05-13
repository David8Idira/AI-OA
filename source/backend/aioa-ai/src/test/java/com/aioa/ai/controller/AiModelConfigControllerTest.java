package com.aioa.ai.controller;

import com.aioa.ai.entity.AiModelConfig;
import com.aioa.ai.service.AiModelConfigService;
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

import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * AiModelConfigController 单元测试
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("AiModelConfigControllerTest AI模型配置控制器测试")
class AiModelConfigControllerTest {

    private MockMvc mockMvc;
    private ObjectMapper objectMapper;

    @Mock
    private AiModelConfigService aiModelConfigService;

    @InjectMocks
    private AiModelConfigController aiModelConfigController;

    private AiModelConfig testConfig;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(aiModelConfigController).build();
        objectMapper = new ObjectMapper();

        testConfig = new AiModelConfig();
        testConfig.setId("1");
        testConfig.setModelCode("gpt-4o");
        testConfig.setModelName("GPT-4o");
        testConfig.setProvider("openai");
        testConfig.setEndpoint("https://api.openai.com/v1");
        testConfig.setApiKey("sk-xxxx");
        testConfig.setDefaultFor("CHAT");
        testConfig.setEnabled(1);
        testConfig.setDailyLimit(1000);
        testConfig.setTodayUsage(100);
        testConfig.setModelType("gpt4");
        testConfig.setSortOrder(1);
    }

    // ==================== 获取所有配置 ====================

    @Test
    @DisplayName("获取所有配置成功")
    void getAllConfigs_success() throws Exception {
        when(aiModelConfigService.getAllConfigs())
                .thenReturn(List.of(testConfig));

        mockMvc.perform(get("/api/ai/config/list"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.length()").value(1))
                .andExpect(jsonPath("$.data[0].modelCode").value("gpt-4o"));

        verify(aiModelConfigService, times(1)).getAllConfigs();
    }

    @Test
    @DisplayName("获取所有配置为空")
    void getAllConfigs_empty() throws Exception {
        when(aiModelConfigService.getAllConfigs())
                .thenReturn(List.of());

        mockMvc.perform(get("/api/ai/config/list"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.length()").value(0));
    }

    // ==================== 获取启用的配置 ====================

    @Test
    @DisplayName("获取启用的配置成功")
    void getEnabledConfigs_success() throws Exception {
        when(aiModelConfigService.getEnabledConfigs())
                .thenReturn(List.of(testConfig));

        mockMvc.perform(get("/api/ai/config/enabled"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.length()").value(1));

        verify(aiModelConfigService, times(1)).getEnabledConfigs();
    }

    // ==================== 根据编码获取配置 ====================

    @Test
    @DisplayName("根据编码获取配置成功")
    void getConfigByCode_success() throws Exception {
        when(aiModelConfigService.getConfigByCode("gpt-4o"))
                .thenReturn(testConfig);

        mockMvc.perform(get("/api/ai/config/gpt-4o"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.modelCode").value("gpt-4o"));

        verify(aiModelConfigService, times(1)).getConfigByCode("gpt-4o");
    }

    @Test
    @DisplayName("根据编码获取配置-不存在")
    void getConfigByCode_notFound() throws Exception {
        when(aiModelConfigService.getConfigByCode("nonexistent"))
                .thenReturn(null);

        mockMvc.perform(get("/api/ai/config/nonexistent"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("AI model configuration not found: nonexistent"));

        verify(aiModelConfigService, times(1)).getConfigByCode("nonexistent");
    }

    // ==================== 根据功能获取配置 ====================

    @Test
    @DisplayName("根据功能获取配置成功")
    void getConfigsByFunction_success() throws Exception {
        when(aiModelConfigService.getConfigsByFunction("CHAT"))
                .thenReturn(List.of(testConfig));

        mockMvc.perform(get("/api/ai/config/function/CHAT"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.length()").value(1));

        verify(aiModelConfigService, times(1)).getConfigsByFunction("CHAT");
    }

    // ==================== 保存配置 ====================

    @Test
    @DisplayName("保存配置成功")
    void saveConfig_success() throws Exception {
        when(aiModelConfigService.saveConfig(any(AiModelConfig.class)))
                .thenReturn(testConfig);

        mockMvc.perform(post("/api/ai/config")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(testConfig)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.modelCode").value("gpt-4o"));

        verify(aiModelConfigService, times(1)).saveConfig(any(AiModelConfig.class));
    }

    @Test
    @DisplayName("保存配置失败-服务异常")
    void saveConfig_serviceException() throws Exception {
        when(aiModelConfigService.saveConfig(any(AiModelConfig.class)))
                .thenThrow(new RuntimeException("Database error"));

        mockMvc.perform(post("/api/ai/config")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(testConfig)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Failed to save AI model configuration: Database error"));

        verify(aiModelConfigService, times(1)).saveConfig(any(AiModelConfig.class));
    }

    // ==================== 切换模型启用状态 ====================

    @Test
    @DisplayName("切换模型启用状态成功")
    void toggleModel_success() throws Exception {
        when(aiModelConfigService.toggleModelEnabled("gpt-4o"))
                .thenReturn(true);

        mockMvc.perform(put("/api/ai/config/toggle/gpt-4o"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data").value("AI model toggled successfully"));

        verify(aiModelConfigService, times(1)).toggleModelEnabled("gpt-4o");
    }

    @Test
    @DisplayName("切换模型启用状态-模型不存在")
    void toggleModel_notFound() throws Exception {
        when(aiModelConfigService.toggleModelEnabled("nonexistent"))
                .thenReturn(false);

        mockMvc.perform(put("/api/ai/config/toggle/nonexistent"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("AI model not found: nonexistent"));

        verify(aiModelConfigService, times(1)).toggleModelEnabled("nonexistent");
    }

    @Test
    @DisplayName("切换模型启用状态-服务异常")
    void toggleModel_serviceException() throws Exception {
        when(aiModelConfigService.toggleModelEnabled("gpt-4o"))
                .thenThrow(new RuntimeException("Toggle failed"));

        mockMvc.perform(put("/api/ai/config/toggle/gpt-4o"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Failed to toggle AI model: Toggle failed"));
    }

    // ==================== 检查配额 ====================

    @Test
    @DisplayName("检查配额成功-有配额")
    void checkQuota_hasQuota() throws Exception {
        when(aiModelConfigService.checkUserQuota("user123", "gpt-4o"))
                .thenReturn(true);

        mockMvc.perform(get("/api/ai/config/check-quota/gpt-4o/user123"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data").value("User has quota available"));

        verify(aiModelConfigService, times(1)).checkUserQuota("user123", "gpt-4o");
    }

    @Test
    @DisplayName("检查配额成功-无配额")
    void checkQuota_noQuota() throws Exception {
        when(aiModelConfigService.checkUserQuota("user456", "gpt-4o"))
                .thenReturn(false);

        mockMvc.perform(get("/api/ai/config/check-quota/gpt-4o/user456"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("User quota exceeded or not available"));

        verify(aiModelConfigService, times(1)).checkUserQuota("user456", "gpt-4o");
    }

    // ==================== 异常场景 ====================

    @Test
    @DisplayName("保存配置-无效JSON")
    void saveConfig_invalidJson() throws Exception {
        mockMvc.perform(post("/api/ai/config")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{invalid json}"))
                .andExpect(status().isBadRequest());

        verify(aiModelConfigService, never()).saveConfig(any());
    }
}