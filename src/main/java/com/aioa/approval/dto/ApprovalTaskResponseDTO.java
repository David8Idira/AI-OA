package com.aioa.approval.dto;

import lombok.Data;

/**
 * 审批任务响应DTO
 */
@Data
public class ApprovalTaskResponseDTO {

    private Long id;

    private Long instanceId;

    private String instanceTitle;

    private Long approverId;

    private String approverName;

    private Integer nodeOrder;

    private String comment;

    private Integer result;

    private String resultName;

    private Integer status;

    private String statusName;

    private String createTime;

    private String approveTime;
}
