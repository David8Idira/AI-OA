package com.aioa.attendance.service.impl;

import cn.hutool.core.date.DateUtil;
import cn.hutool.core.util.StrUtil;
import com.aioa.attendance.dto.AttendanceQueryDTO;
import com.aioa.attendance.dto.CheckinDTO;
import com.aioa.attendance.entity.AttendanceGroup;
import com.aioa.attendance.entity.AttendanceRecord;
import com.aioa.attendance.entity.AttendanceRule;
import com.aioa.attendance.mapper.AttendanceRecordMapper;
import com.aioa.attendance.service.AttendanceGroupService;
import com.aioa.attendance.service.AttendanceRuleService;
import com.aioa.attendance.service.AttendanceService;
import com.aioa.common.vo.PageResult;
import com.aioa.common.vo.Result;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.math3.util.FastMath;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.temporal.ChronoUnit;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Attendance Service Implementation
 */
@Slf4j
@Service
public class AttendanceServiceImpl extends ServiceImpl<AttendanceRecordMapper, AttendanceRecord> implements AttendanceService {

    private static final double EARTH_RADIUS = 6371000; // Earth radius in meters
    
    @Autowired
    private AttendanceGroupService groupService;
    
    @Autowired
    private AttendanceRuleService ruleService;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public AttendanceRecord checkin(CheckinDTO dto) {
        String userId = dto.getUserId();
        LocalDateTime now = LocalDateTime.now();
        LocalDate today = LocalDate.now();
        
        // Get user's attendance group
        AttendanceGroup group = groupService.getUserAttendanceGroup(userId);
        if (group == null) {
            throw new RuntimeException("User does not belong to any attendance group");
        }
        
        // Get attendance rule
        AttendanceRule rule = ruleService.getById(group.getRuleId());
        if (rule == null) {
            throw new RuntimeException("Attendance rule not found");
        }
        
        // Get today's record
        AttendanceRecord record = getTodayAttendance(userId);
        if (record == null) {
            record = new AttendanceRecord();
            record.setUserId(userId);
            record.setAttendanceDate(today);
            record.setGroupId(group.getId());
            record.setRuleId(rule.getId());
            record.setStatus(0); // Normal initially
        }
        
        // Check location if GPS check-in
        if (dto.getMethod() == 0 && dto.getLatitude() != null && dto.getLongitude() != null) {
            // TODO: Validate location against group's allowed locations
            record.setCheckinLatitude(dto.getLatitude());
            record.setCheckinLongitude(dto.getLongitude());
            record.setCheckinAddress(dto.getAddress());
        }
        
        // Check WiFi if WiFi check-in
        if (dto.getMethod() == 1 && StrUtil.isNotBlank(dto.getWifiMac())) {
            // TODO: Validate WiFi against group's allowed WiFi list
            record.setCheckinWifiMac(dto.getWifiMac());
        }
        
        if (dto.getCheckinType() == 0) { // Check in
            record.setCheckinTime(now);
            record.setCheckinMethod(dto.getMethod());
            record.setCheckinDeviceId(dto.getDeviceId());
            record.setCheckinIp(dto.getIp());
            record.setRemark(dto.getRemark());
            
            // Calculate if late
            if (rule.getWorkStartTime() != null) {
                LocalTime checkinTime = now.toLocalTime();
                LocalTime workStartTime = rule.getWorkStartTime();
                
                if (checkinTime.isAfter(workStartTime)) {
                    long lateMinutes = ChronoUnit.MINUTES.between(workStartTime, checkinTime);
                    if (lateMinutes > rule.getAllowLateMinutes()) {
                        record.setStatus(1); // Late
                        record.setLateMinutes((int) lateMinutes);
                        record.setAbnormal(1);
                        record.setAbnormalReason("Late check-in: " + lateMinutes + " minutes");
                    }
                }
            }
            
        } else if (dto.getCheckinType() == 1) { // Check out
            record.setCheckoutTime(now);
            record.setCheckoutMethod(dto.getMethod());
            record.setCheckoutDeviceId(dto.getDeviceId());
            record.setCheckoutIp(dto.getIp());
            
            // Calculate if leave early
            if (record.getCheckinTime() != null && rule.getWorkEndTime() != null) {
                LocalTime checkoutTime = now.toLocalTime();
                LocalTime workEndTime = rule.getWorkEndTime();
                
                if (checkoutTime.isBefore(workEndTime)) {
                    long leaveEarlyMinutes = ChronoUnit.MINUTES.between(checkoutTime, workEndTime);
                    if (leaveEarlyMinutes > rule.getAllowLeaveEarlyMinutes()) {
                        record.setStatus(2); // Leave early
                        record.setLeaveEarlyMinutes((int) leaveEarlyMinutes);
                        record.setAbnormal(1);
                        if (record.getAbnormalReason() != null) {
                            record.setAbnormalReason(record.getAbnormalReason() + "; Leave early: " + leaveEarlyMinutes + " minutes");
                        } else {
                            record.setAbnormalReason("Leave early: " + leaveEarlyMinutes + " minutes");
                        }
                    }
                }
                
                // Calculate work hours
                long minutes = ChronoUnit.MINUTES.between(record.getCheckinTime(), now);
                record.setWorkHours(minutes / 60.0);
                
                // Calculate overtime
                if (rule.getOvertimeRule() == 1 && checkoutTime.isAfter(workEndTime)) {
                    long overtimeMinutes = ChronoUnit.MINUTES.between(workEndTime, checkoutTime);
                    if (overtimeMinutes >= rule.getMinOvertimeDuration()) {
                        record.setStatus(4); // Overtime
                        record.setOvertimeMinutes((int) overtimeMinutes);
                    }
                }
            }
        }
        
        saveOrUpdate(record);
        log.info("User {} checked in/out: type={}, time={}", userId, dto.getCheckinType(), now);
        
        return record;
    }

