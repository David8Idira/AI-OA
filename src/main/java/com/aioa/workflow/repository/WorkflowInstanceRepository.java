package com.aioa.workflow.repository;

import com.aioa.workflow.entity.WorkflowInstance;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface WorkflowInstanceRepository extends JpaRepository<WorkflowInstance, Long> {
    List<WorkflowInstance> findByDefinitionIdOrderByStartedAtDesc(Long definitionId);
    List<WorkflowInstance> findByStartedByOrderByStartedAtDesc(Long startedBy);
    List<WorkflowInstance> findByStatusOrderByStartedAtDesc(Integer status);
}
