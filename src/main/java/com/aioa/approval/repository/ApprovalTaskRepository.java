package com.aioa.approval.repository;

import com.aioa.approval.entity.ApprovalTask;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * 审批任务Repository
 */
@Repository
public interface ApprovalTaskRepository extends JpaRepository<ApprovalTask, Long> {

    List<ApprovalTask> findByInstanceId(Long instanceId);

    Optional<ApprovalTask> findByInstanceIdAndApproverIdAndStatus(Long instanceId, Long approverId, Integer status);

    List<ApprovalTask> findByApproverIdAndStatus(Long approverId, Integer status);
}