    @Override
    public boolean hasCheckedInToday(String userId) {
        LocalDate today = LocalDate.now();
        LambdaQueryWrapper<AttendanceRecord> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(AttendanceRecord::getUserId, userId)
                   .eq(AttendanceRecord::getAttendanceDate, today)
                   .isNotNull(AttendanceRecord::getCheckinTime);
        return count(queryWrapper) > 0;
    }

    @Override
    public AttendanceRecord getTodayAttendance(String userId) {
        LocalDate today = LocalDate.now();
        LambdaQueryWrapper<AttendanceRecord> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(AttendanceRecord::getUserId, userId)
                   .eq(AttendanceRecord::getAttendanceDate, today);
        return getOne(queryWrapper);
    }

    @Override
    public PageResult<AttendanceRecord> getAttendanceRecords(AttendanceQueryDTO query) {
        LambdaQueryWrapper<AttendanceRecord> queryWrapper = new LambdaQueryWrapper<>();
        
        if (StrUtil.isNotBlank(query.getUserId())) {
            queryWrapper.eq(AttendanceRecord::getUserId, query.getUserId());
        }
        
        if (query.getStartDate() != null) {
            queryWrapper.ge(AttendanceRecord::getAttendanceDate, query.getStartDate());
        }
        
        if (query.getEndDate() != null) {
            queryWrapper.le(AttendanceRecord::getAttendanceDate, query.getEndDate());
        }
        
        if (query.getStatus() != null) {
            queryWrapper.eq(AttendanceRecord::getStatus, query.getStatus());
        }
        
        if (query.getAbnormal() != null) {
            queryWrapper.eq(AttendanceRecord::getAbnormal, query.getAbnormal());
        }
        
        if (StrUtil.isNotBlank(query.getKeyword())) {
            queryWrapper.like(AttendanceRecord::getUserName, query.getKeyword())
                       .or()
                       .like(AttendanceRecord::getCheckinAddress, query.getKeyword())
                       .or()
                       .like(AttendanceRecord::getRemark, query.getKeyword());
        }
        
        if (!Boolean.TRUE.equals(query.getIncludeDeleted())) {
            queryWrapper.eq(AttendanceRecord::getDeleted, 0);
        }
        
        // Order by
        if (StrUtil.isNotBlank(query.getOrderBy())) {
            if (query.getOrderBy().contains("desc")) {
                queryWrapper.orderByDesc(AttendanceRecord::getAttendanceDate, AttendanceRecord::getCheckinTime);
            } else {
                queryWrapper.orderByAsc(AttendanceRecord::getAttendanceDate, AttendanceRecord::getCheckinTime);
            }
        } else {
            queryWrapper.orderByDesc(AttendanceRecord::getAttendanceDate, AttendanceRecord::getCheckinTime);
        }
        
        Page<AttendanceRecord> page = new Page<>(query.getPageNum(), query.getPageSize());
        page(page, queryWrapper);
        
        return PageResult.success(page.getRecords(), page.getTotal());
    }

