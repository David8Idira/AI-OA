package com.aioa.attendance.service;

import com.aioa.attendance.entity.AttendanceStat;
import com.aioa.common.vo.PageResult;
import com.aioa.common.vo.Result;

import java.time.LocalDate;
import java.time.YearMonth;
import java.util.List;
import java.util.Map;

/**
 * Attendance Statistics Service
 */
public interface AttendanceStatisticsService {

    /**
     * Generate daily statistics
     */
    void generateDailyStats(LocalDate date);

    /**
     * Generate monthly statistics
     */
    void generateMonthlyStats(YearMonth yearMonth);

    /**
     * Get user statistics
     */
    List<AttendanceStat> getUserStats(String userId, LocalDate startDate, LocalDate endDate);

    /**
     * Get department statistics
     */
    Map<String, Object> getDepartmentStats(String deptId, LocalDate startDate, LocalDate endDate);

    /**
     * Get company statistics
     */
    Map<String, Object> getCompanyStats(LocalDate startDate, LocalDate endDate);

    /**
     * Get attendance ranking
     */
    PageResult<Map<String, Object>> getAttendanceRanking(LocalDate startDate, LocalDate endDate, Integer pageNum, Integer pageSize);

    /**
     * Get attendance trends
     */
    Map<String, Object> getAttendanceTrends(String userId, LocalDate startDate, LocalDate endDate);

    /**
     * Get abnormal analysis
     */
    Map<String, Object> getAbnormalAnalysis(String deptId, LocalDate startDate, LocalDate endDate);

    /**
     * Export statistics report
     */
    Result<?> exportStatsReport(String userId, LocalDate startDate, LocalDate endDate, String format);

    /**
     * Send statistics notification
     */
    void sendStatsNotification(String userId, LocalDate date);
}