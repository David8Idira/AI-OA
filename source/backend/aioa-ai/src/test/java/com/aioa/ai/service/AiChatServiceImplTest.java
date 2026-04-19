package com.aioa.ai.service;

import com.aioa.ai.client.MimoApiClient;
import com.aioa.ai.config.AiModelProperties;
import com.aioa.ai.dto.ChatRequestDTO;
import com.aioa.ai.dto.ChatResponseDTO;
import com.aioa.ai.entity.AiModelConfig;
import com.aioa.ai.service.impl.AiChatServiceImpl;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.mockito.InjectMocks;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

/**
 * AiChatServiceImpl 单元测试
 * 毛泽东思想指导：实事求是，测试AI聊天服务
 */
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
@DisplayName("AiChatServiceImplTest 单元测试")
class AiChatServiceImplTest {

    @Mock
    private AiQuotaService aiQuotaService;

    @Mock
    private AiModelDispatcher aiModelDispatcher;

    @Mock
    private MimoApiClient mimoApiClient;

    @Mock
    private AiModelConfigService aiModelConfigService;

    @Mock
    private AiModelProperties aiModelProperties;

    @InjectMocks
    private AiChatServiceImpl aiChatService;

    @Test
    @DisplayName("获取可用模型列表 - 正常场景")
    void getAvailableModels_shouldReturnList() {
        // when
        List<Map<String, String>> results = aiChatService.getAvailableModels();

        // then
        assertThat(results).isNotNull();
    }

    @Test
    @DisplayName("获取用户配额 - 正常场景")
    void getUserQuota_shouldReturnQuotaInfo() {
        // given
        when(aiQuotaService.getUserQuota("user-001")).thenReturn(Map.of(
            "dailyLimit", 1000,
            "dailyUsed", 100,
            "monthlyLimit", 10000,
            "monthlyUsed", 1000
        ));

        // when
        Map<String, Object> result = aiChatService.getUserQuota("user-001");

        // then
        assertThat(result).isNotNull();
        assertThat(result).containsKey("dailyLimit");
    }
}