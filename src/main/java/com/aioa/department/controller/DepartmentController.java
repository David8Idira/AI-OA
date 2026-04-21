package com.aioa.department.controller;

import com.aioa.department.dto.DepartmentDTO;
import com.aioa.department.dto.DepartmentResponseDTO;
import com.aioa.department.service.DepartmentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 部门Controller
 */
@RestController
@RequestMapping("/departments")
@RequiredArgsConstructor
public class DepartmentController {

    private final DepartmentService departmentService;

    /**
     * 创建部门
     */
    @PostMapping
    public ResponseEntity<DepartmentResponseDTO> createDepartment(@Valid @RequestBody DepartmentDTO dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(departmentService.createDepartment(dto));
    }

    /**
     * 更新部门
     */
    @PutMapping("/{id}")
    public ResponseEntity<DepartmentResponseDTO> updateDepartment(@PathVariable Long id, @Valid @RequestBody DepartmentDTO dto) {
        return ResponseEntity.ok(departmentService.updateDepartment(id, dto));
    }

    /**
     * 删除部门
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteDepartment(@PathVariable Long id) {
        departmentService.deleteDepartment(id);
        return ResponseEntity.noContent().build();
    }

    /**
     * 根据ID获取部门
     */
    @GetMapping("/{id}")
    public ResponseEntity<DepartmentResponseDTO> getDepartmentById(@PathVariable Long id) {
        return ResponseEntity.ok(departmentService.getDepartmentById(id));
    }

    /**
     * 获取所有部门
     */
    @GetMapping
    public ResponseEntity<List<DepartmentResponseDTO>> getAllDepartments() {
        return ResponseEntity.ok(departmentService.getAllDepartments());
    }

    /**
     * 获取子部门
     */
    @GetMapping("/parent/{parentId}")
    public ResponseEntity<List<DepartmentResponseDTO>> getChildDepartments(@PathVariable Long parentId) {
        return ResponseEntity.ok(departmentService.getChildDepartments(parentId));
    }
}
