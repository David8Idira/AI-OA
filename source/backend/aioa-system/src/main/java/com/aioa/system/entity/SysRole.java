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
     * Knowledge base access level (1-6, matches SecurityLevel):
     * 1=绝密级(top-secret), 2=机密级(secret), 3=秘密级(confidential),
     * 4=内部文件(internal), 5=项目文件(project), 6=公开(public)
     */
    private Integer knowledgeAccessLevel;
    
    /**
     * Allowed knowledge security levels (JSON array, overrides knowledgeAccessLevel if set)
     * e.g., ["top-secret", "secret", "confidential"]
     */
    private String allowedSecurityLevels;
    
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
