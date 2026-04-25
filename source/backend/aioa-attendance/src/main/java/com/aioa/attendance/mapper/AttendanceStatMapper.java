package com.aioa.attendance.mapper;

import com.aioa.attendance.entity.AttendanceStat;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

/**
 * Attendance Statistics Mapper
 */
@Mapper
public interface AttendanceStatMapper extends BaseMapper<AttendanceStat> {
}