    @Override
    public Map<String, Object> getUserAttendanceSummary(String userId, LocalDate startDate, LocalDate endDate) {
        LambdaQueryWrapper<AttendanceRecord> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(AttendanceRecord::getUserId, userId)
                   .ge(AttendanceRecord::getAttendanceDate, startDate)
                   .le(AttendanceRecord::getAttendanceDate, endDate);
        
        List<AttendanceRecord> records = list(queryWrapper);
        
        Map<String, Object> summary = new HashMap<>();
        summary.put("totalDays", records.size());
        summary.put("normalDays", (int) records.stream().filter(r -> r.getStatus() == 0).count());
        summary.put("lateDays", (int) records.stream().filter(r -> r.getStatus() == 1).count());
        summary.put("leaveEarlyDays", (int) records.stream().filter(r -> r.getStatus() == 2).count());
        summary.put("absentDays", (int) records.stream().filter(r -> r.getStatus() == 3).count());
        summary.put("overtimeDays", (int) records.stream().filter(r -> r.getStatus() == 4).count());
        
        int totalLateMinutes = records.stream().mapToInt(r -> r.getLateMinutes() != null ? r.getLateMinutes() : 0).sum();
        int totalLeaveEarlyMinutes = records.stream().mapToInt(r -> r.getLeaveEarlyMinutes() != null ? r.getLeaveEarlyMinutes() : 0).sum();
        int totalOvertimeMinutes = records.stream().mapToInt(r -> r.getOvertimeMinutes() != null ? r.getOvertimeMinutes() : 0).sum();
        double totalWorkHours = records.stream().mapToDouble(r -> r.getWorkHours() != null ? r.getWorkHours() : 0).sum();
        
        summary.put("totalLateMinutes", totalLateMinutes);
        summary.put("totalLeaveEarlyMinutes", totalLeaveEarlyMinutes);
        summary.put("totalOvertimeMinutes", totalOvertimeMinutes);
        summary.put("totalWorkHours", totalWorkHours);
        
        if (records.size() > 0) {
            summary.put("normalRate", (double) (int) records.stream().filter(r -> r.getStatus() == 0).count() / records.size() * 100);
            summary.put("attendanceScore", calculateAttendanceScore(records));
        } else {
            summary.put("normalRate", 0.0);
            summary.put("attendanceScore", 100.0);
        }
        
        return summary;
    }

    private double calculateAttendanceScore(List<AttendanceRecord> records) {
        double score = 100.0;
        
        for (AttendanceRecord record : records) {
            if (record.getStatus() == 1) { // Late
                score -= 2.0;
            } else if (record.getStatus() == 2) { // Leave early
                score -= 2.0;
            } else if (record.getStatus() == 3) { // Absent
                score -= 10.0;
            } else if (record.getStatus() == 0) { // Normal
                score += 0.5;
            }
        }
        
        return Math.max(0.0, Math.min(100.0, score));
    }

    @Override
    public double calculateDistance(BigDecimal lat1, BigDecimal lon1, BigDecimal lat2, BigDecimal lon2) {
        if (lat1 == null || lon1 == null || lat2 == null || lon2 == null) {
            return Double.MAX_VALUE;
        }
        
        double lat1Rad = Math.toRadians(lat1.doubleValue());
        double lon1Rad = Math.toRadians(lon1.doubleValue());
        double lat2Rad = Math.toRadians(lat2.doubleValue());
        double lon2Rad = Math.toRadians(lon2.doubleValue());
        
        double dLat = lat2Rad - lat1Rad;
        double dLon = lon2Rad - lon1Rad;
        
        double a = FastMath.sin(dLat / 2) * FastMath.sin(dLat / 2) +
                  FastMath.cos(lat1Rad) * FastMath.cos(lat2Rad) *
                  FastMath.sin(dLon / 2) * FastMath.sin(dLon / 2);
        double c = 2 * FastMath.atan2(FastMath.sqrt(a), FastMath.sqrt(1 - a));
        
        return EARTH_RADIUS * c;
    }

