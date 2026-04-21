package com.aioa.department.dto;

import lombok.Data;

/**
 * 部门响应DTO
 */
@Data
public class DepartmentResponseDTO {

    private Long id;

    private String name;

    private String code;

    private Long parentId;

    private String parentName;

    private Long managerId;

    private String managerName;

    private String description;

    private Integer sortOrder;

    private Integer status;

    private String createTime;

    private String updateTime;
}
