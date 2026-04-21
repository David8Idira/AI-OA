package com.aioa.approval.dto;

import lombok.Data;

/**
 * 审批任务DTO
 */
@Data
public class ApprovalTaskDTO {

    private Long id;

    private Long instanceId;

    private Long approverId;

    private Integer nodeOrder;

    private String comment;

    private Integer result;

    private Integer status;
}
