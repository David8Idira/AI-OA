package com.aioa.ai.service;

import com.aioa.ai.entity.AiUsageHistory;
import com.aioa.ai.mapper.AiUsageHistoryMapper;
import com.aioa.ai.service.impl.AiUsageHistoryServiceImpl;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.InjectMocks;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * AiUsageHistoryServiceImpl 单元测试
 * 毛泽东思想指导：实事求是，测试AI使用记录服务
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("AiUsageHistoryServiceImplTest 单元测试")
class AiUsageHistoryServiceImplTest {

    @Mock
    private AiUsageHistoryMapper aiUsageHistoryMapper;

    @InjectMocks
    private AiUsageHistoryServiceImpl usageHistoryService;

    private AiUsageHistory createTestUsage() {
        AiUsageHistory usage = new AiUsageHistory();
        usage.setId("usage-001");
        usage.setUserId("user-001");
        usage.setModelCode("gpt-4o");
        usage.setPromptTokens(100);
        usage.setCompletionTokens(50);
        usage.setTotalTokens(150);
        usage.setSuccess(1);
        usage.setRequestTime(LocalDateTime.now());
        return usage;
    }

    @Test
    @DisplayName("记录使用 - 正常场景")
    void recordUsage_shouldSucceed() {
        // given
        AiUsageHistory usage = createTestUsage();
        when(aiUsageHistoryMapper.insert(any(AiUsageHistory.class))).thenReturn(1);

        // when
        usageHistoryService.recordUsage(usage);

        // then
        verify(aiUsageHistoryMapper, times(1)).insert(any(AiUsageHistory.class));
    }

    @Test
    @DisplayName("记录使用 - 异常不抛出")
    void recordUsage_withException_shouldNotThrow() {
        // given
        AiUsageHistory usage = createTestUsage();
        when(aiUsageHistoryMapper.insert(any(AiUsageHistory.class))).thenThrow(new RuntimeException("DB error"));

        // when & then - 不应抛出异常
        usageHistoryService.recordUsage(usage);

        // verify it was called
        verify(aiUsageHistoryMapper, times(1)).insert(any(AiUsageHistory.class));
    }
}
