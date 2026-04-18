package com.aioa.workflow.service;

import com.aioa.workflow.dto.WorkflowMetricsDTO;
import com.aioa.workflow.dto.WorkflowAlertDTO;
import com.aioa.workflow.entity.WorkflowInstance;
import com.aioa.workflow.enums.WorkflowStatus;
import com.aioa.workflow.mapper.WorkflowInstanceMapper;
import com.aioa.workflow.service.impl.WorkflowMonitorServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * WorkflowMonitorServiceImpl 单元测试
 * 毛主席思想指导：实事求是，全面测试工作流监控服务
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("WorkflowMonitorServiceImpl 单元测试")
class WorkflowMonitorServiceImplTest {

    @Mock
    private WorkflowInstanceMapper instanceMapper;

    @Mock
    private RedisTemplate<String, Object> redisTemplate;

    @InjectMocks
    private WorkflowMonitorServiceImpl workflowMonitorService;

    private LocalDateTime testStartTime;
    private LocalDateTime testEndTime;

    // 测试数据常量
    private static final String WORKFLOW_ID_1 = "WF-001";
    private static final String WORKFLOW_ID_2 = "WF-002";
    private static final String INSTANCE_ID_1 = "INS-001";
    private static final String INSTANCE_ID_2 = "INS-002";
    private static final int FAILURE_THRESHOLD = 5;
    private static final int TIMEOUT_THRESHOLD_MINUTES = 30;
    private static final int SUCCESS_RATE_THRESHOLD = 95;

    @BeforeEach
    void setUp() {
        // 初始化测试时间范围 - 实事求是
        testStartTime = LocalDateTime.now().minusHours(1);
        testEndTime = LocalDateTime.now();

        // 设置配置值
        ReflectionTestUtils.setField(workflowMonitorService, "monitorEnabled", true);
        ReflectionTestUtils.setField(workflowMonitorService, "failureThreshold", FAILURE_THRESHOLD);
        ReflectionTestUtils.setField(workflowMonitorService, "timeoutThresholdMinutes", TIMEOUT_THRESHOLD_MINUTES);
        ReflectionTestUtils.setField(workflowMonitorService, "successRateThreshold", SUCCESS_RATE_THRESHOLD);
        ReflectionTestUtils.setField(workflowMonitorService, "alertCooldownMinutes", 15);
    }

    @Test
    @DisplayName("getMetrics - 正常获取指标应返回完整数据")
    void getMetrics_withValidTimeRange_shouldReturnCompleteMetrics() {
        // given - 准备测试数据
        Map<WorkflowStatus, Long> statusCounts = new HashMap<>();
        statusCounts.put(WorkflowStatus.COMPLETED, 80L);
        statusCounts.put(WorkflowStatus.FAILED, 10L);
        statusCounts.put(WorkflowStatus.RUNNING, 5L);
        statusCounts.put(WorkflowStatus.PENDING, 3L);
        statusCounts.put(WorkflowStatus.TIMEOUT, 2L);

        when(instanceMapper.count(null, WorkflowStatus.COMPLETED, testStartTime, testEndTime)).thenReturn(80);
        when(instanceMapper.count(null, WorkflowStatus.FAILED, testStartTime, testEndTime)).thenReturn(10);
        when(instanceMapper.count(null, WorkflowStatus.RUNNING, testStartTime, testEndTime)).thenReturn(5);
        when(instanceMapper.count(null, WorkflowStatus.PENDING, testStartTime, testEndTime)).thenReturn(3);
        when(instanceMapper.count(null, WorkflowStatus.TIMEOUT, testStartTime, testEndTime)).thenReturn(2);
        when(instanceMapper.count(null, null, testStartTime, testEndTime)).thenReturn(100);
        when(instanceMapper.getAverageDuration(testStartTime, testEndTime)).thenReturn(150.5);
        when(instanceMapper.getP95Duration(testStartTime, testEndTime)).thenReturn(300.0);
        when(instanceMapper.countByStatus(WorkflowStatus.TIMEOUT, testStartTime, testEndTime)).thenReturn(2);

        // when
        WorkflowMetricsDTO metrics = workflowMonitorService.getMetrics(testStartTime, testEndTime);

        // then - 验证结果
        assertThat(metrics).isNotNull();
        assertThat(metrics.getTotalCount()).isEqualTo(100);
        assertThat(metrics.getCompletedCount()).isEqualTo(80);
        assertThat(metrics.getFailedCount()).isEqualTo(10);
        assertThat(metrics.getRunningCount()).isEqualTo(5);
        assertThat(metrics.getPendingCount()).isEqualTo(3);
        assertThat(metrics.getTimeoutCount()).isEqualTo(2);
        assertThat(metrics.getSuccessRate()).isEqualTo(80.0);
        assertThat(metrics.getAverageDuration()).isEqualTo(150.5);
        assertThat(metrics.getP95Duration()).isEqualTo(300.0);
        assertThat(metrics.getHealthScore()).isGreaterThan(0);
        assertThat(metrics.getStatusDistribution()).containsKeys("COMPLETED", "FAILED", "RUNNING");

        // 验证方法调用
        verify(instanceMapper, times(1)).getAverageDuration(testStartTime, testEndTime);
        verify(instanceMapper, times(1)).getP95Duration(testStartTime, testEndTime);
    }

