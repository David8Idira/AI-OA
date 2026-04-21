package com.aioa.department.service;

import com.aioa.department.dto.DepartmentDTO;
import com.aioa.department.dto.DepartmentResponseDTO;
import com.aioa.department.entity.Department;
import com.aioa.department.repository.DepartmentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 部门Service
 */
@Service
@RequiredArgsConstructor
@Transactional
public class DepartmentService {

    private final DepartmentRepository departmentRepository;

    /**
     * 创建部门
     */
    public DepartmentResponseDTO createDepartment(DepartmentDTO dto) {
        if (dto.getCode() != null && departmentRepository.existsByCode(dto.getCode())) {
            throw new IllegalArgumentException("部门代码已存在");
        }

        Department department = Department.builder()
                .name(dto.getName())
                .code(dto.getCode())
                .parentId(dto.getParentId())
                .managerId(dto.getManagerId())
                .description(dto.getDescription())
                .sortOrder(dto.getSortOrder() != null ? dto.getSortOrder() : 0)
                .status(dto.getStatus() != null ? dto.getStatus() : 1)
                .build();

        Department saved = departmentRepository.save(department);
        return convertToResponseDTO(saved);
    }

    /**
     * 更新部门
     */
    public DepartmentResponseDTO updateDepartment(Long id, DepartmentDTO dto) {
        Department department = departmentRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("部门不存在"));

        if (dto.getName() != null) department.setName(dto.getName());
        if (dto.getCode() != null) {
            if (departmentRepository.existsByCode(dto.getCode()) &&
                !departmentRepository.findByCode(dto.getCode()).get().getId().equals(id)) {
                throw new IllegalArgumentException("部门代码已存在");
            }
            department.setCode(dto.getCode());
        }
        if (dto.getParentId() != null) department.setParentId(dto.getParentId());
        if (dto.getManagerId() != null) department.setManagerId(dto.getManagerId());
        if (dto.getDescription() != null) department.setDescription(dto.getDescription());
        if (dto.getSortOrder() != null) department.setSortOrder(dto.getSortOrder());
        if (dto.getStatus() != null) department.setStatus(dto.getStatus());

        Department saved = departmentRepository.save(department);
        return convertToResponseDTO(saved);
    }

    /**
     * 删除部门
     */
    public void deleteDepartment(Long id) {
        if (!departmentRepository.existsById(id)) {
            throw new IllegalArgumentException("部门不存在");
        }
        departmentRepository.deleteById(id);
    }

    /**
     * 根据ID查找部门
     */
    @Transactional(readOnly = true)
    public DepartmentResponseDTO getDepartmentById(Long id) {
        Department department = departmentRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("部门不存在"));
        return convertToResponseDTO(department);
    }

    /**
     * 获取所有部门
     */
    @Transactional(readOnly = true)
    public List<DepartmentResponseDTO> getAllDepartments() {
        return departmentRepository.findAll().stream()
                .map(this::convertToResponseDTO)
                .collect(Collectors.toList());
    }

    /**
     * 获取子部门
     */
    @Transactional(readOnly = true)
    public List<DepartmentResponseDTO> getChildDepartments(Long parentId) {
        return departmentRepository.findByParentId(parentId).stream()
                .map(this::convertToResponseDTO)
                .collect(Collectors.toList());
    }

    /**
     * 转换实体为响应DTO
     */
    private DepartmentResponseDTO convertToResponseDTO(Department department) {
        DepartmentResponseDTO dto = new DepartmentResponseDTO();
        dto.setId(department.getId());
        dto.setName(department.getName());
        dto.setCode(department.getCode());
        dto.setParentId(department.getParentId());
        dto.setManagerId(department.getManagerId());
        dto.setDescription(department.getDescription());
        dto.setSortOrder(department.getSortOrder());
        dto.setStatus(department.getStatus());
        dto.setCreateTime(department.getCreateTime() != null ? department.getCreateTime().toString() : null);
        dto.setUpdateTime(department.getUpdateTime() != null ? department.getUpdateTime().toString() : null);
        return dto;
    }
}
