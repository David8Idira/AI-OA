package com.aioa.hr.service;

import com.aioa.hr.dto.EmployeeDTO;
import com.aioa.hr.dto.EmployeeQueryDTO;
import com.aioa.hr.entity.Employee;
import com.aioa.hr.mapper.EmployeeMapper;
import com.aioa.hr.service.impl.EmployeeServiceImpl;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * EmployeeServiceImpl 单元测试
 * 覆盖员工增删改查核心业务逻辑
 */
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
@DisplayName("员工服务测试")
class EmployeeServiceImplTest {

    @Mock
    private EmployeeMapper employeeMapper;

    /** Spy on real service so we can stub internal baseMapper calls */
    @Spy
    private EmployeeServiceImpl employeeService;

    private Employee activeEmployee;
    private Employee resignedEmployee;

    @BeforeEach
    void setUp() {
        // Inject the mock mapper into the spied service's baseMapper field
        ReflectionTestUtils.setField(employeeService, "baseMapper", employeeMapper);

        // 构造正式员工
        activeEmployee = new Employee();
        activeEmployee.setId(1L);
        activeEmployee.setEmployeeNo("EMP202401010001");
        activeEmployee.setName("张三");
        activeEmployee.setGender(1);
        activeEmployee.setPhone("13800138000");
        activeEmployee.setEmail("zhangsan@example.com");
        activeEmployee.setIdCard("110101199001011234");
        activeEmployee.setDepartmentId(1L);
        activeEmployee.setDepartmentName("技术部");
        activeEmployee.setPositionId(1L);
        activeEmployee.setPositionName("Java开发");
        activeEmployee.setEntryDate(LocalDate.of(2024, 1, 1));
        activeEmployee.setEmployeeStatus(2); // 正式
        activeEmployee.setStatus(1); // 启用
        activeEmployee.setCreateBy("system");
        activeEmployee.setCreateTime(LocalDateTime.now());

        // 构造离职员工
        resignedEmployee = new Employee();
        resignedEmployee.setId(2L);
        resignedEmployee.setEmployeeNo("EMP202401020002");
        resignedEmployee.setName("李四");
        resignedEmployee.setGender(2);
        resignedEmployee.setPhone("13800138001");
        resignedEmployee.setEmail("lisi@example.com");
        resignedEmployee.setDepartmentId(1L);
        resignedEmployee.setDepartmentName("技术部");
        resignedEmployee.setEmployeeStatus(3); // 离职
        resignedEmployee.setStatus(0); // 禁用
        resignedEmployee.setResignationDate(LocalDate.of(2025, 1, 1));
        resignedEmployee.setCreateBy("system");
        resignedEmployee.setCreateTime(LocalDateTime.now());
    }

    @Test
    @DisplayName("addEmployee - 新增员工成功，自动生成员工编号")
    void testAddEmployee_autoGenerateEmployeeNo() {
        // Given: DTO不包含员工编号
        EmployeeDTO dto = new EmployeeDTO();
        dto.setName("王五");
        dto.setGender(1);
        dto.setPhone("13900000000");
        dto.setDepartmentId(1L);
        dto.setEmployeeStatus(1); // 试用期

        when(employeeMapper.insert(any(Employee.class))).thenReturn(1);

        // When
        boolean result = employeeService.addEmployee(dto);

        // Then
        assertTrue(result);
        ArgumentCaptor<Employee> captor = ArgumentCaptor.forClass(Employee.class);
        verify(employeeMapper).insert(captor.capture());

        Employee saved = captor.getValue();
        assertNotNull(saved.getEmployeeNo());
        assertTrue(saved.getEmployeeNo().startsWith("EMP"));
        assertEquals("system", saved.getCreateBy());
    }

    @Test
    @DisplayName("addEmployee - 使用指定员工编号新增成功")
    void testAddEmployee_withSpecifiedEmployeeNo() {
        // Given
        EmployeeDTO dto = new EmployeeDTO();
        dto.setName("赵六");
        dto.setEmployeeNo("EMP00001"); // 指定编号
        dto.setPhone("13900000001");
        dto.setDepartmentId(1L);

        when(employeeMapper.insert(any(Employee.class))).thenReturn(1);

        // When
        boolean result = employeeService.addEmployee(dto);

        // Then
        assertTrue(result);
        ArgumentCaptor<Employee> captor = ArgumentCaptor.forClass(Employee.class);
        verify(employeeMapper).insert(captor.capture());
        assertEquals("EMP00001", captor.getValue().getEmployeeNo());
    }

