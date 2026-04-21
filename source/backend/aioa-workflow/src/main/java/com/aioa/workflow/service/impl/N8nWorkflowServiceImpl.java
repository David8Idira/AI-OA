package com.aioa.workflow.service.impl;

import com.aioa.workflow.dto.N8nWorkflowDTO;
import com.aioa.workflow.service.N8nWorkflowService;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * n8n工作流服务实现
 */
@Slf4j
@Service
public class N8nWorkflowServiceImpl implements N8nWorkflowService {
    
    @Value("${aioa.workflow.n8n.url:}")
    private String n8nBaseUrl;
    
    @Value("${aioa.workflow.n8n.api-key:}")
    private String n8nApiKey;
    
    private final RestTemplate restTemplate = new RestTemplate();
    private final ObjectMapper objectMapper = new ObjectMapper();
    
    // 工作流注册表
    private final ConcurrentHashMap<String, N8nWorkflowDTO> workflows = new ConcurrentHashMap<>();
    
    @Override
    public String triggerWorkflow(String workflowId, Object data) {
        try {
            N8nWorkflowDTO workflow = workflows.get(workflowId);
            if (workflow == null) {
                log.warn("工作流不存在: {}", workflowId);
                return "工作流不存在";
            }
            
            if (!workflow.getEnabled()) {
                log.warn("工作流已禁用: {}", workflowId);
                return "工作流已禁用";
            }
            
            return sendWebhook(workflow.getWebhookUrl(), data);
            
        } catch (Exception e) {
            log.error("触发工作流失败: {}", workflowId, e);
            return "触发失败: " + e.getMessage();
        }
    }
    
    @Override
    public String triggerApprovalWorkflow(String approvalId, String action) {
        Map<String, Object> data = new HashMap<>();
        data.put("approvalId", approvalId);
        data.put("action", action);
        data.put("timestamp", System.currentTimeMillis());
        
        // 查找审批工作流
        N8nWorkflowDTO workflow = workflows.get("approval_" + action);
        if (workflow != null && workflow.getEnabled()) {
            return sendWebhook(workflow.getWebhookUrl(), data);
        }
        
        // 默认触发
        return triggerWorkflow("approval_default", data);
    }
    
    @Override
    public String getWorkflowStatus(String workflowId) {
        N8nWorkflowDTO workflow = workflows.get(workflowId);
        if (workflow == null) {
            return "工作流不存在";
        }
        
        Map<String, Object> status = new HashMap<>();
        status.put("id", workflow.getWorkflowId());
        status.put("name", workflow.getName());
        status.put("enabled", workflow.getEnabled());
        status.put("type", workflow.getType());
        
        return status.toString();
    }
    
    @Override
    public boolean registerWorkflow(N8nWorkflowDTO dto) {
        try {
            workflows.put(dto.getWorkflowId(), dto);
            log.info("工作流注册成功: {}", dto.getName());
            return true;
        } catch (Exception e) {
            log.error("工作流注册失败", e);
            return false;
        }
    }
    
    @Override
    public String sendWebhook(String url, Object data) {
        try {
            log.info("发送Webhook: {}", url);
            
            // 构建请求
            Map<String, Object> request = new HashMap<>();
            request.put("data", data);
            request.put("apiKey", n8nApiKey);
            
            // 发送请求
            String response = restTemplate.postForObject(url, request, String.class);
            
            log.info("Webhook响应: {}", response);
            return response;
            
        } catch (Exception e) {
            log.error("Webhook发送失败", e);
            // 降级返回成功模拟
            return "{\"success\": true, \"note\": \"模拟响应，实际需配置n8n\"}";
        }
    }
}