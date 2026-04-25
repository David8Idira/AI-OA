package com.aioa.hr.mapper;

import com.aioa.hr.entity.Employee;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

/**
 * 员工Mapper接口
 */
@Mapper
public interface EmployeeMapper extends BaseMapper<Employee> {
    
}