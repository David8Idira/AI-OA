package com.aioa.reimburse.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Reimburse Query DTO - For list/search operations
 */
@Data
@Schema(name = "ReimburseQueryDTO", description = "Reimbursement query parameters DTO")
public class ReimburseQueryDTO {

    @Schema(description = "Page number (starts from 1)")
    private Integer pageNum = 1;

    @Schema(description = "Page size")
    private Integer pageSize = 10;

    @Schema(description = "Keyword search (title, description)")
    private String keyword;

    @Schema(description = "Reimbursement type filter")
    private String type;

    @Schema(description = "Status filter (0-Draft, 1-Pending, 2-Approved, 3-Rejected, 4-Cancelled, 5-Paid)")
    private Integer status;

    @Schema(description = "Applicant user ID")
    private String applicantId;

    @Schema(description = "Approver user ID")
    private String approverId;

    @Schema(description = "Department ID")
    private String deptId;

    @Schema(description = "Start date (create time range start)")
    private LocalDateTime startDate;

    @Schema(description = "End date (create time range end)")
    private LocalDateTime endDate;

    @Schema(description = "Reimbursement date start")
    private LocalDateTime reimburseDateStart;

    @Schema(description = "Reimbursement date end")
    private LocalDateTime reimburseDateEnd;

    @Schema(description = "Min total amount")
    private java.math.BigDecimal minAmount;

    @Schema(description = "Max total amount")
    private java.math.BigDecimal maxAmount;

    @Schema(description = "Query mode: MY_APPLY (my submissions), MY_APPROVE (pending my approval), ALL")
    private String mode = "MY_APPLY";

    @Schema(description = "Sort field: createTime, totalAmount, reimburseDate")
    private String sortField = "createTime";

    @Schema(description = "Sort direction: asc, desc")
    private String sortDirection = "desc";
}
