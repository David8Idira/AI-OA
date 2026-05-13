package com.aioa.ai.service;

import com.aioa.ai.entity.AiUsageHistory;
import com.aioa.ai.mapper.AiUsageHistoryMapper;
import com.aioa.ai.service.impl.AiUsageHistoryServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.InjectMocks;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * AiUsageHistoryServiceImpl 单元测试 - 增强版
 * 毛泽东思想指导：实事求是，测试AI使用记录服务核心功能
 * 
 * 覆盖方法:
 * - recordUsage
 * - getUserHistory
 * - getModelHistory
 * - getDailyStats
 * - getUserSummary
 * - getModelCostStats
 * - getFailedRequests
 * - getSlowRequests
 * - getTodayUsage
 * - cleanOldRecords
 * - getSystemStats
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("AiUsageHistoryServiceImplTest 单元测试")
class AiUsageHistoryServiceImplTest {

    @Mock
    private AiUsageHistoryMapper aiUsageHistoryMapper;

    @InjectMocks
    private AiUsageHistoryServiceImpl usageHistoryService;

    private AiUsageHistory createTestUsage(String id, String userId, String modelCode, int tokens, int success) {
        AiUsageHistory usage = new AiUsageHistory();
        usage.setId(id);
        usage.setUserId(userId);
        usage.setModelCode(modelCode);
        usage.setPromptTokens(tokens / 2);
        usage.setCompletionTokens(tokens / 2);
        usage.setTotalTokens(tokens);
        usage.setSuccess(success);
        usage.setRequestTime(LocalDateTime.now());
        usage.setResponseTime(LocalDateTime.now());
        return usage;
    }

    // ========== recordUsage 测试 ==========

    @Nested
    @DisplayName("recordUsage 方法测试")
    class RecordUsageTests {

        @Test
        @DisplayName("recordUsage - 正常场景")
        void recordUsage_shouldSucceed() {
            // given
            AiUsageHistory usage = createTestUsage("usage-001", "user-001", "gpt-4o", 150, 1);
            when(aiUsageHistoryMapper.insert(any(AiUsageHistory.class))).thenReturn(1);

            // when
            usageHistoryService.recordUsage(usage);

            // then
            verify(aiUsageHistoryMapper, times(1)).insert(usage);
        }

        @Test
        @DisplayName("recordUsage - requestTime为null时自动设置")
        void recordUsage_withNullRequestTime_shouldSetNow() {
            // given
            AiUsageHistory usage = createTestUsage("usage-002", "user-001", "gpt-4o", 150, 1);
            usage.setRequestTime(null);
            when(aiUsageHistoryMapper.insert(any(AiUsageHistory.class))).thenReturn(1);

            // when
            usageHistoryService.recordUsage(usage);

            // then
            verify(aiUsageHistoryMapper).insert(argThat(u -> u.getRequestTime() != null));
        }

        @Test
        @DisplayName("recordUsage - success为null时默认1")
        void recordUsage_withNullSuccess_shouldDefaultTo1() {
            // given
            AiUsageHistory usage = createTestUsage("usage-003", "user-001", "gpt-4o", 150, 1);
            usage.setSuccess(null);
            when(aiUsageHistoryMapper.insert(any(AiUsageHistory.class))).thenReturn(1);

            // when
            usageHistoryService.recordUsage(usage);

            // then
            verify(aiUsageHistoryMapper).insert(argThat(u -> u.getSuccess() == 1));
        }

        @Test
        @DisplayName("recordUsage - totalTokens为null时自动计算")
        void recordUsage_withNullTotalTokens_shouldCalculate() {
            // given
            AiUsageHistory usage = createTestUsage("usage-004", "user-001", "gpt-4o", 150, 1);
            usage.setTotalTokens(null);
            when(aiUsageHistoryMapper.insert(any(AiUsageHistory.class))).thenReturn(1);

            // when
            usageHistoryService.recordUsage(usage);

            // then
            verify(aiUsageHistoryMapper).insert(argThat(u -> u.getTotalTokens() != null));
        }

