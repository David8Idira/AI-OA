package com.aioa.aichat.dto;

import lombok.Data;

/**
 * 会话创建DTO
 */
@Data
public class ChatSessionDTO {

    private Long userId;

    private String title;

    private String type;

    private String contextData;
}
