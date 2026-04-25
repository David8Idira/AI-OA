package com.aioa.attendance.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Attendance Record VO
 */
@Data
@Schema(description = "Attendance Record VO")
public class AttendanceRecordVO {

    @Schema(description = "Record ID")
    private Long id;

    @Schema(description = "User ID")
    private String userId;

    @Schema(description = "User name")
    private String userName;

    @Schema(description = "Attendance date")
    private LocalDate attendanceDate;

    @Schema(description = "Group ID")
    private Long groupId;

    @Schema(description = "Group name")
    private String groupName;

    @Schema(description = "Rule ID")
    private Long ruleId;

    @Schema(description = "Rule name")
    private String ruleName;

    @Schema(description = "Check-in time")
    private LocalDateTime checkinTime;

    @Schema(description = "Check-out time")
    private LocalDateTime checkoutTime;

    @Schema(description = "Check-in location (latitude)")
    private BigDecimal checkinLatitude;

    @Schema(description = "Check-in location (longitude)")
    private BigDecimal checkinLongitude;

    @Schema(description = "Check-out location (latitude)")
    private BigDecimal checkoutLatitude;

    @Schema(description = "Check-out location (longitude)")
    private BigDecimal checkoutLongitude;

    @Schema(description = "Check-in address")
    private String checkinAddress;

    @Schema(description = "Check-out address")
    private String checkoutAddress;

    @Schema(description = "Check-in WiFi MAC")
    private String checkinWifiMac;

    @Schema(description = "Check-out WiFi MAC")
    private String checkoutWifiMac;

    @Schema(description = "Check-in method: 0-GPS, 1-WiFi, 2-Manual, 3-Remote")
    private Integer checkinMethod;

    @Schema(description = "Check-out method: 0-GPS, 1-WiFi, 2-Manual, 3-Remote")
    private Integer checkoutMethod;

    @Schema(description = "Status: 0-Normal, 1-Late, 2-Leave early, 3-Absent, 4-Overtime, 5-On leave, 6-On business trip")
    private Integer status;

    @Schema(description = "Status text")
    private String statusText;

    @Schema(description = "Late minutes")
    private Integer lateMinutes;

    @Schema(description = "Leave early minutes")
    private Integer leaveEarlyMinutes;

    @Schema(description = "Overtime minutes")
    private Integer overtimeMinutes;

    @Schema(description = "Work hours")
    private Double workHours;

    @Schema(description = "Should work hours")
    private Double shouldWorkHours;

    @Schema(description = "Is abnormal")
    private Boolean abnormal;

    @Schema(description = "Abnormal reason")
    private String abnormalReason;

    @Schema(description = "Approval ID")
    private Long approvalId;

    @Schema(description = "Remark")
    private String remark;

    @Schema(description = "Create time")
    private LocalDateTime createTime;

    @Schema(description = "Update time")
    private LocalDateTime updateTime;
}