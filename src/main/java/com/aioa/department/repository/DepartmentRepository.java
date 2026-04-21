package com.aioa.department.repository;

import com.aioa.department.entity.Department;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * 部门Repository
 */
@Repository
public interface DepartmentRepository extends JpaRepository<Department, Long> {

    /**
     * 根据部门代码查找
     */
    Optional<Department> findByCode(String code);

    /**
     * 根据上级部门ID查找子部门
     */
    List<Department> findByParentId(Long parentId);

    /**
     * 检查部门代码是否存在
     */
    boolean existsByCode(String code);

    /**
     * 根据状态查找部门
     */
    List<Department> findByStatus(Integer status);
}
