package com.aioa.workflow.dto;

import lombok.Data;

/**
 * n8n工作流配置DTO
 */
@Data
public class N8nWorkflowDTO {
    
    /**
     * 工作流ID
     */
    private String workflowId;
    
    /**
     * 工作流名称
     */
    private String name;
    
    /**
     * 工作流类型: approval, notification, webhook
     */
    private String type;
    
    /**
     * n8n Webhook URL
     */
    private String webhookUrl;
    
    /**
     * 触发条件
     */
    private String triggerCondition;
    
    /**
     * 认证密钥
     */
    private String apiKey;
    
    /**
     * 启用状态
     */
    private Boolean enabled = true;
    
    /**
     * 描述
     */
    private String description;
}