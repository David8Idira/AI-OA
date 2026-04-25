package com.aioa.hr.dto;

import lombok.Data;

/**
 * 部门查询DTO
 */
@Data
public class DepartmentQueryDTO {
    
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
     * 状态：0-禁用，1-启用
     */
    private Integer status;
    
    /**
     * 页号
     */
    private Integer pageNum = 1;
    
    /**
     * 页大小
     */
    private Integer pageSize = 10;
}