package com.aioa.hr.service;

import com.aioa.hr.dto.DepartmentDTO;
import com.aioa.hr.dto.DepartmentQueryDTO;
import com.aioa.hr.entity.Department;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.List;

/**
 * 部门服务接口
 */
public interface DepartmentService extends IService<Department> {
    
    /**
     * 新增部门
     */
    boolean addDepartment(DepartmentDTO departmentDTO);
    
    /**
     * 更新部门
     */
    boolean updateDepartment(DepartmentDTO departmentDTO);
    
    /**
     * 删除部门
     */
    boolean deleteDepartment(Long id);
    
    /**
     * 根据ID查询部门
     */
    DepartmentDTO getDepartmentById(Long id);
    
    /**
     * 分页查询部门列表
     */
    IPage<DepartmentDTO> queryDepartmentPage(DepartmentQueryDTO queryDTO);
    
    /**
     * 查询所有部门列表
     */
    List<DepartmentDTO> queryDepartmentList(DepartmentQueryDTO queryDTO);
    
    /**
     * 查询部门树形结构
     */
    List<DepartmentDTO> getDepartmentTree();
    
    /**
     * 更新部门状态
     */
    boolean updateDepartmentStatus(Long id, Integer status);
    
    /**
     * 根据父级部门ID查询子部门列表
     */
    List<DepartmentDTO> getDepartmentsByParentId(Long parentId);
    
    /**
     * 生成部门编码
     */
    String generateDepartmentCode();
}