        @Test
        @DisplayName("recordUsage - 数据库异常不抛出")
        void recordUsage_withDbException_shouldNotThrow() {
            // given
            AiUsageHistory usage = createTestUsage("usage-005", "user-001", "gpt-4o", 150, 1);
            when(aiUsageHistoryMapper.insert(any(AiUsageHistory.class))).thenThrow(new RuntimeException("DB error"));

            // when & then - 不应抛出异常
            usageHistoryService.recordUsage(usage);

            verify(aiUsageHistoryMapper, times(1)).insert(any(AiUsageHistory.class));
        }
    }

    // ========== getUserHistory 测试 ==========

    @Nested
    @DisplayName("getUserHistory 方法测试")
    class GetUserHistoryTests {

        @Test
        @DisplayName("getUserHistory - 正常场景")
        void getUserHistory_shouldReturnHistory() {
            // given
            List<AiUsageHistory> history = List.of(
                createTestUsage("u1", "user-001", "gpt-4o", 200, 1),
                createTestUsage("u2", "user-001", "gpt-4o", 300, 1)
            );
            when(aiUsageHistoryMapper.selectByUser("user-001", 50)).thenReturn(history);

            // when
            List<AiUsageHistory> result = usageHistoryService.getUserHistory("user-001", 50);

            // then
            assertThat(result).hasSize(2);
            verify(aiUsageHistoryMapper).selectByUser("user-001", 50);
        }

        @Test
        @DisplayName("getUserHistory - null userId返回空列表")
        void getUserHistory_withNullUserId_shouldReturnEmpty() {
            // when
            List<AiUsageHistory> result = usageHistoryService.getUserHistory(null, 50);

            // then
            assertThat(result).isEmpty();
            verify(aiUsageHistoryMapper, never()).selectByUser(anyString(), anyInt());
        }

        @Test
        @DisplayName("getUserHistory - 空userId返回空列表")
        void getUserHistory_withEmptyUserId_shouldReturnEmpty() {
            // when
            List<AiUsageHistory> result = usageHistoryService.getUserHistory("", 50);

            // then
            assertThat(result).isEmpty();
        }

        @Test
        @DisplayName("getUserHistory - limit超过100时限制")
        void getUserHistory_withLargeLimit_shouldCapAt100() {
            // given
            when(aiUsageHistoryMapper.selectByUser("user-001", 100)).thenReturn(new ArrayList<>());

            // when
            usageHistoryService.getUserHistory("user-001", 500);

            // then
            verify(aiUsageHistoryMapper).selectByUser("user-001", 100);
        }

        @Test
        @DisplayName("getUserHistory - 数据库异常返回空列表")
        void getUserHistory_withException_shouldReturnEmpty() {
            // given
            when(aiUsageHistoryMapper.selectByUser(anyString(), anyInt())).thenThrow(new RuntimeException("DB error"));

            // when
            List<AiUsageHistory> result = usageHistoryService.getUserHistory("user-001", 50);

            // then
            assertThat(result).isEmpty();
        }
    }

    // ========== getModelHistory 测试 ==========

    @Nested
    @DisplayName("getModelHistory 方法测试")
    class GetModelHistoryTests {

        @Test
        @DisplayName("getModelHistory - 正常场景")
        void getModelHistory_shouldReturnHistory() {
            // given
            List<AiUsageHistory> history = List.of(
                createTestUsage("m1", "user-001", "gpt-4o", 200, 1),
                createTestUsage("m2", "user-002", "gpt-4o", 300, 1)
            );
            when(aiUsageHistoryMapper.selectByModel("gpt-4o", 50)).thenReturn(history);

            // when
            List<AiUsageHistory> result = usageHistoryService.getModelHistory("gpt-4o", 50);

            // then
            assertThat(result).hasSize(2);
        }

        @Test
        @DisplayName("getModelHistory - null modelCode返回空列表")
        void getModelHistory_withNullModelCode_shouldReturnEmpty() {
            // when
            List<AiUsageHistory> result = usageHistoryService.getModelHistory(null, 50);

            // then
            assertThat(result).isEmpty();
        }
    }

    // ========== getDailyStats 测试 ==========

    @Nested
    @DisplayName("getDailyStats 方法测试")
    class GetDailyStatsTests {

