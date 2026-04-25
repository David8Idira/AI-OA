package com.aioa.attendance.entity;

import com.aioa.common.entity.BaseEntity;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Attendance Record Entity
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("attendance_record")
public class AttendanceRecord extends BaseEntity {

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
     * Attendance date
     */
    private LocalDate attendanceDate;

    /**
     * Group ID
     */
    private Long groupId;

    /**
     * Rule ID
     */
    private Long ruleId;

    /**
     * Check-in time
     */
    private LocalDateTime checkinTime;

    /**
     * Check-out time
     */
    private LocalDateTime checkoutTime;

    /**
     * Check-in location (latitude)
     */
    private BigDecimal checkinLatitude;

    /**
     * Check-in location (longitude)
     */
    private BigDecimal checkinLongitude;

    /**
     * Check-out location (latitude)
     */
    private BigDecimal checkoutLatitude;

    /**
     * Check-out location (longitude)
     */
    private BigDecimal checkoutLongitude;

    /**
     * Check-in address
     */
    private String checkinAddress;

    /**
     * Check-out address
     */
    private String checkoutAddress;

    /**
     * Check-in WiFi MAC
     */
    private String checkinWifiMac;

    /**
     * Check-out WiFi MAC
     */
    private String checkoutWifiMac;

    /**
     * Check-in device ID
     */
    private String checkinDeviceId;

    /**
     * Check-out device ID
     */
    private String checkoutDeviceId;

    /**
     * Check-in IP address
     */
    private String checkinIp;

    /**
     * Check-out IP address
     */
    private String checkoutIp;

    /**
     * Check-in method: 0-GPS, 1-WiFi, 2-Manual, 3-Remote
     */
    private Integer checkinMethod;

    /**
     * Check-out method: 0-GPS, 1-WiFi, 2-Manual, 3-Remote
     */
    private Integer checkoutMethod;

    /**
     * Status: 0-Normal, 1-Late, 2-Leave early, 3-Absent, 4-Overtime, 5-On leave, 6-On business trip
     */
    private Integer status;

    /**
     * Late minutes (negative if early)
     */
    private Integer lateMinutes;

    /**
     * Leave early minutes
     */
    private Integer leaveEarlyMinutes;

    /**
     * Overtime minutes
     */
    private Integer overtimeMinutes;

    /**
     * Work hours (actual)
     */
    private Double workHours;

    /**
     * Should work hours (expected)
     */
    private Double shouldWorkHours;

    /**
     * Is abnormal: 0-No, 1-Yes
     */
    private Integer abnormal;

    /**
     * Abnormal reason
     */
    private String abnormalReason;

    /**
     * Approval ID (for abnormal approval)
     */
    private Long approvalId;

    /**
     * Remark
     */
    private String remark;
}