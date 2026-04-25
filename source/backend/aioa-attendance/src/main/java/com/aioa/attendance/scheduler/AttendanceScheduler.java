package com.aioa.attendance.scheduler;

import com.aioa.attendance.service.AttendanceExceptionService;
import com.aioa.attendance.service.AttendanceService;
import com.aioa.attendance.service.AttendanceStatisticsService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDate;

/**
 * Attendance Scheduler
 */
@Slf4j
@Component
public class AttendanceScheduler {

    @Autowired
    private AttendanceService attendanceService;

    @Autowired
    private AttendanceExceptionService exceptionService;

    @Autowired
    private AttendanceStatisticsService statisticsService;

    /**
     * Daily attendance processing at 23:59
     */
    @Scheduled(cron = "0 59 23 * * ?")
    public void processDailyAttendance() {
        log.info("Starting daily attendance processing");
        try {
            LocalDate yesterday = LocalDate.now().minusDays(1);
            attendanceService.processAttendanceForDate(yesterday);
            log.info("Daily attendance processing completed");
        } catch (Exception e) {
            log.error("Daily attendance processing failed", e);
        }
    }

    /**
     * Auto checkout at 20:00 every day
     */
    @Scheduled(cron = "0 0 20 * * ?")
    public void autoCheckout() {
        log.info("Starting auto checkout for forgotten users");
        try {
            attendanceService.autoCheckoutForForgotten();
            log.info("Auto checkout completed");
        } catch (Exception e) {
            log.error("Auto checkout failed", e);
        }
    }

    /**
     * Process approved exceptions every hour
     */
    @Scheduled(cron = "0 0 * * * ?")
    public void processApprovedExceptions() {
        log.info("Starting to process approved exceptions");
        try {
            exceptionService.processApprovedExceptions();
            log.info("Approved exceptions processing completed");
        } catch (Exception e) {
            log.error("Approved exceptions processing failed", e);
        }
    }

    /**
     * Auto reject overdue exceptions at 00:30 every day
     */
    @Scheduled(cron = "0 30 0 * * ?")
    public void autoRejectOverdueExceptions() {
        log.info("Starting to auto reject overdue exceptions");
        try {
            exceptionService.autoRejectOverdueExceptions();
            log.info("Auto reject overdue exceptions completed");
        } catch (Exception e) {
            log.error("Auto reject overdue exceptions failed", e);
        }
    }

    /**
     * Generate monthly statistics on 1st day of month at 02:00
     */
    @Scheduled(cron = "0 0 2 1 * ?")
    public void generateMonthlyStatistics() {
        log.info("Starting to generate monthly statistics");
        try {
            LocalDate lastMonth = LocalDate.now().minusMonths(1);
            statisticsService.generateMonthlyStats(
                    java.time.YearMonth.from(lastMonth));
            log.info("Monthly statistics generation completed");
        } catch (Exception e) {
            log.error("Monthly statistics generation failed", e);
        }
    }

    /**
     * Health check every 30 minutes
     */
    @Scheduled(cron = "0 */30 * * * ?")
    public void healthCheck() {
        log.debug("Attendance system health check");
        // TODO: Add health check logic
    }
}