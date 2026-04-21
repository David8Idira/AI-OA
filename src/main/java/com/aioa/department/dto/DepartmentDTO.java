package com.aioa.department.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * 部门DTO
 */
@Data
public class DepartmentDTO {

    private Long id;

    @NotBlank(message = "部门名称不能为空")
    @Size(max = 100, message = "部门名称长度不应超过100个字符")
    private String name;

    @Size(max = 50, message = "部门代码长度不应超过50个字符")
    private String code;

    private Long parentId;

    private Long managerId;

    @Size(max = 500, message = "部门描述长度不应超过500个字符")
    private String description;

    private Integer sortOrder;

    private Integer status;
}