        @Test
        @DisplayName("getDailyStats - 正常场景")
        void getDailyStats_shouldReturnStats() {
            // given
            LocalDateTime start = LocalDateTime.now().minusDays(7);
            LocalDateTime end = LocalDateTime.now();

            AiUsageHistoryMapper.DailyUsageStats stat = mock(AiUsageHistoryMapper.DailyUsageStats.class);
            when(stat.getDate()).thenReturn(LocalDate.now());
            when(stat.getModelCode()).thenReturn("gpt-4o");
            when(stat.getRequestCount()).thenReturn(100L);
            when(stat.getSuccessCount()).thenReturn(95L);
            when(stat.getTotalPromptTokens()).thenReturn(50000L);
            when(stat.getTotalCompletionTokens()).thenReturn(25000L);
            when(stat.getTotalTokens()).thenReturn(75000L);
            when(stat.getTotalCost()).thenReturn(new BigDecimal("1.5"));
            when(stat.getAvgDuration()).thenReturn(1500.0);

            when(aiUsageHistoryMapper.getDailyUsageStats(start, end)).thenReturn(List.of(stat));

            // when
            List<Map<String, Object>> result = usageHistoryService.getDailyStats(start, end);

            // then
            assertThat(result).hasSize(1);
            assertThat(result.get(0).get("modelCode")).isEqualTo("gpt-4o");
            assertThat(result.get(0).get("requestCount")).isEqualTo(100L);
        }

        @Test
        @DisplayName("getDailyStats - startDate为null返回空列表")
        void getDailyStats_withNullStartDate_shouldReturnEmpty() {
            // when
            List<Map<String, Object>> result = usageHistoryService.getDailyStats(null, LocalDateTime.now());

            // then
            assertThat(result).isEmpty();
        }

        @Test
        @DisplayName("getDailyStats - startDate > endDate返回空列表")
        void getDailyStats_withInvalidDateRange_shouldReturnEmpty() {
            // given
            LocalDateTime start = LocalDateTime.now();
            LocalDateTime end = LocalDateTime.now().minusDays(7);

            // when
            List<Map<String, Object>> result = usageHistoryService.getDailyStats(start, end);

            // then
            assertThat(result).isEmpty();
        }
    }

    // ========== getUserSummary 测试 ==========

    @Nested
    @DisplayName("getUserSummary 方法测试")
    class GetUserSummaryTests {

        @Test
        @DisplayName("getUserSummary - 正常场景")
        void getUserSummary_shouldReturnSummary() {
            // given
            LocalDateTime start = LocalDateTime.now().minusDays(7);
            LocalDateTime end = LocalDateTime.now();

            List<AiUsageHistory> history = List.of(
                createTestUsage("s1", "user-001", "gpt-4o", 200, 1),
                createTestUsage("s2", "user-001", "gpt-4o", 300, 1),
                createTestUsage("s3", "user-001", "mimo-v2", 400, 0)
            );
            when(aiUsageHistoryMapper.selectByDateRange(start, end)).thenReturn(history);

            // when
            Map<String, Object> result = usageHistoryService.getUserSummary("user-001", start, end);

            // then
            assertThat(result).isNotNull();
            assertThat(result.get("userId")).isEqualTo("user-001");
            assertThat(result.get("totalRequests")).isEqualTo(3);
            assertThat(result.get("successRequests")).isEqualTo(2);
            assertThat(result.get("totalTokens")).isEqualTo(900);
        }

        @Test
        @DisplayName("getUserSummary - 无数据时返回空统计")
        void getUserSummary_withNoData_shouldReturnEmptyStats() {
            // given
            LocalDateTime start = LocalDateTime.now().minusDays(7);
            LocalDateTime end = LocalDateTime.now();
            when(aiUsageHistoryMapper.selectByDateRange(start, end)).thenReturn(new ArrayList<>());

            // when
            Map<String, Object> result = usageHistoryService.getUserSummary("new-user", start, end);

            // then
            assertThat(result.get("totalRequests")).isEqualTo(0);
            assertThat(result.get("successRequests")).isEqualTo(0);
        }
    }

    // ========== getModelCostStats 测试 ==========

    @Nested
    @DisplayName("getModelCostStats 方法测试")
    class GetModelCostStatsTests {

