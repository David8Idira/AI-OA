package com.aioa.aichat.dto;

import lombok.Data;

/**
 * 会话响应DTO
 */
@Data
public class ChatSessionResponseDTO {

    private Long id;

    private String sessionId;

    private Long userId;

    private String title;

    private String type;

    private String contextData;

    private Integer status;

    private String createTime;

    private String updateTime;

    private String lastActiveTime;
}
