package com.aioa.workflow.service.impl;

import com.aioa.workflow.dto.WorkflowMetricsDTO;
import com.aioa.workflow.dto.WorkflowAlertDTO;
import com.aioa.workflow.entity.WorkflowInstance;
import com.aioa.workflow.enums.WorkflowStatus;
import com.aioa.workflow.mapper.WorkflowInstanceMapper;
import com.aioa.workflow.service.WorkflowMonitorService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * 工作流监控服务实现
 * 
 * F5模块增强：工作流监控与告警
 */
@Service
@Slf4j
public class WorkflowMonitorServiceImpl implements WorkflowMonitorService {

    @Autowired
    private WorkflowInstanceMapper instanceMapper;
    
    @Autowired
    private RedisTemplate<String, Object> redisTemplate;
    
    @Value("${workflow.monitor.enabled:true}")
    private boolean monitorEnabled;
    
    @Value("${workflow.alert.failure-threshold:5}")
    private int failureThreshold;
    
    @Value("${workflow.alert.timeout-threshold-minutes:30}")
    private int timeoutThresholdMinutes;
    
    @Value("${workflow.alert.success-rate-threshold:95}")
    private int successRateThreshold;
    
    // 告警历史记录
    private final ConcurrentHashMap<String, List<WorkflowAlertDTO>> alertHistory = new ConcurrentHashMap<>();
    
    // 告警冷却时间（避免重复告警）
    private final ConcurrentHashMap<String, LocalDateTime> alertCooldowns = new ConcurrentHashMap<>();
    
    @Value("${workflow.alert.cooldown-minutes:15}")
    private int alertCooldownMinutes;
    
    @Override
    public WorkflowMetricsDTO getMetrics(LocalDateTime startTime, LocalDateTime endTime) {
        try {
            WorkflowMetricsDTO metrics = new WorkflowMetricsDTO();
            
            // 获取各状态实例数量
            Map<WorkflowStatus, Long> statusCounts = getStatusCounts(startTime, endTime);
            
            // 计算关键指标
            long totalCount = statusCounts.values().stream().mapToLong(Long::longValue).sum();
            long completedCount = statusCounts.getOrDefault(WorkflowStatus.COMPLETED, 0L);
            long failedCount = statusCounts.getOrDefault(WorkflowStatus.FAILED, 0L);
            long runningCount = statusCounts.getOrDefault(WorkflowStatus.RUNNING, 0L);
            long pendingCount = statusCounts.getOrDefault(WorkflowStatus.PENDING, 0L);
            
            // 计算成功率
            double successRate = totalCount > 0 ? (double) completedCount / totalCount * 100 : 0;
            
            // 计算平均执行时间
            Double avgDuration = instanceMapper.getAverageDuration(startTime, endTime);
            
            // 计算P95执行时间
            Double p95Duration = instanceMapper.getP95Duration(startTime, endTime);
            
            // 获取超时数量
            int timeoutCount = instanceMapper.countByStatus(WorkflowStatus.TIMEOUT, startTime, endTime);
            
            // 计算QPS
            double qps = calculateQPS(totalCount, startTime, endTime);
            
            // 设置指标值
            metrics.setTotalCount(totalCount);
            metrics.setCompletedCount(completedCount);
            metrics.setFailedCount(failedCount);
            metrics.setRunningCount(runningCount);
            metrics.setPendingCount(pendingCount);
            metrics.setTimeoutCount(timeoutCount);
            metrics.setSuccessRate(Math.round(successRate * 100.0) / 100.0);
            metrics.setAverageDuration(avgDuration != null ? Math.round(avgDuration * 100.0) / 100.0 : 0);
            metrics.setP95Duration(p95Duration != null ? Math.round(p95Duration * 100.0) / 100.0 : 0);
            metrics.setQps(Math.round(qps * 100.0) / 100.0);
            metrics.setStartTime(startTime);
            metrics.setEndTime(endTime);
            metrics.setCalculatedAt(LocalDateTime.now());
            
            // 设置状态分布
            Map<String, Long> statusDistribution = new HashMap<>();
            statusCounts.forEach((status, count) -> 
                    statusDistribution.put(status.name(), count));
            metrics.setStatusDistribution(statusDistribution);
            
            // 计算健康度
            metrics.setHealthScore(calculateHealthScore(successRate, timeoutCount, totalCount));
            
            log.info("工作流指标计算完成，总数={}，成功率={}%，健康度={}", 
                    totalCount, successRate, metrics.getHealthScore());
            
            return metrics;
            
        } catch (Exception e) {
            log.error("获取工作流指标失败", e);
            throw new RuntimeException("获取工作流指标失败: " + e.getMessage(), e);
        }
    }

