package com.aioa.aichat.dto;

import lombok.Data;

/**
 * 提示词模板响应DTO
 */
@Data
public class PromptTemplateResponseDTO {

    private Long id;

    private String name;

    private String code;

    private String type;

    private String template;

    private String variables;

    private String description;

    private Integer status;

    private String createTime;
}