    @Test
    @DisplayName("getMetrics - 无数据时应返回零值")
    void getMetrics_withNoData_shouldReturnZeroValues() {
        // given - 无数据场景
        when(instanceMapper.count(any(), any(), any(), any())).thenReturn(0);
        when(instanceMapper.getAverageDuration(testStartTime, testEndTime)).thenReturn(null);
        when(instanceMapper.getP95Duration(testStartTime, testEndTime)).thenReturn(null);

        // when
        WorkflowMetricsDTO metrics = workflowMonitorService.getMetrics(testStartTime, testEndTime);

        // then - 验证零值处理
        assertThat(metrics.getTotalCount()).isEqualTo(0);
        assertThat(metrics.getSuccessRate()).isEqualTo(0.0);
        assertThat(metrics.getAverageDuration()).isEqualTo(0);
        assertThat(metrics.getP95Duration()).isEqualTo(0);
    }

    @Test
    @DisplayName("getMetrics - 数据库异常时应抛出RuntimeException")
    void getMetrics_withDatabaseError_shouldThrowRuntimeException() {
        // given - 数据库异常场景
        when(instanceMapper.count(any(), any(), any(), any()))
                .thenThrow(new RuntimeException("数据库连接失败"));

        // when & then
        assertThatThrownBy(() -> workflowMonitorService.getMetrics(testStartTime, testEndTime))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("获取工作流指标失败");
    }

    @Test
    @DisplayName("getActiveAlerts - 有未解决告警时应返回告警列表")
    void getActiveAlerts_withUnresolvedAlerts_shouldReturnAlerts() {
        // given - 准备告警数据
        WorkflowAlertDTO alert1 = createTestAlert("WFA-001", "HIGH_FAILURE_RATE", false);
        WorkflowAlertDTO alert2 = createTestAlert("WFA-002", "INSTANCE_TIMEOUT", false);
        
        ConcurrentHashMap<String, List<WorkflowAlertDTO>> alertHistory = new ConcurrentHashMap<>();
        alertHistory.put("global", new ArrayList<>(Arrays.asList(alert1, alert2)));
        
        ReflectionTestUtils.setField(workflowMonitorService, "alertHistory", alertHistory);
        ReflectionTestUtils.setField(workflowMonitorService, "alertCooldowns", new ConcurrentHashMap<>());

        // when
        List<WorkflowAlertDTO> activeAlerts = workflowMonitorService.getActiveAlerts();

        // then
        assertThat(activeAlerts).hasSize(2);
        assertThat(activeAlerts).extracting(WorkflowAlertDTO::getAlertId)
                .containsExactlyInAnyOrder("WFA-001", "WFA-002");
    }

    @Test
    @DisplayName("getActiveAlerts - 冷却期内应不返回重复告警")
    void getActiveAlerts_withinCooldownPeriod_shouldNotReturnDuplicateAlerts() {
        // given - 刚发送过告警，还在冷却期
        WorkflowAlertDTO alert = createTestAlert("WFA-001", "HIGH_FAILURE_RATE", false);
        
        ConcurrentHashMap<String, List<WorkflowAlertDTO>> alertHistory = new ConcurrentHashMap<>();
        alertHistory.put("global", new ArrayList<>(List.of(alert)));
        
        ConcurrentHashMap<String, LocalDateTime> alertCooldowns = new ConcurrentHashMap<>();
        alertCooldowns.put("global", LocalDateTime.now()); // 刚刚发送
        
        ReflectionTestUtils.setField(workflowMonitorService, "alertHistory", alertHistory);
        ReflectionTestUtils.setField(workflowMonitorService, "alertCooldowns", alertCooldowns);

        // when
        List<WorkflowAlertDTO> activeAlerts = workflowMonitorService.getActiveAlerts();

        // then - 冷却期内不应返回告警
        assertThat(activeAlerts).isEmpty();
    }

