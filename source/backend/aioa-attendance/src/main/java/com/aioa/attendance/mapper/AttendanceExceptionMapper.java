package com.aioa.attendance.mapper;

import com.aioa.attendance.entity.AttendanceException;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

/**
 * Attendance Exception Mapper
 */
@Mapper
public interface AttendanceExceptionMapper extends BaseMapper<AttendanceException> {
}