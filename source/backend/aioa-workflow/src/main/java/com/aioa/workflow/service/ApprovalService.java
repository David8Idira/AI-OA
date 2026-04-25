package com.aioa.workflow.service;

import com.aioa.workflow.dto.ApprovalActionDTO;
import com.aioa.workflow.dto.ApprovalQueryDTO;
import com.aioa.workflow.dto.CreateApprovalDTO;
import com.aioa.workflow.entity.Approval;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;
import com.aioa.workflow.vo.ApprovalVO;
import com.aioa.common.vo.PageResult;

import java.util.List;

/**
 * Approval Service Interface
 */
public interface ApprovalService extends IService<Approval> {

    /**
     * Create a new approval request
     *
     * @param userId current user ID (applicant)
     * @param dto    create approval DTO
     * @return created ApprovalVO
     */
    ApprovalVO createApproval(String userId, CreateApprovalDTO dto);

    /**
     * Get approval details by ID
     *
     * @param approvalId approval ID
     * @param userId     current user ID (for permission check)
     * @return ApprovalVO with records
     */
    ApprovalVO getApprovalDetail(String approvalId, String userId);

    /**
     * Query approvals with filters and pagination
     *
     * @param userId current user ID
     * @param query  query parameters
     * @return paginated approval list
     */
    PageResult<ApprovalVO> queryApprovals(String userId, ApprovalQueryDTO query);

    /**
     * Perform action on an approval (approve/reject/transfer/cancel)
     *
     * @param approvalId approval ID
     * @param userId     current user ID (approver or applicant)
     * @param dto        action DTO
     * @return updated ApprovalVO
     */
    ApprovalVO doAction(String approvalId, String userId, ApprovalActionDTO dto);

    /**
     * Cancel (withdraw) an approval by the applicant
     *
     * @param approvalId approval ID
     * @param userId     current user ID (must be the applicant)
     * @param reason     cancellation reason
     * @return true if cancelled successfully
     */
    boolean cancelApproval(String approvalId, String userId, String reason);

    /**
     * Get pending approvals for a specific approver
     *
     * @param approverId approver user ID
     * @param pageNum    page number
     * @param pageSize   page size
     * @return paginated pending approval list
     */
    PageResult<ApprovalVO> getPendingApprovals(String approverId, Integer pageNum, Integer pageSize);

    /**
     * Get my submitted approvals
     *
     * @param applicantId applicant user ID
     * @param pageNum     page number
     * @param pageSize    page size
     * @return paginated approval list
     */
    PageResult<ApprovalVO> getMyApprovals(String applicantId, Integer pageNum, Integer pageSize);

    /**
     * Count pending approvals for an approver
     *
     * @param approverId approver user ID
     * @return count
     */
    Long countPending(String approverId);

    /**
     * Reassign approver for an approval
     *
     * @param approvalId       approval ID
     * @param operatorId       operator user ID (must have permission)
     * @param newApproverId    new approver user ID
     * @param reason           reassignment reason
     * @return updated ApprovalVO
     */
    ApprovalVO reassignApprover(String approvalId, String operatorId, String newApproverId, String reason);

    /**
     * Get approval statistics for a user
     *
     * @param userId user ID
     * @return statistics map
     */
    java.util.Map<String, Object> getStatistics(String userId);
}