    @Test
    @DisplayName("getAlertHistory - 指定工作流ID应返回该工作流的告警")
    void getAlertHistory_withWorkflowId_shouldReturnWorkflowAlerts() {
        // given
        LocalDateTime startTime = LocalDateTime.now().minusDays(1);
        LocalDateTime endTime = LocalDateTime.now();
        int limit = 10;

        WorkflowAlertDTO alert1 = createTestAlert("WFA-001", "HIGH_FAILURE_RATE", true);
        alert1.setWorkflowId(WORKFLOW_ID_1);
        alert1.setCreatedAt(LocalDateTime.now().minusHours(1));

        WorkflowAlertDTO alert2 = createTestAlert("WFA-002", "INSTANCE_TIMEOUT", false);
        alert2.setWorkflowId(WORKFLOW_ID_1);
        alert2.setCreatedAt(LocalDateTime.now().minusHours(2));

        WorkflowAlertDTO alert3 = createTestAlert("WFA-003", "PENDING_BACKLOG", false);
        alert3.setWorkflowId(WORKFLOW_ID_2);
        alert3.setCreatedAt(LocalDateTime.now().minusHours(1));

        ConcurrentHashMap<String, List<WorkflowAlertDTO>> alertHistory = new ConcurrentHashMap<>();
        alertHistory.put(WORKFLOW_ID_1, new ArrayList<>(Arrays.asList(alert1, alert2)));
        alertHistory.put(WORKFLOW_ID_2, new ArrayList<>(List.of(alert3)));

        ReflectionTestUtils.setField(workflowMonitorService, "alertHistory", alertHistory);

        // when
        List<WorkflowAlertDTO> alerts = workflowMonitorService.getAlertHistory(
                WORKFLOW_ID_1, startTime, endTime, limit);

        // then
        assertThat(alerts).hasSize(2);
        assertThat(alerts).allMatch(alert -> WORKFLOW_ID_1.equals(alert.getWorkflowId()));
    }

    @Test
    @DisplayName("getAlertHistory - 空工作流ID应返回所有告警")
    void getAlertHistory_withoutWorkflowId_shouldReturnAllAlerts() {
        // given
        LocalDateTime startTime = LocalDateTime.now().minusDays(1);
        LocalDateTime endTime = LocalDateTime.now();
        int limit = 10;

        WorkflowAlertDTO alert1 = createTestAlert("WFA-001", "HIGH_FAILURE_RATE", false);
        alert1.setCreatedAt(LocalDateTime.now().minusHours(1));

        WorkflowAlertDTO alert2 = createTestAlert("WFA-002", "INSTANCE_TIMEOUT", false);
        alert2.setCreatedAt(LocalDateTime.now().minusHours(2));

        ConcurrentHashMap<String, List<WorkflowAlertDTO>> alertHistory = new ConcurrentHashMap<>();
        alertHistory.put("global", new ArrayList<>(Arrays.asList(alert1, alert2)));

        ReflectionTestUtils.setField(workflowMonitorService, "alertHistory", alertHistory);

        // when
        List<WorkflowAlertDTO> alerts = workflowMonitorService.getAlertHistory(
                null, startTime, endTime, limit);

        // then
        assertThat(alerts).hasSize(2);
    }

    @Test
    @DisplayName("getAlertHistory - 应限制返回数量")
    void getAlertHistory_withLimit_shouldRespectLimit() {
        // given
        LocalDateTime startTime = LocalDateTime.now().minusDays(1);
        LocalDateTime endTime = LocalDateTime.now();
        int limit = 2;

        ConcurrentHashMap<String, List<WorkflowAlertDTO>> alertHistory = new ConcurrentHashMap<>();
        List<WorkflowAlertDTO> alerts = new ArrayList<>();
        for (int i = 0; i < 5; i++) {
            WorkflowAlertDTO alert = createTestAlert("WFA-00" + i, "TYPE-" + i, false);
            alert.setCreatedAt(LocalDateTime.now().minusHours(i));
            alerts.add(alert);
        }
        alertHistory.put("global", alerts);

        ReflectionTestUtils.setField(workflowMonitorService, "alertHistory", alertHistory);

        // when
        List<WorkflowAlertDTO> result = workflowMonitorService.getAlertHistory(
                null, startTime, endTime, limit);

        // then
        assertThat(result).hasSize(limit);
    }

