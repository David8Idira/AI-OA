package com.aioa.mapper;

import com.aioa.entity.ApprovalInstance;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * 审批实例Mapper接口
 */
@Repository
public interface ApprovalInstanceMapper extends JpaRepository<ApprovalInstance, Long> {
    
    Optional<ApprovalInstance> findByInstanceNo(String instanceNo);
    
    List<ApprovalInstance> findByProcessId(Long processId);
    
    List<ApprovalInstance> findByApplicant(String applicant);
    
    List<ApprovalInstance> findByStatus(String status);
    
    @Query("SELECT ai FROM ApprovalInstance ai WHERE ai.applicantId = :applicantId AND ai.status = :status")
    List<ApprovalInstance> findByApplicantIdAndStatus(
            @Param("applicantId") Long applicantId,
            @Param("status") String status);
    
    @Query("SELECT ai FROM ApprovalInstance ai WHERE ai.status = :status AND ai.startTime BETWEEN :startDate AND :endDate")
    List<ApprovalInstance> findByStatusAndDateRange(
            @Param("status") String status,
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate);
    
    @Query("SELECT ai FROM ApprovalInstance ai WHERE ai.currentNodeName = :nodeName AND ai.status = 'RUNNING'")
    List<ApprovalInstance> findRunningByNodeName(@Param("nodeName") String nodeName);
    
    long countByStatus(String status);
    
    long countByApplicantIdAndStatus(Long applicantId, String status);
}