    @Test
    @DisplayName("updateEmployee - 更新员工信息成功")
    void testUpdateEmployee_success() {
        // Given
        EmployeeDTO dto = new EmployeeDTO();
        dto.setId(1L);
        dto.setName("张三改名");
        dto.setPositionId(2L);
        dto.setPositionName("高级Java开发");

        when(employeeMapper.updateById(any(Employee.class))).thenReturn(1);

        // When
        boolean result = employeeService.updateEmployee(dto);

        // Then
        assertTrue(result);
        ArgumentCaptor<Employee> captor = ArgumentCaptor.forClass(Employee.class);
        verify(employeeMapper).updateById(captor.capture());
        assertEquals("张三改名", captor.getValue().getName());
        assertEquals("system", captor.getValue().getUpdateBy());
    }

    @Test
    @DisplayName("updateEmployee - 数据库异常时抛出RuntimeException")
    void testUpdateEmployee_exception() {
        // Given
        EmployeeDTO dto = new EmployeeDTO();
        dto.setId(1L);
        dto.setName("测试");

        when(employeeMapper.updateById(any(Employee.class)))
                .thenThrow(new RuntimeException("DB error"));

        // When & Then
        RuntimeException exception = assertThrows(RuntimeException.class,
                () -> employeeService.updateEmployee(dto));
        assertTrue(exception.getMessage().contains("更新员工失败"));
    }

    @Test
    @DisplayName("deleteEmployee - 删除员工成功")
    void testDeleteEmployee_success() {
        // Given: stub removeById to bypass MyBatis Plus tableInfo check
        doReturn(true).when(employeeService).removeById(1L);

        // When
        boolean result = employeeService.deleteEmployee(1L);

        // Then
        assertTrue(result);
        verify(employeeService).removeById(1L);
    }

    @Test
    @DisplayName("deleteEmployee - 数据库异常时抛出RuntimeException")
    void testDeleteEmployee_exception() {
        // Given
        doThrow(new RuntimeException("DB error")).when(employeeService).removeById(1L);

        // When & Then
        RuntimeException exception = assertThrows(RuntimeException.class,
                () -> employeeService.deleteEmployee(1L));
        assertTrue(exception.getMessage().contains("删除员工失败"));
    }

    @Test
    @DisplayName("getEmployeeById - 员工存在时返回DTO")
    void testGetEmployeeById_found() {
        // Given
        when(employeeMapper.selectById(1L)).thenReturn(activeEmployee);

        // When
        EmployeeDTO result = employeeService.getEmployeeById(1L);

        // Then
        assertNotNull(result);
        assertEquals("EMP202401010001", result.getEmployeeNo());
        assertEquals("张三", result.getName());
        assertEquals("13800138000", result.getPhone());
        assertEquals(2, result.getEmployeeStatus()); // 正式
    }

    @Test
    @DisplayName("getEmployeeById - 员工不存在时返回null")
    void testGetEmployeeById_notFound() {
        // Given
        when(employeeMapper.selectById(999L)).thenReturn(null);

        // When
        EmployeeDTO result = employeeService.getEmployeeById(999L);

        // Then
        assertNull(result);
    }

    @Test
    @DisplayName("queryEmployeePage - 分页查询成功")
    void testQueryEmployeePage_success() {
        // Given
        EmployeeQueryDTO queryDTO = new EmployeeQueryDTO();
        queryDTO.setPageNum(1);
        queryDTO.setPageSize(10);
        queryDTO.setDepartmentId(1L);
        queryDTO.setEmployeeStatus(2);

        Page<Employee> page = new Page<>(1, 10);
        page.setRecords(Collections.singletonList(activeEmployee));
        page.setTotal(1);

        when(employeeMapper.selectPage(any(Page.class), any(LambdaQueryWrapper.class)))
                .thenReturn(page);

        // When
        IPage<EmployeeDTO> result = employeeService.queryEmployeePage(queryDTO);

        // Then
        assertNotNull(result);
        assertEquals(1, result.getTotal());
        assertEquals("张三", result.getRecords().get(0).getName());
    }

    @Test
    @DisplayName("queryEmployeePage - 按姓名模糊查询")
    void testQueryEmployeePage_byName() {
        // Given
        EmployeeQueryDTO queryDTO = new EmployeeQueryDTO();
        queryDTO.setPageNum(1);
        queryDTO.setPageSize(10);
        queryDTO.setName("张");

        Page<Employee> page = new Page<>(1, 10);
        page.setRecords(Collections.singletonList(activeEmployee));
        page.setTotal(1);

        when(employeeMapper.selectPage(any(Page.class), any(LambdaQueryWrapper.class)))
                .thenReturn(page);

        // When
        IPage<EmployeeDTO> result = employeeService.queryEmployeePage(queryDTO);

        // Then
        assertNotNull(result);
        assertEquals(1, result.getTotal());
    }

