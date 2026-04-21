package com.aioa.mobile.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(name = "NotificationSendDTO", description = "发送通知DTO")
public class NotificationSendDTO {
    @Schema(description = "用户ID")
    private Long userId;

    @Schema(description = "通知标题")
    private String title;

    @Schema(description = "通知内容")
    private String content;

    @Schema(description = "通知类型")
    private String type;

    @Schema(description = "扩展数据JSON")
    private String payload;

    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }
    public String getType() { return type; }
    public void setType(String type) { this.type = type; }
    public String getPayload() { return payload; }
    public void setPayload(String payload) { this.payload = payload; }
}
