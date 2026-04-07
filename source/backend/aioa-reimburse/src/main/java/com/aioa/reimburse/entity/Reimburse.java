package com.aioa.reimburse.entity;

import com.aioa.common.entity.BaseEntity;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.annotation.TableLogic;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Reimburse Entity - Main reimbursement form table
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("reimburse")
public class Reimburse extends BaseEntity {

    private static final long serialVersionUID = 1L;

    /**
     * Reimbursement title
     */
    private String title;

    /**
     * Reimbursement type: BUSINESS_TRIP, DAILY, COMMUNICATION, ENTERTAINMENT, OTHER
     */
    private String type;

    /**
     * Total amount (sum of all items)
     */
    private BigDecimal totalAmount;

    /**
     * Currency code: CNY, USD, EUR
     */
    private String currency;

    /**
     * Current status: 0-Draft, 1-Pending, 2-Approved, 3-Rejected, 4-Cancelled, 5-Paid
     */
    private Integer status;

    /**
     * Priority: 0-Low, 1-Normal, 2-High, 3-Urgent
     */
    private Integer priority;

    /**
     * Applicant user ID
     */
    private String applicantId;

    /**
     * Applicant name (denormalized)
     */
    private String applicantName;

    /**
     * Department ID of the applicant
     */
    private String deptId;

    /**
     * Department name (denormalized)
     */
    private String deptName;

    /**
     * Approver user ID
     */
    private String approverId;

    /**
     * Approver name (denormalized)
     */
    private String approverName;

    /**
     * CC users (comma-separated user IDs)
     */
    private String ccUsers;

    /**
     * Reimbursement date (usually the month being reimbursed)
     */
    private LocalDateTime reimburseDate;

    /**
     * Expected payment date
     */
    private LocalDateTime expectPayDate;

    /**
     * Actual payment date
     */
    private LocalDateTime payDate;

    /**
     * Payment method: BANK_TRANSFER, ALIPAY, WECHAT, CASH
     */
    private String payMethod;

    /**
     * Bank account info (encrypted)
     */
    private String bankAccount;

    /**
     * Bank name
     */
    private String bankName;

    /**
     * Attachment URLs (comma-separated or JSON array)
     */
    private String attachments;

    /**
     * Description/reason
     */
    private String description;

    /**
     * Approval comment (from approver)
     */
    private String approvalComment;

    /**
     * Rejection reason (if rejected)
     */
    private String rejectReason;

    /**
     * Cancel reason (if cancelled)
     */
    private String cancelReason;

    /**
     * OCR auto-fill flag: 0-manual, 1-auto-filled
     */
    private Integer ocrAutoFill;

    /**
     * Number of invoice items attached
     */
    private Integer invoiceCount;

    /**
     * Whether receipt attached: 0-No, 1-Yes
     */
    private Integer receiptAttached;
}
