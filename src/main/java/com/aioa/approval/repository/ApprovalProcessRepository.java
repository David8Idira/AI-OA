package com.aioa.approval.repository;

import com.aioa.approval.entity.ApprovalProcess;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * 审批流程Repository
 */
@Repository
public interface ApprovalProcessRepository extends JpaRepository<ApprovalProcess, Long> {

    Optional<ApprovalProcess> findByCode(String code);

    boolean existsByCode(String code);

    List<ApprovalProcess> findByType(String type);

    List<ApprovalProcess> findByStatus(Integer status);
}
