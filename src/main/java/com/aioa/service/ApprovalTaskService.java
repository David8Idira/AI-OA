package com.aioa.service;

import com.aioa.entity.ApprovalTask;
import com.aioa.exception.ResourceNotFoundException;
import com.aioa.mapper.ApprovalTaskMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

/**
 * 审批任务Service
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ApprovalTaskService {
    
    private final ApprovalTaskMapper approvalTaskMapper;
    
    /**
     * 根据ID获取审批任务
     */
    public ApprovalTask getTaskById(Long id) {
        log.info("Fetching approval task by id: {}", id);
        return approvalTaskMapper.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("ApprovalTask", id));
    }
    
    /**
     * 根据实例ID获取审批任务列表
     */
    public List<ApprovalTask> getTasksByInstanceId(Long instanceId) {
        log.info("Fetching approval tasks by instanceId: {}", instanceId);
        return approvalTaskMapper.findByInstanceId(instanceId);
    }
    
    /**
     * 根据处理人获取审批任务列表
     */
    public List<ApprovalTask> getTasksByAssignee(String assignee) {
        log.info("Fetching approval tasks by assignee: {}", assignee);
        return approvalTaskMapper.findByAssignee(assignee);
    }
    
    /**
     * 根据处理人和状态获取审批任务列表
     */
    public List<ApprovalTask> getTasksByAssigneeAndStatus(String assignee, String status) {
        log.info("Fetching approval tasks by assignee: {} and status: {}", assignee, status);
        return approvalTaskMapper.findByAssigneeAndStatus(assignee, status);
    }
    
    /**
     * 创建审批任务
     */
    @Transactional
    public ApprovalTask createTask(ApprovalTask task) {
        log.info("Creating approval task: {}", task.getTaskName());
        
        // 设置默认值
        if (task.getStatus() == null) {
            task.setStatus("PENDING");
        }
        if (task.getPriority() == null) {
            task.setPriority(5);
        }
        
        return approvalTaskMapper.save(task);
    }
    
    /**
     * 更新审批任务
     */
    @Transactional
    public ApprovalTask updateTask(ApprovalTask task) {
        log.info("Updating approval task: {}", task.getId());
        
        // 检查任务是否存在
        ApprovalTask existingTask = approvalTaskMapper.findById(task.getId())
                .orElseThrow(() -> new ResourceNotFoundException("ApprovalTask", task.getId()));
        
        // 更新任务信息
        if (task.getTaskName() != null) {
            existingTask.setTaskName(task.getTaskName());
        }
        if (task.getAssignee() != null) {
            existingTask.setAssignee(task.getAssignee());
        }
        if (task.getCandidateUsers() != null) {
            existingTask.setCandidateUsers(task.getCandidateUsers());
        }
        if (task.getCandidateGroups() != null) {
            existingTask.setCandidateGroups(task.getCandidateGroups());
        }
        if (task.getPriority() != null) {
            existingTask.setPriority(task.getPriority());
        }
        if (task.getDueDate() != null) {
            existingTask.setDueDate(task.getDueDate());
        }
        if (task.getStatus() != null) {
            existingTask.setStatus(task.getStatus());
        }
        if (task.getTaskVars() != null) {
            existingTask.setTaskVars(task.getTaskVars());
        }
        
        return approvalTaskMapper.save(existingTask);
    }
    
    /**
     * 删除审批任务
     */
    @Transactional
    public void deleteTask(Long id) {
        log.info("Deleting approval task: {}", id);
        
        if (!approvalTaskMapper.existsById(id)) {
            throw new ResourceNotFoundException("ApprovalTask", id);
        }
        
        approvalTaskMapper.deleteById(id);
    }
    
    /**
     * 签收任务
     */
    @Transactional
    public ApprovalTask claimTask(Long id, String userId) {
        log.info("User {} claiming task: {}", userId, id);
        
        ApprovalTask task = getTaskById(id);
        
        if (!"PENDING".equals(task.getStatus())) {
            throw new IllegalArgumentException("Task is not in PENDING status");
        }
        
        if (task.getAssignee() != null && !task.getAssignee().equals(userId)) {
            throw new IllegalArgumentException("Task is already assigned to another user");
        }
        
        task.setAssignee(userId);
        task.setStatus("CLAIMED");
        
        return approvalTaskMapper.save(task);
    }
    
    /**
     * 完成任务
     */
    @Transactional
    public ApprovalTask completeTask(Long id) {
        log.info("Completing approval task: {}", id);
        
        ApprovalTask task = getTaskById(id);
        
        if (!"CLAIMED".equals(task.getStatus()) && !"PENDING".equals(task.getStatus())) {
            throw new IllegalArgumentException("Task cannot be completed in current status: " + task.getStatus());
        }
        
        task.setStatus("COMPLETED");
        task.setCompleteDate(LocalDateTime.now());
        
        return approvalTaskMapper.save(task);
    }
    
    /**
     * 拒绝任务
     */
    @Transactional
    public ApprovalTask rejectTask(Long id, String reason) {
        log.info("Rejecting approval task: {}, reason: {}", id, reason);
        
        ApprovalTask task = getTaskById(id);
        
        task.setStatus("REJECTED");
        task.setCompleteDate(LocalDateTime.now());
        
        return approvalTaskMapper.save(task);
    }
    
    /**
     * 转办任务
     */
    @Transactional
    public ApprovalTask transferTask(Long id, String newAssignee) {
        log.info("Transferring task: {} to {}", id, newAssignee);
        
        ApprovalTask task = getTaskById(id);
        
        if (task.getAssignee() == null) {
            throw new IllegalArgumentException("Task has no assignee");
        }
        
        task.setAssignee(newAssignee);
        
        return approvalTaskMapper.save(task);
    }
    
    /**
     * 获取待办任务列表
     */
    public List<ApprovalTask> getTodoTasks(String assignee) {
        log.info("Fetching todo tasks for: {}", assignee);
        return approvalTaskMapper.findByAssigneeAndStatus(assignee, "PENDING");
    }
    
    /**
     * 获取已办任务列表
     */
    public List<ApprovalTask> getDoneTasks(String assignee) {
        log.info("Fetching done tasks for: {}", assignee);
        return approvalTaskMapper.findByAssigneeAndStatusIn(assignee, 
                Arrays.asList("COMPLETED", "REJECTED", "CANCELLED"));
    }
    
    /**
     * 获取超时任务列表
     */
    public List<ApprovalTask> getOverdueTasks() {
        log.info("Fetching overdue tasks");
        return approvalTaskMapper.findOverdueTasks(LocalDateTime.now());
    }
    
    /**
     * 根据状态获取任务列表
     */
    public List<ApprovalTask> getTasksByStatus(String status) {
        log.info("Fetching approval tasks by status: {}", status);
        return approvalTaskMapper.findByProcessId(null); // Use findByStatus
    }
    
    /**
     * 根据流程ID获取任务列表
     */
    public List<ApprovalTask> getTasksByProcessId(Long processId) {
        log.info("Fetching approval tasks by processId: {}", processId);
        return approvalTaskMapper.findByProcessId(processId);
    }
    
    /**
     * 统计任务数量
     */
    public long countByStatus(String status) {
        log.info("Counting tasks by status: {}", status);
        return approvalTaskMapper.countByStatus(status);
    }
    
    /**
     * 统计用户的待办任务数量
     */
    public long countTodoTasks(String assignee) {
        log.info("Counting todo tasks for: {}", assignee);
        return approvalTaskMapper.countByAssigneeAndStatus(assignee, "PENDING");
    }
}