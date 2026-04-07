package com.aioa.im.vo;

import lombok.Data;
import java.time.LocalDateTime;

/**
 * Message VO
 */
@Data
public class MessageVO {

    /**
     * Message ID
     */
    private String id;

    /**
     * Conversation ID
     */
    private String conversationId;

    /**
     * Sender user ID
     */
    private String senderId;

    /**
     * Sender nickname
     */
    private String senderNickname;

    /**
     * Sender avatar
     */
    private String senderAvatar;

    /**
     * Message type: 1-text, 2-image, 3-file, 4-audio, 5-video, 6-location, 7-card, 8-system
     */
    private Integer type;

    /**
     * Message content
     */
    private String content;

    /**
     * Extra data
     */
    private String extra;

    /**
     * Reply message
     */
    private MessageVO reply;

    /**
     * Is at all
     */
    private Integer atAll;

    /**
     * At user IDs
     */
    private String atUserIds;

    /**
     * Reaction count
     */
    private Integer reactionCount;

    /**
     * Reactions
     */
    private String reactions;

    /**
     * Recall status
     */
    private Integer recallStatus;

    /**
     * Message status: 0-sending, 1-sent, 2-failed
     */
    private Integer msgStatus;

    /**
     * Create time
     */
    private LocalDateTime createTime;

    /**
     * Is self message (sent by current user)
     */
    private Boolean isSelf;
}
