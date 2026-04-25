package com.aioa.attendance.controller;

import com.aioa.attendance.dto.AttendanceQueryDTO;
import com.aioa.attendance.dto.CheckinDTO;
import com.aioa.attendance.entity.AttendanceRecord;
import com.aioa.attendance.service.AttendanceService;
import com.aioa.common.vo.PageResult;
import com.aioa.common.vo.Result;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.time.LocalDate;
import java.util.Map;

/**
 * Attendance Controller
 */
@Slf4j
@Validated
@RestController
@RequestMapping("/api/attendance")
@Tag(name = "Attendance Management", description = "Attendance related APIs")
public class AttendanceController {

    @Autowired
    private AttendanceService attendanceService;

    @PostMapping("/checkin")
    @Operation(summary = "Check in/out", description = "User check in or check out")
    public Result<AttendanceRecord> checkin(@Valid @RequestBody CheckinDTO dto) {
        try {
            AttendanceRecord record = attendanceService.checkin(dto);
            return Result.success(record);
        } catch (Exception e) {
            log.error("Checkin error: {}", e.getMessage(), e);
            return Result.error(e.getMessage());
        }
    }

    @GetMapping("/today")
    @Operation(summary = "Get today's attendance", description = "Get current user's today attendance record")
    public Result<AttendanceRecord> getTodayAttendance(@RequestParam String userId) {
        try {
            AttendanceRecord record = attendanceService.getTodayAttendance(userId);
            return Result.success(record);
        } catch (Exception e) {
            log.error("Get today attendance error: {}", e.getMessage(), e);
            return Result.error(e.getMessage());
        }
    }

    @GetMapping("/hasCheckedIn")
    @Operation(summary = "Check if user has checked in today", description = "Check if user has checked in today")
    public Result<Boolean> hasCheckedInToday(@RequestParam String userId) {
        try {
            boolean hasCheckedIn = attendanceService.hasCheckedInToday(userId);
            return Result.success(hasCheckedIn);
        } catch (Exception e) {
            log.error("Check has checked in error: {}", e.getMessage(), e);
            return Result.error(e.getMessage());
        }
    }

    @PostMapping("/list")
    @Operation(summary = "List attendance records", description = "List attendance records by query conditions")
    public Result<PageResult<AttendanceRecord>> listAttendanceRecords(@Valid @RequestBody AttendanceQueryDTO query) {
        try {
            PageResult<AttendanceRecord> result = attendanceService.getAttendanceRecords(query);
            return Result.success(result);
        } catch (Exception e) {
            log.error("List attendance records error: {}", e.getMessage(), e);
            return Result.error(e.getMessage());
        }
    }

    @GetMapping("/summary")
    @Operation(summary = "Get attendance summary", description = "Get user attendance summary for a period")
    public Result<Map<String, Object>> getAttendanceSummary(
            @RequestParam String userId,
            @RequestParam(required = false) LocalDate startDate,
            @RequestParam(required = false) LocalDate endDate) {
        try {
            if (startDate == null) {
                startDate = LocalDate.now().minusDays(30);
            }
            if (endDate == null) {
                endDate = LocalDate.now();
            }
            
            Map<String, Object> summary = attendanceService.getUserAttendanceSummary(userId, startDate, endDate);
            return Result.success(summary);
        } catch (Exception e) {
            log.error("Get attendance summary error: {}", e.getMessage(), e);
            return Result.error(e.getMessage());
        }
    }

    @GetMapping("/monthlyReport")
    @Operation(summary = "Get monthly attendance report", description = "Get user monthly attendance report")
    public Result<Map<String, Object>> getMonthlyReport(
            @RequestParam String userId,
            @RequestParam Integer year,
            @RequestParam Integer month) {
        try {
            Map<String, Object> report = attendanceService.getMonthlyReport(userId, year, month);
            return Result.success(report);
        } catch (Exception e) {
            log.error("Get monthly report error: {}", e.getMessage(), e);
            return Result.error(e.getMessage());
        }
    }

    @GetMapping("/departmentReport")
    @Operation(summary = "Get department attendance report", description = "Get department attendance report")
    public Result<Map<String, Object>> getDepartmentReport(
            @RequestParam String deptId,
            @RequestParam(required = false) LocalDate startDate,
            @RequestParam(required = false) LocalDate endDate) {
        try {
            if (startDate == null) {
                startDate = LocalDate.now().minusDays(30);
            }
            if (endDate == null) {
                endDate = LocalDate.now();
            }
            
            Map<String, Object> report = attendanceService.getDepartmentReport(deptId, startDate, endDate);
            return Result.success(report);
        } catch (Exception e) {
            log.error("Get department report error: {}", e.getMessage(), e);
            return Result.error(e.getMessage());
        }
    }

    @PostMapping("/autoCheckout")
    @Operation(summary = "Auto checkout", description = "Auto checkout for users who forgot to check out (admin only)")
    public Result<Void> autoCheckout() {
        try {
            attendanceService.autoCheckoutForForgotten();
            return Result.success();
        } catch (Exception e) {
            log.error("Auto checkout error: {}", e.getMessage(), e);
            return Result.error(e.getMessage());
        }
    }

    @GetMapping("/calculateDistance")
    @Operation(summary = "Calculate distance", description = "Calculate distance between two GPS points")
    public Result<Double> calculateDistance(
            @RequestParam Double lat1,
            @RequestParam Double lon1,
            @RequestParam Double lat2,
            @RequestParam Double lon2) {
        try {
            double distance = attendanceService.calculateDistance(
                    java.math.BigDecimal.valueOf(lat1),
                    java.math.BigDecimal.valueOf(lon1),
                    java.math.BigDecimal.valueOf(lat2),
                    java.math.BigDecimal.valueOf(lon2));
            return Result.success(distance);
        } catch (Exception e) {
            log.error("Calculate distance error: {}", e.getMessage(), e);
            return Result.error(e.getMessage());
        }
    }
}