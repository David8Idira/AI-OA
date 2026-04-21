package com.aioa.workflow.controller;

import com.aioa.common.annotation.Login;
import com.aioa.common.result.Result;
import com.aioa.workflow.dto.ApprovalActionDTO;
import com.aioa.workflow.dto.ApprovalQueryDTO;
import com.aioa.workflow.dto.CreateApprovalDTO;
import com.aioa.workflow.service.ApprovalService;
import com.aioa.workflow.vo.ApprovalVO;
import com.aioa.workflow.vo.PageResult;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * Approval Controller
 * Provides REST APIs for approval workflow management
 */
@Slf4j
@RestController
@RequestMapping("/api/v1/approvals")
@RequiredArgsConstructor
@Tag(name = "Approval Workflow", description = "Approval workflow management APIs")
public class ApprovalController {

    private final ApprovalService approvalService;

    /**
     * GET /api/v1/approvals
     * Query approvals with filters and pagination
     */
    @GetMapping
    @Operation(summary = "Query approval list", description = "Query approvals with filters, supports pagination and mode (MY_APPLY/MY_APPROVE)")
    @Login
    public Result<PageResult<ApprovalVO>> listApprovals(
            @RequestAttribute("userId") String userId,
            @ModelAttribute ApprovalQueryDTO query) {
        log.info("List approvals: userId={}, query={}", userId, query);
        PageResult<ApprovalVO> result = approvalService.queryApprovals(userId, query);
        return Result.success(result);
    }

    /**
     * GET /api/v1/approvals/{id}
     * Get approval detail by ID
     */
    @GetMapping("/{id}")
    @Operation(summary = "Get approval detail", description = "Get detailed approval info including approval history records")
    @Login
    public Result<ApprovalVO> getApprovalDetail(
            @Parameter(description = "Approval ID") @PathVariable("id") String approvalId,
            @RequestAttribute("userId") String userId) {
        log.info("Get approval detail: approvalId={}, userId={}", approvalId, userId);
        ApprovalVO vo = approvalService.getApprovalDetail(approvalId, userId);
        return Result.success(vo);
    }

    /**
     * POST /api/v1/approvals
     * Create a new approval request
     */
    @PostMapping
    @Operation(summary = "Create approval", description = "Submit a new approval request")
    @Login
    public Result<ApprovalVO> createApproval(
            @RequestAttribute("userId") String userId,
            @Valid @RequestBody CreateApprovalDTO dto) {
        log.info("Create approval: userId={}, type={}, title={}", userId, dto.getType(), dto.getTitle());
        ApprovalVO vo = approvalService.createApproval(userId, dto);
        return Result.success("审批提交成功", vo);
    }

    /**
     * POST /api/v1/approvals/{id}/action
     * Perform action on approval (approve/reject/transfer/cancel)
     */
    @PostMapping("/{id}/action")
    @Operation(summary = "Perform approval action", description = "Approve, reject, transfer, or cancel an approval")
    @Login
    public Result<ApprovalVO> doAction(
            @Parameter(description = "Approval ID") @PathVariable("id") String approvalId,
            @RequestAttribute("userId") String userId,
            @Valid @RequestBody ApprovalActionDTO dto) {
        log.info("Approval action: approvalId={}, userId={}, action={}", approvalId, userId, dto.getActionType());
        ApprovalVO vo = approvalService.doAction(approvalId, userId, dto);
        return Result.success(vo);
    }

    /**
     * POST /api/v1/approvals/{id}/cancel
     * Cancel (withdraw) an approval by applicant
     */
    @PostMapping("/{id}/cancel")
    @Operation(summary = "Cancel approval", description = "Applicant cancels their own pending approval")
    @Login
    public Result<Void> cancelApproval(
            @Parameter(description = "Approval ID") @PathVariable("id") String approvalId,
            @RequestAttribute("userId") String userId,
            @RequestParam(value = "reason", required = false) String reason) {
        log.info("Cancel approval: approvalId={}, userId={}", approvalId, userId);
        boolean success = approvalService.cancelApproval(approvalId, userId, reason);
        return success ? Result.<Void>success("审批已撤回", null) : Result.<Void>error();
    }

    /**
     * GET /api/v1/approvals/pending
     * Get pending approvals for current user (as approver)
     */
    @GetMapping("/pending")
    @Operation(summary = "Get pending approvals", description = "Get all pending approvals for the current user as approver")
    @Login
    public Result<PageResult<ApprovalVO>> getPendingApprovals(
            @RequestAttribute("userId") String userId,
            @Parameter(description = "Page number") @RequestParam(value = "pageNum", defaultValue = "1") Integer pageNum,
            @Parameter(description = "Page size") @RequestParam(value = "pageSize", defaultValue = "10") Integer pageSize) {
        log.info("Get pending approvals: userId={}", userId);
        PageResult<ApprovalVO> result = approvalService.getPendingApprovals(userId, pageNum, pageSize);
        return Result.success(result);
    }

    /**
     * GET /api/v1/approvals/my
     * Get my submitted approvals
     */
    @GetMapping("/my")
    @Operation(summary = "Get my approvals", description = "Get all approvals submitted by current user")
    @Login
    public Result<PageResult<ApprovalVO>> getMyApprovals(
            @RequestAttribute("userId") String userId,
            @Parameter(description = "Page number") @RequestParam(value = "pageNum", defaultValue = "1") Integer pageNum,
            @Parameter(description = "Page size") @RequestParam(value = "pageSize", defaultValue = "10") Integer pageSize) {
        log.info("Get my approvals: userId={}", userId);
        PageResult<ApprovalVO> result = approvalService.getMyApprovals(userId, pageNum, pageSize);
        return Result.success(result);
    }

    /**
     * GET /api/v1/approvals/{id}/statistics
     * Get approval statistics for current user
     */
    @GetMapping("/statistics")
    @Operation(summary = "Get approval statistics", description = "Get approval statistics for the current user")
    @Login
    public Result<Map<String, Object>> getStatistics(
            @RequestAttribute("userId") String userId) {
        log.info("Get statistics: userId={}", userId);
        Map<String, Object> stats = approvalService.getStatistics(userId);
        return Result.success(stats);
    }

    /**
     * GET /api/v1/approvals/pending/count
     * Get count of pending approvals for current user
     */
    @GetMapping("/pending/count")
    @Operation(summary = "Get pending approval count", description = "Get the number of pending approvals for current user")
    @Login
    public Result<Long> getPendingCount(
            @RequestAttribute("userId") String userId) {
        Long count = approvalService.countPending(userId);
        return Result.success(count);
    }

    /**
     * POST /api/v1/approvals/{id}/reassign
     * Reassign approver for an approval
     */
    @PostMapping("/{id}/reassign")
    @Operation(summary = "Reassign approver", description = "Reassign the approver for an existing approval")
    @Login
    public Result<ApprovalVO> reassignApprover(
            @Parameter(description = "Approval ID") @PathVariable("id") String approvalId,
            @RequestAttribute("userId") String userId,
            @RequestParam("newApproverId") String newApproverId,
            @RequestParam(value = "reason", required = false) String reason) {
        log.info("Reassign approver: approvalId={}, userId={}, newApproverId={}", approvalId, userId, newApproverId);
        ApprovalVO vo = approvalService.reassignApprover(approvalId, userId, newApproverId, reason);
        return Result.success("审批人已变更", vo);
    }
}
