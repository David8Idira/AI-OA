package com.aioa.workflow.service.impl;

import com.aioa.workflow.entity.Approval;
import com.aioa.workflow.enums.ApprovalStatusEnum;
import com.aioa.workflow.mapper.ApprovalMapper;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Workflow Monitor Service Implementation
 * 流程监控服务 - 监控审批流程的运行状态
 * 
 * 毛泽东思想指导：实事求是，务实简单
 */
@Slf4j
@Service
public class WorkflowMonitorServiceImpl {

    @Autowired
    private ApprovalMapper approvalMapper;

    /**
     * 获取流程统计数据
     * 返回各状态的审批单数量
     */
    public Map<String, Object> getWorkflowStatistics(LocalDateTime startTime, LocalDateTime endTime) {
        Map<String, Object> statistics = new HashMap<>();
        
        try {
            // 统计各状态的审批数量
            long pendingCount = approvalMapper.selectCount(new LambdaQueryWrapper<Approval>()
                    .eq(Approval::getStatus, ApprovalStatusEnum.PENDING.getCode())
                    .between(startTime != null, Approval::getCreateTime, startTime, endTime));
            
            long approvedCount = approvalMapper.selectCount(new LambdaQueryWrapper<Approval>()
                    .eq(Approval::getStatus, ApprovalStatusEnum.APPROVED.getCode())
                    .between(startTime != null, Approval::getCreateTime, startTime, endTime));
            
            long rejectedCount = approvalMapper.selectCount(new LambdaQueryWrapper<Approval>()
                    .eq(Approval::getStatus, ApprovalStatusEnum.REJECTED.getCode())
                    .between(startTime != null, Approval::getCreateTime, startTime, endTime));
            
            long totalCount = pendingCount + approvedCount + rejectedCount;
            
            statistics.put("total", totalCount);
            statistics.put("pending", pendingCount);
            statistics.put("approved", approvedCount);
            statistics.put("rejected", rejectedCount);
            statistics.put("successRate", totalCount > 0 ? (double) approvedCount / totalCount * 100 : 0);
            statistics.put("calculatedAt", LocalDateTime.now());
            
            log.info("流程统计: 总数={}, 待审批={}, 已同意={}, 已驳回={}", 
                    totalCount, pendingCount, approvedCount, rejectedCount);
            
        } catch (Exception e) {
            log.error("获取流程统计失败", e);
            statistics.put("error", e.getMessage());
        }
        
        return statistics;
    }

    /**
     * 获取超时审批单列表
     * 超时定义：状态为待审批且超过期望完成时间
     */
    public List<Approval> getTimeoutApprovals() {
        try {
            List<Approval> overdueList = approvalMapper.selectOverdue();
            log.info("发现{}个超时审批单", overdueList.size());
            return overdueList;
        } catch (Exception e) {
            log.error("获取超时审批单失败", e);
            return List.of();
        }
    }

    /**
     * 获取待审批数量
     */
    public long getPendingCount() {
        try {
            return approvalMapper.selectCount(new LambdaQueryWrapper<Approval>()
                    .eq(Approval::getStatus, ApprovalStatusEnum.PENDING.getCode()));
        } catch (Exception e) {
            log.error("获取待审批数量失败", e);
            return 0;
        }
    }

    /**
     * 健康检查定时任务
     * 每5分钟执行一次
     */
    @Scheduled(fixedRate = 300000)
    public void healthCheck() {
        try {
            long pendingCount = getPendingCount();
            long overdueCount = getTimeoutApprovals().size();
            
            log.info("流程健康检查: 待审批={}, 超时={}", pendingCount, overdueCount);
            
            if (overdueCount > 10) {
                log.warn("警告: 超时审批单数量过多({}), 请关注", overdueCount);
            }
        } catch (Exception e) {
            log.error("健康检查失败", e);
        }
    }

    /**
     * 获取流程健康状态
     * 返回健康分数(0-100)
     */
    public Map<String, Object> getHealthStatus() {
        Map<String, Object> health = new HashMap<>();
        
        try {
            long pendingCount = getPendingCount();
            long overdueCount = getTimeoutApprovals().size();
            long totalCount = approvalMapper.selectCount(null);
            
            // 计算健康分数
            // 基础分100分，每超时一个扣5分，最低0分
            int score = 100 - (int) (overdueCount * 5);
            score = Math.max(0, Math.min(100, score));
            
            health.put("score", score);
            health.put("pending", pendingCount);
            health.put("overdue", overdueCount);
            health.put("total", totalCount);
            health.put("status", score >= 80 ? "HEALTHY" : score >= 60 ? "WARNING" : "CRITICAL");
            health.put("checkedAt", LocalDateTime.now());
            
        } catch (Exception e) {
            log.error("获取健康状态失败", e);
            health.put("error", e.getMessage());
        }
        
        return health;
    }
}