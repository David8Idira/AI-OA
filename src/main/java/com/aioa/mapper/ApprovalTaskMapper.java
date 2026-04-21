package com.aioa.mapper;

import com.aioa.entity.ApprovalTask;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 审批任务Mapper接口
 */
@Repository
public interface ApprovalTaskMapper extends JpaRepository<ApprovalTask, Long> {
    
    List<ApprovalTask> findByInstanceId(Long instanceId);
    
    List<ApprovalTask> findByAssignee(String assignee);
    
    List<ApprovalTask> findByStatus(String status);
    
    List<ApprovalTask> findByProcessId(Long processId);
    
    @Query("SELECT at FROM ApprovalTask at WHERE at.assignee = :assignee AND at.status = :status")
    List<ApprovalTask> findByAssigneeAndStatus(
            @Param("assignee") String assignee,
            @Param("status") String status);
    
    @Query("SELECT at FROM ApprovalTask at WHERE at.dueDate < :date AND at.status = 'PENDING'")
    List<ApprovalTask> findOverdueTasks(@Param("date") LocalDateTime date);
    
    @Query("SELECT at FROM ApprovalTask at WHERE at.assignee = :assignee AND at.status IN :statuses")
    List<ApprovalTask> findByAssigneeAndStatusIn(
            @Param("assignee") String assignee,
            @Param("statuses") List<String> statuses);
    
    long countByStatus(String status);
    
    long countByAssigneeAndStatus(String assignee, String status);
}