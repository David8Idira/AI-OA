package com.aioa.im.vo;

import lombok.Data;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Conversation VO
 */
@Data
public class ConversationVO {

    /**
     * Conversation ID
     */
    private String id;

    /**
     * Conversation type: 1-private, 2-group, 3-channel
     */
    private Integer type;

    /**
     * Conversation name
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
     * Last message preview
     */
    private String lastMessageContent;

    /**
     * Last message time
     */
    private String lastMessageTime;

    /**
     * Last message sender nickname
     */
    private String lastMessageSender;

    /**
     * Unread count
     */
    private Integer unreadCount;

    /**
     * Mute status
     */
    private Integer muteStatus;

    /**
     * Top status
     */
    private Integer topStatus;

    /**
     * Member count
     */
    private Integer memberCount;

    /**
     * Members list (for private chat or preview)
     */
    private List<MemberVO> members;

    /**
     * Create time
     */
    private LocalDateTime createTime;

    /**
     * Update time
     */
    private LocalDateTime updateTime;
}
