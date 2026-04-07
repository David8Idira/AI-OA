package com.aioa.workflow.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * Approval Query DTO - for filtering approval list
 */
@Data
@Schema(name = "ApprovalQueryDTO", description = "Approval query parameters")
public class ApprovalQueryDTO {

    @Schema(description = "Page number (starts from 1)")
    private Integer pageNum = 1;

    @Schema(description = "Page size")
    private Integer pageSize = 10;

    @Schema(description = "Approval type")
    private String type;

    @Schema(description = "Status filter")
    private Integer status;

    @Schema(description = "Priority filter")
    private Integer priority;

    @Schema(description = "Applicant user ID")
    private String applicantId;

    @Schema(description = "Approver user ID (current approver)")
    private String approverId;

    @Schema(description = "Title keyword search")
    private String keyword;

    @Schema(description = "Start date for create time range")
    private String startDate;

    @Schema(description = "End date for create time range")
    private String endDate;

    @Schema(description = "Query mode: MY_APPLY (my applications), MY_APPROVE (my approvals to handle)")
    private String mode;
}
