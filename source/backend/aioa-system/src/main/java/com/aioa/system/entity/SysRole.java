package com.aioa.system.entity;

import com.aioa.common.entity.BaseEntity;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * Role Entity
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("sys_role")
public class SysRole extends BaseEntity {
    
    /**
     * Role code
     */
    private String roleCode;
    
    /**
     * Role name
     */
    private String roleName;
    
    /**
     * Role type: system-builtin, custom
     */
    private String roleType;
    
    /**
     * Data scope
     */
    private String dataScope;
    
    /**
     * Status: 0-disabled, 1-enabled
     */
    private Integer status;
    
    /**
     * Sort order
     */
    private Integer sortOrder;
    
    /**
     * Remark
     */
    private String remark;
}
