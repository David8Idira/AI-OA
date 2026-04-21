package com.aioa.system.controller;

import com.aioa.common.result.Result;
import com.aioa.system.entity.SysDepartment;
import com.aioa.system.service.SysDepartmentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Department Controller
 */
@RestController
@RequestMapping("/api/v1/departments")
public class SysDepartmentController {

    @Autowired
    private SysDepartmentService sysDepartmentService;

    /**
     * Get department tree
     */
    @GetMapping("/tree")
    public Result<List<SysDepartment>> getTree() {
        return Result.success(sysDepartmentService.getDeptTree());
    }

    /**
     * Get department list
     */
    @GetMapping
    public Result<List<SysDepartment>> list() {
        return Result.success(sysDepartmentService.list());
    }

    /**
     * Get department detail
     */
    @GetMapping("/{id}")
    public Result<SysDepartment> getById(@PathVariable String id) {
        return Result.success(sysDepartmentService.getById(id));
    }

    /**
     * Create department
     */
    @PostMapping
    public Result<String> create(@RequestBody SysDepartment dept) {
        sysDepartmentService.save(dept);
        return Result.success(dept.getId().toString());
    }

    /**
     * Update department
     */
    @PutMapping("/{id}")
    public Result<Void> update(@PathVariable String id, @RequestBody SysDepartment dept) {
        dept.setId(id);
        sysDepartmentService.updateById(dept);
        return Result.success();
    }

    /**
     * Delete department
     */
    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable String id) {
        sysDepartmentService.removeById(id);
        return Result.success();
    }

    /**
     * Get sub-department IDs
     */
    @GetMapping("/{id}/children")
    public Result<List<String>> getSubDeptIds(@PathVariable String id) {
        return Result.success(sysDepartmentService.getSubDeptIds(id));
    }
}
