package com.aioa.reimburse.service;

import com.aioa.reimburse.dto.CreateReimburseDTO;
import com.aioa.reimburse.dto.OcrAutoFillDTO;
import com.aioa.reimburse.dto.ReimburseActionDTO;
import com.aioa.reimburse.dto.ReimburseQueryDTO;
import com.aioa.reimburse.entity.Reimburse;
import com.aioa.reimburse.entity.Invoice;
import com.aioa.reimburse.vo.OcrAutoFillVO;
import com.aioa.reimburse.vo.ReimburseVO;
import com.aioa.workflow.vo.PageResult;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.List;

/**
 * Reimburse Service Interface
 */
public interface ReimburseService extends IService<Reimburse> {

    /**
     * Create a new reimbursement request
     *
     * @param userId current user ID (applicant)
     * @param dto    create reimbursement DTO
     * @return created ReimburseVO
     */
    ReimburseVO createReimburse(String userId, CreateReimburseDTO dto);

    /**
     * Get reimbursement detail by ID
     *
     * @param reimburseId reimbursement ID
     * @param userId      current user ID (for permission check)
     * @return ReimburseVO with items
     */
    ReimburseVO getReimburseDetail(String reimburseId, String userId);

    /**
     * Query reimbursements with filters and pagination
     *
     * @param userId current user ID
     * @param query  query parameters
     * @return paginated reimbursement list
     */
    PageResult<ReimburseVO> queryReimburses(String userId, ReimburseQueryDTO query);

    /**
     * Delete (soft-delete) a reimbursement by ID
     *
     * @param reimburseId reimbursement ID
     * @param userId      current user ID (must be the applicant)
     * @return true if deleted successfully
     */
    boolean deleteReimburse(String reimburseId, String userId);

    /**
     * Perform action on a reimbursement (approve/reject/cancel)
     *
     * @param reimburseId reimbursement ID
     * @param userId      current user ID (approver or applicant)
     * @param dto         action DTO
     * @return updated ReimburseVO
     */
    ReimburseVO doAction(String reimburseId, String userId, ReimburseActionDTO dto);

    /**
     * OCR auto-fill: generate reimbursement preview from OCR data
     *
     * @param userId current user ID
     * @param dto    OCR auto-fill DTO
     * @return auto-fill preview VO
     */
    OcrAutoFillVO ocrAutoFill(String userId, OcrAutoFillDTO dto);

    /**
     * OCR auto-fill and create draft reimbursement
     *
     * @param userId current user ID
     * @param dto    OCR auto-fill DTO
     * @return created ReimburseVO
     */
    ReimburseVO ocrAutoFillAndCreate(String userId, OcrAutoFillDTO dto);

    /**
     * Get pending reimbursements for current user (as approver)
     *
     * @param userId   approver user ID
     * @param pageNum  page number
     * @param pageSize page size
     * @return paginated pending reimbursement list
     */
    PageResult<ReimburseVO> getPendingReimburses(String userId, Integer pageNum, Integer pageSize);

    /**
     * Get my submitted reimbursements
     *
     * @param userId   applicant user ID
     * @param pageNum  page number
     * @param pageSize page size
     * @return paginated reimbursement list
     */
    PageResult<ReimburseVO> getMyReimburses(String userId, Integer pageNum, Integer pageSize);

    /**
     * Count pending reimbursements for an approver
     *
     * @param approverId approver user ID
     * @return count
     */
    Long countPending(String approverId);

    /**
     * Verify an invoice
     *
     * @param invoiceId     invoice ID
     * @param userId         current user ID
     * @param verified       verified status (0-No, 1-Yes)
     * @param verifyRemark   verification remark
     * @return true if verified successfully
     */
    boolean verifyInvoice(String invoiceId, String userId, Integer verified, String verifyRemark);

    /**
     * Get invoice by ID
     *
     * @param invoiceId invoice ID
     * @return Invoice entity
     */
    Invoice getInvoiceById(String invoiceId);

    /**
     * Get invoices for a reimbursement
     *
     * @param reimburseId reimbursement ID
     * @return list of Invoice entities
     */
    List<Invoice> getInvoicesByReimburseId(String reimburseId);

    /**
     * Get reimbursement statistics for a user
     *
     * @param userId user ID
     * @return statistics map
     */
    java.util.Map<String, Object> getStatistics(String userId);
}
