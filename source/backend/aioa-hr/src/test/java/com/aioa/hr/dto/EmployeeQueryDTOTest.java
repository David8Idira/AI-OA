package com.aioa.hr.dto;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

/**
 * EmployeeQueryDTO 单元测试
 * 覆盖员工查询条件的字段和默认值
 */
@DisplayName("EmployeeQueryDTO Tests")
class EmployeeQueryDTOTest {

    @Test
    @DisplayName("EmployeeQueryDTO 默认构造")
    void defaultConstructor() {
        EmployeeQueryDTO dto = new EmployeeQueryDTO();
        assertNull(dto.getEmployeeNo());
        assertNull(dto.getName());
        assertNull(dto.getPhone());
        assertNull(dto.getDepartmentId());
    }

    @Test
    @DisplayName("设置所有查询条件字段")
    void allFields() {
        EmployeeQueryDTO dto = new EmployeeQueryDTO();
        dto.setEmployeeNo("EMP001");
        dto.setName("张");
        dto.setPhone("13800138000");
        dto.setDepartmentId(10L);
        dto.setPositionId(20L);
        dto.setEmployeeStatus(2);
        dto.setStatus(1);
        dto.setEntryDateStart(LocalDate.of(2024, 1, 1));
        dto.setEntryDateEnd(LocalDate.of(2024, 12, 31));
        dto.setPageNum(1);
        dto.setPageSize(20);

        assertEquals("EMP001", dto.getEmployeeNo());
        assertEquals("张", dto.getName());
        assertEquals("13800138000", dto.getPhone());
        assertEquals(10L, dto.getDepartmentId());
        assertEquals(20L, dto.getPositionId());
        assertEquals(2, dto.getEmployeeStatus());
        assertEquals(1, dto.getStatus());
        assertEquals(LocalDate.of(2024, 1, 1), dto.getEntryDateStart());
        assertEquals(LocalDate.of(2024, 12, 31), dto.getEntryDateEnd());
        assertEquals(1, dto.getPageNum());
        assertEquals(20, dto.getPageSize());
    }

    @Test
    @DisplayName("分页参数默认值")
    void paginationDefaults() {
        EmployeeQueryDTO dto = new EmployeeQueryDTO();
        assertEquals(1, dto.getPageNum());
        assertEquals(10, dto.getPageSize());
    }

    @Test
    @DisplayName("可按日期范围查询")
    void dateRange() {
        EmployeeQueryDTO dto = new EmployeeQueryDTO();
        LocalDate start = LocalDate.of(2024, 1, 1);
        LocalDate end = LocalDate.of(2024, 6, 30);
        dto.setEntryDateStart(start);
        dto.setEntryDateEnd(end);

        assertEquals(start, dto.getEntryDateStart());
        assertEquals(end, dto.getEntryDateEnd());
        assertTrue(dto.getEntryDateEnd().isAfter(dto.getEntryDateStart()));
    }

    @Test
    @DisplayName("可按员工状态筛选")
    void filterByEmployeeStatus() {
        EmployeeQueryDTO dto = new EmployeeQueryDTO();
        dto.setEmployeeStatus(3); // 离职
        assertEquals(3, dto.getEmployeeStatus());
    }

    @Test
    @DisplayName("可按部门筛选")
    void filterByDepartment() {
        EmployeeQueryDTO dto = new EmployeeQueryDTO();
        dto.setDepartmentId(5L);
        assertEquals(5L, dto.getDepartmentId());
    }
}