    @Override
    public List<WorkflowAlertDTO> getActiveAlerts() {
        List<WorkflowAlertDTO> activeAlerts = new ArrayList<>();
        
        LocalDateTime now = LocalDateTime.now();
        
        // 遍历所有告警历史
        alertHistory.forEach((workflowId, alerts) -> {
            alerts.stream()
                    .filter(alert -> !alert.getResolved())
                    .filter(alert -> {
                        // 检查是否在冷却时间内
                        LocalDateTime lastAlert = alertCooldowns.get(workflowId);
                        if (lastAlert != null) {
                            long minutesSinceLastAlert = ChronoUnit.MINUTES.between(lastAlert, now);
                            return minutesSinceLastAlert >= alertCooldownMinutes;
                        }
                        return true;
                    })
                    .forEach(activeAlerts::add);
        });
        
        return activeAlerts;
    }

    @Override
    public List<WorkflowAlertDTO> getAlertHistory(String workflowId, LocalDateTime startTime, 
                                                  LocalDateTime endTime, int limit) {
        if (workflowId != null && alertHistory.containsKey(workflowId)) {
            return alertHistory.get(workflowId).stream()
                    .filter(alert -> alert.getCreatedAt().isAfter(startTime))
                    .filter(alert -> alert.getCreatedAt().isBefore(endTime))
                    .limit(limit)
                    .collect(Collectors.toList());
        }
        
        return alertHistory.values().stream()
                .flatMap(List::stream)
                .filter(alert -> alert.getCreatedAt().isAfter(startTime))
                .filter(alert -> alert.getCreatedAt().isBefore(endTime))
                .sorted(Comparator.comparing(WorkflowAlertDTO::getCreatedAt).reversed())
                .limit(limit)
                .collect(Collectors.toList());
    }

    @Override
    public void resolveAlert(String alertId, String resolvedBy, String resolution) {
        alertHistory.forEach((workflowId, alerts) -> {
            alerts.stream()
                    .filter(alert -> alert.getAlertId().equals(alertId))
                    .findFirst()
                    .ifPresent(alert -> {
                        alert.setResolved(true);
                        alert.setResolvedAt(LocalDateTime.now());
                        alert.setResolvedBy(resolvedBy);
                        alert.setResolution(resolution);
                        
                        // 清除冷却时间
                        alertCooldowns.remove(workflowId);
                        
                        log.info("告警已解决，ID：{}，解决者：{}，说明：{}", 
                                alertId, resolvedBy, resolution);
                    });
        });
    }

    @Override
    @Scheduled(fixedDelay = 60000) // 每分钟执行一次
    public void checkWorkflowHealth() {
        if (!monitorEnabled) {
            return;
        }
        
        try {
            LocalDateTime now = LocalDateTime.now();
            LocalDateTime recentStart = now.minusMinutes(5);
            LocalDateTime hourStart = now.minusHours(1);
            
            // 检查失败率
            checkFailureRate(hourStart, now);
            
            // 检查超时实例
            checkTimeoutInstances(now);
            
            // 检查成功率
            checkSuccessRate(hourStart, now);
            
            // 检查待处理积压
            checkPendingBacklog(recentStart, now);
            
        } catch (Exception e) {
            log.error("工作流健康检查失败", e);
        }
    }