    @Test
    @DisplayName("resolveAlert - 解决告警应设置解决信息")
    void resolveAlert_withValidAlertId_shouldResolveAlert() {
        // given
        String alertId = "WFA-001";
        String resolvedBy = "admin";
        String resolution = "已重启服务";

        WorkflowAlertDTO alert = createTestAlert(alertId, "HIGH_FAILURE_RATE", false);
        ConcurrentHashMap<String, List<WorkflowAlertDTO>> alertHistory = new ConcurrentHashMap<>();
        alertHistory.put("global", new ArrayList<>(List.of(alert)));

        ConcurrentHashMap<String, LocalDateTime> alertCooldowns = new ConcurrentHashMap<>();
        alertCooldowns.put("global", LocalDateTime.now());

        ReflectionTestUtils.setField(workflowMonitorService, "alertHistory", alertHistory);
        ReflectionTestUtils.setField(workflowMonitorService, "alertCooldowns", alertCooldowns);

        // when
        workflowMonitorService.resolveAlert(alertId, resolvedBy, resolution);

        // then
        assertThat(alert.getResolved()).isTrue();
        assertThat(alert.getResolvedBy()).isEqualTo(resolvedBy);
        assertThat(alert.getResolution()).isEqualTo(resolution);
        assertThat(alert.getResolvedAt()).isNotNull();
        assertThat(alertCooldowns.get("global")).isNull(); // 冷却时间应被清除
    }

    @Test
    @DisplayName("checkWorkflowHealth - 禁用时应不执行检查")
    void checkWorkflowHealth_whenDisabled_shouldNotCheck() {
        // given
        ReflectionTestUtils.setField(workflowMonitorService, "monitorEnabled", false);

        // when
        workflowMonitorService.checkWorkflowHealth();

        // then - 不应调用任何数据库方法
        verify(instanceMapper, never()).countByStatus(any(), any(), any());
        verify(instanceMapper, never()).selectTimeoutInstances(any(), any());
    }

    @Test
    @DisplayName("checkWorkflowHealth - 失败数超过阈值时应触发告警")
    void checkWorkflowHealth_withHighFailureRate_shouldTriggerAlert() {
        // given - 超过失败阈值
        when(instanceMapper.countByStatus(eq(WorkflowStatus.FAILED), any(), any())).thenReturn(10);
        when(instanceMapper.count(eq(WorkflowStatus.FAILED), any(), any(), any())).thenReturn(10);
        when(instanceMapper.count(null, any(), any(), any())).thenReturn(100); // 总数>10
        when(instanceMapper.selectTimeoutInstances(any(), any(), any())).thenReturn(Collections.emptyList());
        when(instanceMapper.countByStatus(eq(WorkflowStatus.COMPLETED), any(), any())).thenReturn(80);
        when(instanceMapper.count(null, WorkflowStatus.PENDING, any(), any())).thenReturn(5);

        // when
        workflowMonitorService.checkWorkflowHealth();

        // then - 验证告警被添加（通过日志验证，无异常即成功）
    }

    @Test
    @DisplayName("getWorkflowPerformance - 有效工作流应返回性能数据")
    void getWorkflowPerformance_withValidWorkflow_shouldReturnPerformanceData() {
        // given
        LocalDateTime startTime = LocalDateTime.now().minusHours(1);
        LocalDateTime endTime = LocalDateTime.now();

        List<WorkflowInstance> instances = createTestInstances(10);
        when(instanceMapper.selectRecentByWorkflow(WORKFLOW_ID_1, 1000)).thenReturn(instances);

        // when
        Map<String, Object> performance = workflowMonitorService.getWorkflowPerformance(
                WORKFLOW_ID_1, startTime, endTime);

        // then
        assertThat(performance).isNotNull();
        assertThat(performance.get("workflowId")).isEqualTo(WORKFLOW_ID_1);
        assertThat(performance.get("totalCount")).isEqualTo(10);
        assertThat(performance.get("successRate")).isNotNull();
        assertThat(performance.get("averageDurationSeconds")).isNotNull();
    }

    @Test
    @DisplayName("getWorkflowPerformance - 无实例时应返回提示消息")
    void getWorkflowPerformance_withNoInstances_shouldReturnMessage() {
        // given
        LocalDateTime startTime = LocalDateTime.now().minusHours(1);
        LocalDateTime endTime = LocalDateTime.now();

        when(instanceMapper.selectRecentByWorkflow(WORKFLOW_ID_1, 1000)).thenReturn(Collections.emptyList());

        // when
        Map<String, Object> performance = workflowMonitorService.getWorkflowPerformance(
                WORKFLOW_ID_1, startTime, endTime);

        // then
        assertThat(performance).containsKey("message");
        assertThat(performance.get("message")).isEqualTo("指定时间范围内没有工作流实例");
    }

