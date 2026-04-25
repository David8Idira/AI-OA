package com.aioa.hr.mapper;

import com.aioa.hr.entity.Department;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

/**
 * 部门Mapper接口
 */
@Mapper
public interface DepartmentMapper extends BaseMapper<Department> {
    
}