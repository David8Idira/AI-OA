package com.aioa.system.entity;

import com.aioa.common.entity.BaseEntity;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * User Entity
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("sys_user")
public class SysUser extends BaseEntity {
    
    /**
     * Username
     */
    private String username;
    
    /**
     * Password (encrypted)
     */
    private String password;
    
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
     * Position
     */
    private String position;
    
    /**
     * Status: 0-disabled, 1-enabled
     */
    private Integer status;
    
    /**
     * Last login IP
     */
    private String lastLoginIp;
    
    /**
     * Last login time
     */
    private String lastLoginTime;
    
    /**
     * Remark
     */
    private String remark;
}
