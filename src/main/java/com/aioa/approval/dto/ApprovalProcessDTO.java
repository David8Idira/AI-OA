package com.aioa.approval.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * 审批流程DTO
 */
@Data
public class ApprovalProcessDTO {

    private Long id;

    @NotBlank(message = "流程名称不能为空")
    @Size(max = 100, message = "流程名称长度不应超过100个字符")
    private String name;

    @Size(max = 50, message = "流程编码长度不应超过50个字符")
    private String code;

    @NotBlank(message = "流程类型不能为空")
    private String type;

    private String formTemplate;

    @Size(max = 500, message = "流程描述长度不应超过500个字符")
    private String description;

    private Integer status;
}
