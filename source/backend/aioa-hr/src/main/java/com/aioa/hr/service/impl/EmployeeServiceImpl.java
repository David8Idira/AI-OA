package com.aioa.hr.service.impl;

import com.aioa.hr.dto.EmployeeDTO;
import com.aioa.hr.dto.EmployeeQueryDTO;
import com.aioa.hr.entity.Employee;
import com.aioa.hr.mapper.EmployeeMapper;
import com.aioa.hr.service.EmployeeService;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 员工服务实现类
 */
@Slf4j
@Service
public class EmployeeServiceImpl extends ServiceImpl<EmployeeMapper, Employee> implements EmployeeService {
    
    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean addEmployee(EmployeeDTO employeeDTO) {
        try {
            Employee employee = new Employee();
            BeanUtils.copyProperties(employeeDTO, employee);
            
            // 生成员工编号
            if (!StringUtils.hasText(employee.getEmployeeNo())) {
                employee.setEmployeeNo(generateEmployeeNo());
            }
            
            // 设置创建人（这里需要从上下文中获取，暂时用固定值）
            employee.setCreateBy("system");
            
            return this.save(employee);
        } catch (Exception e) {
            log.error("新增员工失败", e);
            throw new RuntimeException("新增员工失败", e);
        }
    }
    
    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean updateEmployee(EmployeeDTO employeeDTO) {
        try {
            Employee employee = new Employee();
            BeanUtils.copyProperties(employeeDTO, employee);
            
            // 设置更新人
            employee.setUpdateBy("system");
            
            return this.updateById(employee);
        } catch (Exception e) {
            log.error("更新员工失败", e);
            throw new RuntimeException("更新员工失败", e);
        }
    }
    
    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean deleteEmployee(Long id) {
        try {
            return this.removeById(id);
        } catch (Exception e) {
            log.error("删除员工失败", e);
            throw new RuntimeException("删除员工失败", e);
        }
    }
    
    @Override
    public EmployeeDTO getEmployeeById(Long id) {
        try {
            Employee employee = this.getById(id);
            if (employee == null) {
                return null;
            }
            
            EmployeeDTO employeeDTO = new EmployeeDTO();
            BeanUtils.copyProperties(employee, employeeDTO);
            return employeeDTO;
        } catch (Exception e) {
            log.error("查询员工详情失败", e);
            throw new RuntimeException("查询员工详情失败", e);
        }
    }
    
    @Override
    public IPage<EmployeeDTO> queryEmployeePage(EmployeeQueryDTO queryDTO) {
        try {
            LambdaQueryWrapper<Employee> wrapper = buildQueryWrapper(queryDTO);
            
            Page<Employee> page = new Page<>(queryDTO.getPageNum(), queryDTO.getPageSize());
            IPage<Employee> employeePage = this.page(page, wrapper);
            
            // 转换为DTO
            return employeePage.convert(employee -> {
                EmployeeDTO dto = new EmployeeDTO();
                BeanUtils.copyProperties(employee, dto);
                return dto;
            });
        } catch (Exception e) {
            log.error("分页查询员工列表失败", e);
            throw new RuntimeException("分页查询员工列表失败", e);
        }
    }
    
    @Override
    public List<EmployeeDTO> queryEmployeeList(EmployeeQueryDTO queryDTO) {
        try {
            LambdaQueryWrapper<Employee> wrapper = buildQueryWrapper(queryDTO);
            
            List<Employee> employees = this.list(wrapper);
            return employees.stream().map(employee -> {
                EmployeeDTO dto = new EmployeeDTO();
                BeanUtils.copyProperties(employee, dto);
                return dto;
            }).collect(Collectors.toList());
        } catch (Exception e) {
            log.error("查询员工列表失败", e);
            throw new RuntimeException("查询员工列表失败", e);
        }
    }
    
    @Override
    public boolean updateEmployeeStatus(Long id, Integer status) {
        try {
            Employee employee = new Employee();
            employee.setId(id);
            employee.setStatus(status);
            employee.setUpdateBy("system");
            
            return this.updateById(employee);
        } catch (Exception e) {
            log.error("更新员工状态失败", e);
            throw new RuntimeException("更新员工状态失败", e);
        }
    }
    
    @Override
    public List<EmployeeDTO> getEmployeesByDepartmentId(Long departmentId) {
        try {
            LambdaQueryWrapper<Employee> wrapper = Wrappers.lambdaQuery();
            wrapper.eq(Employee::getDepartmentId, departmentId)
                  .eq(Employee::getStatus, 1)
                  .orderByAsc(Employee::getEmployeeNo);
            
            List<Employee> employees = this.list(wrapper);
            return employees.stream().map(employee -> {
                EmployeeDTO dto = new EmployeeDTO();
                BeanUtils.copyProperties(employee, dto);
                return dto;
            }).collect(Collectors.toList());
        } catch (Exception e) {
            log.error("根据部门ID查询员工列表失败", e);
            throw new RuntimeException("根据部门ID查询员工列表失败", e);
        }
    }
    
    @Override
    public String generateEmployeeNo() {
        try {
            // 生成规则：EMP + 年月日 + 4位随机数
            String datePart = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
            String randomPart = String.format("%04d", (int) (Math.random() * 10000));
            return "EMP" + datePart + randomPart;
        } catch (Exception e) {
            log.error("生成员工编号失败", e);
            // 如果生成失败，返回简单编号
            return "EMP" + System.currentTimeMillis();
        }
    }
    
    /**
     * 构建查询条件
     */
    private LambdaQueryWrapper<Employee> buildQueryWrapper(EmployeeQueryDTO queryDTO) {
        LambdaQueryWrapper<Employee> wrapper = Wrappers.lambdaQuery();
        
        if (StringUtils.hasText(queryDTO.getEmployeeNo())) {
            wrapper.like(Employee::getEmployeeNo, queryDTO.getEmployeeNo());
        }
        
        if (StringUtils.hasText(queryDTO.getName())) {
            wrapper.like(Employee::getName, queryDTO.getName());
        }
        
        if (StringUtils.hasText(queryDTO.getPhone())) {
            wrapper.like(Employee::getPhone, queryDTO.getPhone());
        }
        
        if (queryDTO.getDepartmentId() != null) {
            wrapper.eq(Employee::getDepartmentId, queryDTO.getDepartmentId());
        }
        
        if (queryDTO.getPositionId() != null) {
            wrapper.eq(Employee::getPositionId, queryDTO.getPositionId());
        }
        
        if (queryDTO.getEmployeeStatus() != null) {
            wrapper.eq(Employee::getEmployeeStatus, queryDTO.getEmployeeStatus());
        }
        
        if (queryDTO.getStatus() != null) {
            wrapper.eq(Employee::getStatus, queryDTO.getStatus());
        }
        
        if (queryDTO.getEntryDateStart() != null) {
            wrapper.ge(Employee::getEntryDate, queryDTO.getEntryDateStart());
        }
        
        if (queryDTO.getEntryDateEnd() != null) {
            wrapper.le(Employee::getEntryDate, queryDTO.getEntryDateEnd());
        }
        
        // 默认按创建时间倒序排序
        wrapper.orderByDesc(Employee::getCreateTime);
        
        return wrapper;
    }
}