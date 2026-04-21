package com.aioa.service;

import com.aioa.entity.ApprovalProcess;
import com.aioa.exception.ResourceConflictException;
import com.aioa.exception.ResourceNotFoundException;
import com.aioa.mapper.ApprovalProcessMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * 审批流程Service
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ApprovalProcessService {
    
    private final ApprovalProcessMapper approvalProcessMapper;
    
    /**
     * 根据ID获取审批流程
     */
    public ApprovalProcess getProcessById(Long id) {
        log.info("Fetching approval process by id: {}", id);
        return approvalProcessMapper.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("ApprovalProcess", id));
    }
    
    /**
     * 根据流程Key获取审批流程
     */
    public ApprovalProcess getProcessByKey(String processKey) {
        log.info("Fetching approval process by key: {}", processKey);
        return approvalProcessMapper.findByProcessKey(processKey)
                .orElseThrow(() -> new ResourceNotFoundException("ApprovalProcess", "processKey", processKey));
    }
    
    /**
     * 创建审批流程
     */
    @Transactional
    public ApprovalProcess createProcess(ApprovalProcess process) {
        log.info("Creating approval process: {}", process.getProcessName());
        
        // 检查流程Key是否已存在
        if (approvalProcessMapper.existsByProcessKey(process.getProcessKey())) {
            throw new ResourceConflictException("ApprovalProcess", "processKey", process.getProcessKey());
        }
        
        // 设置默认值
        if (process.getStatus() == null) {
            process.setStatus("DRAFT");
        }
        if (process.getVersion() == null) {
            process.setVersion(1);
        }
        
        return approvalProcessMapper.save(process);
    }
    
    /**
     * 更新审批流程
     */
    @Transactional
    public ApprovalProcess updateProcess(ApprovalProcess process) {
        log.info("Updating approval process: {}", process.getId());
        
        // 检查流程是否存在
        ApprovalProcess existingProcess = approvalProcessMapper.findById(process.getId())
                .orElseThrow(() -> new ResourceNotFoundException("ApprovalProcess", process.getId()));
        
        // 如果更新流程Key，检查是否与其他流程冲突
        if (process.getProcessKey() != null && 
            !process.getProcessKey().equals(existingProcess.getProcessKey())) {
            if (approvalProcessMapper.existsByProcessKey(process.getProcessKey())) {
                throw new ResourceConflictException("ApprovalProcess", "processKey", process.getProcessKey());
            }
        }
        
        // 更新流程信息
        if (process.getProcessName() != null) {
            existingProcess.setProcessName(process.getProcessName());
        }
        if (process.getProcessKey() != null) {
            existingProcess.setProcessKey(process.getProcessKey());
        }
        if (process.getDescription() != null) {
            existingProcess.setDescription(process.getDescription());
        }
        if (process.getCategory() != null) {
            existingProcess.setCategory(process.getCategory());
        }
        if (process.getFormSchema() != null) {
            existingProcess.setFormSchema(process.getFormSchema());
        }
        if (process.getFlowConfig() != null) {
            existingProcess.setFlowConfig(process.getFlowConfig());
        }
        if (process.getStatus() != null) {
            existingProcess.setStatus(process.getStatus());
        }
        
        // 每次更新增加版本号
        existingProcess.setVersion(existingProcess.getVersion() + 1);
        
        return approvalProcessMapper.save(existingProcess);
    }
    
    /**
     * 删除审批流程
     */
    @Transactional
    public void deleteProcess(Long id) {
        log.info("Deleting approval process: {}", id);
        
        if (!approvalProcessMapper.existsById(id)) {
            throw new ResourceNotFoundException("ApprovalProcess", id);
        }
        
        approvalProcessMapper.deleteById(id);
    }
    
    /**
     * 发布审批流程
     */
    @Transactional
    public ApprovalProcess publishProcess(Long id) {
        log.info("Publishing approval process: {}", id);
        
        ApprovalProcess process = getProcessById(id);
        
        if ("PUBLISHED".equals(process.getStatus())) {
            throw new IllegalArgumentException("Process is already published");
        }
        
        if (process.getFormSchema() == null || process.getFlowConfig() == null) {
            throw new IllegalArgumentException("Process must have form schema and flow config before publishing");
        }
        
        process.setStatus("PUBLISHED");
        return approvalProcessMapper.save(process);
    }
    
    /**
     * 停用审批流程
     */
    @Transactional
    public ApprovalProcess deactivateProcess(Long id) {
        log.info("Deactivating approval process: {}", id);
        
        ApprovalProcess process = getProcessById(id);
        
        if ("DRAFT".equals(process.getStatus())) {
            throw new IllegalArgumentException("Cannot deactivate a draft process");
        }
        
        process.setStatus("INACTIVE");
        return approvalProcessMapper.save(process);
    }
    
    /**
     * 根据分类获取审批流程列表
     */
    public List<ApprovalProcess> getProcessesByCategory(String category) {
        log.info("Fetching approval processes by category: {}", category);
        return approvalProcessMapper.findByCategory(category);
    }
    
    /**
     * 根据状态获取审批流程列表
     */
    public List<ApprovalProcess> getProcessesByStatus(String status) {
        log.info("Fetching approval processes by status: {}", status);
        return approvalProcessMapper.findByStatus(status);
    }
    
    /**
     * 根据创建者获取审批流程列表
     */
    public List<ApprovalProcess> getProcessesByCreatedBy(String createdBy) {
        log.info("Fetching approval processes by createdBy: {}", createdBy);
        return approvalProcessMapper.findByCreatedBy(createdBy);
    }
    
    /**
     * 搜索审批流程
     */
    public List<ApprovalProcess> searchProcesses(String processName, String category, String status) {
        log.info("Searching processes with processName: {}, category: {}, status: {}", 
                processName, category, status);
        return approvalProcessMapper.searchProcesses(processName, category, status);
    }
    
    /**
     * 获取所有审批流程
     */
    public List<ApprovalProcess> getAllProcesses() {
        log.info("Fetching all approval processes");
        return approvalProcessMapper.findAll();
    }
    
    /**
     * 统计审批流程数量
     */
    public long countByStatus(String status) {
        log.info("Counting processes by status: {}", status);
        return approvalProcessMapper.countByStatus(status);
    }
    
    /**
     * 复制审批流程
     */
    @Transactional
    public ApprovalProcess copyProcess(Long id, String newProcessKey, String newProcessName) {
        log.info("Copying approval process: {} to new key: {}", id, newProcessKey);
        
        ApprovalProcess originalProcess = getProcessById(id);
        
        // 检查新流程Key是否已存在
        if (approvalProcessMapper.existsByProcessKey(newProcessKey)) {
            throw new ResourceConflictException("ApprovalProcess", "processKey", newProcessKey);
        }
        
        // 创建副本
        ApprovalProcess copiedProcess = ApprovalProcess.builder()
                .processName(newProcessName)
                .processKey(newProcessKey)
                .description(originalProcess.getDescription())
                .category(originalProcess.getCategory())
                .formSchema(originalProcess.getFormSchema())
                .flowConfig(originalProcess.getFlowConfig())
                .version(1)
                .status("DRAFT")
                .createdBy(originalProcess.getCreatedBy())
                .build();
        
        return approvalProcessMapper.save(copiedProcess);
    }
}