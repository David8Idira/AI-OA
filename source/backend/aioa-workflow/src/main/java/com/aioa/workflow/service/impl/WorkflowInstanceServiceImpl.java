package com.aioa.workflow.service.impl;

import com.aioa.workflow.dto.N8nWorkflowDTO;
import com.aioa.workflow.dto.WorkflowInstanceDTO;
import com.aioa.workflow.entity.WorkflowInstance;
import com.aioa.workflow.enums.WorkflowStatus;
import com.aioa.workflow.mapper.WorkflowInstanceMapper;
import com.aioa.workflow.service.WorkflowInstanceService;
import com.aioa.workflow.service.N8nWorkflowService;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * 工作流实例服务实现
 * 
 * F5模块增强：工作流实例管理
 */
@Service
@Slf4j
public class WorkflowInstanceServiceImpl implements WorkflowInstanceService {

    @Autowired
    private WorkflowInstanceMapper instanceMapper;
    
    @Autowired
    private N8nWorkflowService n8nWorkflowService;
    
    @Autowired
    private ObjectMapper objectMapper;
    
    @Value("${workflow.instance.timeout.minutes:30}")
    private int instanceTimeoutMinutes;
    
    @Override
    @Transactional
    public WorkflowInstanceDTO createInstance(String workflowId, Map<String, Object> inputData, 
                                              String triggerBy, String triggerType) {
        try {
            log.info("创建工作流实例，工作流ID：{}，触发者：{}，类型：{}", 
                    workflowId, triggerBy, triggerType);
            
            WorkflowInstance instance = new WorkflowInstance();
            instance.setInstanceId(generateInstanceId());
            instance.setWorkflowId(workflowId);
            instance.setTriggerBy(triggerBy);
            instance.setTriggerType(triggerType);
            instance.setInputData(convertMapToString(inputData));
            instance.setStatus(WorkflowStatus.PENDING);
            instance.setCreatedAt(LocalDateTime.now());
            instance.setUpdatedAt(LocalDateTime.now());
            
            // 尝试触发工作流
            try {
                String triggerResult = n8nWorkflowService.triggerWorkflow(workflowId, inputData);
                if (triggerResult.contains("成功") || !triggerResult.contains("失败")) {
                    instance.setStatus(WorkflowStatus.RUNNING);
                    instance.setStartedAt(LocalDateTime.now());
                    log.info("工作流触发成功，实例ID：{}", instance.getInstanceId());
                } else {
                    instance.setStatus(WorkflowStatus.FAILED);
                    instance.setErrorMsg(triggerResult);
                    log.error("工作流触发失败：{}", triggerResult);
                }
            } catch (Exception e) {
                instance.setStatus(WorkflowStatus.FAILED);
                instance.setErrorMsg(e.getMessage());
                log.error("工作流触发异常", e);
            }
            
            // 保存实例
            instanceMapper.insert(instance);
            log.info("工作流实例创建成功，ID：{}", instance.getInstanceId());
            
            return convertToDTO(instance);
            
        } catch (Exception e) {
            log.error("创建工作流实例失败", e);
            throw new RuntimeException("创建实例失败: " + e.getMessage(), e);
        }
    }

    @Override
    public WorkflowInstanceDTO getInstance(String instanceId) {
        WorkflowInstance instance = instanceMapper.selectById(instanceId);
        if (instance == null) {
            throw new RuntimeException("工作流实例不存在: " + instanceId);
        }
        return convertToDTO(instance);
    }

    @Override
    public Page<WorkflowInstanceDTO> listInstances(String workflowId, WorkflowStatus status, 
                                                   LocalDateTime startTime, LocalDateTime endTime,
                                                   int page, int size) {
        int offset = page * size;
        
        // 查询列表
        List<WorkflowInstance> instances = instanceMapper.selectList(
                workflowId, status, startTime, endTime, offset, size);
        
        // 查询总数
        int total = instanceMapper.count(workflowId, status, startTime, endTime);
        
        // 转换为DTO
        List<WorkflowInstanceDTO> dtoList = instances.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
        
        return new PageImpl<>(dtoList, PageRequest.of(page, size), total);
    }

    @Override
    @Transactional
    public WorkflowInstanceDTO updateInstanceStatus(String instanceId, WorkflowStatus newStatus,
                                                   String outputData, String errorMsg) {
        WorkflowInstance instance = instanceMapper.selectById(instanceId);
        if (instance == null) {
            throw new RuntimeException("工作流实例不存在: " + instanceId);
        }
        
        instance.setStatus(newStatus);
        instance.setUpdatedAt(LocalDateTime.now());
        
        if (newStatus == WorkflowStatus.COMPLETED) {
            instance.setCompletedAt(LocalDateTime.now());
            instance.setOutputData(outputData);
            instance.setErrorMsg(null);
        } else if (newStatus == WorkflowStatus.FAILED) {
            instance.setErrorMsg(errorMsg);
        }
        
        instanceMapper.updateById(instance);
        log.info("更新工作流实例状态，ID：{}，新状态：{}", instanceId, newStatus);
        
        return convertToDTO(instance);
    }

