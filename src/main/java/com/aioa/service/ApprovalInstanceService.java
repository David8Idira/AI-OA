package com.aioa.service;

import com.aioa.entity.ApprovalInstance;
import com.aioa.entity.ApprovalProcess;
import com.aioa.exception.ResourceNotFoundException;
import com.aioa.mapper.ApprovalInstanceMapper;
import com.aioa.mapper.ApprovalProcessMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.concurrent.atomic.AtomicLong;

/**
 * 审批实例Service
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ApprovalInstanceService {
    
    private final ApprovalInstanceMapper approvalInstanceMapper;
    private final ApprovalProcessMapper approvalProcessMapper;
    
    // 用于生成实例编号的简单计数器（生产环境应使用分布式ID生成器）
    private static final AtomicLong instanceNoGenerator = new AtomicLong(System.currentTimeMillis() % 100000);
    
    private static final DateTimeFormatter INSTANCE_NO_FORMAT = DateTimeFormatter.ofPattern("yyyyMMdd");
    
    /**
     * 根据ID获取审批实例
     */
    public ApprovalInstance getInstanceById(Long id) {
        log.info("Fetching approval instance by id: {}", id);
        return approvalInstanceMapper.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("ApprovalInstance", id));
    }
    
    /**
     * 根据实例编号获取审批实例
     */
    public ApprovalInstance getInstanceByNo(String instanceNo) {
        log.info("Fetching approval instance by no: {}", instanceNo);
        return approvalInstanceMapper.findByInstanceNo(instanceNo)
                .orElseThrow(() -> new ResourceNotFoundException("ApprovalInstance", "instanceNo", instanceNo));
    }
    
    /**
     * 创建审批实例
     */
    @Transactional
    public ApprovalInstance createInstance(ApprovalInstance instance) {
        log.info("Creating approval instance for process: {}", instance.getProcessId());
        
        // 验证流程是否存在
        ApprovalProcess process = approvalProcessMapper.findById(instance.getProcessId())
                .orElseThrow(() -> new ResourceNotFoundException("ApprovalProcess", instance.getProcessId()));
        
        // 验证流程是否已发布
        if (!"PUBLISHED".equals(process.getStatus())) {
            throw new IllegalArgumentException("Cannot start an instance from an unpublished process");
        }
        
        // 生成实例编号
        String instanceNo = generateInstanceNo();
        instance.setInstanceNo(instanceNo);
        instance.setProcessKey(process.getProcessKey());
        
        // 设置默认值
        if (instance.getStatus() == null) {
            instance.setStatus("RUNNING");
        }
        if (instance.getStartTime() == null) {
            instance.setStartTime(LocalDateTime.now());
        }
        if (instance.getTitle() == null) {
            instance.setTitle(process.getProcessName());
        }
        
        return approvalInstanceMapper.save(instance);
    }
    
    /**
     * 更新审批实例
     */
    @Transactional
    public ApprovalInstance updateInstance(ApprovalInstance instance) {
        log.info("Updating approval instance: {}", instance.getId());
        
        // 检查实例是否存在
        ApprovalInstance existingInstance = approvalInstanceMapper.findById(instance.getId())
                .orElseThrow(() -> new ResourceNotFoundException("ApprovalInstance", instance.getId()));
        
        // 验证实例状态是否可以更新
        if ("COMPLETED".equals(existingInstance.getStatus()) || "CANCELLED".equals(existingInstance.getStatus())) {
            throw new IllegalArgumentException("Cannot update a completed or cancelled instance");
        }
        
        // 更新实例信息
        if (instance.getTitle() != null) {
            existingInstance.setTitle(instance.getTitle());
        }
        if (instance.getFormData() != null) {
            existingInstance.setFormData(instance.getFormData());
        }
        if (instance.getCurrentNodeId() != null) {
            existingInstance.setCurrentNodeId(instance.getCurrentNodeId());
        }
        if (instance.getCurrentNodeName() != null) {
            existingInstance.setCurrentNodeName(instance.getCurrentNodeName());
        }
        if (instance.getTotalAmount() != null) {
            existingInstance.setTotalAmount(instance.getTotalAmount());
        }
        if (instance.getStatus() != null) {
            existingInstance.setStatus(instance.getStatus());
            
            // 如果状态变为完成，设置完成时间
            if ("COMPLETED".equals(instance.getStatus())) {
                existingInstance.setEndTime(LocalDateTime.now());
                existingInstance.setDuration(calculateDuration(existingInstance.getStartTime(), existingInstance.getEndTime()));
            }
        }
        
        return approvalInstanceMapper.save(existingInstance);
    }
    
    /**
     * 撤回审批实例
     */
    @Transactional
    public ApprovalInstance withdrawInstance(Long id) {
        log.info("Withdrawing approval instance: {}", id);
        
        ApprovalInstance instance = getInstanceById(id);
        
        if (!"RUNNING".equals(instance.getStatus())) {
            throw new IllegalArgumentException("Can only withdraw a running instance");
        }
        
        instance.setStatus("WITHDRAWN");
        instance.setEndTime(LocalDateTime.now());
        instance.setDuration(calculateDuration(instance.getStartTime(), instance.getEndTime()));
        
        return approvalInstanceMapper.save(instance);
    }
    
    /**
     * 终止审批实例
     */
    @Transactional
    public ApprovalInstance terminateInstance(Long id, String reason) {
        log.info("Terminating approval instance: {}, reason: {}", id, reason);
        
        ApprovalInstance instance = getInstanceById(id);
        
        if (!"RUNNING".equals(instance.getStatus())) {
            throw new IllegalArgumentException("Can only terminate a running instance");
        }
        
        instance.setStatus("TERMINATED");
        instance.setEndTime(LocalDateTime.now());
        instance.setDuration(calculateDuration(instance.getStartTime(), instance.getEndTime()));
        
        return approvalInstanceMapper.save(instance);
    }
    
    /**
     * 审批通过
     */
    @Transactional
    public ApprovalInstance approveInstance(Long id) {
        log.info("Approving approval instance: {}", id);
        
        ApprovalInstance instance = getInstanceById(id);
        
        if (!"RUNNING".equals(instance.getStatus())) {
            throw new IllegalArgumentException("Can only approve a running instance");
        }
        
        instance.setStatus("COMPLETED");
        instance.setEndTime(LocalDateTime.now());
        instance.setDuration(calculateDuration(instance.getStartTime(), instance.getEndTime()));
        
        return approvalInstanceMapper.save(instance);
    }
    
    /**
     * 审批拒绝
     */
    @Transactional
    public ApprovalInstance rejectInstance(Long id, String reason) {
        log.info("Rejecting approval instance: {}, reason: {}", id, reason);
        
        ApprovalInstance instance = getInstanceById(id);
        
        if (!"RUNNING".equals(instance.getStatus())) {
            throw new IllegalArgumentException("Can only reject a running instance");
        }
        
        instance.setStatus("REJECTED");
        instance.setEndTime(LocalDateTime.now());
        instance.setDuration(calculateDuration(instance.getStartTime(), instance.getEndTime()));
        
        return approvalInstanceMapper.save(instance);
    }
    
    /**
     * 根据流程ID获取实例列表
     */
    public List<ApprovalInstance> getInstancesByProcessId(Long processId) {
        log.info("Fetching approval instances by processId: {}", processId);
        return approvalInstanceMapper.findByProcessId(processId);
    }
    
    /**
     * 根据申请人获取实例列表
     */
    public List<ApprovalInstance> getInstancesByApplicant(String applicant) {
        log.info("Fetching approval instances by applicant: {}", applicant);
        return approvalInstanceMapper.findByApplicant(applicant);
    }
    
    /**
     * 根据状态获取实例列表
     */
    public List<ApprovalInstance> getInstancesByStatus(String status) {
        log.info("Fetching approval instances by status: {}", status);
        return approvalInstanceMapper.findByStatus(status);
    }
    
    /**
     * 获取申请人的待审批实例
     */
    public List<ApprovalInstance> getPendingInstances(Long applicantId) {
        log.info("Fetching pending instances for applicant: {}", applicantId);
        return approvalInstanceMapper.findByApplicantIdAndStatus(applicantId, "RUNNING");
    }
    
    /**
     * 获取指定时间范围内的实例
     */
    public List<ApprovalInstance> getInstancesByDateRange(String status, LocalDateTime startDate, LocalDateTime endDate) {
        log.info("Fetching instances by date range, status: {}, from {} to {}", status, startDate, endDate);
        return approvalInstanceMapper.findByStatusAndDateRange(status, startDate, endDate);
    }
    
    /**
     * 统计实例数量
     */
    public long countByStatus(String status) {
        log.info("Counting instances by status: {}", status);
        return approvalInstanceMapper.countByStatus(status);
    }
    
    /**
     * 统计申请人的实例数量
     */
    public long countByApplicantAndStatus(Long applicantId, String status) {
        log.info("Counting instances for applicant: {} with status: {}", applicantId, status);
        return approvalInstanceMapper.countByApplicantIdAndStatus(applicantId, status);
    }
    
    /**
     * 生成实例编号
     */
    private String generateInstanceNo() {
        String datePrefix = LocalDateTime.now().format(INSTANCE_NO_FORMAT);
        long sequence = instanceNoGenerator.incrementAndGet() % 100000;
        return String.format("AI%s%05d", datePrefix, sequence);
    }
    
    /**
     * 计算审批时长（毫秒）
     */
    private Long calculateDuration(LocalDateTime startTime, LocalDateTime endTime) {
        if (startTime == null || endTime == null) {
            return null;
        }
        return java.time.Duration.between(startTime, endTime).toMillis();
    }
}