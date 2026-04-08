package com.aioa.workflow.service;

import com.aioa.workflow.dto.N8nWorkflowDTO;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/**
 * 工作流服务测试
 */
@ExtendWith(MockitoExtension.class)
public class N8nWorkflowServiceTest {
    
    @InjectMocks
    private N8nWorkflowServiceImpl n8nWorkflowService;
    
    @Test
    void testRegisterWorkflow() {
        N8nWorkflowDTO dto = new N8nWorkflowDTO();
        dto.setWorkflowId("test-001");
        dto.setName("测试工作流");
        dto.setType("approval");
        dto.setWebhookUrl("https://example.com/webhook");
        dto.setEnabled(true);
        
        boolean result = n8nWorkflowService.registerWorkflow(dto);
        assertTrue(result);
    }
    
    @Test
    void testTriggerWorkflow() {
        Map<String, Object> data = new HashMap<>();
        data.put("test", "data");
        
        String result = n8nWorkflowService.triggerWorkflow("test-trigger", data);
        assertNotNull(result);
    }
    
    @Test
    void testGetWorkflowStatus() {
        String status = n8nWorkflowService.getWorkflowStatus("test-status");
        assertNotNull(status);
    }
}