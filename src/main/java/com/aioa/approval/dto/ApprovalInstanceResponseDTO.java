package com.aioa.approval.dto;

import lombok.Data;

/**
 * 审批实例响应DTO
 */
@Data
public class ApprovalInstanceResponseDTO {

    private Long id;

    private Long processId;

    private String processName;

    private Long applicantId;

    private String applicantName;

    private String title;

    private String formData;

    private Integer currentNode;

    private Integer totalNodes;

    private Integer status;

    private String statusName;

    private String createTime;

    private String updateTime;

    private String finishTime;
}
