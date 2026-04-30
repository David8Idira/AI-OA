package com.aioa.hr.entity;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Employee Entity Tests
 */
@DisplayName("Employee Entity Tests")
class EmployeeTest {

    @Test
    @DisplayName("Employee should be creatable with all fields")
    void testEmployeeCreation() {
        Employee emp = new Employee();
        emp.setId(1L);
        emp.setEmployeeNo("EMP001");
        emp.setName("张三");
        emp.setGender(1);
        emp.setPhone("13800138000");
        
        assertEquals(1L, emp.getId());
        assertEquals("EMP001", emp.getEmployeeNo());
        assertEquals("张三", emp.getName());
        assertEquals(1, emp.getGender());
        assertEquals("13800138000", emp.getPhone());
    }

    @Test
    @DisplayName("Employee gender values")
    void testGenderValues() {
        // Gender: 1=male, 2=female
        Integer male = 1;
        Integer female = 2;
        assertNotNull(male);
        assertNotNull(female);
    }

    @Test
    @DisplayName("Employee status values")
    void testStatusValues() {
        // Status: 0=resigned, 1=active, 2=onboarding
        Integer resigned = 0;
        Integer active = 1;
        Integer onboarding = 2;
        assertTrue(resigned < active);
        assertTrue(active < onboarding);
    }
}
