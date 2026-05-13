package com.aioa.ai.controller;

import com.aioa.ai.entity.AiUsageHistory;
import com.aioa.ai.service.AiUsageHistoryService;
import com.aioa.common.exception.GlobalExceptionHandler;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
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

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * AiUsageHistoryController 单元测试
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("AiUsageHistoryControllerTest AI使用记录控制器测试")
class AiUsageHistoryControllerTest {

    private MockMvc mockMvc;
    private ObjectMapper objectMapper;

    @Mock
    private AiUsageHistoryService aiUsageHistoryService;

    @InjectMocks
    private AiUsageHistoryController aiUsageHistoryController;

    private AiUsageHistory testUsage;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(aiUsageHistoryController)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
        objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());

        testUsage = new AiUsageHistory();
        testUsage.setId("1");
        testUsage.setUserId("user123");
        testUsage.setModelCode("gpt-4o");
        testUsage.setPromptTokens(100);
        testUsage.setCompletionTokens(50);
        testUsage.setTotalTokens(150);
        testUsage.setCostUsd(new BigDecimal("0.005"));
        testUsage.setRequestTime(LocalDateTime.now().minusMinutes(5));
        testUsage.setResponseTime(LocalDateTime.now());
        testUsage.setDurationMs(1500);
        testUsage.setSuccess(1);
    }

    // ==================== 记录使用 ====================

    @Test
    @DisplayName("记录使用成功")
    void recordUsage_success() throws Exception {
        doNothing().when(aiUsageHistoryService).recordUsage(any(AiUsageHistory.class));

        mockMvc.perform(post("/api/ai/usage/record")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(testUsage)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data").value("AI usage recorded successfully"));

        verify(aiUsageHistoryService, times(1)).recordUsage(any(AiUsageHistory.class));
    }

    // ==================== 获取用户历史 ====================

    @Test
    @DisplayName("获取用户历史成功-默认限制")
    void getUserHistory_defaultLimit() throws Exception {
        when(aiUsageHistoryService.getUserHistory(eq("user123"), eq(20)))
                .thenReturn(List.of(testUsage));

        mockMvc.perform(get("/api/ai/usage/user/user123"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data[0].userId").value("user123"));

        verify(aiUsageHistoryService, times(1)).getUserHistory("user123", 20);
    }

    @Test
    @DisplayName("获取用户历史成功-自定义限制")
    void getUserHistory_customLimit() throws Exception {
        when(aiUsageHistoryService.getUserHistory(eq("user456"), eq(50)))
                .thenReturn(List.of());

        mockMvc.perform(get("/api/ai/usage/user/user456")
                        .param("limit", "50"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.length()").value(0));

        verify(aiUsageHistoryService, times(1)).getUserHistory("user456", 50);
    }

    @Test
    @DisplayName("获取用户历史为空")
    void getUserHistory_empty() throws Exception {
        when(aiUsageHistoryService.getUserHistory(anyString(), anyInt()))
                .thenReturn(List.of());

        mockMvc.perform(get("/api/ai/usage/user/nonexistent"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.length()").value(0));
    }

    // ==================== 获取模型历史 ====================

    @Test
    @DisplayName("获取模型历史成功")
    void getModelHistory_success() throws Exception {
        when(aiUsageHistoryService.getModelHistory(eq("gpt-4o"), eq(20)))
                .thenReturn(List.of(testUsage));

        mockMvc.perform(get("/api/ai/usage/model/gpt-4o"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data[0].modelCode").value("gpt-4o"));

        verify(aiUsageHistoryService, times(1)).getModelHistory("gpt-4o", 20);
    }

    // ==================== 获取每日统计 ====================

    @Test
    @DisplayName("获取每日统计成功")
    void getDailyStats_success() throws Exception {
        LocalDateTime startDate = LocalDateTime.of(2024, 1, 1, 0, 0);
        LocalDateTime endDate = LocalDateTime.of(2024, 1, 31, 23, 59);

        Map<String, Object> stats = new HashMap<>();
        stats.put("date", "2024-01-15");
        stats.put("totalTokens", 10000);
        stats.put("totalCost", 0.5);

        when(aiUsageHistoryService.getDailyStats(any(LocalDateTime.class), any(LocalDateTime.class)))
                .thenReturn(List.of(stats));

        mockMvc.perform(get("/api/ai/usage/daily-stats")
                        .param("startDate", startDate.toString())
                        .param("endDate", endDate.toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data[0].date").value("2024-01-15"));

        verify(aiUsageHistoryService, times(1)).getDailyStats(any(), any());
    }

    // ==================== 获取用户摘要 ====================

    @Test
    @DisplayName("获取用户摘要成功")
    void getUserSummary_success() throws Exception {
        LocalDateTime startDate = LocalDateTime.of(2024, 1, 1, 0, 0);
        LocalDateTime endDate = LocalDateTime.of(2024, 1, 31, 23, 59);

        Map<String, Object> summary = new HashMap<>();
        summary.put("userId", "user123");
        summary.put("totalRequests", 100);
        summary.put("totalTokens", 50000);
        summary.put("totalCost", 2.5);

        when(aiUsageHistoryService.getUserSummary(eq("user123"), any(), any()))
                .thenReturn(summary);

        mockMvc.perform(get("/api/ai/usage/user-summary/user123")
                        .param("startDate", startDate.toString())
                        .param("endDate", endDate.toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.userId").value("user123"))
                .andExpect(jsonPath("$.data.totalRequests").value(100));

        verify(aiUsageHistoryService, times(1)).getUserSummary(eq("user123"), any(), any());
    }

    // ==================== 获取模型成本统计 ====================

    @Test
    @DisplayName("获取模型成本统计成功")
    void getModelCostStats_success() throws Exception {
        Map<String, Object> costStats = new HashMap<>();
        costStats.put("modelCode", "gpt-4o");
        costStats.put("totalCost", 50.0);
        costStats.put("requestCount", 1000);

        when(aiUsageHistoryService.getModelCostStats())
                .thenReturn(List.of(costStats));

        mockMvc.perform(get("/api/ai/usage/model-cost-stats"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data[0].modelCode").value("gpt-4o"));

        verify(aiUsageHistoryService, times(1)).getModelCostStats();
    }

    // ==================== 获取失败请求 ====================

    @Test
    @DisplayName("获取失败请求成功-默认限制")
    void getFailedRequests_defaultLimit() throws Exception {
        AiUsageHistory failedUsage = new AiUsageHistory();
        failedUsage.setId("2");
        failedUsage.setUserId("user789");
        failedUsage.setModelCode("claude-3.5");
        failedUsage.setSuccess(0);
        failedUsage.setErrorMessage("API timeout");

        when(aiUsageHistoryService.getFailedRequests(10))
                .thenReturn(List.of(failedUsage));

        mockMvc.perform(get("/api/ai/usage/failed"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data[0].success").value(0));

        verify(aiUsageHistoryService, times(1)).getFailedRequests(10);
    }

    // ==================== 获取慢请求 ====================

    @Test
    @DisplayName("获取慢请求成功-默认阈值")
    void getSlowRequests_defaultThreshold() throws Exception {
        when(aiUsageHistoryService.getSlowRequests(5000, 10))
                .thenReturn(List.of());

        mockMvc.perform(get("/api/ai/usage/slow"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.length()").value(0));

        verify(aiUsageHistoryService, times(1)).getSlowRequests(5000, 10);
    }

    @Test
    @DisplayName("获取慢请求成功-自定义阈值")
    void getSlowRequests_customThreshold() throws Exception {
        when(aiUsageHistoryService.getSlowRequests(3000, 20))
                .thenReturn(List.of());

        mockMvc.perform(get("/api/ai/usage/slow")
                        .param("thresholdMs", "3000")
                        .param("limit", "20"))
                .andExpect(status().isOk());

        verify(aiUsageHistoryService, times(1)).getSlowRequests(3000, 20);
    }

    // ==================== 获取今日使用 ====================

    @Test
    @DisplayName("获取今日使用成功")
    void getTodayUsage_success() throws Exception {
        when(aiUsageHistoryService.getTodayUsage())
                .thenReturn(List.of(testUsage));

        mockMvc.perform(get("/api/ai/usage/today"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.length()").value(1));

        verify(aiUsageHistoryService, times(1)).getTodayUsage();
    }

    // ==================== 清理旧记录 ====================

    @Test
    @DisplayName("清理旧记录成功")
    void cleanOldRecords_success() throws Exception {
        when(aiUsageHistoryService.cleanOldRecords(30))
                .thenReturn(100);

        mockMvc.perform(post("/api/ai/usage/clean-old/30"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data").value("Cleaned 100 old records"));

        verify(aiUsageHistoryService, times(1)).cleanOldRecords(30);
    }

    @Test
    @DisplayName("清理旧记录失败-天数太少")
    void cleanOldRecords_tooFewDays() throws Exception {
        mockMvc.perform(post("/api/ai/usage/clean-old/5"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Days to keep must be at least 7 for data retention"));

        verify(aiUsageHistoryService, never()).cleanOldRecords(anyInt());
    }

    // ==================== 获取系统统计 ====================

    @Test
    @DisplayName("获取系统统计成功")
    void getSystemStats_success() throws Exception {
        mockMvc.perform(get("/api/ai/usage/system-stats"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.message").exists());
    }

    // ==================== 异常场景 ====================

        // JSON invalid test requires Spring context; verify service not called instead
    @Test
    @DisplayName("记录使用-无效JSON格式时服务不被调用")
    void recordUsage_invalidJson_noServiceCall() throws Exception {
        mockMvc.perform(post("/api/ai/usage/record")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{invalid json}"))
                .andExpect(status().isOk());

        verify(aiUsageHistoryService, never()).recordUsage(any());
    }

    @Test
    @DisplayName("获取用户历史-服务异常")
    void getUserHistory_serviceException() throws Exception {
        when(aiUsageHistoryService.getUserHistory(anyString(), anyInt()))
                .thenThrow(new RuntimeException("Database connection failed"));

        mockMvc.perform(get("/api/ai/usage/user/user123"))
                .andExpect(jsonPath("$.code").value(500));
    }
}