package com.aioa.hr.controller;

import com.aioa.hr.dto.EmployeeDTO;
import com.aioa.hr.dto.EmployeeQueryDTO;
import com.aioa.hr.service.EmployeeService;
import com.aioa.hr.vo.PageResult;
import com.aioa.hr.vo.Result;
import com.baomidou.mybatisplus.core.metadata.IPage;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 员工控制器
 */
@Slf4j
@RestController
@RequestMapping("/hr/employee")

public class EmployeeController {
    
    @Autowired
    private EmployeeService employeeService;
    
    @PostMapping("/add")

    public Result<Boolean> addEmployee(@Validated @RequestBody EmployeeDTO employeeDTO) {
        try {
            boolean success = employeeService.addEmployee(employeeDTO);
            return Result.success(success, "新增员工成功");
        } catch (Exception e) {
            log.error("新增员工失败", e);
            return Result.error("新增员工失败: " + e.getMessage());
        }
    }
    
    @PutMapping("/update")

    public Result<Boolean> updateEmployee(@Validated @RequestBody EmployeeDTO employeeDTO) {
        try {
            boolean success = employeeService.updateEmployee(employeeDTO);
            return Result.success(success, "更新员工成功");
        } catch (Exception e) {
            log.error("更新员工失败", e);
            return Result.error("更新员工失败: " + e.getMessage());
        }
    }
    
    @DeleteMapping("/delete/{id}")

    public Result<Boolean> deleteEmployee(@PathVariable Long id) {
        try {
            boolean success = employeeService.deleteEmployee(id);
            return Result.success(success, "删除员工成功");
        } catch (Exception e) {
            log.error("删除员工失败", e);
            return Result.error("删除员工失败: " + e.getMessage());
        }
    }
    
    @GetMapping("/get/{id}")

    public Result<EmployeeDTO> getEmployeeById(@PathVariable Long id) {
        try {
            EmployeeDTO employeeDTO = employeeService.getEmployeeById(id);
            return Result.success(employeeDTO, "查询成功");
        } catch (Exception e) {
            log.error("查询员工详情失败", e);
            return Result.error("查询员工详情失败: " + e.getMessage());
        }
    }
    
    @PostMapping("/page")

    public Result<PageResult<EmployeeDTO>> queryEmployeePage(@RequestBody EmployeeQueryDTO queryDTO) {
        try {
            IPage<EmployeeDTO> page = employeeService.queryEmployeePage(queryDTO);
            PageResult<EmployeeDTO> pageResult = new PageResult<>(
                page.getRecords(),
                page.getTotal(),
                page.getCurrent(),
                page.getSize()
            );
            return Result.success(pageResult, "查询成功");
        } catch (Exception e) {
            log.error("分页查询员工列表失败", e);
            return Result.error("分页查询员工列表失败: " + e.getMessage());
        }
    }
    
    @PostMapping("/list")

    public Result<List<EmployeeDTO>> queryEmployeeList(@RequestBody EmployeeQueryDTO queryDTO) {
        try {
            List<EmployeeDTO> list = employeeService.queryEmployeeList(queryDTO);
            return Result.success(list, "查询成功");
        } catch (Exception e) {
            log.error("查询员工列表失败", e);
            return Result.error("查询员工列表失败: " + e.getMessage());
        }
    }
    
    @PutMapping("/status/{id}")

    public Result<Boolean> updateEmployeeStatus(@PathVariable Long id, @RequestParam Integer status) {
        try {
            boolean success = employeeService.updateEmployeeStatus(id, status);
            return Result.success(success, "更新员工状态成功");
        } catch (Exception e) {
            log.error("更新员工状态失败", e);
            return Result.error("更新员工状态失败: " + e.getMessage());
        }
    }
    
    @GetMapping("/department/{departmentId}")

    public Result<List<EmployeeDTO>> getEmployeesByDepartmentId(@PathVariable Long departmentId) {
        try {
            List<EmployeeDTO> list = employeeService.getEmployeesByDepartmentId(departmentId);
            return Result.success(list, "查询成功");
        } catch (Exception e) {
            log.error("根据部门ID查询员工列表失败", e);
            return Result.error("根据部门ID查询员工列表失败: " + e.getMessage());
        }
    }
    
    @GetMapping("/generate-no")

    public Result<String> generateEmployeeNo() {
        try {
            String employeeNo = employeeService.generateEmployeeNo();
            return Result.success(employeeNo, "生成成功");
        } catch (Exception e) {
            log.error("生成员工编号失败", e);
            return Result.error("生成员工编号失败: " + e.getMessage());
        }
    }
}