package com.aioa.reimburse.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * Reimburse Action DTO - For approve/reject/cancel operations
 */
@Data
@Schema(name = "ReimburseActionDTO", description = "Reimburse action request DTO")
public class ReimburseActionDTO {

    @Schema(description = "Action type: APPROVE, REJECT, CANCEL, REQUEST_EXTRA")
    @NotBlank(message = "操作类型不能为空")
    @Size(max = 50, message = "操作类型长度不能超过50")
    private String actionType;

    @Schema(description = "Comment/remark for the action")
    @Size(max = 500, message = "意见长度不能超过500")
    private String comment;

    @Schema(description = "Reason (required for REJECT, CANCEL)")
    private String reason;

    @Schema(description = "Next approver ID (for REQUEST_EXTRA)")
    private String nextApproverId;
}
