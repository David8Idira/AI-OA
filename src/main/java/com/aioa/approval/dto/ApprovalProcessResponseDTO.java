package com.aioa.approval.dto;

import lombok.Data;

/**
 * 审批流程响应DTO
 */
@Data
public class ApprovalProcessResponseDTO {

    private Long id;

    private String name;

    private String code;

    private String type;

    private String formTemplate;

    private String description;

    private Integer status;

    private String createTime;

    private String updateTime;
}
