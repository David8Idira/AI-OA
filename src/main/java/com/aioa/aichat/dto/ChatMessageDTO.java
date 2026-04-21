package com.aioa.aichat.dto;

import lombok.Data;

/**
 * 聊天消息请求DTO
 */
@Data
public class ChatMessageDTO {

    private String sessionId;

    private Long userId;

    private String content;

    private String type;

    private String contextData;
}