    @Test
    @DisplayName("queryEmployeeList - 列表查询成功")
    void testQueryEmployeeList_success() {
        // Given
        EmployeeQueryDTO queryDTO = new EmployeeQueryDTO();
        queryDTO.setStatus(1);

        when(employeeMapper.selectList(any(LambdaQueryWrapper.class)))
                .thenReturn(Arrays.asList(activeEmployee, resignedEmployee));

        // When
        List<EmployeeDTO> result = employeeService.queryEmployeeList(queryDTO);

        // Then
        assertNotNull(result);
        assertEquals(2, result.size());
    }

    @Test
    @DisplayName("queryEmployeeList - 入职日期范围查询")
    void testQueryEmployeeList_byEntryDateRange() {
        // Given
        EmployeeQueryDTO queryDTO = new EmployeeQueryDTO();
        queryDTO.setEntryDateStart(LocalDate.of(2024, 1, 1));
        queryDTO.setEntryDateEnd(LocalDate.of(2024, 12, 31));

        when(employeeMapper.selectList(any(LambdaQueryWrapper.class)))
                .thenReturn(Collections.singletonList(activeEmployee));

        // When
        List<EmployeeDTO> result = employeeService.queryEmployeeList(queryDTO);

        // Then
        assertNotNull(result);
        assertEquals(1, result.size());
    }

    @Test
    @DisplayName("queryEmployeeList - 查询条件为空时返回所有记录")
    void testQueryEmployeeList_emptyQuery() {
        // Given
        EmployeeQueryDTO emptyQuery = new EmployeeQueryDTO();
        emptyQuery.setPageNum(1);
        emptyQuery.setPageSize(10);

        when(employeeMapper.selectList(any(LambdaQueryWrapper.class)))
                .thenReturn(Collections.singletonList(activeEmployee));

        // When
        List<EmployeeDTO> result = employeeService.queryEmployeeList(emptyQuery);

        // Then
        assertNotNull(result);
        assertEquals(1, result.size());
    }

    @Test
    @DisplayName("updateEmployeeStatus - 更新员工状态成功")
    void testUpdateEmployeeStatus_success() {
        // Given
        when(employeeMapper.updateById(any(Employee.class))).thenReturn(1);

        // When
        boolean result = employeeService.updateEmployeeStatus(1L, 0);

        // Then
        assertTrue(result);
        ArgumentCaptor<Employee> captor = ArgumentCaptor.forClass(Employee.class);
        verify(employeeMapper).updateById(captor.capture());
        assertEquals(0, captor.getValue().getStatus());
        assertEquals("system", captor.getValue().getUpdateBy());
    }

    @Test
    @DisplayName("getEmployeesByDepartmentId - 根据部门ID查询在职员工成功")
    void testGetEmployeesByDepartmentId_success() {
        // Given
        when(employeeMapper.selectList(any(LambdaQueryWrapper.class)))
                .thenReturn(Collections.singletonList(activeEmployee));

        // When
        List<EmployeeDTO> result = employeeService.getEmployeesByDepartmentId(1L);

        // Then
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("张三", result.get(0).getName());
        assertEquals(1, result.get(0).getStatus());
    }

    @Test
    @DisplayName("getEmployeesByDepartmentId - 部门下无员工时返回空列表")
    void testGetEmployeesByDepartmentId_noEmployees() {
        // Given
        when(employeeMapper.selectList(any(LambdaQueryWrapper.class)))
                .thenReturn(Collections.emptyList());

        // When
        List<EmployeeDTO> result = employeeService.getEmployeesByDepartmentId(999L);

        // Then
        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    @Test
    @DisplayName("generateEmployeeNo - 生成格式正确的员工编号")
    void testGenerateEmployeeNo_correctFormat() {
        // When
        String employeeNo = employeeService.generateEmployeeNo();

        // Then
        assertNotNull(employeeNo);
        assertTrue(employeeNo.startsWith("EMP"));
        // EMP(3) + yyyyMMdd(8) + 4位随机数(4) = 15
        assertEquals(15, employeeNo.length());
    }

    @Test
    @DisplayName("addEmployee - 数据库异常时抛出RuntimeException")
    void testAddEmployee_exception() {
        // Given
        EmployeeDTO dto = new EmployeeDTO();
        dto.setName("测试员工");

        when(employeeMapper.insert(any(Employee.class)))
                .thenThrow(new RuntimeException("DB error"));

        // When & Then
        RuntimeException exception = assertThrows(RuntimeException.class,
                () -> employeeService.addEmployee(dto));
        assertTrue(exception.getMessage().contains("新增员工失败"));
    }
}
