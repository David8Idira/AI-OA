package com.aioa.system.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import java.io.Serializable;

/**
 * User-Role Relation
 */
@Data
@TableName("sys_user_role")
public class SysUserRole implements Serializable {
    private static final long serialVersionUID = 1L;

    /**
     * User ID
     */
    private String userId;
    
    /**
     * Role ID
     */
    private String roleId;
}
