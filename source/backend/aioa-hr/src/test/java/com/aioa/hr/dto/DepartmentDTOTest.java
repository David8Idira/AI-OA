package com.aioa.hr.dto;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * DepartmentDTO 单元测试
 * 覆盖部门数据传输对象的字段getter/setter
 */
@DisplayName("DepartmentDTO Tests")
class DepartmentDTOTest {

    @Test
    @DisplayName("DepartmentDTO 默认构造")
    void defaultConstructor() {
        DepartmentDTO dto = new DepartmentDTO();
        assertNull(dto.getId());
        assertNull(dto.getDepartmentCode());
        assertNull(dto.getDepartmentName());
    }

    @Test
    @DisplayName("设置所有字段后getter返回正确值")
    void allFields() {
        DepartmentDTO dto = new DepartmentDTO();
        dto.setId(1L);
        dto.setDepartmentCode("DEPT001");
        dto.setDepartmentName("技术部");
        dto.setParentId(0L);
        dto.setManager("张三");
        dto.setManagerId("MGR001");
        dto.setLevel(1);
        dto.setSortOrder(10);
        dto.setStatus(1);
        dto.setRemark("测试部门");

        assertEquals(1L, dto.getId());
        assertEquals("DEPT001", dto.getDepartmentCode());
        assertEquals("技术部", dto.getDepartmentName());
        assertEquals(0L, dto.getParentId());
        assertEquals("张三", dto.getManager());
        assertEquals("MGR001", dto.getManagerId());
        assertEquals(1, dto.getLevel());
        assertEquals(10, dto.getSortOrder());
        assertEquals(1, dto.getStatus());
        assertEquals("测试部门", dto.getRemark());
    }

    @Test
    @DisplayName("status=0 表示禁用，status=1 表示启用")
    void status_meanings() {
        DepartmentDTO enabled = new DepartmentDTO();
        enabled.setStatus(1);
        assertEquals(1, enabled.getStatus());

        DepartmentDTO disabled = new DepartmentDTO();
        disabled.setStatus(0);
        assertEquals(0, disabled.getStatus());
    }

    @Test
    @DisplayName("部门可以设置父级部门构成层级关系")
    void parentChildRelationship() {
        DepartmentDTO parent = new DepartmentDTO();
        parent.setId(1L);
        parent.setDepartmentName("总公司");
        parent.setLevel(1);

        DepartmentDTO child = new DepartmentDTO();
        child.setId(2L);
        child.setParentId(parent.getId());
        child.setDepartmentName("子公司");
        child.setLevel(2);

        assertEquals(parent.getId(), child.getParentId());
        assertTrue(child.getLevel() > parent.getLevel());
    }
}