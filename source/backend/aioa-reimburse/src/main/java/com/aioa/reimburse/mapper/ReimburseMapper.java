package com.aioa.reimburse.mapper;

import com.aioa.reimburse.entity.Reimburse;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * Reimburse Mapper
 */
@Mapper
public interface ReimburseMapper extends BaseMapper<Reimburse> {

    /**
     * Find reimbursements by applicant ID (paginated)
     */
    IPage<Reimburse> selectByApplicantId(Page<Reimburse> page, @Param("applicantId") String applicantId);

    /**
     * Find reimbursements by approver ID (paginated)
     */
    IPage<Reimburse> selectPendingByApproverId(Page<Reimburse> page, @Param("approverId") String approverId);

    /**
     * Find reimbursements by status (paginated)
     */
    IPage<Reimburse> selectByStatus(Page<Reimburse> page, @Param("status") Integer status);

    /**
     * Search reimbursements by keyword (title, description)
     */
    IPage<Reimburse> searchByKeyword(Page<Reimburse> page, @Param("keyword") String keyword,
                                      @Param("applicantId") String applicantId,
                                      @Param("approverId") String approverId,
                                      @Param("type") String type,
                                      @Param("status") Integer status);

    /**
     * Count by applicant ID
     */
    Long countByApplicantId(@Param("applicantId") String applicantId);

    /**
     * Count pending by approver ID
     */
    Long countPendingByApproverId(@Param("approverId") String approverId);

    /**
     * Find overdue reimbursements (pending and past expected pay date)
     */
    List<Reimburse> selectOverdue();

    /**
     * Find all by IDs (for batch queries)
     */
    List<Reimburse> selectByIds(@Param("ids") List<String> ids);
}
