package com.aioa.im.dto;

import lombok.Data;

/**
 * Read Confirm DTO
 */
@Data
public class ReadConfirmDTO {

    /**
     * Last read message ID (mark all messages before this as read)
     */
    private String lastReadMsgId;

    /**
     * Conversation ID (alternative to path variable)
     */
    private String conversationId;
}
