package com.aioa.attendance.service;

import com.aioa.attendance.entity.AttendanceException;
import com.aioa.common.vo.PageResult;
import com.aioa.common.vo.Result;
import com.baomidou.mybatisplus.extension.service.IService;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * Attendance Exception Service
 */
public interface AttendanceExceptionService extends IService<AttendanceException> {

    /**
     * Apply for attendance exception
     */
    Result<?> applyException(String userId, Integer type, LocalDateTime startTime, LocalDateTime endTime, String reason, String attachments);

    /**
     * Approve attendance exception
     */
    Result<?> approveException(Long exceptionId, String approverId, boolean approved, String comment);

    /**
     * Get exceptions by user
     */
    List<AttendanceException> getUserExceptions(String userId, LocalDate startDate, LocalDate endDate);

    /**
     * Get exceptions by status
     */
    PageResult<AttendanceException> getExceptionsByStatus(Integer status, Integer pageNum, Integer pageSize);

    /**
     * Get exception statistics
     */
    Map<String, Object> getExceptionStatistics(String userId, LocalDate startDate, LocalDate endDate);

    /**
     * Process approved exceptions
     */
    void processApprovedExceptions();

    /**
     * Auto reject overdue exceptions
     */
    void autoRejectOverdueExceptions();
}