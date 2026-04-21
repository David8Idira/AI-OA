package com.aioa.system.service;

import com.aioa.system.entity.SysDepartment;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.List;

/**
 * Department Service Interface
 */
public interface SysDepartmentService extends IService<SysDepartment> {
    
    /**
     * Get department tree
     */
    List<SysDepartment> getDeptTree();
    
    /**
     * Get department with children
     */
    List<SysDepartment> getDeptTreeWithChildren();
    
    /**
     * Get sub-department IDs
     */
    List<String> getSubDeptIds(String deptId);
}
