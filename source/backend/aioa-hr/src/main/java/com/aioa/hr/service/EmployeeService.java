package com.aioa.hr.service;

import com.aioa.hr.dto.EmployeeDTO;
import com.aioa.hr.dto.EmployeeQueryDTO;
import com.aioa.hr.entity.Employee;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.List;

/**
 * 员工服务接口
 */
public interface EmployeeService extends IService<Employee> {
    
    /**
     * 新增员工
     */
    boolean addEmployee(EmployeeDTO employeeDTO);
    
    /**
     * 更新员工
     */
    boolean updateEmployee(EmployeeDTO employeeDTO);
    
    /**
     * 删除员工
     */
    boolean deleteEmployee(Long id);
    
    /**
     * 根据ID查询员工
     */
    EmployeeDTO getEmployeeById(Long id);
    
    /**
     * 分页查询员工列表
     */
    IPage<EmployeeDTO> queryEmployeePage(EmployeeQueryDTO queryDTO);
    
    /**
     * 查询所有员工列表
     */
    List<EmployeeDTO> queryEmployeeList(EmployeeQueryDTO queryDTO);
    
    /**
     * 更新员工状态
     */
    boolean updateEmployeeStatus(Long id, Integer status);
    
    /**
     * 根据部门ID查询员工列表
     */
    List<EmployeeDTO> getEmployeesByDepartmentId(Long departmentId);
    
    /**
     * 生成员工编号
     */
    String generateEmployeeNo();
}