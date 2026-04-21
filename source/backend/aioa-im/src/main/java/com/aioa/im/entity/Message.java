package com.aioa.im.entity;

import com.aioa.common.entity.BaseEntity;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * Message Entity - represents a chat message
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("im_message")
public class Message extends BaseEntity {

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
     * Extra data (JSON string for rich content)
     */
    private String extra;

    /**
     * Reply to message ID
     */
    private String replyId;

    /**
     * Reply message content preview
     */
    private String replyContent;

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

    /**
     * Read status: 0-unread, 1-read
     */
    private Integer readStatus;

    /**
     * Read time
     */
    private String readTime;

    /**
     * Read by user IDs (for group messages)
     */
    private String readBy;

    /**
     * Reaction count
     */
    private Integer reactionCount;

    /**
     * Reaction details (JSON string)
     */
    private String reactions;

    /**
     * Recall status: 0-normal, 1-recalled
     */
    private Integer recallStatus;

    /**
     * Recall time
     */
    private String recallTime;

    /**
     * Sender deleted: 0-not deleted, 1-deleted
     */
    private Integer senderDeleted;

    /**
     * Message status: 0-sending, 1-sent, 2-failed
     */
    private Integer msgStatus;
}
