package com.aioa.aichat.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * 提示词模板DTO
 */
@Data
public class PromptTemplateDTO {

    private Long id;

    @NotBlank(message = "模板名称不能为空")
    private String name;

    private String code;

    @NotBlank(message = "模板类型不能为空")
    private String type;

    @NotBlank(message = "模板内容不能为空")
    private String template;

    private String variables;

    private String description;

    private Integer status;
}
