package com.aioa.workflow.mapper;

import com.aioa.workflow.entity.Approval;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * Approval Mapper
 */
@Mapper
public interface ApprovalMapper extends BaseMapper<Approval> {

    /**
     * Find approvals by applicant ID (paginated)
     */
    IPage<Approval> selectByApplicantId(Page<Approval> page, @Param("applicantId") String applicantId);

    /**
     * Find pending approvals by approver ID (paginated)
     */
    IPage<Approval> selectPendingByApproverId(Page<Approval> page, @Param("approverId") String approverId);

    /**
     * Find approvals by status (paginated)
     */
    IPage<Approval> selectByStatus(Page<Approval> page, @Param("status") Integer status);

    /**
     * Search approvals by keyword across title and content (paginated)
     */
    IPage<Approval> searchByKeyword(Page<Approval> page, @Param("keyword") String keyword,
                                    @Param("applicantId") String applicantId,
                                    @Param("approverId") String approverId);

    /**
     * Count approvals by applicant
     */
    Long countByApplicantId(@Param("applicantId") String applicantId);

    /**
     * Count pending approvals by approver
     */
    Long countPendingByApproverId(@Param("approverId") String approverId);

    /**
     * Find overdue approvals (pending and past expected finish time)
     */
    List<Approval> selectOverdue();
}
