package com.aioa.hr.dto;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * DepartmentQueryDTO 单元测试
 * 覆盖部门查询条件的字段和默认值
 */
@DisplayName("DepartmentQueryDTO Tests")
class DepartmentQueryDTOTest {

    @Test
    @DisplayName("DepartmentQueryDTO 默认构造")
    void defaultConstructor() {
        DepartmentQueryDTO dto = new DepartmentQueryDTO();
        assertNull(dto.getDepartmentCode());
        assertNull(dto.getDepartmentName());
        assertNull(dto.getParentId());
        assertNull(dto.getManager());
        assertNull(dto.getStatus());
    }

    @Test
    @DisplayName("设置查询条件字段")
    void allFields() {
        DepartmentQueryDTO dto = new DepartmentQueryDTO();
        dto.setDepartmentCode("DEPT%");
        dto.setDepartmentName("技术");
        dto.setParentId(0L);
        dto.setManager("张三");
        dto.setStatus(1);
        dto.setPageNum(2);
        dto.setPageSize(20);

        assertEquals("DEPT%", dto.getDepartmentCode());
        assertEquals("技术", dto.getDepartmentName());
        assertEquals(0L, dto.getParentId());
        assertEquals("张三", dto.getManager());
        assertEquals(1, dto.getStatus());
        assertEquals(2, dto.getPageNum());
        assertEquals(20, dto.getPageSize());
    }

    @Test
    @DisplayName("分页参数默认值")
    void paginationDefaults() {
        DepartmentQueryDTO dto = new DepartmentQueryDTO();
        // 注意: @Data会自动生成getter/setter但不会在构造时设置默认值
        // 这里测试实际getter返回的默认值
        assertEquals(1, dto.getPageNum());
        assertEquals(10, dto.getPageSize());
    }

    @Test
    @DisplayName("可查询禁用状态的部门")
    void queryDisabled() {
        DepartmentQueryDTO dto = new DepartmentQueryDTO();
        dto.setStatus(0);
        assertEquals(0, dto.getStatus());
    }
}