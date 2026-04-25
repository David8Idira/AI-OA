package com.aioa.attendance.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.time.LocalTime;
import java.util.List;

/**
 * Attendance Rule DTO
 */
@Data
@Schema(description = "Attendance Rule DTO")
public class AttendanceRuleDTO {

    @Schema(description = "Rule ID")
    private Long id;

    @NotBlank(message = "Rule name cannot be empty")
    @Schema(description = "Rule name", required = true)
    private String ruleName;

    @NotBlank(message = "Rule code cannot be empty")
    @Schema(description = "Rule code", required = true)
    private String ruleCode;

    @NotNull(message = "Status cannot be null")
    @Schema(description = "Status: 0-Disabled, 1-Enabled", required = true)
    private Integer status;

    @NotNull(message = "Work start time cannot be null")
    @Schema(description = "Work start time", required = true, example = "09:00:00")
    private LocalTime workStartTime;

    @NotNull(message = "Work end time cannot be null")
    @Schema(description = "Work end time", required = true, example = "18:00:00")
    private LocalTime workEndTime;

    @Schema(description = "Allow late minutes", example = "15")
    private Integer allowLateMinutes = 15;

    @Schema(description = "Allow leave early minutes", example = "15")
    private Integer allowLeaveEarlyMinutes = 15;

    @Schema(description = "Overtime rule: 0-No overtime, 1-Calculate after work time, 2-Fixed overtime", example = "1")
    private Integer overtimeRule = 1;

    @Schema(description = "Overtime start time")
    private LocalTime overtimeStartTime;

    @Schema(description = "Minimum overtime duration (minutes)", example = "30")
    private Integer minOvertimeDuration = 30;

    @Schema(description = "Is flexible work time: 0-No, 1-Yes", example = "0")
    private Integer flexibleWork = 0;

    @Schema(description = "Flexible work duration (hours)", example = "8.0")
    private Double flexibleWorkHours = 8.0;

    @Schema(description = "Is rest day included: 0-No, 1-Yes", example = "0")
    private Integer includeRestDays = 0;

    @Schema(description = "Weekdays to apply (e.g., [1,2,3,4,5] for Mon-Fri)")
    private List<Integer> weekdays;

    @Schema(description = "Holidays to exclude")
    private List<String> excludeHolidays;

    @Schema(description = "Special work days")
    private List<String> specialWorkDays;

    @Schema(description = "Applicable department IDs")
    private List<String> deptIds;

    @Schema(description = "Applicable position IDs")
    private List<String> positionIds;

    @Schema(description = "Remark")
    private String remark;

    @Schema(description = "Timezone", example = "Asia/Shanghai")
    private String timezone = "Asia/Shanghai";
}