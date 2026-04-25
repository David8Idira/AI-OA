package com.aioa.attendance.service;

import com.aioa.attendance.dto.AttendanceQueryDTO;
import com.aioa.attendance.dto.CheckinDTO;
import com.aioa.attendance.entity.AttendanceRecord;
import com.aioa.common.vo.PageResult;
import com.aioa.common.vo.Result;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * Attendance Service Interface
 */
public interface AttendanceService {

    /**
     * Check in/out
     */
    AttendanceRecord checkin(CheckinDTO dto);

    /**
     * Check if user has checked in today
     */
    boolean hasCheckedInToday(String userId);

    /**
     * Get today's attendance record
     */
    AttendanceRecord getTodayAttendance(String userId);

    /**
     * Get attendance records by query
     */
    PageResult<AttendanceRecord> getAttendanceRecords(AttendanceQueryDTO query);

    /**
     * Get user attendance summary
     */
    Map<String, Object> getUserAttendanceSummary(String userId, LocalDate startDate, LocalDate endDate);

    /**
     * Calculate distance between two GPS points (meters)
     */
    double calculateDistance(BigDecimal lat1, BigDecimal lon1, BigDecimal lat2, BigDecimal lon2);

    /**
     * Check if location is within allowed range
     */
    boolean isLocationInRange(BigDecimal userLat, BigDecimal userLon, BigDecimal targetLat, BigDecimal targetLon, Integer maxDistance);

    /**
     * Process attendance for a date (calculate status)
     */
    void processAttendanceForDate(LocalDate date);

    /**
     * Generate attendance statistics
     */
    void generateAttendanceStats(LocalDate startDate, LocalDate endDate, String userId);

    /**
     * Get monthly attendance report
     */
    Map<String, Object> getMonthlyReport(String userId, Integer year, Integer month);

    /**
     * Get department attendance report
     */
    Map<String, Object> getDepartmentReport(String deptId, LocalDate startDate, LocalDate endDate);

    /**
     * Apply attendance exception (leave, business trip, etc.)
     */
    Result<?> applyAttendanceException(String userId, Integer type, LocalDateTime startTime, LocalDateTime endTime, String reason, String attachments);

    /**
     * Approve attendance exception
     */
    Result<?> approveAttendanceException(Long exceptionId, String approverId, boolean approved, String comment);

    /**
     * Auto check out for users who forgot to check out
     */
    void autoCheckoutForForgotten();
}