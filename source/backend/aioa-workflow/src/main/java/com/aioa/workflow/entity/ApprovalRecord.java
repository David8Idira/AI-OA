package com.aioa.workflow.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableField;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * Approval Record Entity - Records every action taken on an approval
 */
@Data
@TableName("approval_record")
public class ApprovalRecord implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * Primary key
     */
    @TableId(type = IdType.ASSIGN_ID)
    private String id;

    /**
     * Approval ID reference
     */
    private String approvalId;

    /**
     * User ID who performed the action
     */
    private String operatorId;

    /**
     * Operator name (denormalized)
     */
    private String operatorName;

    /**
     * Action type: 1-Approve, 2-Reject, 3-Transfer, 4-Cancel
     */
    private Integer actionType;

    /**
     * Action type description
     */
    private String actionDesc;

    /**
     * Comment/reason for the action
     */
    private String comment;

    /**
     * Status after this action: 0-Pending, 1-Approved, 2-Rejected, 3-Cancelled, 4-Transferred
     */
    private Integer statusAfter;

    /**
     * Step number in multi-step approval flow
     */
    private Integer step;

    /**
     * Transfer target user ID (only for TRANSFER action)
     */
    private String transferToId;

    /**
     * Transfer target user name (only for TRANSFER action)
     */
    private String transferToName;

    /**
     * Previous approver ID (for transfer tracking)
     */
    private String previousApproverId;

    /**
     * Next approver ID (for transfer and multi-step)
     */
    private String nextApproverId;

    /**
     * IP address of the operator
     */
    private String operatorIp;

    /**
     * User agent / device info
     */
    private String userAgent;

    /**
     * Record creation time
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @TableField(fill = com.baomidou.mybatisplus.annotation.FieldFill.INSERT)
    private LocalDateTime createTime;

    /**
     * Attachment URLs related to this record
     */
    private String attachments;
}
