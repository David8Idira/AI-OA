package com.aioa.reimburse.controller;

import com.aioa.common.annotation.Login;
import com.aioa.common.result.Result;
import com.aioa.reimburse.dto.CreateReimburseDTO;
import com.aioa.reimburse.dto.OcrAutoFillDTO;
import com.aioa.reimburse.dto.ReimburseActionDTO;
import com.aioa.reimburse.dto.ReimburseQueryDTO;
import com.aioa.reimburse.entity.Invoice;
import com.aioa.reimburse.service.ReimburseService;
import com.aioa.reimburse.vo.OcrAutoFillVO;
import com.aioa.reimburse.vo.ReimburseVO;
import com.aioa.workflow.vo.PageResult;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * Reimburse Controller
 * Provides REST APIs for reimbursement management
 */
@Slf4j
@RestController
@RequestMapping("/api/v1/reimburse")
@RequiredArgsConstructor
@Tag(name = "Reimburse Management", description = "Reimbursement management APIs including OCR auto-fill")
public class ReimburseController {

    private final ReimburseService reimburseService;

    /**
     * POST /api/v1/reimburse
     * Submit a new reimbursement request
     */
    @PostMapping
    @Operation(summary = "Submit reimbursement", description = "Submit a new reimbursement request with items")
    @Login
    public Result<ReimburseVO> createReimburse(
            @RequestAttribute("userId") String userId,
            @Valid @RequestBody CreateReimburseDTO dto) {
        log.info("Create reimburse: userId={}, type={}, title={}", userId, dto.getType(), dto.getTitle());
        ReimburseVO vo = reimburseService.createReimburse(userId, dto);
        return Result.success("报销提交成功", vo);
    }

    /**
     * GET /api/v1/reimburse
     * Query reimbursement list with filters and pagination
     */
    @GetMapping
    @Operation(summary = "Query reimbursement list", description = "Query reimbursements with filters, supports pagination and mode (MY_APPLY/MY_APPROVE)")
    @Login
    public Result<PageResult<ReimburseVO>> listReimburses(
            @RequestAttribute("userId") String userId,
            @ModelAttribute ReimburseQueryDTO query) {
        log.info("List reimbursements: userId={}, query={}", userId, query);
        PageResult<ReimburseVO> result = reimburseService.queryReimburses(userId, query);
        return Result.success(result);
    }

    /**
     * GET /api/v1/reimburse/{id}
     * Get reimbursement detail by ID
     */
    @GetMapping("/{id}")
    @Operation(summary = "Get reimbursement detail", description = "Get detailed reimbursement info including items")
    @Login
    public Result<ReimburseVO> getReimburseDetail(
            @Parameter(description = "Reimbursement ID") @PathVariable("id") String reimburseId,
            @RequestAttribute("userId") String userId) {
        log.info("Get reimburse detail: reimburseId={}, userId={}", reimburseId, userId);
        ReimburseVO vo = reimburseService.getReimburseDetail(reimburseId, userId);
        return Result.success(vo);
    }

    /**
     * DELETE /api/v1/reimburse/{id}
     * Delete (withdraw) a reimbursement by ID
     */
    @DeleteMapping("/{id}")
    @Operation(summary = "Delete reimbursement", description = "Delete a reimbursement (only draft or pending, only by applicant)")
    @Login
    public Result<Void> deleteReimburse(
            @Parameter(description = "Reimbursement ID") @PathVariable("id") String reimburseId,
            @RequestAttribute("userId") String userId) {
        log.info("Delete reimburse: reimburseId={}, userId={}", reimburseId, userId);
        boolean success = reimburseService.deleteReimburse(reimburseId, userId);
        return success ? Result.success("报销已删除") : Result.error();
    }

    /**
     * POST /api/v1/reimburse/{id}/action
     * Perform action on reimbursement (approve/reject/cancel)
     */
    @PostMapping("/{id}/action")
    @Operation(summary = "Perform reimburse action", description = "Approve, reject, cancel, or request extra info for a reimbursement")
    @Login
    public Result<ReimburseVO> doAction(
            @Parameter(description = "Reimbursement ID") @PathVariable("id") String reimburseId,
            @RequestAttribute("userId") String userId,
            @Valid @RequestBody ReimburseActionDTO dto) {
        log.info("Reimburse action: reimburseId={}, userId={}, action={}", reimburseId, userId, dto.getActionType());
        ReimburseVO vo = reimburseService.doAction(reimburseId, userId, dto);
        return Result.success(vo);
    }

    /**
     * POST /api/v1/reimburse/ocr-auto-fill
     * OCR auto-fill: preview how OCR data maps to reimbursement form
     */
    @PostMapping("/ocr-auto-fill")
    @Operation(summary = "OCR auto-fill preview", description = "Preview auto-fill result from OCR recognition record")
    @Login
    public Result<OcrAutoFillVO> ocrAutoFill(
            @RequestAttribute("userId") String userId,
            @Valid @RequestBody OcrAutoFillDTO dto) {
        log.info("OCR auto-fill: userId={}, ocrRecordId={}", userId, dto.getOcrRecordId());
        OcrAutoFillVO vo = reimburseService.ocrAutoFill(userId, dto);
        return Result.success(vo);
    }

