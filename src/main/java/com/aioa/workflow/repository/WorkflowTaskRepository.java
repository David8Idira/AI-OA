package com.aioa.workflow.repository;

import com.aioa.workflow.entity.WorkflowTask;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface WorkflowTaskRepository extends JpaRepository<WorkflowTask, Long> {
    List<WorkflowTask> findByInstanceIdOrderByCreatedAtAsc(Long instanceId);
    List<WorkflowTask> findByAssigneeIdAndStatusOrderByCreatedAtAsc(Long assigneeId, Integer status);
    List<WorkflowTask> findByStatusOrderByCreatedAtAsc(Integer status);
}
