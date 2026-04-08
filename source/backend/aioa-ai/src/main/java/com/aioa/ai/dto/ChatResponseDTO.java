package com.aioa.ai.dto;

import lombok.Data;

/**
 * AI聊天响应DTO
 */
@Data
public class ChatResponseDTO {
    
    /**
     * 对话ID
     */
    private String conversationId;
    
    /**
     * AI回复内容
     */
    private String reply;
    
    /**
     * 使用的模型
     */
    private String modelCode;
    
    /**
     * 消耗的Token数
     */
    private Integer tokens;
    
    /**
     * 回复时间(毫秒)
     */
    private Long timeUsed;
    
    /**
     * 状态码
     */
    private Integer code = 200;
    
    /**
     * 消息
     */
    private String message = "success";
}