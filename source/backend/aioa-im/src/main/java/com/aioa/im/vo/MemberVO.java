package com.aioa.im.vo;

import lombok.Data;
import java.time.LocalDateTime;

/**
 * Member VO
 */
@Data
public class MemberVO {

    /**
     * User ID
     */
    private String userId;

    /**
     * Nickname
     */
    private String nickname;

    /**
     * Avatar
     */
    private String avatar;

    /**
     * Role: 1-owner, 2-admin, 3-member
     */
    private Integer role;

    /**
     * Join time
     */
    private LocalDateTime joinTime;

    /**
     * Mute status
     */
    private Integer muteStatus;
}
