package com.aioa.system.vo;

import lombok.Data;
import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.List;

/**
 * User View Object
 */
@Data
public class UserVO implements Serializable {
    private static final long serialVersionUID = 1L;

    /**
     * User ID
     */
    private String id;

    /**
     * Username
     */
    private String username;

    /**
     * Nickname
     */
    private String nickname;

    /**
     * Email
     */
    private String email;

    /**
     * Mobile
     */
    private String mobile;

    /**
     * Avatar URL
     */
    private String avatar;

    /**
     * Department ID
     */
    private String deptId;

    /**
     * Department name
     */
    private String deptName;

    /**
     * Position
     */
    private String position;

    /**
     * Roles
     */
    private List<String> roles;

    /**
     * Permissions
     */
    private List<String> permissions;

    /**
     * Token (for login)
     */
    private String token;

    /**
     * Token expiry time (seconds)
     */
    private Long expiresIn;

    /**
     * Last login time
     */
    private LocalDateTime lastLoginTime;
}
