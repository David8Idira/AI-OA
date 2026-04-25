package com.aioa.attendance.entity;

import com.aioa.common.entity.BaseEntity;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDate;
import java.time.YearMonth;

/**
 * Attendance Statistics Entity
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("attendance_stat")
public class AttendanceStat extends BaseEntity {

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
     * Department ID
     */
    private String deptId;

    /**
     * Department name
     */
    private String deptName;

    /**
     * Stat period type: 0-Daily, 1-Weekly, 2-Monthly, 3-Quarterly, 4-Yearly
     */
    private Integer periodType;

    /**
     * Start date
     */
    private LocalDate startDate;

    /**
     * End date
     */
    private LocalDate endDate;

    /**
     * Year and month (for monthly stats)
     */
    private YearMonth yearMonth;

    /**
     * Quarter (1-4)
     */
    private Integer quarter;

    /**
     * Year
     */
    private Integer year;

    /**
     * Total work days
     */
    private Integer totalWorkDays;

    /**
     * Actual work days
     */
    private Integer actualWorkDays;

    /**
     * Normal days
     */
    private Integer normalDays;

    /**
     * Late days
     */
    private Integer lateDays;

    /**
     * Leave early days
     */
    private Integer leaveEarlyDays;

    /**
     * Absent days
     */
    private Integer absentDays;

    /**
     * Overtime days
     */
    private Integer overtimeDays;

    /**
     * Leave days
     */
    private Integer leaveDays;

    /**
     * Business trip days
     */
    private Integer businessTripDays;

    /**
     * Total late minutes
     */
    private Integer totalLateMinutes;

    /**
     * Total leave early minutes
     */
    private Integer totalLeaveEarlyMinutes;

    /**
     * Total overtime minutes
     */
    private Integer totalOvertimeMinutes;

    /**
     * Total work hours
     */
    private Double totalWorkHours;

    /**
     * Should work hours
     */
    private Double shouldWorkHours;

    /**
     * Normal rate (percentage)
     */
    private Double normalRate;

    /**
     * Overtime rate (percentage)
     */
    private Double overtimeRate;

    /**
     * Attendance score (0-100)
     */
    private Double attendanceScore;

    /**
     * Remark
     */
    private String remark;

    /**
     * Is final: 0-No, 1-Yes (finalized stats)
     */
    private Integer isFinal;
}