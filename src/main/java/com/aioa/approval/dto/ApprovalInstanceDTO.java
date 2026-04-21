package com.aioa.approval.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * 审批实例DTO
 */
@Data
public class ApprovalInstanceDTO {

    private Long id;

    @NotNull(message = "流程ID不能为空")
    private Long processId;

    @NotNull(message = "申请人ID不能为空")
    private Long applicantId;

    @NotBlank(message = "申请标题不能为空")
    private String title;

    private String formData;

    private Integer currentNode;

    private Integer totalNodes;

    private Integer status;
}