    @Override
    public Map<String, Object> getWorkflowPerformance(String workflowId, LocalDateTime startTime, 
                                                       LocalDateTime endTime) {
        Map<String, Object> performance = new HashMap<>();
        
        // 获取该工作流的实例统计
        List<WorkflowInstance> instances = instanceMapper.selectRecentByWorkflow(workflowId, 1000)
                .stream()
                .filter(inst -> inst.getCreatedAt().isAfter(startTime))
                .filter(inst -> inst.getCreatedAt().isBefore(endTime))
                .collect(Collectors.toList());
        
        if (instances.isEmpty()) {
            performance.put("message", "指定时间范围内没有工作流实例");
            return performance;
        }
        
        // 计算各项指标
        int totalCount = instances.size();
        long completedCount = instances.stream()
                .filter(inst -> inst.getStatus() == WorkflowStatus.COMPLETED)
                .count();
        long failedCount = instances.stream()
                .filter(inst -> inst.getStatus() == WorkflowStatus.FAILED)
                .count();
        
        // 计算平均执行时间
        double avgDuration = instances.stream()
                .filter(inst -> inst.getCompletedAt() != null && inst.getStartedAt() != null)
                .mapToLong(inst -> ChronoUnit.SECONDS.between(inst.getStartedAt(), inst.getCompletedAt()))
                .average()
                .orElse(0);
        
        // 计算成功率
        double successRate = totalCount > 0 ? (double) completedCount / totalCount * 100 : 0;
        
        // 计算峰值时间
        Map<Integer, Long> hourlyDistribution = instances.stream()
                .collect(Collectors.groupingBy(
                        inst -> inst.getCreatedAt().getHour(),
                        Collectors.counting()
                ));
        
        int peakHour = hourlyDistribution.entrySet().stream()
                .max(Map.Entry.comparingByValue())
                .map(Map.Entry::getKey)
                .orElse(0);
        
        // 设置结果
        performance.put("workflowId", workflowId);
        performance.put("totalCount", totalCount);
        performance.put("completedCount", completedCount);
        performance.put("failedCount", failedCount);
        performance.put("successRate", Math.round(successRate * 100.0) / 100.0);
        performance.put("averageDurationSeconds", Math.round(avgDuration * 100.0) / 100.0);
        performance.put("peakHour", peakHour + ":00");
        performance.put("hourlyDistribution", hourlyDistribution);
        performance.put("startTime", startTime);
        performance.put("endTime", endTime);
        
        return performance;
    }

    @Override
    public List<Map<String, Object>> getTopFailingWorkflows(LocalDateTime startTime, 
                                                             LocalDateTime endTime, int limit) {
        List<WorkflowInstance> failedInstances = instanceMapper.selectByStatus(
                WorkflowStatus.FAILED, startTime, endTime, 0, 1000);
        
        // 按工作流分组统计失败数
        Map<String, Long> failureCounts = failedInstances.stream()
                .collect(Collectors.groupingBy(
                        WorkflowInstance::getWorkflowId,
                        Collectors.counting()
                ));
        
        // 排序并取Top N
        return failureCounts.entrySet().stream()
                .sorted(Map.Entry.<String, Long>comparingByValue().reversed())
                .limit(limit)
                .map(entry -> {
                    Map<String, Object> item = new HashMap<>();
                    item.put("workflowId", entry.getKey());
                    item.put("failureCount", entry.getValue());
                    item.put("failureRate", calculateFailureRate(entry.getKey(), startTime, endTime));
                    return item;
                })
                .collect(Collectors.toList());
    }

    /**
     * 检查失败率
     */
    private void checkFailureRate(LocalDateTime startTime, LocalDateTime endTime) {
        int recentFailures = instanceMapper.countByStatus(WorkflowStatus.FAILED, startTime, endTime);
        int recentTotal = instanceMapper.count(null, null, startTime, endTime);
        
        if (recentTotal > 10 && recentFailures > failureThreshold) {
            String alertId = generateAlertId();
            WorkflowAlertDTO alert = createAlert(
                    alertId,
                    "HIGH_FAILURE_RATE",
                    "工作流失败率过高",
                    String.format("最近1小时内失败数=%d（阈值=%d），失败率=%.2f%%", 
                            recentFailures, failureThreshold, 
                            recentTotal > 0 ? (double) recentFailures / recentTotal * 100 : 0),
                    "WARNING",
                    null
            );
            
            addAlert(null, alert);
            log.warn("触发失败率告警：失败数={}，阈值={}", recentFailures, failureThreshold);
        }
    }

    /**
     * 检查超时实例
     */
    private void checkTimeoutInstances(LocalDateTime now) {
        LocalDateTime timeoutThreshold = now.minusMinutes(timeoutThresholdMinutes);
        
        List<WorkflowInstance> timeoutInstances = instanceMapper.selectTimeoutInstances(
                timeoutThreshold, WorkflowStatus.RUNNING, WorkflowStatus.PENDING);
        
        if (!timeoutInstances.isEmpty()) {
            for (WorkflowInstance instance : timeoutInstances) {
                String alertId = generateAlertId();
                WorkflowAlertDTO alert = createAlert(
                        alertId,
                        "INSTANCE_TIMEOUT",
                        "工作流实例执行超时",
                        String.format("实例ID=%s，执行时间超过%d分钟", 
                                instance.getInstanceId(), timeoutThresholdMinutes),
                        "WARNING",
                        instance.getWorkflowId()
                );
                
                addAlert(instance.getWorkflowId(), alert);
            }
            
            log.warn("触发超时告警：超时实例数={}", timeoutInstances.size());
        }
    }

