package com.aioa.approval.repository;

import com.aioa.approval.entity.ApprovalInstance;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * 审批实例Repository
 */
@Repository
public interface ApprovalInstanceRepository extends JpaRepository<ApprovalInstance, Long> {

    List<ApprovalInstance> findByApplicantId(Long applicantId);

    List<ApprovalInstance> findByProcessId(Long processId);

    List<ApprovalInstance> findByStatus(Integer status);

    List<ApprovalInstance> findByApplicantIdAndStatus(Long applicantId, Integer status);
}
