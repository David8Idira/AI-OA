package com.aioa.workflow.service.impl;

import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.aioa.common.exception.BusinessException;
import com.aioa.common.result.ResultCode;
import com.aioa.system.entity.SysUser;
import com.aioa.workflow.dto.ApprovalActionDTO;
import com.aioa.workflow.dto.ApprovalQueryDTO;
import com.aioa.workflow.dto.CreateApprovalDTO;
import com.aioa.workflow.entity.Approval;
import com.aioa.workflow.entity.ApprovalRecord;
import com.aioa.workflow.enums.ApprovalActionEnum;
import com.aioa.workflow.enums.ApprovalPriorityEnum;
import com.aioa.workflow.enums.ApprovalStatusEnum;
import com.aioa.workflow.mapper.ApprovalMapper;
import com.aioa.workflow.mapper.ApprovalRecordMapper;
import com.aioa.workflow.service.ApprovalRecordService;
import com.aioa.workflow.service.ApprovalService;
import com.aioa.workflow.vo.ApprovalRecordVO;
import com.aioa.workflow.vo.ApprovalVO;
import com.aioa.common.vo.PageResult;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Approval Service Implementation
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ApprovalServiceImpl extends ServiceImpl<ApprovalMapper, Approval> implements ApprovalService {

    private final ApprovalRecordService approvalRecordService;

    @Autowired(required = false)
    private com.aioa.system.mapper.SysUserMapper sysUserMapper;

    /**
     * Current system operator name cache (in real impl, get from security context)
     */
    private static final String SYSTEM_OPERATOR = "system";

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ApprovalVO createApproval(String userId, CreateApprovalDTO dto) {
        log.info("Creating approval: userId={}, type={}, title={}", userId, dto.getType(), dto.getTitle());

        // Fetch applicant info
        SysUser applicant = getUserById(userId);
        if (applicant == null) {
            throw new BusinessException(ResultCode.USER_NOT_FOUND);
        }

        // Fetch approver info
        SysUser approver = getUserById(dto.getApproverId());
        if (approver == null) {
            throw new BusinessException(ResultCode.USER_NOT_FOUND.getCode(), "指定的审批人不存在");
        }

        // Build approval entity
        Approval approval = new Approval();
        approval.setTitle(dto.getTitle());
        approval.setType(dto.getType());
        approval.setContent(dto.getContent());
        approval.setStatus(ApprovalStatusEnum.PENDING.getCode());
        approval.setPriority(dto.getPriority() != null ? dto.getPriority() : ApprovalPriorityEnum.NORMAL.getCode());
        approval.setApplicantId(userId);
        approval.setApplicantName(applicant.getNickname() != null ? applicant.getNickname() : applicant.getUsername());
        approval.setApproverId(dto.getApproverId());
        approval.setApproverName(approver.getNickname() != null ? approver.getNickname() : approver.getUsername());
        approval.setDeptId(applicant.getDeptId());
        approval.setDeptName(applicant.getDeptId()); // TODO: resolve dept name
        approval.setCcUsers(dto.getCcUsers());
        approval.setExpectFinishTime(dto.getExpectFinishTime());
        approval.setCurrentStep(1);
        approval.setTotalSteps(1);
        approval.setAttachments(dto.getAttachments());
        approval.setRemark(dto.getRemark());

        // Serialize form data if provided
        if (dto.getFormData() != null && !dto.getFormData().isEmpty()) {
            approval.setFormData(JSONUtil.toJsonStr(dto.getFormData()));
        }

        approval.setCreateBy(userId);
        approval.setUpdateBy(userId);

        // Save approval
        this.save(approval);

        // Create the first approval record (submission)
        approvalRecordService.createRecord(
            approval.getId(),
            userId,
            approval.getApplicantName(),
            ApprovalActionEnum.CANCEL.getCode(), // Using CANCEL code for "submit" as placeholder
            "提交审批",
            ApprovalStatusEnum.PENDING.getCode(),
            1
        );

        log.info("Approval created successfully: id={}", approval.getId());
        return buildApprovalVO(approval, userId);
    }

    @Override
    public ApprovalVO getApprovalDetail(String approvalId, String userId) {
        Approval approval = this.getById(approvalId);
        if (approval == null) {
            throw new BusinessException(ResultCode.APPROVAL_NOT_FOUND);
        }

        // Permission check: only applicant, approver, or CC users can view
        if (!canView(userId, approval)) {
            throw new BusinessException(ResultCode.APPROVAL_PERMISSION_DENIED);
        }

        ApprovalVO vo = buildApprovalVO(approval, userId);

        // Load approval records
        List<ApprovalRecordVO> records = approvalRecordService.getRecordsByApprovalId(approvalId);
        vo.setRecords(records);

        return vo;
    }

    @Override
    public PageResult<ApprovalVO> queryApprovals(String userId, ApprovalQueryDTO query) {
        Integer pageNum = query.getPageNum() != null ? query.getPageNum() : 1;
        Integer pageSize = query.getPageSize() != null ? query.getPageSize() : 10;
        Page<Approval> page = new Page<>(pageNum, pageSize);

        LambdaQueryWrapper<Approval> wrapper = new LambdaQueryWrapper<>();

        // Mode-based filtering
        if ("MY_APPLY".equalsIgnoreCase(query.getMode())) {
            wrapper.eq(Approval::getApplicantId, userId);
        } else if ("MY_APPROVE".equalsIgnoreCase(query.getMode())) {
            wrapper.eq(Approval::getApproverId, userId);
        } else {
            // Default: user can see their own applications and approvals they're assigned to
            wrapper.and(w -> w.eq(Approval::getApplicantId, userId).or().eq(Approval::getApproverId, userId));
        }

        // Type filter
        if (StrUtil.isNotBlank(query.getType())) {
            wrapper.eq(Approval::getType, query.getType());
        }

        // Status filter
        if (query.getStatus() != null) {
            wrapper.eq(Approval::getStatus, query.getStatus());
        }

        // Priority filter
        if (query.getPriority() != null) {
            wrapper.eq(Approval::getPriority, query.getPriority());
        }

        // Keyword search
        if (StrUtil.isNotBlank(query.getKeyword())) {
            wrapper.and(w -> w.like(Approval::getTitle, query.getKeyword())
                    .or().like(Approval::getContent, query.getKeyword()));
        }

        // Date range
        if (StrUtil.isNotBlank(query.getStartDate())) {
            wrapper.ge(Approval::getCreateTime, query.getStartDate());
        }
        if (StrUtil.isNotBlank(query.getEndDate())) {
            wrapper.le(Approval::getCreateTime, query.getEndDate());
        }

        wrapper.orderByDesc(Approval::getCreateTime);

        IPage<Approval> resultPage = this.page(page, wrapper);
        List<ApprovalVO> voList = resultPage.getRecords().stream()
            .map(a -> buildApprovalVO(a, userId))
            .collect(Collectors.toList());

        return PageResult.of(resultPage.getTotal(), pageNum, pageSize, voList);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ApprovalVO doAction(String approvalId, String userId, ApprovalActionDTO dto) {
        log.info("Doing approval action: approvalId={}, userId={}, action={}",
                 approvalId, userId, dto.getActionType());

        Approval approval = this.getById(approvalId);
        if (approval == null) {
            throw new BusinessException(ResultCode.APPROVAL_NOT_FOUND);
        }

        ApprovalActionEnum action = ApprovalActionEnum.getByCode(dto.getActionType());
        if (action == null) {
            throw new BusinessException(ResultCode.BAD_REQUEST.getCode(), "无效的审批操作类型");
        }

        SysUser operator = getUserById(userId);
        if (operator == null) {
            throw new BusinessException(ResultCode.USER_NOT_FOUND);
        }
        String operatorName = operator.getNickname() != null ? operator.getNickname() : operator.getUsername();

        switch (action) {
            case APPROVE -> {
                handleApprove(approval, userId, operatorName, dto);
            }
            case REJECT -> {
                handleReject(approval, userId, operatorName, dto);
            }
            case TRANSFER -> {
                handleTransfer(approval, userId, operatorName, dto);
            }
            case CANCEL -> {
                handleCancel(approval, userId, operatorName, dto);
            }
            default -> throw new BusinessException(ResultCode.BAD_REQUEST.getCode(), "不支持的审批操作");
        }

        this.updateById(approval);

        // Reload records
        List<ApprovalRecordVO> records = approvalRecordService.getRecordsByApprovalId(approvalId);
        ApprovalVO vo = buildApprovalVO(approval, userId);
        vo.setRecords(records);
        return vo;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean cancelApproval(String approvalId, String userId, String reason) {
        Approval approval = this.getById(approvalId);
        if (approval == null) {
            throw new BusinessException(ResultCode.APPROVAL_NOT_FOUND);
        }

        // Only applicant can cancel
        if (!userId.equals(approval.getApplicantId())) {
            throw new BusinessException(ResultCode.APPROVAL_PERMISSION_DENIED);
        }

        // Can only cancel pending approvals
        if (!ApprovalStatusEnum.PENDING.getCode().equals(approval.getStatus())) {
            throw new BusinessException(ResultCode.APPROVAL_STATUS_ERROR);
        }

        SysUser operator = getUserById(userId);
        String operatorName = operator != null && operator.getNickname() != null
            ? operator.getNickname() : operator.getUsername();

        approval.setStatus(ApprovalStatusEnum.CANCELLED.getCode());
        approval.setFinishTime(LocalDateTime.now());
        approval.setApprovalComment(reason != null ? reason : "申请人撤回");
        approval.setUpdateBy(userId);

        this.updateById(approval);

        // Create cancellation record
        approvalRecordService.createRecord(
            approvalId, userId, operatorName,
            ApprovalActionEnum.CANCEL.getCode(),
            reason != null ? reason : "申请人撤回",
            ApprovalStatusEnum.CANCELLED.getCode(),
            approval.getCurrentStep()
        );

        log.info("Approval cancelled: approvalId={}, userId={}", approvalId, userId);
        return true;
    }

    @Override
    public PageResult<ApprovalVO> getPendingApprovals(String approverId, Integer pageNum, Integer pageSize) {
        pageNum = pageNum != null ? pageNum : 1;
        pageSize = pageSize != null ? pageSize : 10;
        Page<Approval> page = new Page<>(pageNum, pageSize);

        LambdaQueryWrapper<Approval> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Approval::getApproverId, approverId)
               .eq(Approval::getStatus, ApprovalStatusEnum.PENDING.getCode())
               .orderByDesc(Approval::getCreateTime);

        IPage<Approval> resultPage = this.page(page, wrapper);
        List<ApprovalVO> voList = resultPage.getRecords().stream()
            .map(a -> buildApprovalVO(a, approverId))
            .collect(Collectors.toList());

        return PageResult.of(resultPage.getTotal(), pageNum, pageSize, voList);
    }

    @Override
    public PageResult<ApprovalVO> getMyApprovals(String applicantId, Integer pageNum, Integer pageSize) {
        pageNum = pageNum != null ? pageNum : 1;
        pageSize = pageSize != null ? pageSize : 10;
        Page<Approval> page = new Page<>(pageNum, pageSize);

        LambdaQueryWrapper<Approval> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Approval::getApplicantId, applicantId)
               .orderByDesc(Approval::getCreateTime);

        IPage<Approval> resultPage = this.page(page, wrapper);
        List<ApprovalVO> voList = resultPage.getRecords().stream()
            .map(a -> buildApprovalVO(a, applicantId))
            .collect(Collectors.toList());

        return PageResult.of(resultPage.getTotal(), pageNum, pageSize, voList);
    }

    @Override
    public Long countPending(String approverId) {
        LambdaQueryWrapper<Approval> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Approval::getApproverId, approverId)
               .eq(Approval::getStatus, ApprovalStatusEnum.PENDING.getCode());
        return this.count(wrapper);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ApprovalVO reassignApprover(String approvalId, String operatorId, String newApproverId, String reason) {
        Approval approval = this.getById(approvalId);
        if (approval == null) {
            throw new BusinessException(ResultCode.APPROVAL_NOT_FOUND);
        }

        SysUser operator = getUserById(operatorId);
        if (operator == null) {
            throw new BusinessException(ResultCode.USER_NOT_FOUND);
        }

        SysUser newApprover = getUserById(newApproverId);
        if (newApprover == null) {
            throw new BusinessException(ResultCode.USER_NOT_FOUND.getCode(), "新审批人不存在");
        }

        String operatorName = operator.getNickname() != null ? operator.getNickname() : operator.getUsername();
        String previousApproverId = approval.getApproverId();

        approval.setApproverId(newApproverId);
        approval.setApproverName(newApprover.getNickname() != null ? newApprover.getNickname() : newApprover.getUsername());
        approval.setStatus(ApprovalStatusEnum.TRANSFERRED.getCode());
        approval.setUpdateBy(operatorId);

        this.updateById(approval);

        // Create transfer record
        approvalRecordService.createTransferRecord(
            approvalId, operatorId, operatorName,
            previousApproverId, newApproverId,
            approval.getApproverName(),
            reason,
            ApprovalStatusEnum.TRANSFERRED.getCode()
        );

        List<ApprovalRecordVO> records = approvalRecordService.getRecordsByApprovalId(approvalId);
        ApprovalVO vo = buildApprovalVO(approval, operatorId);
        vo.setRecords(records);
        return vo;
    }

    @Override
    public Map<String, Object> getStatistics(String userId) {
        Map<String, Object> stats = new HashMap<>();

        // Total submitted by user
        LambdaQueryWrapper<Approval> myApplyWrapper = new LambdaQueryWrapper<>();
        myApplyWrapper.eq(Approval::getApplicantId, userId);
        stats.put("totalMyApply", this.count(myApplyWrapper));

        // Total pending for user (as approver)
        stats.put("totalPendingApprove", countPending(userId));

        // Approved count
        LambdaQueryWrapper<Approval> approvedWrapper = new LambdaQueryWrapper<>();
        approvedWrapper.eq(Approval::getApplicantId, userId)
                       .eq(Approval::getStatus, ApprovalStatusEnum.APPROVED.getCode());
        stats.put("totalApproved", this.count(approvedWrapper));

        // Rejected count
        LambdaQueryWrapper<Approval> rejectedWrapper = new LambdaQueryWrapper<>();
        rejectedWrapper.eq(Approval::getApplicantId, userId)
                       .eq(Approval::getStatus, ApprovalStatusEnum.REJECTED.getCode());
        stats.put("totalRejected", this.count(rejectedWrapper));

        return stats;
    }

    // ==================== Private Helper Methods ====================

    /**
     * Handle approve action
     */
    private void handleApprove(Approval approval, String userId, String operatorName, ApprovalActionDTO dto) {
        // Permission check: must be current approver
        if (!userId.equals(approval.getApproverId())) {
            throw new BusinessException(ResultCode.APPROVAL_PERMISSION_DENIED);
        }

        // Status check
        if (!ApprovalStatusEnum.PENDING.getCode().equals(approval.getStatus())) {
            throw new BusinessException(ResultCode.APPROVAL_STATUS_ERROR);
        }

        approval.setStatus(ApprovalStatusEnum.APPROVED.getCode());
        approval.setFinishTime(LocalDateTime.now());
        approval.setApprovalComment(dto.getComment());
        approval.setUpdateBy(userId);

        approvalRecordService.createRecord(
            approval.getId(), userId, operatorName,
            ApprovalActionEnum.APPROVE.getCode(),
            dto.getComment(),
            ApprovalStatusEnum.APPROVED.getCode(),
            approval.getCurrentStep()
        );

        log.info("Approval approved: approvalId={}, userId={}", approval.getId(), userId);
    }

    /**
     * Handle reject action
     */
    private void handleReject(Approval approval, String userId, String operatorName, ApprovalActionDTO dto) {
        if (!userId.equals(approval.getApproverId())) {
            throw new BusinessException(ResultCode.APPROVAL_PERMISSION_DENIED);
        }

        if (!ApprovalStatusEnum.PENDING.getCode().equals(approval.getStatus())) {
            throw new BusinessException(ResultCode.APPROVAL_STATUS_ERROR);
        }

        approval.setStatus(ApprovalStatusEnum.REJECTED.getCode());
        approval.setFinishTime(LocalDateTime.now());
        approval.setApprovalComment(dto.getComment());
        approval.setUpdateBy(userId);

        approvalRecordService.createRecord(
            approval.getId(), userId, operatorName,
            ApprovalActionEnum.REJECT.getCode(),
            dto.getComment(),
            ApprovalStatusEnum.REJECTED.getCode(),
            approval.getCurrentStep()
        );

        log.info("Approval rejected: approvalId={}, userId={}", approval.getId(), userId);
    }

    /**
     * Handle transfer action
     */
    private void handleTransfer(Approval approval, String userId, String operatorName, ApprovalActionDTO dto) {
        if (!userId.equals(approval.getApproverId())) {
            throw new BusinessException(ResultCode.APPROVAL_PERMISSION_DENIED);
        }

        if (!ApprovalStatusEnum.PENDING.getCode().equals(approval.getStatus())) {
            throw new BusinessException(ResultCode.APPROVAL_STATUS_ERROR);
        }

        if (StrUtil.isBlank(dto.getTransferToId())) {
            throw new BusinessException(ResultCode.BAD_REQUEST.getCode(), "转交目标用户不能为空");
        }

        SysUser newApprover = getUserById(dto.getTransferToId());
        if (newApprover == null) {
            throw new BusinessException(ResultCode.USER_NOT_FOUND.getCode(), "转交目标用户不存在");
        }

        String previousApproverId = approval.getApproverId();
        String previousApproverName = approval.getApproverName();

        approval.setApproverId(dto.getTransferToId());
        approval.setApproverName(newApprover.getNickname() != null ? newApprover.getNickname() : newApprover.getUsername());
        approval.setStatus(ApprovalStatusEnum.TRANSFERRED.getCode());
        approval.setUpdateBy(userId);

        approvalRecordService.createTransferRecord(
            approval.getId(), userId, operatorName,
            previousApproverId, dto.getTransferToId(),
            approval.getApproverName(),
            dto.getComment(),
            ApprovalStatusEnum.TRANSFERRED.getCode()
        );

        log.info("Approval transferred: approvalId={}, from={} to={}", approval.getId(), previousApproverId, dto.getTransferToId());
    }

    /**
     * Handle cancel action (withdraw by approver or applicant context)
     */
    private void handleCancel(Approval approval, String userId, String operatorName, ApprovalActionDTO dto) {
        // Applicant cancels their own submission
        if (!userId.equals(approval.getApplicantId())) {
            throw new BusinessException(ResultCode.APPROVAL_PERMISSION_DENIED);
        }

        if (!ApprovalStatusEnum.PENDING.getCode().equals(approval.getStatus())) {
            throw new BusinessException(ResultCode.APPROVAL_STATUS_ERROR);
        }

        approval.setStatus(ApprovalStatusEnum.CANCELLED.getCode());
        approval.setFinishTime(LocalDateTime.now());
        approval.setApprovalComment(dto.getComment());
        approval.setUpdateBy(userId);

        approvalRecordService.createRecord(
            approval.getId(), userId, operatorName,
            ApprovalActionEnum.CANCEL.getCode(),
            dto.getComment(),
            ApprovalStatusEnum.CANCELLED.getCode(),
            approval.getCurrentStep()
        );

        log.info("Approval cancelled: approvalId={}, userId={}", approval.getId(), userId);
    }

    /**
     * Check if user can view the approval
     */
    private boolean canView(String userId, Approval approval) {
        if (userId.equals(approval.getApplicantId())) return true;
        if (userId.equals(approval.getApproverId())) return true;
        if (StrUtil.isNotBlank(approval.getCcUsers())) {
            return Arrays.stream(approval.getCcUsers().split(","))
                .anyMatch(uid -> userId.equals(uid.trim()));
        }
        return false;
    }

    /**
     * Build ApprovalVO from entity with enriched descriptions
     */
    private ApprovalVO buildApprovalVO(Approval approval, String userId) {
        ApprovalVO vo = ApprovalVO.fromEntity(approval);

        // Enrich with descriptions
        ApprovalStatusEnum statusEnum = ApprovalStatusEnum.getByCode(approval.getStatus());
        vo.setStatusDesc(statusEnum != null ? statusEnum.getDescription() : "未知");

        ApprovalPriorityEnum priorityEnum = ApprovalPriorityEnum.getByCode(approval.getPriority());
        vo.setPriorityDesc(priorityEnum != null ? priorityEnum.getDescription() : "未知");

        vo.setTypeDesc(getTypeDescription(approval.getType()));

        return vo;
    }

    /**
     * Get human-readable type description
     */
    private String getTypeDescription(String type) {
        if (type == null) return "未知";
        return switch (type.toUpperCase()) {
            case "LEAVE" -> "请假";
            case "EXPENSE" -> "报销";
            case "PURCHASE" -> "采购";
            case "OVERTIME" -> "加班";
            case "TRAVEL" -> "出差";
            case "RECRUIT" -> "招聘";
            case "CONTRACT" -> "合同";
            default -> type;
        };
    }

    /**
     * Get user by ID (from system module)
     */
    private SysUser getUserById(String userId) {
        if (sysUserMapper != null) {
            return sysUserMapper.selectById(userId);
        }
        return null;
    }
}
