package com.aioa.reimburse.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Reimburse VO - Reimbursement view object
 */
@Data
@Schema(name = "ReimburseVO", description = "Reimbursement view object")
public class ReimburseVO {

    @Schema(description = "Reimbursement ID")
    private String id;

    @Schema(description = "Reimbursement title")
    private String title;

    @Schema(description = "Reimbursement type")
    private String type;

    @Schema(description = "Reimbursement type name")
    private String typeName;

    @Schema(description = "Total amount")
    private BigDecimal totalAmount;

    @Schema(description = "Currency code")
    private String currency;

    @Schema(description = "Status: 0-Draft, 1-Pending, 2-Approved, 3-Rejected, 4-Cancelled, 5-Paid")
    private Integer status;

    @Schema(description = "Status name")
    private String statusName;

    @Schema(description = "Priority: 0-Low, 1-Normal, 2-High, 3-Urgent")
    private Integer priority;

    @Schema(description = "Priority name")
    private String priorityName;

    @Schema(description = "Applicant user ID")
    private String applicantId;

    @Schema(description = "Applicant name")
    private String applicantName;

    @Schema(description = "Department ID")
    private String deptId;

    @Schema(description = "Department name")
    private String deptName;

    @Schema(description = "Approver user ID")
    private String approverId;

    @Schema(description = "Approver name")
    private String approverName;

    @Schema(description = "Reimbursement date")
    private LocalDateTime reimburseDate;

    @Schema(description = "Expected payment date")
    private LocalDateTime expectPayDate;

    @Schema(description = "Actual payment date")
    private LocalDateTime payDate;

    @Schema(description = "Payment method")
    private String payMethod;

    @Schema(description = "Description")
    private String description;

    @Schema(description = "Approval comment")
    private String approvalComment;

    @Schema(description = "Reject reason")
    private String rejectReason;

    @Schema(description = "Cancel reason")
    private String cancelReason;

    @Schema(description = "OCR auto-fill flag")
    private Integer ocrAutoFill;

    @Schema(description = "Invoice count")
    private Integer invoiceCount;

    @Schema(description = "Receipt attached")
    private Integer receiptAttached;

    @Schema(description = "Reimbursement items")
    private List<ReimburseItemVO> items;

    @Schema(description = "Create time")
    private LocalDateTime createTime;

    @Schema(description = "Update time")
    private LocalDateTime updateTime;
}