    /**
     * POST /api/v1/reimburse/ocr-auto-fill/create
     * OCR auto-fill and create draft reimbursement
     */
    @PostMapping("/ocr-auto-fill/create")
    @Operation(summary = "OCR auto-fill and create", description = "Auto-fill from OCR and create reimbursement directly")
    @Login
    public Result<ReimburseVO> ocrAutoFillAndCreate(
            @RequestAttribute("userId") String userId,
            @Valid @RequestBody OcrAutoFillDTO dto) {
        log.info("OCR auto-fill create: userId={}, ocrRecordId={}", userId, dto.getOcrRecordId());
        ReimburseVO vo = reimburseService.ocrAutoFillAndCreate(userId, dto);
        return Result.success("OCR自动填单成功，报销已创建", vo);
    }

    /**
     * GET /api/v1/reimburse/pending
     * Get pending reimbursements for current user (as approver)
     */
    @GetMapping("/pending")
    @Operation(summary = "Get pending reimbursements", description = "Get all pending reimbursements for current user as approver")
    @Login
    public Result<PageResult<ReimburseVO>> getPendingReimburses(
            @RequestAttribute("userId") String userId,
            @Parameter(description = "Page number") @RequestParam(value = "pageNum", defaultValue = "1") Integer pageNum,
            @Parameter(description = "Page size") @RequestParam(value = "pageSize", defaultValue = "10") Integer pageSize) {
        log.info("Get pending reimbursements: userId={}", userId);
        PageResult<ReimburseVO> result = reimburseService.getPendingReimburses(userId, pageNum, pageSize);
        return Result.success(result);
    }

    /**
     * GET /api/v1/reimburse/my
     * Get my submitted reimbursements
     */
    @GetMapping("/my")
    @Operation(summary = "Get my reimbursements", description = "Get all reimbursements submitted by current user")
    @Login
    public Result<PageResult<ReimburseVO>> getMyReimburses(
            @RequestAttribute("userId") String userId,
            @Parameter(description = "Page number") @RequestParam(value = "pageNum", defaultValue = "1") Integer pageNum,
            @Parameter(description = "Page size") @RequestParam(value = "pageSize", defaultValue = "10") Integer pageSize) {
        log.info("Get my reimbursements: userId={}", userId);
        PageResult<ReimburseVO> result = reimburseService.getMyReimburses(userId, pageNum, pageSize);
        return Result.success(result);
    }

    /**
     * GET /api/v1/reimburse/pending/count
     * Get count of pending reimbursements for current user
     */
    @GetMapping("/pending/count")
    @Operation(summary = "Get pending reimbursement count", description = "Get the number of pending reimbursements for current user")
    @Login
    public Result<Long> getPendingCount(
            @RequestAttribute("userId") String userId) {
        Long count = reimburseService.countPending(userId);
        return Result.success(count);
    }

    /**
     * GET /api/v1/reimburse/{id}/invoices
     * Get invoices for a reimbursement
     */
    @GetMapping("/{id}/invoices")
    @Operation(summary = "Get reimbursement invoices", description = "Get all invoice records for a reimbursement")
    @Login
    public Result<List<Invoice>> getInvoices(
            @Parameter(description = "Reimbursement ID") @PathVariable("id") String reimburseId,
            @RequestAttribute("userId") String userId) {
        log.info("Get invoices: reimburseId={}, userId={}", reimburseId, userId);
        List<Invoice> invoices = reimburseService.getInvoicesByReimburseId(reimburseId);
        return Result.success(invoices);
    }

    /**
     * POST /api/v1/reimburse/invoices/{invoiceId}/verify
     * Verify an invoice
     */
    @PostMapping("/invoices/{invoiceId}/verify")
    @Operation(summary = "Verify invoice", description = "Mark an invoice as verified or rejected")
    @Login
    public Result<Void> verifyInvoice(
            @Parameter(description = "Invoice ID") @PathVariable("invoiceId") String invoiceId,
            @RequestAttribute("userId") String userId,
            @Parameter(description = "Verified: 0-No, 1-Yes") @RequestParam("verified") Integer verified,
            @Parameter(description = "Verification remark") @RequestParam(value = "remark", required = false) String remark) {
        log.info("Verify invoice: invoiceId={}, userId={}, verified={}", invoiceId, userId, verified);
        boolean success = reimburseService.verifyInvoice(invoiceId, userId, verified, remark);
        return success ? Result.success("发票核验成功") : Result.error();
    }

    /**
     * GET /api/v1/reimburse/statistics
     * Get reimbursement statistics for current user
     */
    @GetMapping("/statistics")
    @Operation(summary = "Get reimbursement statistics", description = "Get reimbursement statistics for current user")
    @Login
    public Result<Map<String, Object>> getStatistics(
            @RequestAttribute("userId") String userId) {
        log.info("Get statistics: userId={}", userId);
        Map<String, Object> stats = reimburseService.getStatistics(userId);
        return Result.success(stats);
    }
}
