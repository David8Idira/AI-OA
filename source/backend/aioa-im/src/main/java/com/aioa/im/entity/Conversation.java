package com.aioa.im.entity;

import com.aioa.common.entity.BaseEntity;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * Conversation Entity - represents a chat conversation/session
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("im_conversation")
public class Conversation extends BaseEntity {

    /**
     * Conversation type: 1-private, 2-group, 3-channel
     */
    private Integer type;

    /**
     * Conversation name (for group/channel)
     */
    private String name;

    /**
     * Avatar URL
     */
    private String avatar;

    /**
     * Owner/Creator user ID
     */
    private String ownerId;

    /**
     * Last message ID
     */
    private String lastMessageId;

    /**
     * Last message content preview (max 200 chars)
     */
    private String lastMessageContent;

    /**
     * Last message time
     */
    private String lastMessageTime;

    /**
     * Unread message count for current user
     */
    private Integer unreadCount;

    /**
     * Mute status: 0-normal, 1-muted
     */
    private Integer muteStatus;

    /**
     * Top status: 0-normal, 1-pinned to top
     */
    private Integer topStatus;

    /**
     * Archive status: 0-normal, 1-archived
     */
    private Integer archiveStatus;

    /**
     * Max members allowed (-1 for unlimited)
     */
    private Integer maxMembers;

    /**
     * Description
     */
    private String description;

    /**
     * Status: 0-closed, 1-active
     */
    private Integer status;

    /**
     * Remark
     */
    private String remark;
}
