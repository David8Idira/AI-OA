package com.aioa.attendance.service.impl;

import cn.hutool.json.JSONUtil;
import com.aioa.attendance.entity.AttendanceGroup;
import com.aioa.attendance.mapper.AttendanceGroupMapper;
import com.aioa.attendance.service.AttendanceGroupService;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Attendance Group Service Implementation
 */
@Slf4j
@Service
public class AttendanceGroupServiceImpl extends ServiceImpl<AttendanceGroupMapper, AttendanceGroup> implements AttendanceGroupService {

    @Override
    public AttendanceGroup getGroupById(Long id) {
        return getById(id);
    }

    @Override
    public AttendanceGroup getGroupByCode(String groupCode) {
        LambdaQueryWrapper<AttendanceGroup> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(AttendanceGroup::getGroupCode, groupCode);
        return getOne(queryWrapper);
    }

    @Override
    public AttendanceGroup getUserAttendanceGroup(String userId) {
        // TODO: Implement logic to find which group the user belongs to
        // This should check department, position, and direct user assignments
        
        // For now, return the first active group
        LambdaQueryWrapper<AttendanceGroup> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(AttendanceGroup::getStatus, 1);
        List<AttendanceGroup> groups = list(queryWrapper);
        
        if (!groups.isEmpty()) {
            return groups.get(0);
        }
        
        return null;
    }

    @Override
    public boolean isUserInGroup(String userId, Long groupId) {
        AttendanceGroup group = getById(groupId);
        if (group == null) {
            return false;
        }
        
        // Check direct user assignment
        if (group.getUserIds() != null) {
            List<String> userIds = JSONUtil.toList(group.getUserIds(), String.class);
            if (userIds.contains(userId)) {
                return true;
            }
        }
        
        // TODO: Check department and position assignments
        
        return false;
    }
}