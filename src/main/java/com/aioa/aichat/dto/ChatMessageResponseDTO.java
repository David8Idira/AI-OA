package com.aioa.aichat.dto;

import lombok.Data;

/**
 * 聊天消息响应DTO
 */
@Data
public class ChatMessageResponseDTO {

    private Long id;

    private String sessionId;

    private Long sessionEntityId;

    private String role;

    private String content;

    private String modelName;

    private Integer tokenCount;

    private String createTime;
}
