package com.aioa.attendance.service.impl;

import cn.hutool.core.date.DateUtil;
import cn.hutool.core.util.StrUtil;
import com.aioa.attendance.entity.AttendanceException;
import com.aioa.attendance.entity.AttendanceRecord;
import com.aioa.attendance.mapper.AttendanceExceptionMapper;
import com.aioa.attendance.service.AttendanceExceptionService;
import com.aioa.attendance.service.AttendanceService;
import com.aioa.common.vo.PageResult;
import com.aioa.common.vo.Result;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Attendance Exception Service Implementation
 */
@Slf4j
@Service
public class AttendanceExceptionServiceImpl extends ServiceImpl<AttendanceExceptionMapper, AttendanceException> implements AttendanceExceptionService {

    @Autowired
    private AttendanceService attendanceService;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Result<?> applyException(String userId, Integer type, LocalDateTime startTime, LocalDateTime endTime, String reason, String attachments) {
        AttendanceException exception = new AttendanceException();
        exception.setUserId(userId);
        exception.setType(type);
        exception.setStartTime(startTime);
        exception.setEndTime(endTime);
        exception.setReason(reason);
        exception.setAttachments(attachments);
        exception.setStatus(0); // Pending
        exception.setApplicationDate(LocalDate.now());
        
        // Validate time range
        if (endTime.isBefore(startTime)) {
            return Result.error("End time must be after start time");
        }
        
        long hours = ChronoUnit.HOURS.between(startTime, endTime);
        if (hours > 24 * 7) { // Max 1 week
            return Result.error("Exception duration cannot exceed 7 days");
        }
        
        save(exception);
        log.info("User {} applied attendance exception type {}: {}", userId, type, exception.getId());
        
        // TODO: Trigger workflow approval
        
        return Result.success("Attendance exception application submitted", exception);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Result<?> approveException(Long exceptionId, String approverId, boolean approved, String comment) {
        AttendanceException exception = getById(exceptionId);
        if (exception == null) {
            return Result.error("Attendance exception not found");
        }
        
        if (exception.getStatus() != 0) {
            return Result.error("Exception already processed");
        }
        
        exception.setStatus(approved ? 1 : 2); // 1-Approved, 2-Rejected
        exception.setApproverId(approverId);
        exception.setApprovalTime(LocalDateTime.now());
        exception.setApprovalComment(comment);
        
        updateById(exception);
        
        if (approved) {
            // Process the exception - update attendance records
            processException(exception);
        }
        
        log.info("Approver {} {} attendance exception {}", approverId, approved ? "approved" : "rejected", exceptionId);
        
        return Result.success("Attendance exception " + (approved ? "approved" : "rejected"));
    }

    @Override
    public List<AttendanceException> getUserExceptions(String userId, LocalDate startDate, LocalDate endDate) {
        LambdaQueryWrapper<AttendanceException> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(AttendanceException::getUserId, userId);
        
        if (startDate != null) {
            queryWrapper.ge(AttendanceException::getStartTime, startDate.atStartOfDay());
        }
        
        if (endDate != null) {
            queryWrapper.le(AttendanceException::getEndTime, endDate.atTime(23, 59, 59));
        }
        
        queryWrapper.orderByDesc(AttendanceException::getCreateTime);
        
        return list(queryWrapper);
    }

    @Override
    public PageResult<AttendanceException> getExceptionsByStatus(Integer status, Integer pageNum, Integer pageSize) {
        LambdaQueryWrapper<AttendanceException> queryWrapper = new LambdaQueryWrapper<>();
        
        if (status != null) {
            queryWrapper.eq(AttendanceException::getStatus, status);
        }
        
        queryWrapper.orderByDesc(AttendanceException::getApplyTime);
        
        Page<AttendanceException> page = new Page<>(pageNum, pageSize);
        page(page, queryWrapper);
        
        return PageResult.success(page.getRecords(), page.getTotal());
    }

    @Override
    public Map<String, Object> getExceptionStatistics(String userId, LocalDate startDate, LocalDate endDate) {
        LambdaQueryWrapper<AttendanceException> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(AttendanceException::getUserId, userId);
        
        if (startDate != null) {
            queryWrapper.ge(AttendanceException::getStartTime, startDate.atStartOfDay());
        }
        
        if (endDate != null) {
            queryWrapper.le(AttendanceException::getEndTime, endDate.atTime(23, 59, 59));
        }
        
        List<AttendanceException> exceptions = list(queryWrapper);
        
        Map<String, Object> stats = new HashMap<>();
        stats.put("totalExceptions", exceptions.size());
        stats.put("pendingExceptions", (int) exceptions.stream().filter(e -> e.getStatus() == 0).count());
        stats.put("approvedExceptions", (int) exceptions.stream().filter(e -> e.getStatus() == 1).count());
        stats.put("rejectedExceptions", (int) exceptions.stream().filter(e -> e.getStatus() == 2).count());
        
        // Group by type
        Map<Integer, Long> typeCount = new HashMap<>();
        for (AttendanceException exception : exceptions) {
            typeCount.merge(exception.getType(), 1L, Long::sum);
        }
        stats.put("typeDistribution", typeCount);
        
        return stats;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void processApprovedExceptions() {
        log.info("Processing approved attendance exceptions");
        
        LambdaQueryWrapper<AttendanceException> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(AttendanceException::getStatus, 1) // Approved
                   .eq(AttendanceException::getProcessed, 0) // Not processed
                   .lt(AttendanceException::getEndTime, LocalDateTime.now()); // Ended
        
        List<AttendanceException> exceptions = list(queryWrapper);
        
        for (AttendanceException exception : exceptions) {
            processException(exception);
            exception.setProcessed(1);
            updateById(exception);
        }
    }

    @Override
    public void autoRejectOverdueExceptions() {
        log.info("Auto rejecting overdue attendance exceptions");
        
        LocalDateTime oneWeekAgo = LocalDateTime.now().minusDays(7);
        
        LambdaQueryWrapper<AttendanceException> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(AttendanceException::getStatus, 0) // Pending
                   .lt(AttendanceException::getApplyTime, oneWeekAgo); // Applied more than 1 week ago
        
        List<AttendanceException> exceptions = list(queryWrapper);
        
        for (AttendanceException exception : exceptions) {
            exception.setStatus(2); // Rejected
            exception.setApprovalComment("Auto rejected: No action taken within 7 days");
            exception.setApprovalTime(LocalDateTime.now());
            updateById(exception);
            log.info("Auto rejected overdue exception: {}", exception.getId());
        }
    }

    private void processException(AttendanceException exception) {
        // For leave or business trip exceptions, update attendance records
        if (exception.getType() == 0 || exception.getType() == 1) {
            // 0-Makeup card, 1-Leave
            // TODO: Update attendance records for the exception period
            // Mark records as on leave or business trip
            
            log.info("Processing exception {} for user {}: {} to {}", 
                    exception.getId(), exception.getUserId(), 
                    exception.getStartTime(), exception.getEndTime());
        }
    }
}