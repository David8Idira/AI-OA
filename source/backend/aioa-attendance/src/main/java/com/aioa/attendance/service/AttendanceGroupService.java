package com.aioa.attendance.service;

import com.aioa.attendance.entity.AttendanceGroup;
import com.baomidou.mybatisplus.extension.service.IService;

/**
 * Attendance Group Service Interface
 */
public interface AttendanceGroupService extends IService<AttendanceGroup> {

    /**
     * Get attendance group by ID
     */
    AttendanceGroup getGroupById(Long id);

    /**
     * Get attendance group by code
     */
    AttendanceGroup getGroupByCode(String groupCode);

    /**
     * Get user's attendance group
     */
    AttendanceGroup getUserAttendanceGroup(String userId);
    
    /**
     * Check if user belongs to a group
     */
    boolean isUserInGroup(String userId, Long groupId);
}