package com.aioa.attendance.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Attendance Query DTO
 */
@Data
@Schema(description = "Attendance Query DTO")
public class AttendanceQueryDTO {

    @Schema(description = "Page number", example = "1")
    private Integer pageNum = 1;

    @Schema(description = "Page size", example = "20")
    private Integer pageSize = 20;

    @Schema(description = "User ID")
    private String userId;

    @Schema(description = "User name")
    private String userName;

    @Schema(description = "Department ID")
    private String deptId;

    @Schema(description = "Group ID")
    private Long groupId;

    @Schema(description = "Rule ID")
    private Long ruleId;

    @Schema(description = "Start date")
    private LocalDate startDate;

    @Schema(description = "End date")
    private LocalDate endDate;

    @Schema(description = "Start time")
    private LocalDateTime startTime;

    @Schema(description = "End time")
    private LocalDateTime endTime;

    @Schema(description = "Status: 0-Normal, 1-Late, 2-Leave early, 3-Absent, 4-Overtime, 5-On leave, 6-On business trip")
    private Integer status;

    @Schema(description = "Is abnormal: 0-No, 1-Yes")
    private Integer abnormal;

    @Schema(description = "Check-in method: 0-GPS, 1-WiFi, 2-Manual, 3-Remote")
    private Integer checkinMethod;

    @Schema(description = "Search keyword")
    private String keyword;

    @Schema(description = "Order by field")
    private String orderBy = "attendance_date desc, checkin_time desc";

    @Schema(description = "Include deleted records")
    private Boolean includeDeleted = false;
}