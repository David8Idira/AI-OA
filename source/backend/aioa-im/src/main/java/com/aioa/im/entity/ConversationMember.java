package com.aioa.im.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * ConversationMember Entity - represents membership in a conversation
 */
@Data
@TableName("im_conversation_member")
public class ConversationMember implements Serializable {
    private static final long serialVersionUID = 1L;

    /**
     * Primary key
     */
    @TableId(type = IdType.ASSIGN_ID)
    private String id;

    /**
     * Conversation ID
     */
    private String conversationId;

    /**
     * User ID
     */
    private String userId;

    /**
     * User nickname in this conversation
     */
    private String nickname;

    /**
     * User avatar
     */
    private String avatar;

    /**
     * Member role: 1-owner, 2-admin, 3-member
     */
    private Integer role;

    /**
     * Join time
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime joinTime;

    /**
     * Last read message ID
     */
    private String lastReadMsgId;

    /**
     * Last read time
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime lastReadTime;

    /**
     * Unread count for this member
     */
    private Integer unreadCount;

    /**
     * Mute status: 0-normal, 1-muted
     */
    private Integer muteStatus;

    /**
     * Top status: 0-normal, 1-pinned
     */
    private Integer topStatus;

    /**
     * Display order
     */
    private Integer sortOrder;

    /**
     * Member status: 0-disabled, 1-active
     */
    private Integer status;

    /**
     * Leave time (when member left)
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime leaveTime;

    /**
     * Inviter user ID
     */
    private String inviterId;

    /**
     * Remark/nickname set by self
     */
    private String remark;
}
