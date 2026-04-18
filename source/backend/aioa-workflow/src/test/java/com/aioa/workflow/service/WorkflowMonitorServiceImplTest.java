package com.aioa.workflow.service;

import com.aioa.workflow.entity.WorkflowMetrics;
import com.aioa.workflow.entity.WorkflowAlert;
import com.aioa.workflow.enums.AlertLevel;
import com.aioa.workflow.enums.WorkflowStatus;
import com.aioa.workflow.service.impl.WorkflowMonitorServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * WorkflowMonitorServiceImpl单元测试
 * 
 * 测试工作流监控服务的核心功能：
 * 1. 工作流指标统计
 * 2. 告警管理
 * 3. 健康检查
 * 4. 性能分析
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("WorkflowMonitorServiceImpl 单元测试")
class WorkflowMonitorServiceImplTest {

    @Mock
    private ApprovalService approvalService;

    @Mock
    private WorkflowMetricsService workflowMetricsService;

    @Mock
    private WorkflowAlertService workflowAlertService;

    @InjectMocks
    private WorkflowMonitorServiceImpl workflowMonitorService;

    private WorkflowMetrics testMetrics;
    private WorkflowAlert testAlert;

    @BeforeEach
    void setUp() {
        // 创建测试指标数据
        testMetrics = new WorkflowMetrics();
        testMetrics.setId("metrics-001");
        testMetrics.setDate(LocalDateTime.now());
        testMetrics.setTotalWorkflows(100);
        testMetrics.setCompletedWorkflows(85);
        testMetrics.setFailedWorkflows(5);
        testMetrics.setAvgCompletionTime(3600L); // 1小时
        testMetrics.setP99CompletionTime(7200L); // 2小时
        testMetrics.setSuccessRate(85.0);
        testMetrics.setCreateTime(LocalDateTime.now());

        // 创建测试告警数据
        testAlert = new WorkflowAlert();
        testAlert.setId("alert-001");
        testAlert.setWorkflowId("workflow-123");
        testAlert.setAlertLevel(AlertLevel.WARNING);
        testAlert.setAlertMessage("工作流执行超时");
        testAlert.setResolved(false);
        testAlert.setCreateTime(LocalDateTime.now());
    }

    @Test
    @DisplayName("测试收集工作流指标 - 成功场景")
    void testCollectWorkflowMetrics_Success() {
        // 准备测试数据
        LocalDateTime startTime = LocalDateTime.now().minusDays(1);
        LocalDateTime endTime = LocalDateTime.now();
        
        // 模拟统计数据
        when(approvalService.countWorkflowsByStatus(any(), any(), any()))
                .thenReturn(100L)  // 总数
                .thenReturn(85L)   // 已完成
                .thenReturn(5L);   // 已失败
        
        when(approvalService.getAverageCompletionTime(any(), any()))
                .thenReturn(3600L);
        
        when(approvalService.getP99CompletionTime(any(), any()))
                .thenReturn(7200L);

        // 模拟保存指标
        when(workflowMetricsService.save(any(WorkflowMetrics.class)))
                .thenReturn(true);

        // 执行指标收集
        boolean result = workflowMonitorService.collectWorkflowMetrics(startTime, endTime);

        // 验证结果
        assertTrue(result);

        // 验证方法调用
        verify(approvalService, times(3)).countWorkflowsByStatus(any(), any(), any());
        verify(approvalService).getAverageCompletionTime(startTime, endTime);
        verify(approvalService).getP99CompletionTime(startTime, endTime);
        verify(workflowMetricsService).save(argThat(metrics ->
                metrics.getTotalWorkflows() == 100 &&
                metrics.getCompletedWorkflows() == 85 &&
                metrics.getSuccessRate() == 85.0
        ));
    }

    @Test
    @DisplayName("测试检查超时工作流 - 发现超时")
    void testCheckTimeoutWorkflows_FoundTimeout() {
        // 准备测试数据
        long timeoutThreshold = 7200L; // 2小时
        List<String> timeoutWorkflows = Arrays.asList("workflow-1", "workflow-2", "workflow-3");
        
        // 模拟查找超时工作流
        when(approvalService.findTimeoutWorkflows(timeoutThreshold))
                .thenReturn(timeoutWorkflows);

        // 模拟保存告警
        when(workflowAlertService.save(any(WorkflowAlert.class)))
                .thenReturn(true);

        // 执行检查
        List<String> result = workflowMonitorService.checkTimeoutWorkflows(timeoutThreshold);

        // 验证结果
        assertNotNull(result);
        assertEquals(3, result.size());
        assertTrue(result.containsAll(timeoutWorkflows));

        // 验证方法调用
        verify(approvalService).findTimeoutWorkflows(timeoutThreshold);
        verify(workflowAlertService, times(3)).save(argThat(alert ->
                alert.getAlertLevel() == AlertLevel.WARNING &&
                alert.getAlertMessage().contains("执行超时") &&
                !alert.isResolved()
        ));
    }

