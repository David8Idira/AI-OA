package com.aioa.hr.dto;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

/**
 * EmployeeDTO 单元测试
 * 覆盖员工数据传输对象的字段getter/setter
 */
@DisplayName("EmployeeDTO Tests")
class EmployeeDTOTest {

    @Test
    @DisplayName("EmployeeDTO 默认构造")
    void defaultConstructor() {
        EmployeeDTO dto = new EmployeeDTO();
        assertNull(dto.getId());
        assertNull(dto.getEmployeeNo());
        assertNull(dto.getName());
    }

    @Test
    @DisplayName("设置所有字段后getter返回正确值")
    void allFields() {
        EmployeeDTO dto = new EmployeeDTO();
        dto.setId(1L);
        dto.setEmployeeNo("EMP001");
        dto.setName("张三");
        dto.setGender(1);
        dto.setPhone("13800138000");
        dto.setEmail("zhangsan@example.com");
        dto.setIdCard("110101199001011234");
        dto.setDepartmentId(10L);
        dto.setDepartmentName("技术部");
        dto.setPositionId(20L);
        dto.setPositionName("工程师");
        dto.setEntryDate(LocalDate.of(2024, 1, 15));
        dto.setRegularizationDate(LocalDate.of(2024, 4, 15));
        dto.setResignationDate(null);
        dto.setEmployeeStatus(2);
        dto.setStatus(1);
        dto.setRemark("正式员工");

        assertEquals(1L, dto.getId());
        assertEquals("EMP001", dto.getEmployeeNo());
        assertEquals("张三", dto.getName());
        assertEquals(1, dto.getGender());
        assertEquals("13800138000", dto.getPhone());
        assertEquals("zhangsan@example.com", dto.getEmail());
        assertEquals("110101199001011234", dto.getIdCard());
        assertEquals(10L, dto.getDepartmentId());
        assertEquals("技术部", dto.getDepartmentName());
        assertEquals(20L, dto.getPositionId());
        assertEquals("工程师", dto.getPositionName());
        assertEquals(LocalDate.of(2024, 1, 15), dto.getEntryDate());
        assertEquals(LocalDate.of(2024, 4, 15), dto.getRegularizationDate());
        assertNull(dto.getResignationDate());
        assertEquals(2, dto.getEmployeeStatus());
        assertEquals(1, dto.getStatus());
        assertEquals("正式员工", dto.getRemark());
    }

    @Test
    @DisplayName("gender: 1=男, 2=女")
    void genderValues() {
        EmployeeDTO male = new EmployeeDTO();
        male.setGender(1);
        assertEquals(1, male.getGender());

        EmployeeDTO female = new EmployeeDTO();
        female.setGender(2);
        assertEquals(2, female.getGender());
    }

    @Test
    @DisplayName("employeeStatus: 1=试用, 2=正式, 3=离职")
    void employeeStatusValues() {
        EmployeeDTO probation = new EmployeeDTO();
        probation.setEmployeeStatus(1);
        assertEquals(1, probation.getEmployeeStatus());

        EmployeeDTO regular = new EmployeeDTO();
        regular.setEmployeeStatus(2);
        assertEquals(2, regular.getEmployeeStatus());

        EmployeeDTO resigned = new EmployeeDTO();
        resigned.setEmployeeStatus(3);
        assertEquals(3, resigned.getEmployeeStatus());
    }

    @Test
    @DisplayName("status: 0=禁用, 1=启用")
    void statusValues() {
        EmployeeDTO enabled = new EmployeeDTO();
        enabled.setStatus(1);
        assertEquals(1, enabled.getStatus());

        EmployeeDTO disabled = new EmployeeDTO();
        disabled.setStatus(0);
        assertEquals(0, disabled.getStatus());
    }
}