package com.aioa.mapper;

import com.aioa.entity.ApprovalProcess;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * 审批流程Mapper接口
 */
@Repository
public interface ApprovalProcessMapper extends JpaRepository<ApprovalProcess, Long> {
    
    Optional<ApprovalProcess> findByProcessKey(String processKey);
    
    List<ApprovalProcess> findByCategory(String category);
    
    List<ApprovalProcess> findByStatus(String status);
    
    List<ApprovalProcess> findByCreatedBy(String createdBy);
    
    @Query("SELECT ap FROM ApprovalProcess ap WHERE " +
           "(:processName IS NULL OR ap.processName LIKE %:processName%) AND " +
           "(:category IS NULL OR ap.category = :category) AND " +
           "(:status IS NULL OR ap.status = :status)")
    List<ApprovalProcess> searchProcesses(
            @Param("processName") String processName,
            @Param("category") String category,
            @Param("status") String status);
    
    boolean existsByProcessKey(String processKey);
    
    long countByStatus(String status);
}