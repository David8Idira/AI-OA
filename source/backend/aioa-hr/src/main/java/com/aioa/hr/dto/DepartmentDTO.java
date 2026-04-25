package com.aioa.hr.dto;

import lombok.Data;

/**
 * 部门DTO
 */
@Data
public class DepartmentDTO {
    
    /**
     * 主键ID
     */
    private Long id;
    
    /**
     * 部门编码
     */
    private String departmentCode;
    
    /**
     * 部门名称
     */
    private String departmentName;
    
    /**
     * 父级部门ID
     */
    private Long parentId;
    
    /**
     * 部门负责人
     */
    private String manager;
    
    /**
     * 部门负责人ID
     */
    private String managerId;
    
    /**
     * 部门级别
     */
    private Integer level;
    
    /**
     * 排序号
     */
    private Integer sortOrder;
    
    /**
     * 状态：0-禁用，1-启用
     */
    private Integer status;
    
    /**
     * 备注
     */
    private String remark;
}