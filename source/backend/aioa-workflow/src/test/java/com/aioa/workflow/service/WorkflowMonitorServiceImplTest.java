package com.aioa.workflow.service;

import com.aioa.workflow.entity.Approval;
import com.aioa.workflow.enums.ApprovalStatusEnum;
import com.aioa.workflow.mapper.ApprovalMapper;
import com.aioa.workflow.service.impl.WorkflowMonitorServiceImpl;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

/**
 * WorkflowMonitorServiceImpl 单元测试
 * 毛泽东思想指导：实事求是，测试流程监控功能
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("WorkflowMonitorServiceImpl 单元测试")
class WorkflowMonitorServiceImplTest {

    @Mock
    private ApprovalMapper approvalMapper;

    @InjectMocks
    private WorkflowMonitorServiceImpl workflowMonitorService;

    @Test
    @DisplayName("获取待审批数量 - 正常场景")
    void getPendingCount_shouldReturnCorrectCount() {
        // given
        when(approvalMapper.selectCount(any(LambdaQueryWrapper.class))).thenReturn(5L);

        // when
        long count = workflowMonitorService.getPendingCount();

        // then
        assertThat(count).isEqualTo(5L);
    }

    @Test
    @DisplayName("获取待审批数量 - 数据库异常")
    void getPendingCount_withException_shouldReturnZero() {
        // given
        when(approvalMapper.selectCount(any(LambdaQueryWrapper.class)))
                .thenThrow(new RuntimeException("数据库错误"));

        // when
        long count = workflowMonitorService.getPendingCount();

        // then
        assertThat(count).isEqualTo(0L);
    }

    @Test
    @DisplayName("获取超时审批单 - 正常场景")
    void getTimeoutApprovals_shouldReturnList() {
        // given
        Approval approval = new Approval();
        approval.setId("test-id");
        approval.setTitle("测试审批");
        approval.setStatus(ApprovalStatusEnum.PENDING.getCode());
        
        when(approvalMapper.selectOverdue()).thenReturn(List.of(approval));

        // when
        List<Approval> result = workflowMonitorService.getTimeoutApprovals();

        // then
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getTitle()).isEqualTo("测试审批");
    }

    @Test
    @DisplayName("获取超时审批单 - 数据库异常")
    void getTimeoutApprovals_withException_shouldReturnEmptyList() {
        // given
        when(approvalMapper.selectOverdue()).thenThrow(new RuntimeException("数据库错误"));

        // when
        List<Approval> result = workflowMonitorService.getTimeoutApprovals();

        // then
        assertThat(result).isEmpty();
    }

    @Test
    @DisplayName("获取流程健康状态 - 健康")
    void getHealthStatus_whenHealthy_shouldReturnGoodScore() {
        // given
        when(approvalMapper.selectCount(any(LambdaQueryWrapper.class))).thenReturn(100L);
        when(approvalMapper.selectOverdue()).thenReturn(List.of());

        // when
        Map<String, Object> health = workflowMonitorService.getHealthStatus();

        // then
        assertThat(health.get("score")).isEqualTo(100);
        assertThat(health.get("status")).isEqualTo("HEALTHY");
    }

    @Test
    @DisplayName("获取流程健康状态 - 警告状态")
    void getHealthStatus_whenWarning_shouldReturnLowerScore() {
        // given
        when(approvalMapper.selectCount(any(LambdaQueryWrapper.class))).thenReturn(100L);
        
        Approval overdue = new Approval();
        when(approvalMapper.selectOverdue()).thenReturn(List.of(overdue));

        // when
        Map<String, Object> health = workflowMonitorService.getHealthStatus();

        // then
        assertThat(health.get("score")).isEqualTo(95);  // 100 - 5*1 = 95
        assertThat(health.get("status")).isEqualTo("HEALTHY");
    }

    @Test
    @DisplayName("获取流程健康状态 - 严重状态")
    void getHealthStatus_whenCritical_shouldReturnLowScore() {
        // given
        when(approvalMapper.selectCount(any(LambdaQueryWrapper.class))).thenReturn(50L);
        
        // 5个超时审批
        when(approvalMapper.selectOverdue()).thenReturn(List.of(
            new Approval(), new Approval(), new Approval(), new Approval(), new Approval()
        ));

        // when
        Map<String, Object> health = workflowMonitorService.getHealthStatus();

        // then
        assertThat(health.get("score")).isEqualTo(75);  // 100 - 5*5 = 75
        assertThat(health.get("status")).isEqualTo("WARNING");
    }

    @Test
    @DisplayName("获取流程统计数据 - 正常场景")
    void getWorkflowStatistics_shouldReturnCorrectData() {
        // given
        when(approvalMapper.selectCount(any(LambdaQueryWrapper.class))).thenReturn(10L);

        // when
        Map<String, Object> stats = workflowMonitorService.getWorkflowStatistics(null, null);

        // then
        assertThat(stats).containsKeys("total", "pending", "approved", "rejected", "successRate");
    }

    @Test
    @DisplayName("获取流程健康状态 - 无超时审批")
    void getHealthStatus_withNoOverdue_shouldReturnPerfectScore() {
        // given
        when(approvalMapper.selectCount(any(LambdaQueryWrapper.class))).thenReturn(50L);
        when(approvalMapper.selectOverdue()).thenReturn(List.of());

        // when
        Map<String, Object> health = workflowMonitorService.getHealthStatus();

        // then
        assertThat(health.get("score")).isEqualTo(100);
        assertThat(health.get("status")).isEqualTo("HEALTHY");
    }

    @Test
    @DisplayName("获取流程统计数据 - 带时间范围")
    void getWorkflowStatistics_withTimeRange_shouldFilterByTime() {
        // given
        LocalDateTime start = LocalDateTime.now().minusDays(7);
        LocalDateTime end = LocalDateTime.now();
        when(approvalMapper.selectCount(any(LambdaQueryWrapper.class))).thenReturn(5L);

        // when
        Map<String, Object> stats = workflowMonitorService.getWorkflowStatistics(start, end);

        // then
        assertThat(stats).containsKeys("total", "pending", "approved", "rejected");
    }
}