    @Override
    @Transactional
    public void retryInstance(String instanceId) {
        WorkflowInstance instance = instanceMapper.selectById(instanceId);
        if (instance == null) {
            throw new RuntimeException("工作流实例不存在: " + instanceId);
        }
        
        if (instance.getStatus() != WorkflowStatus.FAILED) {
            throw new RuntimeException("只能重试失败状态的工作流实例");
        }
        
        log.info("重试工作流实例，ID：{}", instanceId);
        
        try {
            // 重新触发工作流
            Map<String, Object> inputData = convertStringToMap(instance.getInputData());
            String triggerResult = n8nWorkflowService.triggerWorkflow(instance.getWorkflowId(), inputData);
            
            if (triggerResult.contains("成功") || !triggerResult.contains("失败")) {
                instance.setStatus(WorkflowStatus.RUNNING);
                instance.setRetryCount(instance.getRetryCount() + 1);
                instance.setErrorMsg(null);
                instance.setUpdatedAt(LocalDateTime.now());
                
                instanceMapper.updateById(instance);
                log.info("工作流实例重试成功，ID：{}", instanceId);
            } else {
                throw new RuntimeException("重试触发失败: " + triggerResult);
            }
            
        } catch (Exception e) {
            log.error("重试工作流实例失败", e);
            instance.setErrorMsg("重试失败: " + e.getMessage());
            instanceMapper.updateById(instance);
            throw new RuntimeException("重试失败: " + e.getMessage(), e);
        }
    }

    @Override
    @Transactional
    public void cancelInstance(String instanceId) {
        WorkflowInstance instance = instanceMapper.selectById(instanceId);
        if (instance == null) {
            throw new RuntimeException("工作流实例不存在: " + instanceId);
        }
        
        if (instance.getStatus() != WorkflowStatus.RUNNING && 
            instance.getStatus() != WorkflowStatus.PENDING) {
            throw new RuntimeException("只能取消运行中或等待中的工作流实例");
        }
        
        instance.setStatus(WorkflowStatus.CANCELLED);
        instance.setCancelledAt(LocalDateTime.now());
        instance.setUpdatedAt(LocalDateTime.now());
        
        instanceMapper.updateById(instance);
        log.info("取消工作流实例，ID：{}", instanceId);
    }

    @Override
    public Map<String, Object> getInstanceStatistics(LocalDateTime startTime, LocalDateTime endTime) {
        Map<String, Object> stats = new HashMap<>();
        
        // 总数统计
        int totalCount = instanceMapper.count(null, null, startTime, endTime);
        stats.put("totalCount", totalCount);
        
        // 状态统计
        Map<WorkflowStatus, Integer> statusCounts = new HashMap<>();
        for (WorkflowStatus status : WorkflowStatus.values()) {
            int count = instanceMapper.count(null, status, startTime, endTime);
            statusCounts.put(status, count);
        }
        stats.put("statusCounts", statusCounts);
        
        // 成功率计算
        int completedCount = statusCounts.getOrDefault(WorkflowStatus.COMPLETED, 0);
        int failedCount = statusCounts.getOrDefault(WorkflowStatus.FAILED, 0);
        double successRate = totalCount > 0 ? (double) completedCount / totalCount * 100 : 0;
        stats.put("successRate", Math.round(successRate * 100) / 100.0);
        stats.put("completedCount", completedCount);
        stats.put("failedCount", failedCount);
        
        // 平均执行时间
        Double avgDuration = instanceMapper.getAverageDuration(startTime, endTime);
        stats.put("averageDuration", avgDuration != null ? Math.round(avgDuration * 100) / 100.0 : 0);
        
        // 最近活动时间
        LocalDateTime lastActivity = instanceMapper.getLastActivityTime();
        stats.put("lastActivityTime", lastActivity);
        
        log.info("工作流实例统计：总数={}，成功率={}%，平均时长={}秒", 
                totalCount, stats.get("successRate"), stats.get("averageDuration"));
        
        return stats;
    }