    /**
     * 检查成功率
     */
    private void checkSuccessRate(LocalDateTime startTime, LocalDateTime endTime) {
        int totalCount = instanceMapper.count(null, null, startTime, endTime);
        
        if (totalCount < 10) {
            return; // 样本太少不告警
        }
        
        int completedCount = instanceMapper.countByStatus(WorkflowStatus.COMPLETED, startTime, endTime);
        double successRate = (double) completedCount / totalCount * 100;
        
        if (successRate < successRateThreshold) {
            String alertId = generateAlertId();
            WorkflowAlertDTO alert = createAlert(
                    alertId,
                    "LOW_SUCCESS_RATE",
                    "工作流成功率低于阈值",
                    String.format("当前成功率=%.2f%%，阈值=%d%%", successRate, successRateThreshold),
                    "WARNING",
                    null
            );
            
            addAlert(null, alert);
            log.warn("触发成功率告警：成功率={}%，阈值={}%", successRate, successRateThreshold);
        }
    }

    /**
     * 检查待处理积压
     */
    private void checkPendingBacklog(LocalDateTime startTime, LocalDateTime endTime) {
        int pendingCount = instanceMapper.count(null, WorkflowStatus.PENDING, startTime, endTime);
        int runningCount = instanceMapper.count(null, WorkflowStatus.RUNNING, startTime, endTime);
        
        if (pendingCount > 100) {
            String alertId = generateAlertId();
            WorkflowAlertDTO alert = createAlert(
                    alertId,
                    "PENDING_BACKLOG",
                    "待处理工作流积压严重",
                    String.format("待处理数=%d，可能存在性能问题或外部服务故障", pendingCount),
                    "INFO",
                    null
            );
            
            addAlert(null, alert);
            log.warn("触发积压告警：待处理数={}", pendingCount);
        }
    }

    /**
     * 计算QPS
     */
    private double calculateQPS(long totalCount, LocalDateTime startTime, LocalDateTime endTime) {
        long seconds = ChronoUnit.SECONDS.between(startTime, endTime);
        return seconds > 0 ? (double) totalCount / seconds : 0;
    }

    /**
     * 计算健康度
     */
    private int calculateHealthScore(double successRate, int timeoutCount, long totalCount) {
        int score = 100;
        
        // 成功率扣分
        if (successRate < 99) score -= 10;
        if (successRate < 95) score -= 20;
        if (successRate < 90) score -= 30;
        
        // 超时扣分
        if (totalCount > 0) {
            double timeoutRate = (double) timeoutCount / totalCount;
            if (timeoutRate > 0.05) score -= 15;
            if (timeoutRate > 0.10) score -= 25;
        }
        
        return Math.max(0, score);
    }

    /**
     * 获取各状态数量
     */
    private Map<WorkflowStatus, Long> getStatusCounts(LocalDateTime startTime, LocalDateTime endTime) {
        Map<WorkflowStatus, Long> counts = new HashMap<>();
        
        for (WorkflowStatus status : WorkflowStatus.values()) {
            counts.put(status, (long) instanceMapper.count(null, status, startTime, endTime));
        }
        
        return counts;
    }

    /**
     * 计算失败率
     */
    private double calculateFailureRate(String workflowId, LocalDateTime startTime, LocalDateTime endTime) {
        int totalCount = instanceMapper.count(workflowId, null, startTime, endTime);
        int failedCount = instanceMapper.count(workflowId, WorkflowStatus.FAILED, startTime, endTime);
        
        return totalCount > 0 ? (double) failedCount / totalCount * 100 : 0;
    }

    private String generateAlertId() {
        return "WFA-" + System.currentTimeMillis();
    }

    private WorkflowAlertDTO createAlert(String alertId, String type, String title, 
                                         String description, String severity, String workflowId) {
        WorkflowAlertDTO alert = new WorkflowAlertDTO();
        alert.setAlertId(alertId);
        alert.setAlertType(type);
        alert.setTitle(title);
        alert.setDescription(description);
        alert.setSeverity(severity);
        alert.setWorkflowId(workflowId);
        alert.setCreatedAt(LocalDateTime.now());
        alert.setResolved(false);
        return alert;
    }

    private void addAlert(String workflowId, WorkflowAlertDTO alert) {
        String key = workflowId != null ? workflowId : "global";
        
        alertHistory.computeIfAbsent(key, k -> new ArrayList<>()).add(alert);
        
        // 设置冷却时间
        alertCooldowns.put(key, LocalDateTime.now());
        
        // 限制历史记录数量
        List<WorkflowAlertDTO> alerts = alertHistory.get(key);
        if (alerts.size() > 1000) {
            alerts.subList(0, alerts.size() - 1000).clear();
        }
    }
}