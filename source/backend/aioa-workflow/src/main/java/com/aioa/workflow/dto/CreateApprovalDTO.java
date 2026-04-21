package com.aioa.workflow.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * Create Approval Request DTO
 */
@Data
@Schema(name = "CreateApprovalDTO", description = "Create approval request DTO")
public class CreateApprovalDTO {

    @Schema(description = "Approval title")
    @NotBlank(message = "标题不能为空")
    @Size(max = 200, message = "标题长度不能超过200")
    private String title;

    @Schema(description = "Approval type")
    @NotBlank(message = "审批类型不能为空")
    @Size(max = 50, message = "审批类型长度不能超过50")
    private String type;

    @Schema(description = "Approval content/description")
    @NotBlank(message = "审批内容不能为空")
    @Size(max = 5000, message = "审批内容长度不能超过5000")
    private String content;

    @Schema(description = "Priority: 0-Low, 1-Normal, 2-High, 3-Urgent")
    @NotNull(message = "优先级不能为空")
    private Integer priority;

    @Schema(description = "Approver user ID")
    @NotBlank(message = "审批人不能为空")
    private String approverId;

    @Schema(description = "CC user IDs (comma-separated)")
    private String ccUsers;

    @Schema(description = "Expected finish time")
    private LocalDateTime expectFinishTime;

    @Schema(description = "Remark from applicant")
    @Size(max = 500, message = "备注长度不能超过500")
    private String remark;

    @Schema(description = "Attachment URLs (comma-separated)")
    private String attachments;

    @Schema(description = "Flexible form data as key-value pairs")
    private Map<String, Object> formData;
}
