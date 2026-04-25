package com.aioa.hr.controller;

import com.aioa.hr.dto.DepartmentDTO;
import com.aioa.hr.dto.DepartmentQueryDTO;
import com.aioa.hr.service.DepartmentService;
import com.aioa.hr.vo.PageResult;
import com.aioa.hr.vo.Result;
import com.baomidou.mybatisplus.core.metadata.IPage;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 部门控制器
 */
@Slf4j
@RestController
@RequestMapping("/hr/department")

public class DepartmentController {
    
    @Autowired
    private DepartmentService departmentService;
    
    @PostMapping("/add")

    public Result<Boolean> addDepartment(@Validated @RequestBody DepartmentDTO departmentDTO) {
        try {
            boolean success = departmentService.addDepartment(departmentDTO);
            return Result.success(success, "新增部门成功");
        } catch (Exception e) {
            log.error("新增部门失败", e);
            return Result.error("新增部门失败: " + e.getMessage());
        }
    }
    
    @PutMapping("/update")

    public Result<Boolean> updateDepartment(@Validated @RequestBody DepartmentDTO departmentDTO) {
        try {
            boolean success = departmentService.updateDepartment(departmentDTO);
            return Result.success(success, "更新部门成功");
        } catch (Exception e) {
            log.error("更新部门失败", e);
            return Result.error("更新部门失败: " + e.getMessage());
        }
    }
    
    @DeleteMapping("/delete/{id}")

    public Result<Boolean> deleteDepartment(@PathVariable Long id) {
        try {
            boolean success = departmentService.deleteDepartment(id);
            return Result.success(success, "删除部门成功");
        } catch (Exception e) {
            log.error("删除部门失败", e);
            return Result.error("删除部门失败: " + e.getMessage());
        }
    }
    
    @GetMapping("/get/{id}")

    public Result<DepartmentDTO> getDepartmentById(@PathVariable Long id) {
        try {
            DepartmentDTO departmentDTO = departmentService.getDepartmentById(id);
            return Result.success(departmentDTO, "查询成功");
        } catch (Exception e) {
            log.error("查询部门详情失败", e);
            return Result.error("查询部门详情失败: " + e.getMessage());
        }
    }
    
    @PostMapping("/page")

    public Result<PageResult<DepartmentDTO>> queryDepartmentPage(@RequestBody DepartmentQueryDTO queryDTO) {
        try {
            IPage<DepartmentDTO> page = departmentService.queryDepartmentPage(queryDTO);
            PageResult<DepartmentDTO> pageResult = new PageResult<>(
                page.getRecords(),
                page.getTotal(),
                page.getCurrent(),
                page.getSize()
            );
            return Result.success(pageResult, "查询成功");
        } catch (Exception e) {
            log.error("分页查询部门列表失败", e);
            return Result.error("分页查询部门列表失败: " + e.getMessage());
        }
    }
    
    @PostMapping("/list")

    public Result<List<DepartmentDTO>> queryDepartmentList(@RequestBody DepartmentQueryDTO queryDTO) {
        try {
            List<DepartmentDTO> list = departmentService.queryDepartmentList(queryDTO);
            return Result.success(list, "查询成功");
        } catch (Exception e) {
            log.error("查询部门列表失败", e);
            return Result.error("查询部门列表失败: " + e.getMessage());
        }
    }
    
    @GetMapping("/tree")

    public Result<List<DepartmentDTO>> getDepartmentTree() {
        try {
            List<DepartmentDTO> tree = departmentService.getDepartmentTree();
            return Result.success(tree, "查询成功");
        } catch (Exception e) {
            log.error("查询部门树失败", e);
            return Result.error("查询部门树失败: " + e.getMessage());
        }
    }
    
    @PutMapping("/status/{id}")

    public Result<Boolean> updateDepartmentStatus(@PathVariable Long id, @RequestParam Integer status) {
        try {
            boolean success = departmentService.updateDepartmentStatus(id, status);
            return Result.success(success, "更新部门状态成功");
        } catch (Exception e) {
            log.error("更新部门状态失败", e);
            return Result.error("更新部门状态失败: " + e.getMessage());
        }
    }
    
    @GetMapping("/parent/{parentId}")

    public Result<List<DepartmentDTO>> getDepartmentsByParentId(@PathVariable Long parentId) {
        try {
            List<DepartmentDTO> list = departmentService.getDepartmentsByParentId(parentId);
            return Result.success(list, "查询成功");
        } catch (Exception e) {
            log.error("根据父级部门ID查询部门列表失败", e);
            return Result.error("根据父级部门ID查询部门列表失败: " + e.getMessage());
        }
    }
    
    @GetMapping("/generate-code")

    public Result<String> generateDepartmentCode() {
        try {
            String departmentCode = departmentService.generateDepartmentCode();
            return Result.success(departmentCode, "生成成功");
        } catch (Exception e) {
            log.error("生成部门编码失败", e);
            return Result.error("生成部门编码失败: " + e.getMessage());
        }
    }
}