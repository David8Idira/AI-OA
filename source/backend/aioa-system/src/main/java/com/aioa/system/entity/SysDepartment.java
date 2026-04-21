package com.aioa.system.entity;

import com.aioa.common.entity.BaseEntity;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.List;

/**
 * Department Entity
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("sys_department")
public class SysDepartment extends BaseEntity {
    
    /**
     * Department name
     */
    private String deptName;
    
    /**
     * Parent department ID
     */
    private String parentId;
    
    /**
     * Tree path
     */
    private String treePath;
    
    /**
     * Sort order
     */
    private Integer sortOrder;
    
    /**
     * Leader ID
     */
    private String leaderId;
    
    /**
     * Leader name
     */
    private String leaderName;
    
    /**
     * Contact phone
     */
    private String phone;
    
    /**
     * Email
     */
    private String email;
    
    /**
     * Status: 0-disabled, 1-enabled
     */
    private Integer status;
    
    /**
     * Child departments (not persisted)
     */
    @TableField(exist = false)
    private List<SysDepartment> children;
}