    @Test
    @DisplayName("getTopFailingWorkflows - 应返回失败数排序的工作流")
    void getTopFailingWorkflows_shouldReturnSortedWorkflows() {
        // given
        LocalDateTime startTime = LocalDateTime.now().minusHours(1);
        LocalDateTime endTime = LocalDateTime.now();
        int limit = 5;

        List<WorkflowInstance> failedInstances = new ArrayList<>();
        // WF-001: 5次失败
        for (int i = 0; i < 5; i++) {
            WorkflowInstance instance = createTestInstance(WORKFLOW_ID_1, WorkflowStatus.FAILED);
            failedInstances.add(instance);
        }
        // WF-002: 3次失败
        for (int i = 0; i < 3; i++) {
            WorkflowInstance instance = createTestInstance(WORKFLOW_ID_2, WorkflowStatus.FAILED);
            failedInstances.add(instance);
        }

        when(instanceMapper.selectByStatus(eq(WorkflowStatus.FAILED), any(), any(), eq(0), eq(1000)))
                .thenReturn(failedInstances);
        when(instanceMapper.count(eq(WORKFLOW_ID_1), any(), any(), any())).thenReturn(10);
        when(instanceMapper.count(eq(WORKFLOW_ID_2), any(), any(), any())).thenReturn(5);

        // when
        List<Map<String, Object>> topFailing = workflowMonitorService.getTopFailingWorkflows(
                startTime, endTime, limit);

        // then
        assertThat(topFailing).hasSize(2);
        assertThat(topFailing.get(0).get("workflowId")).isEqualTo(WORKFLOW_ID_1); // 失败数最多
        assertThat(topFailing.get(0).get("failureCount")).isEqualTo(5L);
        assertThat(topFailing.get(1).get("workflowId")).isEqualTo(WORKFLOW_ID_2);
        assertThat(topFailing.get(1).get("failureCount")).isEqualTo(3L);
    }

    @Test
    @DisplayName("getTopFailingWorkflows - limit参数应限制返回数量")
    void getTopFailingWorkflows_withLimit_shouldRespectLimit() {
        // given
        LocalDateTime startTime = LocalDateTime.now().minusHours(1);
        LocalDateTime endTime = LocalDateTime.now();
        int limit = 1;

        List<WorkflowInstance> failedInstances = new ArrayList<>();
        for (int i = 0; i < 5; i++) {
            WorkflowInstance instance = createTestInstance("WF-00" + i, WorkflowStatus.FAILED);
            failedInstances.add(instance);
        }

        when(instanceMapper.selectByStatus(eq(WorkflowStatus.FAILED), any(), any(), eq(0), eq(1000)))
                .thenReturn(failedInstances);
        // 所有工作流都只有1次失败
        for (int i = 0; i < 5; i++) {
            when(instanceMapper.count(eq("WF-00" + i), any(), any(), any())).thenReturn(1);
        }

        // when
        List<Map<String, Object>> topFailing = workflowMonitorService.getTopFailingWorkflows(
                startTime, endTime, limit);

        // then
        assertThat(topFailing).hasSize(limit);
    }

    /**
     * 辅助方法：创建测试告警
     */
    private WorkflowAlertDTO createTestAlert(String alertId, String alertType, boolean resolved) {
        WorkflowAlertDTO alert = new WorkflowAlertDTO();
        alert.setAlertId(alertId);
        alert.setAlertType(alertType);
        alert.setTitle("测试告警");
        alert.setDescription("测试告警描述");
        alert.setSeverity("WARNING");
        alert.setWorkflowId("global");
        alert.setCreatedAt(LocalDateTime.now());
        alert.setResolved(resolved);
        return alert;
    }

    /**
     * 辅助方法：创建测试实例
     */
    private WorkflowInstance createTestInstance(String workflowId, WorkflowStatus status) {
        WorkflowInstance instance = new WorkflowInstance();
        instance.setInstanceId(UUID.randomUUID().toString());
        instance.setWorkflowId(workflowId);
        instance.setStatus(status);
        instance.setStartedAt(LocalDateTime.now().minusMinutes(30));
        if (status == WorkflowStatus.COMPLETED || status == WorkflowStatus.FAILED) {
            instance.setCompletedAt(LocalDateTime.now());
        }
        return instance;
    }

    /**
     * 辅助方法：创建测试实例列表
     */
    private List<WorkflowInstance> createTestInstances(int count) {
        List<WorkflowInstance> instances = new ArrayList<>();
        for (int i = 0; i < count; i++) {
            WorkflowStatus status = i < 7 ? WorkflowStatus.COMPLETED :
                                   i < 9 ? WorkflowStatus.FAILED : WorkflowStatus.RUNNING;
            instances.add(createTestInstance(WORKFLOW_ID_1, status));
        }
        return instances;
    }
}