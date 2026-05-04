package com.aioa.hr.controller;

import com.aioa.hr.dto.EmployeeDTO;
import com.aioa.hr.dto.EmployeeQueryDTO;
import com.aioa.hr.service.EmployeeService;
import com.aioa.hr.vo.PageResult;
import com.aioa.hr.vo.Result;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.time.LocalDate;
import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * EmployeeController 单元测试
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("EmployeeControllerTest 员工控制器测试")
class EmployeeControllerTest {

    private MockMvc mockMvc;
    private ObjectMapper objectMapper;

    @Mock
    private EmployeeService employeeService;

    @InjectMocks
    private EmployeeController employeeController;

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper();
        MappingJackson2HttpMessageConverter converter = new MappingJackson2HttpMessageConverter();
        converter.setObjectMapper(objectMapper);
        mockMvc = MockMvcBuilders.standaloneSetup(employeeController)
                .setMessageConverters(converter)
                .build();
    }

    private EmployeeDTO createMockEmployee(Long id) {
        EmployeeDTO dto = new EmployeeDTO();
        dto.setId(id);
        dto.setEmployeeNo("EMP-001");
        dto.setName("张三");
        dto.setGender(1);
        dto.setPhone("13800138000");
        dto.setEmail("zhangsan@example.com");
        dto.setDepartmentId(1L);
        dto.setDepartmentName("技术部");
        dto.setPositionId(1L);
        dto.setPositionName("Java开发工程师");
        dto.setStatus(1);
        return dto;
    }

    @Nested
    @DisplayName("新增员工测试")
    class AddEmployeeTests {

        @Test
        @DisplayName("新增员工成功")
        void addEmployee_success() throws Exception {
            EmployeeDTO dto = createMockEmployee(null);
            dto.setName("新员工");

            when(employeeService.addEmployee(any(EmployeeDTO.class))).thenReturn(true);

            mockMvc.perform(post("/hr/employee/add")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(dto)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(200))
                    .andExpect(jsonPath("$.data").value(true));
        }

        @Test
        @DisplayName("新增员工失败 - 服务异常")
        void addEmployee_serviceError() throws Exception {
            EmployeeDTO dto = createMockEmployee(null);

            when(employeeService.addEmployee(any(EmployeeDTO.class)))
                    .thenThrow(new RuntimeException("Employee number already exists"));

            mockMvc.perform(post("/hr/employee/add")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(dto)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(500))
                    .andExpect(jsonPath("$.message").value("新增员工失败: Employee number already exists"));
        }
    }

    @Nested
    @DisplayName("更新员工测试")
    class UpdateEmployeeTests {

        @Test
        @DisplayName("更新员工成功")
        void updateEmployee_success() throws Exception {
            EmployeeDTO dto = createMockEmployee(1L);
            dto.setName("更新后的名称");

            when(employeeService.updateEmployee(any(EmployeeDTO.class))).thenReturn(true);

            mockMvc.perform(put("/hr/employee/update")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(dto)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(200))
                    .andExpect(jsonPath("$.data").value(true));
        }

        @Test
        @DisplayName("更新员工失败 - 员工不存在")
        void updateEmployee_notFound() throws Exception {
            EmployeeDTO dto = createMockEmployee(999L);

            when(employeeService.updateEmployee(any(EmployeeDTO.class)))
                    .thenThrow(new RuntimeException("Employee not found"));

            mockMvc.perform(put("/hr/employee/update")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(dto)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(500));
        }
    }

    @Nested
    @DisplayName("删除员工测试")
    class DeleteEmployeeTests {

        @Test
        @DisplayName("删除员工成功")
        void deleteEmployee_success() throws Exception {
            when(employeeService.deleteEmployee(1L)).thenReturn(true);

            mockMvc.perform(delete("/hr/employee/delete/1"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(200))
                    .andExpect(jsonPath("$.data").value(true));
        }

        @Test
        @DisplayName("删除员工失败 - 员工不存在")
        void deleteEmployee_notFound() throws Exception {
            when(employeeService.deleteEmployee(999L))
                    .thenThrow(new RuntimeException("Employee not found"));

            mockMvc.perform(delete("/hr/employee/delete/999"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(500));
        }
    }

    @Nested
    @DisplayName("查询员工测试")
    class QueryEmployeeTests {

        @Test
        @DisplayName("根据ID获取员工详情成功")
        void getEmployeeById_success() throws Exception {
            EmployeeDTO dto = createMockEmployee(1L);
            when(employeeService.getEmployeeById(1L)).thenReturn(dto);

            mockMvc.perform(get("/hr/employee/get/1"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(200))
                    .andExpect(jsonPath("$.data.id").value(1))
                    .andExpect(jsonPath("$.data.name").value("张三"));
        }

        @Test
        @DisplayName("根据ID获取员工详情 - 不存在")
        void getEmployeeById_notFound() throws Exception {
            when(employeeService.getEmployeeById(999L)).thenReturn(null);

            mockMvc.perform(get("/hr/employee/get/999"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(200))
                    .andExpect(jsonPath("$.data").isEmpty());
        }
    }

    @Nested
    @DisplayName("分页查询员工测试")
    class PageQueryEmployeeTests {

        @Test
        @DisplayName("分页查询员工列表成功")
        void queryEmployeePage_success() throws Exception {
            EmployeeQueryDTO queryDTO = new EmployeeQueryDTO();
            queryDTO.setName("张");

            IPage<EmployeeDTO> page = new Page<>(1, 20);
            page.setRecords(List.of(createMockEmployee(1L), createMockEmployee(2L)));
            page.setTotal(2L);
            page.setCurrent(1);
            page.setSize(20);

            when(employeeService.queryEmployeePage(any(EmployeeQueryDTO.class))).thenReturn(page);

            mockMvc.perform(post("/hr/employee/page")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(queryDTO)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(200))
                    .andExpect(jsonPath("$.data.records.length()").value(2))
                    .andExpect(jsonPath("$.data.total").value(2));
        }

        @Test
        @DisplayName("分页查询员工列表 - 空结果")
        void queryEmployeePage_empty() throws Exception {
            EmployeeQueryDTO queryDTO = new EmployeeQueryDTO();

            IPage<EmployeeDTO> page = new Page<>(1, 20);
            page.setRecords(List.of());
            page.setTotal(0L);

            when(employeeService.queryEmployeePage(any(EmployeeQueryDTO.class))).thenReturn(page);

            mockMvc.perform(post("/hr/employee/page")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(queryDTO)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(200))
                    .andExpect(jsonPath("$.data.records.length()").value(0));
        }
    }

    @Nested
    @DisplayName("列表查询员工测试")
    class ListQueryEmployeeTests {

        @Test
        @DisplayName("查询员工列表成功")
        void queryEmployeeList_success() throws Exception {
            EmployeeQueryDTO queryDTO = new EmployeeQueryDTO();
            queryDTO.setDepartmentId(1L);

            when(employeeService.queryEmployeeList(any(EmployeeQueryDTO.class)))
                    .thenReturn(List.of(createMockEmployee(1L), createMockEmployee(2L)));

            mockMvc.perform(post("/hr/employee/list")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(queryDTO)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(200))
                    .andExpect(jsonPath("$.data.length()").value(2));
        }

        @Test
        @DisplayName("查询员工列表 - 空结果")
        void queryEmployeeList_empty() throws Exception {
            EmployeeQueryDTO queryDTO = new EmployeeQueryDTO();

            when(employeeService.queryEmployeeList(any(EmployeeQueryDTO.class)))
                    .thenReturn(List.of());

            mockMvc.perform(post("/hr/employee/list")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(queryDTO)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(200))
                    .andExpect(jsonPath("$.data.length()").value(0));
        }
    }

    @Nested
    @DisplayName("更新员工状态测试")
    class UpdateEmployeeStatusTests {

        @Test
        @DisplayName("更新员工状态成功")
        void updateEmployeeStatus_success() throws Exception {
            when(employeeService.updateEmployeeStatus(1L, 0)).thenReturn(true);

            mockMvc.perform(put("/hr/employee/status/1")
                            .param("status", "0"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(200))
                    .andExpect(jsonPath("$.data").value(true));
        }

        @Test
        @DisplayName("更新员工状态失败 - 员工不存在")
        void updateEmployeeStatus_notFound() throws Exception {
            when(employeeService.updateEmployeeStatus(999L, 0))
                    .thenThrow(new RuntimeException("Employee not found"));

            mockMvc.perform(put("/hr/employee/status/999")
                            .param("status", "0"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(500));
        }
    }

    @Nested
    @DisplayName("根据部门查询员工测试")
    class GetEmployeesByDepartmentTests {

        @Test
        @DisplayName("根据部门ID查询员工列表成功")
        void getEmployeesByDepartmentId_success() throws Exception {
            when(employeeService.getEmployeesByDepartmentId(1L))
                    .thenReturn(List.of(createMockEmployee(1L), createMockEmployee(2L)));

            mockMvc.perform(get("/hr/employee/department/1"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(200))
                    .andExpect(jsonPath("$.data.length()").value(2));
        }

        @Test
        @DisplayName("根据部门ID查询员工列表 - 部门无员工")
        void getEmployeesByDepartmentId_empty() throws Exception {
            when(employeeService.getEmployeesByDepartmentId(999L))
                    .thenReturn(List.of());

            mockMvc.perform(get("/hr/employee/department/999"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(200))
                    .andExpect(jsonPath("$.data.length()").value(0));
        }
    }

    @Nested
    @DisplayName("生成员工编号测试")
    class GenerateEmployeeNoTests {

        @Test
        @DisplayName("生成员工编号成功")
        void generateEmployeeNo_success() throws Exception {
            when(employeeService.generateEmployeeNo()).thenReturn("EMP-202504280001");

            mockMvc.perform(get("/hr/employee/generate-no"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(200))
                    .andExpect(jsonPath("$.data").value("EMP-202504280001"));
        }

        @Test
        @DisplayName("生成员工编号失败 - 服务异常")
        void generateEmployeeNo_serviceError() throws Exception {
            when(employeeService.generateEmployeeNo())
                    .thenThrow(new RuntimeException("Failed to generate employee number"));

            mockMvc.perform(get("/hr/employee/generate-no"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(500));
        }
    }
}