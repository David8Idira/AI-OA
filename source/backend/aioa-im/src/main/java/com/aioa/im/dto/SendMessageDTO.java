package com.aioa.im.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * Send Message DTO
 */
@Data
public class SendMessageDTO {

    /**
     * Conversation ID (required for reply/forward)
     */
    private String conversationId;

    /**
     * Receiver user ID (for private chat, alternative to conversationId)
     */
    private String receiverId;

    /**
     * Message type: 1-text, 2-image, 3-file, 4-audio, 5-video, 6-location, 7-card, 8-system
     */
    @NotNull(message = "Message type is required")
    private Integer type;

    /**
     * Message content
     */
    @NotBlank(message = "Message content is required")
    private String content;

    /**
     * Extra data (JSON string)
     */
    private String extra;

    /**
     * Reply to message ID
     */
    private String replyId;

    /**
     * Forward from message ID
     */
    private String forwardId;

    /**
     * At user IDs (comma separated)
     */
    private String atUserIds;

    /**
     * Is at all: 0-no, 1-yes
     */
    private Integer atAll;
}
