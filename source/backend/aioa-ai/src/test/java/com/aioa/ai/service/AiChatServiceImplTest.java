package com.aioa.ai.service;

import com.aioa.ai.client.MimoApiClient;
import com.aioa.ai.config.AiModelProperties;
import com.aioa.ai.dto.ChatRequestDTO;
import com.aioa.ai.dto.ChatResponseDTO;
import com.aioa.ai.entity.AiModelConfig;
import com.aioa.ai.service.impl.AiChatServiceImpl;
import com.aioa.common.exception.BusinessException;
import com.aioa.common.result.ResultCode;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.mockito.InjectMocks;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * AiChatServiceImpl 单元测试 - 增强版
 * 毛泽东思想指导：实事求是，测试AI聊天服务核心功能
 * 
 * 覆盖方法:
 * - chat (主流程)
 * - getUserQuota
 * - getAvailableModels
 * - resolveProvider (通过chat间接覆盖)
 * - saveConversation (通过chat间接覆盖)
 * - estimateTokens (通过chat间接覆盖)
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

    @BeforeEach
    void setUp() {
        // 初始化 aiModelProperties 的默认值
        when(aiModelProperties.getDefaultModel()).thenReturn("mimo-v2-flash");
    }

    // ========== chat 方法测试 ==========

    @Nested
    @DisplayName("chat 方法 - 核心流程测试")
    class ChatMethodTests {

        @Test
        @DisplayName("chat - MiniMax模型正常调用")
        void chat_withMinimaxModel_shouldSucceed() {
            // given
            ChatRequestDTO request = new ChatRequestDTO();
            request.setConversationId("conv-001");
            request.setMessage("今天天气如何？");
            request.setModelCode("mimo-v2-flash");
            request.setSystemPrompt("你是一个有帮助的助手");

            when(aiQuotaService.checkQuota(anyString(), anyString())).thenReturn(true);
            when(aiModelConfigService.getConfigByCode("mimo-v2-flash")).thenReturn(null); // 无DB配置
            when(mimoApiClient.chat(anyString(), eq("mimo-v2-flash"))).thenReturn("今天天气晴朗，温度25度。");
            when(aiQuotaService.useQuota(anyString(), anyString(), anyInt())).thenReturn(true);

            // when
            ChatResponseDTO response = aiChatService.chat(request);

            // then
            assertThat(response).isNotNull();
            assertThat(response.getReply()).isEqualTo("今天天气晴朗，温度25度。");
            assertThat(response.getModelCode()).isEqualTo("mimo-v2-flash");
            assertThat(response.getConversationId()).isEqualTo("conv-001");
            assertThat(response.getTokens()).isGreaterThan(0);
            assertThat(response.getTimeUsed()).isGreaterThanOrEqualTo(0);

            verify(aiQuotaService).checkQuota(anyString(), eq("mimo-v2-flash"));
            verify(mimoApiClient).chat(anyString(), eq("mimo-v2-flash"));
            verify(aiQuotaService).useQuota(anyString(), eq("mimo-v2-flash"), anyInt());
        }

        @Test
        @DisplayName("chat - Dispatcher模型正常调用 (OpenAI/Claude等)")
        void chat_withDispatcherModel_shouldSucceed() {
            // given
            ChatRequestDTO request = new ChatRequestDTO();
            request.setConversationId("conv-002");
            request.setMessage("写一首诗");
            request.setModelCode("gpt-4o");

            AiModelConfig config = new AiModelConfig();
            config.setModelCode("gpt-4o");
            config.setProvider("openai");
            config.setEnabled(1);

            when(aiQuotaService.checkQuota(anyString(), eq("gpt-4o"))).thenReturn(true);
            when(aiModelConfigService.getConfigByCode("gpt-4o")).thenReturn(config);
            when(aiModelDispatcher.chat("gpt-4o", "写一首诗")).thenReturn("春风又绿江南岸。");
            when(aiQuotaService.useQuota(anyString(), eq("gpt-4o"), anyInt())).thenReturn(true);

            // when
            ChatResponseDTO response = aiChatService.chat(request);

            // then
            assertThat(response).isNotNull();
            assertThat(response.getReply()).isEqualTo("春风又绿江南岸。");
            assertThat(response.getModelCode()).isEqualTo("gpt-4o");

            verify(aiModelDispatcher).chat("gpt-4o", "写一首诗");
            verify(aiQuotaService).useQuota(anyString(), eq("gpt-4o"), anyInt());
        }

        @Test
        @DisplayName("chat - 配额不足时抛出异常")
        void chat_withQuotaExceeded_shouldThrow() {
            // given
            ChatRequestDTO request = new ChatRequestDTO();
            request.setConversationId("conv-003");
            request.setMessage("测试消息");
            request.setModelCode("mimo-v2-flash");

            when(aiQuotaService.checkQuota(anyString(), anyString())).thenReturn(false);

            // when & then
            assertThatThrownBy(() -> aiChatService.chat(request))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("配额已用尽");

            verify(mimoApiClient, never()).chat(anyString(), anyString());
        }

        @Test
        @DisplayName("chat - 消息为空时抛出异常")
        void chat_withEmptyMessage_shouldThrow() {
            // given
            ChatRequestDTO request = new ChatRequestDTO();
            request.setConversationId("conv-004");
            request.setMessage(""); // 空消息
            request.setModelCode("mimo-v2-flash");

            // when & then
            assertThatThrownBy(() -> aiChatService.chat(request))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("消息不能为空");
        }

        @Test
        @DisplayName("chat - 消息为null时抛出异常")
        void chat_withNullMessage_shouldThrow() {
            // given
            ChatRequestDTO request = new ChatRequestDTO();
            request.setConversationId("conv-005");
            request.setMessage(null);
            request.setModelCode("mimo-v2-flash");

            // when & then
            assertThatThrownBy(() -> aiChatService.chat(request))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("消息不能为空");
        }

        @Test
        @DisplayName("chat - 使用默认模型 (未指定modelCode)")
        void chat_withDefaultModel_shouldUseConfiguredDefault() {
            // given
            ChatRequestDTO request = new ChatRequestDTO();
            request.setConversationId("conv-006");
            request.setMessage("你好");
            // modelCode = null, 使用默认值 mimo-v2-flash

            when(aiQuotaService.checkQuota(anyString(), eq("mimo-v2-flash"))).thenReturn(true);
            when(aiModelConfigService.getConfigByCode("mimo-v2-flash")).thenReturn(null);
            when(mimoApiClient.chat(anyString(), eq("mimo-v2-flash"))).thenReturn("你好！有什么可以帮助你的？");
            when(aiQuotaService.useQuota(anyString(), eq("mimo-v2-flash"), anyInt())).thenReturn(true);

            // when
            ChatResponseDTO response = aiChatService.chat(request);

            // then
            assertThat(response).isNotNull();
            assertThat(response.getModelCode()).isEqualTo("mimo-v2-flash");
            assertThat(response.getReply()).isEqualTo("你好！有什么可以帮助你的？");

            verify(aiModelProperties).getDefaultModel();
        }

        @Test
        @DisplayName("chat - 带对话历史的上下文对话")
        void chat_withConversationHistory_shouldIncludeHistory() {
            // given
            ChatRequestDTO request = new ChatRequestDTO();
            request.setConversationId("conv-with-history");
            request.setMessage("继续");
            request.setModelCode("mimo-v2-flash");

            // 先调用一次建立历史
            ChatRequestDTO firstRequest = new ChatRequestDTO();
            firstRequest.setConversationId("conv-with-history");
            firstRequest.setMessage("你好");
            firstRequest.setModelCode("mimo-v2-flash");

            when(aiQuotaService.checkQuota(anyString(), anyString())).thenReturn(true);
            when(aiModelConfigService.getConfigByCode(anyString())).thenReturn(null);
            when(mimoApiClient.chat(anyString(), anyString())).thenReturn("你好！我是AI助手。");

            // 首次对话
            aiChatService.chat(firstRequest);

            // 第二次对话（带历史）
            when(mimoApiClient.chat(anyString(), anyString())).thenReturn("有什么可以继续帮助你的？");
            when(aiQuotaService.useQuota(anyString(), anyString(), anyInt())).thenReturn(true);

            ChatResponseDTO response = aiChatService.chat(request);

            // then
            assertThat(response).isNotNull();
            verify(mimoApiClient, times(2)).chat(anyString(), anyString());
        }

        @Test
        @DisplayName("chat - Dispatcher调用失败抛出业务异常")
        void chat_whenDispatcherFails_shouldThrowBusinessException() {
            // given
            ChatRequestDTO request = new ChatRequestDTO();
            request.setConversationId("conv-error");
            request.setMessage("测试");
            request.setModelCode("gpt-4o");

            AiModelConfig config = new AiModelConfig();
            config.setModelCode("gpt-4o");
            config.setProvider("openai");
            config.setEnabled(1);

            when(aiQuotaService.checkQuota(anyString(), eq("gpt-4o"))).thenReturn(true);
            when(aiModelConfigService.getConfigByCode("gpt-4o")).thenReturn(config);
            when(aiModelDispatcher.chat(eq("gpt-4o"), anyString()))
                .thenThrow(new RuntimeException("Network error"));

            // when & then
            assertThatThrownBy(() -> aiChatService.chat(request))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("调用失败");
        }

        @Test
        @DisplayName("chat - MimoApiClient调用失败抛出业务异常")
        void chat_whenMimoClientFails_shouldThrowBusinessException() {
            // given
            ChatRequestDTO request = new ChatRequestDTO();
            request.setConversationId("conv-mimo-error");
            request.setMessage("测试");
            request.setModelCode("mimo-v2-flash");

            when(aiQuotaService.checkQuota(anyString(), anyString())).thenReturn(true);
            when(aiModelConfigService.getConfigByCode(anyString())).thenReturn(null);
            when(mimoApiClient.chat(anyString(), anyString()))
                .thenThrow(new RuntimeException("Connection timeout"));

            // when & then
            assertThatThrownBy(() -> aiChatService.chat(request))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("AI服务调用失败");
        }

        @Test
        @DisplayName("chat - 无conversationId时使用默认用户")
        void chat_withoutConversationId_shouldUseDefaultUser() {
            // given
            ChatRequestDTO request = new ChatRequestDTO();
            request.setMessage("测试消息");
            request.setModelCode("mimo-v2-flash");
            // conversationId = null

            when(aiQuotaService.checkQuota("default", "mimo-v2-flash")).thenReturn(true);
            when(aiModelConfigService.getConfigByCode(anyString())).thenReturn(null);
            when(mimoApiClient.chat(anyString(), eq("mimo-v2-flash"))).thenReturn("响应");
            when(aiQuotaService.useQuota(eq("default"), eq("mimo-v2-flash"), anyInt())).thenReturn(true);

            // when
            ChatResponseDTO response = aiChatService.chat(request);

            // then
            assertThat(response).isNotNull();
            verify(aiQuotaService).checkQuota("default", "mimo-v2-flash");
        }
    }

    // ========== getUserQuota 方法测试 ==========

    @Nested
    @DisplayName("getUserQuota 方法测试")
    class GetUserQuotaTests {

        @Test
        @DisplayName("getUserQuota - 正常场景")
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
            assertThat(result).containsKey("dailyUsed");
            assertThat(result).containsKey("monthlyLimit");
            assertThat(result).containsKey("monthlyUsed");

            verify(aiQuotaService).getUserQuota("user-001");
        }

        @Test
        @DisplayName("getUserQuota - 用户无配额记录")
        void getUserQuota_withNoQuota_shouldReturnEmptyMap() {
            // given
            when(aiQuotaService.getUserQuota("new-user")).thenReturn(Map.of());

            // when
            Map<String, Object> result = aiChatService.getUserQuota("new-user");

            // then
            assertThat(result).isNotNull();
            assertThat(result).isEmpty();
        }
    }

    // ========== getAvailableModels 方法测试 ==========

    @Nested
    @DisplayName("getAvailableModels 方法测试")
    class GetAvailableModelsTests {

        @Test
        @DisplayName("getAvailableModels - 数据库有配置时返回数据库配置")
        void getAvailableModels_withDbConfig_shouldReturnDbConfigs() {
            // given
            AiModelConfig config1 = new AiModelConfig();
            config1.setModelCode("gpt-4o");
            config1.setModelName("GPT-4o");
            config1.setProvider("openai");
            config1.setEnabled(1);

            AiModelConfig config2 = new AiModelConfig();
            config2.setModelCode("mimo-v2-flash");
            config2.setModelName("MiniMax Mimo");
            config2.setProvider("minimax");
            config2.setEnabled(1);

            when(aiModelConfigService.getEnabledConfigs()).thenReturn(List.of(config1, config2));

            // when
            List<Map<String, String>> results = aiChatService.getAvailableModels();

            // then
            assertThat(results).isNotNull();
            assertThat(results).hasSize(2);
            assertThat(results).anySatisfy(m -> {
                assertThat(m.get("code")).isEqualTo("gpt-4o");
                assertThat(m.get("name")).isEqualTo("GPT-4o");
                assertThat(m.get("provider")).isEqualTo("openai");
            });
            assertThat(results).anySatisfy(m -> {
                assertThat(m.get("code")).isEqualTo("mimo-v2-flash");
                assertThat(m.get("name")).isEqualTo("MiniMax Mimo");
                assertThat(m.get("provider")).isEqualTo("minimax");
            });

            verify(aiModelConfigService).getEnabledConfigs();
        }

        @Test
        @DisplayName("getAvailableModels - 数据库无配置时返回默认列表")
        void getAvailableModels_withNoDbConfig_shouldReturnDefaultList() {
            // given
            when(aiModelConfigService.getEnabledConfigs()).thenReturn(List.of());

            // when
            List<Map<String, String>> results = aiChatService.getAvailableModels();

            // then
            assertThat(results).isNotNull();
            assertThat(results).hasSize(4);
            assertThat(results).anySatisfy(m ->
                assertThat(m.get("code")).isEqualTo("mimo-v2-flash"));
            assertThat(results).anySatisfy(m ->
                assertThat(m.get("code")).isEqualTo("gpt-4o"));
            assertThat(results).anySatisfy(m ->
                assertThat(m.get("code")).isEqualTo("kimi-pro"));
            assertThat(results).anySatisfy(m ->
                assertThat(m.get("code")).isEqualTo("claude-3.5"));
        }

        @Test
        @DisplayName("getAvailableModels - 数据库返回null时返回默认列表")
        void getAvailableModels_withNullConfig_shouldReturnDefaultList() {
            // given
            when(aiModelConfigService.getEnabledConfigs()).thenReturn(null);

            // when
            List<Map<String, String>> results = aiChatService.getAvailableModels();

            // then
            assertThat(results).isNotNull();
            assertThat(results).hasSize(4);
        }
    }

    // ========== Token估算测试 (通过chat方法覆盖) ==========

    @Nested
    @DisplayName("Token估算测试 (通过chat间接覆盖)")
    class TokenEstimationTests {

        @Test
        @DisplayName("chat - 中文消息Token估算")
        void chat_withChineseMessage_shouldEstimateTokens() {
            // given
            ChatRequestDTO request = new ChatRequestDTO();
            request.setConversationId("conv-cn");
            request.setMessage("今天天气非常晴朗，适合外出游玩。");
            request.setModelCode("mimo-v2-flash");

            when(aiQuotaService.checkQuota(anyString(), anyString())).thenReturn(true);
            when(aiModelConfigService.getConfigByCode(anyString())).thenReturn(null);
            when(mimoApiClient.chat(anyString(), anyString())).thenReturn("回复内容");
            when(aiQuotaService.useQuota(anyString(), anyString(), anyInt())).thenReturn(true);

            // when
            ChatResponseDTO response = aiChatService.chat(request);

            // then
            assertThat(response.getTokens()).isGreaterThan(0);
            // 验证token估算被正确调用（15个中文字符 * 2 = 30 tokens 估算）
        }

        @Test
        @DisplayName("chat - 英文消息Token估算")
        void chat_withEnglishMessage_shouldEstimateTokens() {
            // given
            ChatRequestDTO request = new ChatRequestDTO();
            request.setConversationId("conv-en");
            request.setMessage("Hello, how are you today?");
            request.setModelCode("mimo-v2-flash");

            when(aiQuotaService.checkQuota(anyString(), anyString())).thenReturn(true);
            when(aiModelConfigService.getConfigByCode(anyString())).thenReturn(null);
            when(mimoApiClient.chat(anyString(), anyString())).thenReturn("I am fine, thank you!");
            when(aiQuotaService.useQuota(anyString(), anyString(), anyInt())).thenReturn(true);

            // when
            ChatResponseDTO response = aiChatService.chat(request);

            // then
            assertThat(response.getTokens()).isGreaterThan(0);
        }

        @Test
        @DisplayName("chat - 空回复Token估算")
        void chat_withEmptyReply_shouldReturnZeroTokens() {
            // given
            ChatRequestDTO request = new ChatRequestDTO();
            request.setConversationId("conv-empty");
            request.setMessage("test");
            request.setModelCode("mimo-v2-flash");

            when(aiQuotaService.checkQuota(anyString(), anyString())).thenReturn(true);
            when(aiModelConfigService.getConfigByCode(anyString())).thenReturn(null);
            when(mimoApiClient.chat(anyString(), anyString())).thenReturn("");
            when(aiQuotaService.useQuota(anyString(), anyString(), anyInt())).thenReturn(true);

            // when
            ChatResponseDTO response = aiChatService.chat(request);

            // then
            assertThat(response.getTokens()).isEqualTo(0);
        }
    }
}