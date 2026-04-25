package com.aioa.attendance.entity;

import com.aioa.common.entity.BaseEntity;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * Attendance Group Entity
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("attendance_group")
public class AttendanceGroup extends BaseEntity {

    private static final long serialVersionUID = 1L;

    /**
     * Group name
     */
    private String groupName;

    /**
     * Group code
     */
    private String groupCode;

    /**
     * Status: 0-Disabled, 1-Enabled
     */
    private Integer status;

    /**
     * Rule ID
     */
    private Long ruleId;

    /**
     * Schedule type: 0-Normal shift, 1-Rotating shift, 2-Flexible
     */
    private Integer scheduleType;

    /**
     * Schedule data (JSON for shift schedules)
     */
    private String scheduleData;

    /**
     * Manager user ID
     */
    private String managerId;

    /**
     * Manager name
     */
    private String managerName;

    /**
     * Department IDs (JSON array, empty for all)
     */
    private String deptIds;

    /**
     * Position IDs (JSON array, empty for all)
     */
    private String positionIds;

    /**
     * User IDs (JSON array, direct assignment)
     */
    private String userIds;

    /**
     * Check-in locations (JSON array of location data)
     */
    private String checkinLocations;

    /**
     * WiFi list (JSON array of WiFi data)
     */
    private String wifiList;

    /**
     * Allow remote check-in: 0-No, 1-Yes
     */
    private Integer allowRemote;

    /**
     * Maximum remote distance (meters)
     */
    private Integer maxRemoteDistance;

    /**
     * Remark
     */
    private String remark;
}