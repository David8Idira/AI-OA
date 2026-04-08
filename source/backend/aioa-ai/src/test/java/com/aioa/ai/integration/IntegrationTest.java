package com.aioa.workflow.integration;

import com.aioa.ai.client.MimoApiClient;
import com.aioa.workflow.service.N8nWorkflowService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

/**
 * 集成测试配置
 */
@SpringBootTest
public class IntegrationTest {
    
    @Autowired
    private MimoApiClient mimoApiClient;
    
    @Autowired
    private N8nWorkflowService workflowService;
    
    /**
     * 测试AI服务
     */
    @Test
    void testAiService() {
        // 配置有效API Key时会调用真实服务
        boolean connected = mimoApiClient.testConnection();
        System.out.println("AI服务连接: " + (connected ? "成功" : "降级模式"));
    }
    
    /**
     * 测试工作流服务
     */
    @Test
    void testWorkflowService() {
        String status = workflowService.getWorkflowStatus("default");
        System.out.println("工作流状态: " + status);
    }
    
    /**
     * 测试Mimo API
     */
    @Test
    void testMimoChat() {
        String response = mimoApiClient.chat("你好");
        System.out.println("Mimo回复: " + response.substring(0, 50) + "...");
    }
}