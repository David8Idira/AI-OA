package com.aioa.ai.dto;

import lombok.Data;

/**
 * AI聊天请求DTO
 */
@Data
public class ChatRequestDTO {
    
    /**
     * 对话ID
     */
    private String conversationId;
    
    /**
     * 用户消息
     */
    private String message;
    
    /**
     * 模型代码 (gpt-4o, kimi-pro, claude-3.5)
     */
    private String modelCode;
    
    /**
     * 会话历史保留条数
     */
    private Integer historyCount = 10;
    
    /**
     * 温度参数 (0.0-2.0)
     */
    private Double temperature = 0.7;
    
    /**
     * 最大Token数
     */
    private Integer maxTokens = 2048;
    
    /**
     * 系统提示词
     */
    private String systemPrompt;
}