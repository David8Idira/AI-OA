package com.aioa.attendance.entity;

import com.aioa.common.entity.BaseEntity;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Attendance Exception Application Entity
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("attendance_exception")
public class AttendanceException extends BaseEntity {

    private static final long serialVersionUID = 1L;

    /**
     * User ID
     */
    private String userId;

    /**
     * User name
     */
    private String userName;

    /**
     * Application type: 0-Makeup card, 1-Leave, 2-Business trip, 3-Other
     */
    private Integer type;

    /**
     * Application date
     */
    private LocalDate applicationDate;

    /**
     * Start time
     */
    private LocalDateTime startTime;

    /**
     * End time
     */
    private LocalDateTime endTime;

    /**
     * Reason
     */
    private String reason;

    /**
     * Attachment URLs (JSON array)
     */
    private String attachments;

    /**
     * Status: 0-Pending, 1-Approved, 2-Rejected, 3-Canceled
     */
    private Integer status;

    /**
     * Approval ID (from workflow)
     */
    private Long approvalId;

    /**
     * Approver ID
     */
    private String approverId;

    /**
     * Approver name
     */
    private String approverName;

    /**
     * Approval time
     */
    private LocalDateTime approvalTime;

    /**
     * Approval comment
     */
    private String approvalComment;

    /**
     * Related attendance record IDs (JSON array)
     */
    private String relatedRecordIds;

    /**
     * Department ID
     */
    private String deptId;

    /**
     * Department name
     */
    private String deptName;

    /**
     * Process result: 0-Not processed, 1-Processed, 2-Error
     */
    private Integer processResult;

    /**
     * Process message
     */
    private String processMessage;

    /**
     * Process time
     */
    private LocalDateTime processTime;

    /**
     * Remark
     */
    private String remark;
}