        @Test
        @DisplayName("getModelCostStats - 正常场景")
        void getModelCostStats_shouldReturnStats() {
            // given
            AiUsageHistoryMapper.ModelCostStats stat = mock(AiUsageHistoryMapper.ModelCostStats.class);
            when(stat.getModelCode()).thenReturn("gpt-4o");
            when(stat.getRequestCount()).thenReturn(500L);
            when(stat.getTotalTokens()).thenReturn(250000L);
            when(stat.getTotalCost()).thenReturn(new BigDecimal("5.0"));
            when(stat.getAvgCostPerRequest()).thenReturn(new BigDecimal("0.01"));
            when(stat.getAvgCostPerToken()).thenReturn(new BigDecimal("0.00002"));

            when(aiUsageHistoryMapper.getModelCostStats()).thenReturn(List.of(stat));

            // when
            List<Map<String, Object>> result = usageHistoryService.getModelCostStats();

            // then
            assertThat(result).hasSize(1);
            assertThat(result.get(0).get("modelCode")).isEqualTo("gpt-4o");
            assertThat(result.get(0).get("requestCount")).isEqualTo(500L);
        }

        @Test
        @DisplayName("getModelCostStats - 数据库异常返回空列表")
        void getModelCostStats_withException_shouldReturnEmpty() {
            // given
            when(aiUsageHistoryMapper.getModelCostStats()).thenThrow(new RuntimeException("DB error"));

            // when
            List<Map<String, Object>> result = usageHistoryService.getModelCostStats();

            // then
            assertThat(result).isEmpty();
        }
    }

    // ========== getFailedRequests 测试 ==========

    @Nested
    @DisplayName("getFailedRequests 方法测试")
    class GetFailedRequestsTests {

        @Test
        @DisplayName("getFailedRequests - 正常场景")
        void getFailedRequests_shouldReturnRequests() {
            // given
            List<AiUsageHistory> failed = List.of(
                createTestUsage("f1", "user-001", "gpt-4o", 100, 0),
                createTestUsage("f2", "user-002", "mimo-v2", 200, 0)
            );
            when(aiUsageHistoryMapper.getFailedRequests(50)).thenReturn(failed);

            // when
            List<AiUsageHistory> result = usageHistoryService.getFailedRequests(50);

            // then
            assertThat(result).hasSize(2);
        }

        @Test
        @DisplayName("getFailedRequests - limit超过50时限制")
        void getFailedRequests_withLargeLimit_shouldCapAt50() {
            // given
            when(aiUsageHistoryMapper.getFailedRequests(50)).thenReturn(new ArrayList<>());

            // when
            usageHistoryService.getFailedRequests(200);

            // then
            verify(aiUsageHistoryMapper).getFailedRequests(50);
        }
    }

    // ========== getSlowRequests 测试 ==========

    @Nested
    @DisplayName("getSlowRequests 方法测试")
    class GetSlowRequestsTests {

        @Test
        @DisplayName("getSlowRequests - 正常场景")
        void getSlowRequests_shouldReturnRequests() {
            // given
            List<AiUsageHistory> slow = List.of(
                createTestUsage("sl1", "user-001", "gpt-4o", 100, 1)
            );
            when(aiUsageHistoryMapper.getSlowRequests(5000, 50)).thenReturn(slow);

            // when
            List<AiUsageHistory> result = usageHistoryService.getSlowRequests(5000, 50);

            // then
            assertThat(result).hasSize(1);
        }
    }

    // ========== getTodayUsage 测试 ==========

    @Nested
    @DisplayName("getTodayUsage 方法测试")
    class GetTodayUsageTests {

        @Test
        @DisplayName("getTodayUsage - 正常场景")
        void getTodayUsage_shouldReturnTodayUsage() {
            // given
            List<AiUsageHistory> today = List.of(
                createTestUsage("t1", "user-001", "gpt-4o", 300, 1)
            );
            when(aiUsageHistoryMapper.getTodayUsage()).thenReturn(today);

            // when
            List<AiUsageHistory> result = usageHistoryService.getTodayUsage();

            // then
            assertThat(result).hasSize(1);
        }

