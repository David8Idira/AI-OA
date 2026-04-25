package com.aioa.attendance.entity;

import com.aioa.common.entity.BaseEntity;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalTime;

/**
 * Attendance Rule Entity
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("attendance_rule")
public class AttendanceRule extends BaseEntity {

    private static final long serialVersionUID = 1L;

    /**
     * Rule name
     */
    private String ruleName;

    /**
     * Rule code
     */
    private String ruleCode;

    /**
     * Status: 0-Disabled, 1-Enabled
     */
    private Integer status;

    /**
     * Work start time
     */
    private LocalTime workStartTime;

    /**
     * Work end time
     */
    private LocalTime workEndTime;

    /**
     * Allow late minutes (can be late within this time)
     */
    private Integer allowLateMinutes;

    /**
     * Allow leave early minutes (can leave early within this time)
     */
    private Integer allowLeaveEarlyMinutes;

    /**
     * Overtime rule: 0-No overtime, 1-Calculate after work time, 2-Fixed overtime
     */
    private Integer overtimeRule;

    /**
     * Overtime start time (for fixed overtime)
     */
    private LocalTime overtimeStartTime;

    /**
     * Minimum overtime duration (minutes)
     */
    private Integer minOvertimeDuration;

    /**
     * Is flexible work time: 0-No, 1-Yes
     */
    private Integer flexibleWork;

    /**
     * Flexible work duration (hours)
     */
    private Double flexibleWorkHours;

    /**
     * Is rest day included: 0-No, 1-Yes
     */
    private Integer includeRestDays;

    /**
     * Weekdays to apply (JSON array, e.g., [1,2,3,4,5] for Mon-Fri)
     */
    private String weekdays;

    /**
     * Holidays to exclude (JSON array of dates)
     */
    private String excludeHolidays;

    /**
     * Special work days (JSON array of dates)
     */
    private String specialWorkDays;

    /**
     * Applicable department IDs (JSON array, empty for all)
     */
    private String deptIds;

    /**
     * Applicable position IDs (JSON array, empty for all)
     */
    private String positionIds;

    /**
     * Remark
     */
    private String remark;

    /**
     * Timezone
     */
    private String timezone;
}