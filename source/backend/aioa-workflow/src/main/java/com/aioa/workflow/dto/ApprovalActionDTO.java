package com.aioa.workflow.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * Approval Action Request DTO
 * Used for approve, reject, transfer, and cancel actions
 */
@Data
@Schema(name = "ApprovalActionDTO", description = "Approval action request DTO")
public class ApprovalActionDTO {

    @Schema(description = "Action type: 1-Approve, 2-Reject, 3-Transfer, 4-Cancel")
    @NotNull(message = "操作类型不能为空")
    private Integer actionType;

    @Schema(description = "Comment/reason for the action")
    @Size(max = 500, message = "审批意见长度不能超过500")
    private String comment;

    @Schema(description = "Target user ID for transfer action")
    private String transferToId;

    @Schema(description = "Attachment URLs after action (optional)")
    private String attachments;

    @Schema(description = "Next approver ID for multi-step approval")
    private String nextApproverId;
}
