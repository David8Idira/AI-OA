package com.aioa.workflow.mapper;

import com.aioa.workflow.entity.ApprovalRecord;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * Approval Record Mapper
 */
@Mapper
public interface ApprovalRecordMapper extends BaseMapper<ApprovalRecord> {

    /**
     * Find all records for an approval, ordered by create time ascending
     */
    List<ApprovalRecord> selectByApprovalIdOrderByCreateTimeAsc(@Param("approvalId") String approvalId);

    /**
     * Find all records for an approval, ordered by create time descending
     */
    List<ApprovalRecord> selectByApprovalIdOrderByCreateTimeDesc(@Param("approvalId") String approvalId);

    /**
     * Find the latest record for an approval
     */
    ApprovalRecord selectLatestByApprovalId(@Param("approvalId") String approvalId);

    /**
     * Find records by operator ID
     */
    List<ApprovalRecord> selectByOperatorId(@Param("operatorId") String operatorId);

    /**
     * Find records by action type
     */
    List<ApprovalRecord> selectByApprovalIdAndActionType(@Param("approvalId") String approvalId,
                                                         @Param("actionType") Integer actionType);

    /**
     * Count records by approval ID
     */
    Long countByApprovalId(@Param("approvalId") String approvalId);
}
