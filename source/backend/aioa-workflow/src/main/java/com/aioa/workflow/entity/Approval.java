package com.aioa.workflow.entity;

import com.aioa.common.entity.BaseEntity;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.annotation.TableField;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

/**
 * Approval Entity - Main approval form table
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("approval")
public class Approval extends BaseEntity {

    private static final long serialVersionUID = 1L;

    /**
     * Title of the approval request
     */
    private String title;

    /**
     * Approval type (e.g., LEAVE, EXPENSE, PURCHASE)
     */
    private String type;

    /**
     * Approval content/description (JSON or text)
     */
    private String content;

    /**
     * Current status: 0-Pending, 1-Approved, 2-Rejected, 3-Cancelled, 4-Transferred
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
     * Applicant name (denormalized for display)
     */
    private String applicantName;

    /**
     * Current approver user ID
     */
    private String approverId;

    /**
     * Approver name (denormalized for display)
     */
    private String approverName;

    /**
     * Department ID of the applicant
     */
    private String deptId;

    /**
     * Department name (denormalized for display)
     */
    private String deptName;

    /**
     * CC users (comma-separated user IDs)
     */
    private String ccUsers;

    /**
     * Expected completion date
     */
    private LocalDateTime expectFinishTime;

    /**
     * Actual completion time
     */
    private LocalDateTime finishTime;

    /**
     * Current step in the approval flow (for multi-step approvals)
     */
    private Integer currentStep;

    /**
     * Total steps in the approval flow
     */
    private Integer totalSteps;

    /**
     * Attachment URLs (comma-separated or JSON array)
     */
    private String attachments;

    /**
     * Form data JSON (for flexible form fields)
     */
    private String formData;

    /**
     * Remark/comment from applicant
     */
    private String remark;

    /**
     * Approval result comment (filled by approver)
     */
    private String approvalComment;
}