    @Override
    public boolean isLocationInRange(BigDecimal userLat, BigDecimal userLon, BigDecimal targetLat, BigDecimal targetLon, Integer maxDistance) {
        if (maxDistance == null || maxDistance <= 0) {
            return true; // No distance restriction
        }
        
        double distance = calculateDistance(userLat, userLon, targetLat, targetLon);
        return distance <= maxDistance;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void processAttendanceForDate(LocalDate date) {
        log.info("Processing attendance for date: {}", date);
        // TODO: Implement daily attendance processing
        // 1. Find users who should work but didn't check in
        // 2. Mark them as absent
        // 3. Process exceptions (leave, business trips)
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void generateAttendanceStats(LocalDate startDate, LocalDate endDate, String userId) {
        log.info("Generating attendance stats for user {} from {} to {}", userId, startDate, endDate);
        // TODO: Implement attendance statistics generation
    }

    @Override
    public Map<String, Object> getMonthlyReport(String userId, Integer year, Integer month) {
        LocalDate startDate = LocalDate.of(year, month, 1);
        LocalDate endDate = startDate.withDayOfMonth(startDate.lengthOfMonth());
        
        Map<String, Object> summary = getUserAttendanceSummary(userId, startDate, endDate);
        
        // Add monthly specific data
        summary.put("year", year);
        summary.put("month", month);
        // Use DateTimeFormatter instead of DateUtil.format for LocalDate
        summary.put("monthName", startDate.getMonth().toString());
        
        return summary;
    }

    @Override
    public Map<String, Object> getDepartmentReport(String deptId, LocalDate startDate, LocalDate endDate) {
        log.info("Generating department report for {} from {} to {}", deptId, startDate, endDate);
        
        Map<String, Object> report = new HashMap<>();
        report.put("deptId", deptId);
        report.put("startDate", startDate);
        report.put("endDate", endDate);
        
        // TODO: Implement department report
        // 1. Get all users in department
        // 2. Calculate summary for each user
        // 3. Calculate department averages
        
        return report;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Result<?> applyAttendanceException(String userId, Integer type, LocalDateTime startTime, LocalDateTime endTime, String reason, String attachments) {
        log.info("User {} applying attendance exception type {} from {} to {}", userId, type, startTime, endTime);
        // TODO: Implement attendance exception application
        // This should create an approval request in the workflow system
        
        return Result.success("Attendance exception application submitted");
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Result<?> approveAttendanceException(Long exceptionId, String approverId, boolean approved, String comment) {
        log.info("Approver {} {} attendance exception {}", approverId, approved ? "approved" : "rejected", exceptionId);
        // TODO: Implement attendance exception approval
        // This should update the exception record and process the attendance records
        
        return Result.success("Attendance exception " + (approved ? "approved" : "rejected"));
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void autoCheckoutForForgotten() {
        log.info("Auto checking out for users who forgot to check out");
        
        LocalDateTime now = LocalDateTime.now();
        LocalDate today = LocalDate.now();
        
        // Find users who checked in but didn't check out today
        LambdaQueryWrapper<AttendanceRecord> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(AttendanceRecord::getAttendanceDate, today)
                   .isNotNull(AttendanceRecord::getCheckinTime)
                   .isNull(AttendanceRecord::getCheckoutTime)
                   .lt(AttendanceRecord::getCheckinTime, now.minusHours(12)); // Checked in more than 12 hours ago
        
        List<AttendanceRecord> records = list(queryWrapper);
        
        for (AttendanceRecord record : records) {
            record.setCheckoutTime(record.getCheckinTime().plusHours(9)); // Assume 9-hour workday
            record.setCheckoutMethod(2); // Manual (auto)
            record.setRemark("Auto checkout: Forgot to check out");
            
            // Recalculate work hours
            if (record.getCheckinTime() != null) {
                long minutes = ChronoUnit.MINUTES.between(record.getCheckinTime(), record.getCheckoutTime());
                record.setWorkHours(minutes / 60.0);
            }
            
            updateById(record);
            log.info("Auto checked out user {} at {}", record.getUserId(), record.getCheckoutTime());
        }
    }
}