package com.aioa.hr.entity;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Department Entity Tests
 */
@DisplayName("Department Entity Tests")
class DepartmentTest {

    @Test
    @DisplayName("Department 应该可以创建并设置所有字段")
    void testDepartmentCreation() {
        Department dept = new Department();
        dept.setId(1L);
        dept.setDepartmentCode("DEPT001");
        dept.setDepartmentName("技术部");
        dept.setParentId(0L);
        dept.setManager("张三");
        dept.setManagerId("MGR001");
        dept.setLevel(1);
        dept.setSortOrder(1);
        dept.setStatus(1);
        dept.setRemark("测试部门");

        assertEquals(1L, dept.getId());
        assertEquals("DEPT001", dept.getDepartmentCode());
        assertEquals("技术部", dept.getDepartmentName());
        assertEquals(0L, dept.getParentId());
        assertEquals("张三", dept.getManager());
        assertEquals("MGR001", dept.getManagerId());
        assertEquals(1, dept.getLevel());
        assertEquals(1, dept.getSortOrder());
        assertEquals(1, dept.getStatus());
        assertEquals("测试部门", dept.getRemark());
    }

    @Test
    @DisplayName("部门status=0为禁用，status=1为启用")
    void testStatusValues() {
        Department enabled = new Department();
        enabled.setStatus(1);
        assertEquals(1, enabled.getStatus());

        Department disabled = new Department();
        disabled.setStatus(0);
        assertEquals(0, disabled.getStatus());
    }

    @Test
    @DisplayName("部门可以设置创建人和创建时间")
    void testAuditFields() {
        Department dept = new Department();
        LocalDateTime now = LocalDateTime.now();
        dept.setCreateBy("admin");
        dept.setCreateTime(now);
        dept.setUpdateBy("admin");
        dept.setUpdateTime(now);

        assertEquals("admin", dept.getCreateBy());
        assertEquals(now, dept.getCreateTime());
        assertEquals("admin", dept.getUpdateBy());
        assertEquals(now, dept.getUpdateTime());
    }

    @Test
    @DisplayName("父级部门ID用于构建树形结构")
    void testParentChildRelationship() {
        Department parent = new Department();
        parent.setId(1L);
        parent.setDepartmentName("总公司");
        parent.setLevel(1);
        parent.setParentId(0L);

        Department child = new Department();
        child.setId(2L);
        child.setDepartmentName("子公司");
        child.setLevel(2);
        child.setParentId(parent.getId());

        assertEquals(0L, parent.getParentId());
        assertEquals(parent.getId(), child.getParentId());
        assertTrue(child.getLevel() > parent.getLevel());
    }

    @Test
    @DisplayName("部门可以设置子部门列表（非数据库字段）")
    void testChildrenField() {
        Department parent = new Department();
        Department child1 = new Department();
        child1.setId(2L);
        child1.setDepartmentName("研发组");

        Department child2 = new Department();
        child2.setId(3L);
        child2.setDepartmentName("测试组");

        parent.setChildren(java.util.List.of(child1, child2));

        assertEquals(2, parent.getChildren().size());
        assertEquals("研发组", parent.getChildren().get(0).getDepartmentName());
        assertEquals("测试组", parent.getChildren().get(1).getDepartmentName());
    }
}