        @Test
        @DisplayName("getTodayUsage - 无数据返回空列表")
        void getTodayUsage_withNoData_shouldReturnEmpty() {
            // given
            when(aiUsageHistoryMapper.getTodayUsage()).thenReturn(new ArrayList<>());

            // when
            List<AiUsageHistory> result = usageHistoryService.getTodayUsage();

            // then
            assertThat(result).isEmpty();
        }
    }

    // ========== cleanOldRecords 测试 ==========

    @Nested
    @DisplayName("cleanOldRecords 方法测试")
    class CleanOldRecordsTests {

        @Test
        @DisplayName("cleanOldRecords - 正常场景")
        void cleanOldRecords_shouldReturnDeletedCount() {
            // given
            when(aiUsageHistoryMapper.cleanOldRecords(any(LocalDateTime.class))).thenReturn(100);

            // when
            int result = usageHistoryService.cleanOldRecords(90);

            // then
            assertThat(result).isEqualTo(100);
        }

        @Test
        @DisplayName("cleanOldRecords - daysToKeep < 1返回0")
        void cleanOldRecords_withInvalidDays_shouldReturn0() {
            // when
            int result = usageHistoryService.cleanOldRecords(0);

            // then
            assertThat(result).isEqualTo(0);
            verify(aiUsageHistoryMapper, never()).cleanOldRecords(any());
        }

        @Test
        @DisplayName("cleanOldRecords - 负数返回0")
        void cleanOldRecords_withNegativeDays_shouldReturn0() {
            // when
            int result = usageHistoryService.cleanOldRecords(-5);

            // then
            assertThat(result).isEqualTo(0);
        }
    }

    // ========== getSystemStats 测试 ==========

    @Nested
    @DisplayName("getSystemStats 方法测试")
    class GetSystemStatsTests {

        @Test
        @DisplayName("getSystemStats - 正常场景")
        void getSystemStats_shouldReturnStats() {
            // given
            List<AiUsageHistory> todayUsage = List.of(
                createTestUsage("sys1", "user-001", "gpt-4o", 200, 1),
                createTestUsage("sys2", "user-002", "gpt-4o", 300, 1)
            );
            when(aiUsageHistoryMapper.getTodayUsage()).thenReturn(todayUsage);

            AiUsageHistoryMapper.DailyUsageStats stat = mock(AiUsageHistoryMapper.DailyUsageStats.class);
            when(stat.getDate()).thenReturn(LocalDate.now());
            when(stat.getModelCode()).thenReturn("gpt-4o");
            when(stat.getRequestCount()).thenReturn(50L);
            when(stat.getSuccessCount()).thenReturn(48L);
            when(stat.getTotalTokens()).thenReturn(25000L);
            when(stat.getTotalCost()).thenReturn(new BigDecimal("0.5"));
            when(aiUsageHistoryMapper.getDailyUsageStats(any(), any())).thenReturn(List.of(stat));

            when(aiUsageHistoryMapper.selectCount(any())).thenReturn(1000L);

            // when
            Map<String, Object> result = usageHistoryService.getSystemStats();

            // then
            assertThat(result).isNotNull();
            assertThat(result).containsKey("today");
            assertThat(result).containsKey("weeklyStats");
            assertThat(result).containsKey("totalRecords");
            assertThat(result.get("totalRecords")).isEqualTo(1000L);
        }

        @Test
        @DisplayName("getSystemStats - 数据库异常时返回error标记")
        void getSystemStats_withException_shouldHaveErrorMark() {
            // given - getDailyStats swallows exceptions internally, so we need to test the catch-all
            // The try-catch in getSystemStats wraps everything, so if anything throws inside
            // it will be caught and result will have error key
            // But getDailyStats has its own try-catch, so we need to make selectCount throw
            when(aiUsageHistoryMapper.getTodayUsage()).thenReturn(new ArrayList<>());
            when(aiUsageHistoryMapper.selectCount(any())).thenThrow(new RuntimeException("DB error"));

            // when
            Map<String, Object> result = usageHistoryService.getSystemStats();

            // then - selectCount throws inside the try block, caught and error key added
            assertThat(result).containsKey("error");
        }
    }
}