    @Test
    @DisplayName("测试检查超时工作流 - 无超时")
    void testCheckTimeoutWorkflows_NoTimeout() {
        // 准备测试数据
        long timeoutThreshold = 7200L;
        
        // 模拟无超时工作流
        when(approvalService.findTimeoutWorkflows(timeoutThreshold))
                .thenReturn(Arrays.asList());

        // 执行检查
        List<String> result = workflowMonitorService.checkTimeoutWorkflows(timeoutThreshold);

        // 验证结果
        assertNotNull(result);
        assertTrue(result.isEmpty());

        // 验证方法调用
        verify(approvalService).findTimeoutWorkflows(timeoutThreshold);
        verify(workflowAlertService, never()).save(any());
    }

    @Test
    @DisplayName("测试获取性能指标 - 成功场景")
    void testGetPerformanceMetrics_Success() {
        // 准备测试数据
        LocalDateTime startTime = LocalDateTime.now().minusDays(7);
        LocalDateTime endTime = LocalDateTime.now();
        
        // 模拟查询历史指标
        List<WorkflowMetrics> historicalMetrics = Arrays.asList(
                createTestMetrics(LocalDateTime.now().minusDays(6), 80.0),
                createTestMetrics(LocalDateTime.now().minusDays(5), 82.0),
                createTestMetrics(LocalDateTime.now().minusDays(4), 85.0),
                createTestMetrics(LocalDateTime.now().minusDays(3), 83.0),
                createTestMetrics(LocalDateTime.now().minusDays(2), 87.0),
                createTestMetrics(LocalDateTime.now().minusDays(1), 88.0),
                createTestMetrics(LocalDateTime.now(), 85.0)
        );
        
        when(workflowMetricsService.findByDateRange(startTime, endTime))
                .thenReturn(historicalMetrics);

        // 执行获取性能指标
        List<WorkflowMetrics> result = workflowMonitorService.getPerformanceMetrics(startTime, endTime);

        // 验证结果
        assertNotNull(result);
        assertEquals(7, result.size());
        
        // 验证趋势分析
        double trend = workflowMonitorService.calculateSuccessRateTrend(historicalMetrics);
        assertTrue(trend > 0, "成功率应有提升趋势");

        // 验证方法调用
        verify(workflowMetricsService).findByDateRange(startTime, endTime);
    }

    @Test
    @DisplayName("测试获取活动告警 - 成功场景")
    void testGetActiveAlerts_Success() {
        // 准备测试数据
        List<WorkflowAlert> activeAlerts = Arrays.asList(
                createTestAlert("alert-1", AlertLevel.WARNING, false),
                createTestAlert("alert-2", AlertLevel.ERROR, false),
                createTestAlert("alert-3", AlertLevel.INFO, false)
        );
        
        when(workflowAlertService.findActiveAlerts())
                .thenReturn(activeAlerts);

        // 执行获取活动告警
        List<WorkflowAlert> result = workflowMonitorService.getActiveAlerts();

        // 验证结果
        assertNotNull(result);
        assertEquals(3, result.size());
        assertTrue(result.stream().noneMatch(WorkflowAlert::isResolved));

        // 验证方法调用
        verify(workflowAlertService).findActiveAlerts();
    }

    @Test
    @DisplayName("测试解析告警 - 成功场景")
    void testResolveAlert_Success() {
        // 准备测试数据
        String alertId = "alert-001";
        String resolution = "手动重启工作流后解决";
        
        // 模拟查找告警
        when(workflowAlertService.findById(alertId))
                .thenReturn(testAlert);
        
        // 模拟更新告警
        when(workflowAlertService.update(any(WorkflowAlert.class)))
                .thenReturn(true);

        // 执行解析告警
        boolean result = workflowMonitorService.resolveAlert(alertId, resolution);

        // 验证结果
        assertTrue(result);

        // 验证方法调用
        verify(workflowAlertService).findById(alertId);
        verify(workflowAlertService).update(argThat(alert ->
                alert.getId().equals(alertId) &&
                alert.isResolved() &&
                alert.getResolution().equals(resolution) &&
                alert.getResolvedTime() != null
        ));
    }

    @Test
    @DisplayName("测试解析告警 - 告警不存在")
    void testResolveAlert_NotFound() {
        // 准备测试数据
        String alertId = "nonexistent-alert";
        String resolution = "测试解析";
        
        // 模拟告警不存在
        when(workflowAlertService.findById(alertId))
                .thenReturn(null);

        // 执行解析告警
        boolean result = workflowMonitorService.resolveAlert(alertId, resolution);

        // 验证结果
        assertFalse(result);

        // 验证方法调用
        verify(workflowAlertService).findById(alertId);
        verify(workflowAlertService, never()).update(any());
    }

