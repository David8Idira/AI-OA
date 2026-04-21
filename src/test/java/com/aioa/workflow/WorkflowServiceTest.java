package com.aioa.workflow;

import com.aioa.workflow.entity.WorkflowDefinition;
import com.aioa.workflow.entity.WorkflowInstance;
import com.aioa.workflow.repository.WorkflowDefinitionRepository;
import com.aioa.workflow.repository.WorkflowInstanceRepository;
import com.aioa.workflow.repository.WorkflowTaskRepository;
import com.aioa.workflow.service.WorkflowService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class WorkflowServiceTest {

    @Mock
    private WorkflowDefinitionRepository definitionRepo;

    @Mock
    private WorkflowInstanceRepository instanceRepo;

    @Mock
    private WorkflowTaskRepository taskRepo;

    @InjectMocks
    private WorkflowService workflowService;

    @Test
    void testGetDefinition_Found() {
        WorkflowDefinition def = new WorkflowDefinition();
        def.setId(1L);
        def.setName("Test Workflow");
        when(definitionRepo.findById(1L)).thenReturn(Optional.of(def));

        WorkflowDefinition result = workflowService.getDefinition(1L);
        assertNotNull(result);
        assertEquals(1L, result.getId());
    }

    @Test
    void testGetDefinition_NotFound() {
        when(definitionRepo.findById(999L)).thenReturn(Optional.empty());
        assertThrows(com.aioa.common.BusinessException.class, () -> workflowService.getDefinition(999L));
    }
}
