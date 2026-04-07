package com.aioa.workflow.service;

import com.aioa.workflow.entity.ApprovalRecord;
import com.baomidou.mybatisplus.extension.service.IService;
import com.aioa.workflow.vo.ApprovalRecordVO;

import java.util.List;

/**
 * Approval Record Service Interface
 */
public interface ApprovalRecordService extends IService<ApprovalRecord> {

    /**
     * Get all records for an approval
     *
     * @param approvalId approval ID
     * @return list of record VOs
     */
    List<ApprovalRecordVO> getRecordsByApprovalId(String approvalId);

    /**
     * Create an approval record for an action
     *
     * @param approvalId approval ID
     * @param operatorId operator user ID
     * @param operatorName operator name
     * @param actionType action type code
     * @param comment comment
     * @param statusAfter status after action
     * @param step step number
     * @return created record
     */
    ApprovalRecord createRecord(String approvalId, String operatorId, String operatorName,
                                 Integer actionType, String comment, Integer statusAfter, Integer step);

    /**
     * Create a transfer record
     *
     * @param approvalId approval ID
     * @param operatorId transferrer user ID
     * @param operatorName transferrer name
     * @param fromApproverId previous approver ID
     * @param toApproverId new approver ID
     * @param toApproverName new approver name
     * @param comment transfer reason
     * @param statusAfter status after transfer
     * @return created record
     */
    ApprovalRecord createTransferRecord(String approvalId, String operatorId, String operatorName,
                                          String fromApproverId, String toApproverId,
                                          String toApproverName, String comment, Integer statusAfter);

    /**
     * Count records by approval ID
     *
     * @param approvalId approval ID
     * @return count
     */
    Long countByApprovalId(String approvalId);
}