    @Override
    public List<WorkflowInstanceDTO> getInstancesByWorkflow(String workflowId, int limit) {
        List<WorkflowInstance> instances = instanceMapper.selectRecentByWorkflow(workflowId, limit);
        return instances.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    @Override
    public List<Map<String, Object>> getInstanceTimeline(String instanceId) {
        WorkflowInstance instance = instanceMapper.selectById(instanceId);
        if (instance == null) {
            throw new RuntimeException("工作流实例不存在: " + instanceId);
        }
        
        List<Map<String, Object>> timeline = new java.util.ArrayList<>();
        
        // 创建时间点
        addTimelinePoint(timeline, "created", "实例创建", instance.getCreatedAt(), 
                "工作流实例已创建", null);
        
        // 开始时间点
        if (instance.getStartedAt() != null) {
            addTimelinePoint(timeline, "started", "开始执行", instance.getStartedAt(), 
                    "工作流开始执行", null);
        }
        
        // 完成/取消/失败时间点
        if (instance.getCompletedAt() != null) {
            addTimelinePoint(timeline, "completed", "执行完成", instance.getCompletedAt(), 
                    "工作流执行完成", instance.getOutputData());
        } else if (instance.getCancelledAt() != null) {
            addTimelinePoint(timeline, "cancelled", "已取消", instance.getCancelledAt(), 
                    "工作流已取消", instance.getErrorMsg());
        } else if (instance.getErrorMsg() != null && instance.getUpdatedAt() != null) {
            addTimelinePoint(timeline, "failed", "执行失败", instance.getUpdatedAt(), 
                    "工作流执行失败", instance.getErrorMsg());
        }
        
        return timeline;
    }

    /**
     * 定时清理超时实例
     */
    @Scheduled(fixedDelay = 300000) // 每5分钟执行一次
    @Transactional
    public void cleanupTimeoutInstances() {
        try {
            LocalDateTime timeoutThreshold = LocalDateTime.now().minusMinutes(instanceTimeoutMinutes);
            
            List<WorkflowInstance> timeoutInstances = instanceMapper.selectTimeoutInstances(
                    timeoutThreshold, WorkflowStatus.RUNNING, WorkflowStatus.PENDING);
            
            if (!timeoutInstances.isEmpty()) {
                log.warn("发现{}个超时工作流实例", timeoutInstances.size());
                
                for (WorkflowInstance instance : timeoutInstances) {
                    instance.setStatus(WorkflowStatus.TIMEOUT);
                    instance.setErrorMsg("工作流执行超时（超过" + instanceTimeoutMinutes + "分钟）");
                    instance.setUpdatedAt(LocalDateTime.now());
                    
                    instanceMapper.updateById(instance);
                    log.info("标记工作流实例超时，ID：{}", instance.getInstanceId());
                }
            }
            
        } catch (Exception e) {
            log.error("清理超时实例失败", e);
        }
    }

    /**
     * 定时重试失败实例
     */
    @Scheduled(fixedDelay = 600000) // 每10分钟执行一次
    @Transactional
    public void autoRetryFailedInstances() {
        try {
            // 只重试最近1小时内的失败实例，且重试次数小于3次
            LocalDateTime recentThreshold = LocalDateTime.now().minusHours(1);
            
            List<WorkflowInstance> failedInstances = instanceMapper.selectRecentFailedInstances(
                    recentThreshold, 3); // 最大重试次数
            
            if (!failedInstances.isEmpty()) {
                log.info("自动重试{}个失败的工作流实例", failedInstances.size());
                
                for (WorkflowInstance instance : failedInstances) {
                    try {
                        retryInstance(instance.getInstanceId());
                        log.info("自动重试成功，实例ID：{}", instance.getInstanceId());
                    } catch (Exception e) {
                        log.error("自动重试失败，实例ID：{}", instance.getInstanceId(), e);
                    }
                }
            }
            
        } catch (Exception e) {
            log.error("自动重试失败实例失败", e);
        }
    }

    private String generateInstanceId() {
        return "WFI-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
    }

    private String convertMapToString(Map<String, Object> map) {
        try {
            return objectMapper.writeValueAsString(map);
        } catch (Exception e) {
            log.error("转换Map为JSON字符串失败", e);
            return "{}";
        }
    }

    private Map<String, Object> convertStringToMap(String json) {
        try {
            return objectMapper.readValue(json, Map.class);
        } catch (Exception e) {
            log.error("转换JSON字符串为Map失败", e);
            return new HashMap<>();
        }
    }

    private WorkflowInstanceDTO convertToDTO(WorkflowInstance entity) {
        WorkflowInstanceDTO dto = new WorkflowInstanceDTO();
        BeanUtils.copyProperties(entity, dto);
        
        // 转换输入输出数据
        if (entity.getInputData() != null) {
            dto.setInputData(convertStringToMap(entity.getInputData()));
        }
        
        if (entity.getOutputData() != null) {
            dto.setOutputData(convertStringToMap(entity.getOutputData()));
        }
        
        return dto;
    }

    private void addTimelinePoint(List<Map<String, Object>> timeline, String type, String title,
                                 LocalDateTime time, String description, String details) {
        Map<String, Object> point = new HashMap<>();
        point.put("type", type);
        point.put("title", title);
        point.put("time", time);
        point.put("description", description);
        point.put("details", details);
        timeline.add(point);
    }
}