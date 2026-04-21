package com.aioa.workflow.repository;

import com.aioa.workflow.entity.WorkflowDefinition;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface WorkflowDefinitionRepository extends JpaRepository<WorkflowDefinition, Long> {
    Optional<WorkflowDefinition> findByWorkflowKey(String workflowKey);
    List<WorkflowDefinition> findByActiveTrue();
    boolean existsByWorkflowKey(String workflowKey);
}