    @Test
    @DisplayName("测试系统健康检查 - 所有组件正常")
    void testHealthCheck_AllHealthy() {
        // 模拟所有组件健康
        when(approvalService.isHealthy())
                .thenReturn(true);
        when(workflowMetricsService.isHealthy())
                .thenReturn(true);
        when(workflowAlertService.isHealthy())
                .thenReturn(true);

        // 执行健康检查
        boolean result = workflowMonitorService.healthCheck();

        // 验证结果
        assertTrue(result);

        // 验证方法调用
        verify(approvalService).isHealthy();
        verify(workflowMetricsService).isHealthy();
        verify(workflowAlertService).isHealthy();
    }

    @Test
    @DisplayName("测试系统健康检查 - 组件异常")
    void testHealthCheck_ComponentUnhealthy() {
        // 模拟approval服务异常
        when(approvalService.isHealthy())
                .thenReturn(false);
        when(workflowMetricsService.isHealthy())
                .thenReturn(true);
        when(workflowAlertService.isHealthy())
                .thenReturn(true);

        // 执行健康检查
        boolean result = workflowMonitorService.healthCheck();

        // 验证结果
        assertFalse(result);

        // 验证方法调用
        verify(approvalService).isHealthy();
        verify(workflowMetricsService).isHealthy();
        verify(workflowAlertService).isHealthy();
    }

    @Test
    @DisplayName("测试生成性能报告 - 成功场景")
    void testGeneratePerformanceReport_Success() {
        // 准备测试数据
        LocalDateTime reportStart = LocalDateTime.now().minusDays(30);
        LocalDateTime reportEnd = LocalDateTime.now();
        
        // 模拟性能指标数据
        List<WorkflowMetrics> metrics = Arrays.asList(
                createTestMetrics(LocalDateTime.now().minusDays(7), 85.0),
                createTestMetrics(LocalDateTime.now(), 88.0)
        );
        
        when(workflowMetricsService.findByDateRange(reportStart, reportEnd))
                .thenReturn(metrics);
        
        when(approvalService.countWorkflowsByStatus(reportStart, reportEnd, WorkflowStatus.COMPLETED))
                .thenReturn(150L);
        when(approvalService.countWorkflowsByStatus(reportStart, reportEnd, WorkflowStatus.FAILED))
                .thenReturn(10L);
        when(approvalService.countWorkflowsByStatus(reportStart, reportEnd, null))
                .thenReturn(200L);

        // 执行生成报告
        String report = workflowMonitorService.generatePerformanceReport(reportStart, reportEnd);

        // 验证结果
        assertNotNull(report);
        assertTrue(report.contains("性能报告"));
        assertTrue(report.contains("成功率"));
        assertTrue(report.contains("处理量"));

        // 验证方法调用
        verify(workflowMetricsService).findByDateRange(reportStart, reportEnd);
        verify(approvalService, times(3)).countWorkflowsByStatus(any(), any(), any());
    }

    @Test
    @DisplayName("测试监控任务调度 - 集成检查")
    void testMonitorTaskIntegration() {
        // 模拟监控任务执行的各个组件
        when(approvalService.findTimeoutWorkflows(anyLong()))
                .thenReturn(Arrays.asList("workflow-1"));
        when(workflowAlertService.save(any()))
                .thenReturn(true);
        when(approvalService.isHealthy())
                .thenReturn(true);

        // 执行完整的监控流程
        List<String> timeouts = workflowMonitorService.checkTimeoutWorkflows(7200L);
        boolean health = workflowMonitorService.healthCheck();

        // 验证集成结果
        assertEquals(1, timeouts.size());
        assertTrue(health);

        // 验证方法调用
        verify(approvalService).findTimeoutWorkflows(7200L);
        verify(workflowAlertService).save(any());
        verify(approvalService).isHealthy();
    }

    // 辅助方法
    private WorkflowMetrics createTestMetrics(LocalDateTime date, double successRate) {
        WorkflowMetrics metrics = new WorkflowMetrics();
        metrics.setDate(date);
        metrics.setTotalWorkflows(100);
        metrics.setCompletedWorkflows((int) successRate);
        metrics.setFailedWorkflows(5);
        metrics.setAvgCompletionTime(3600L);
        metrics.setSuccessRate(successRate);
        return metrics;
    }

    private WorkflowAlert createTestAlert(String id, AlertLevel level, boolean resolved) {
        WorkflowAlert alert = new WorkflowAlert();
        alert.setId(id);
        alert.setAlertLevel(level);
        alert.setAlertMessage("测试告警 " + id);
        alert.setResolved(resolved);
        alert.setCreateTime(LocalDateTime.now());
        return alert;
    }
}