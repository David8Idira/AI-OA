package com.aioa.hr.controller;

import com.aioa.hr.dto.DepartmentDTO;
import com.aioa.hr.dto.DepartmentQueryDTO;
import com.aioa.hr.service.DepartmentService;
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

import java.util.ArrayList;
import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * DepartmentController 单元测试
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("DepartmentControllerTest 部门控制器测试")
class DepartmentControllerTest {

    private MockMvc mockMvc;
    private ObjectMapper objectMapper;

    @Mock
    private DepartmentService departmentService;

    @InjectMocks
    private DepartmentController departmentController;

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper();
        MappingJackson2HttpMessageConverter converter = new MappingJackson2HttpMessageConverter();
        converter.setObjectMapper(objectMapper);
        mockMvc = MockMvcBuilders.standaloneSetup(departmentController)
                .setMessageConverters(converter)
                .build();
    }

    private DepartmentDTO createMockDepartment(Long id) {
        DepartmentDTO dto = new DepartmentDTO();
        dto.setId(id);
        dto.setDepartmentCode("DEPT-001");
        dto.setDepartmentName("技术部");
        dto.setParentId(0L);
        dto.setManager("张三");
        dto.setManagerId("1");
        dto.setStatus(1);
        dto.setSortOrder(1);
        return dto;
    }

    @Nested
    @DisplayName("新增部门测试")
    class AddDepartmentTests {

        @Test
        @DisplayName("新增部门成功")
        void addDepartment_success() throws Exception {
            DepartmentDTO dto = createMockDepartment(null);
            dto.setDepartmentName("新部门");

            when(departmentService.addDepartment(any(DepartmentDTO.class))).thenReturn(true);

            mockMvc.perform(post("/hr/department/add")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(dto)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(200))
                    .andExpect(jsonPath("$.data").value(true));
        }

        @Test
        @DisplayName("新增部门失败 - 服务异常")
        void addDepartment_serviceError() throws Exception {
            DepartmentDTO dto = createMockDepartment(null);

            when(departmentService.addDepartment(any(DepartmentDTO.class)))
                    .thenThrow(new RuntimeException("Department code already exists"));

            mockMvc.perform(post("/hr/department/add")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(dto)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(500))
                    .andExpect(jsonPath("$.message").value("新增部门失败: Department code already exists"));
        }
    }

    @Nested
    @DisplayName("更新部门测试")
    class UpdateDepartmentTests {

        @Test
        @DisplayName("更新部门成功")
        void updateDepartment_success() throws Exception {
            DepartmentDTO dto = createMockDepartment(1L);
            dto.setDepartmentName("更新后的部门名称");

            when(departmentService.updateDepartment(any(DepartmentDTO.class))).thenReturn(true);

            mockMvc.perform(put("/hr/department/update")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(dto)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(200))
                    .andExpect(jsonPath("$.data").value(true));
        }

        @Test
        @DisplayName("更新部门失败 - 部门不存在")
        void updateDepartment_notFound() throws Exception {
            DepartmentDTO dto = createMockDepartment(999L);

            when(departmentService.updateDepartment(any(DepartmentDTO.class)))
                    .thenThrow(new RuntimeException("Department not found"));

            mockMvc.perform(put("/hr/department/update")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(dto)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(500));
        }
    }

    @Nested
    @DisplayName("删除部门测试")
    class DeleteDepartmentTests {

        @Test
        @DisplayName("删除部门成功")
        void deleteDepartment_success() throws Exception {
            when(departmentService.deleteDepartment(1L)).thenReturn(true);

            mockMvc.perform(delete("/hr/department/delete/1"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(200))
                    .andExpect(jsonPath("$.data").value(true));
        }

        @Test
        @DisplayName("删除部门失败 - 部门不存在")
        void deleteDepartment_notFound() throws Exception {
            when(departmentService.deleteDepartment(999L))
                    .thenThrow(new RuntimeException("Department not found"));

            mockMvc.perform(delete("/hr/department/delete/999"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(500));
        }
    }

    @Nested
    @DisplayName("查询部门测试")
    class QueryDepartmentTests {

        @Test
        @DisplayName("根据ID获取部门详情成功")
        void getDepartmentById_success() throws Exception {
            DepartmentDTO dto = createMockDepartment(1L);
            when(departmentService.getDepartmentById(1L)).thenReturn(dto);

            mockMvc.perform(get("/hr/department/get/1"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(200))
                    .andExpect(jsonPath("$.data.id").value(1))
                    .andExpect(jsonPath("$.data.departmentName").value("技术部"));
        }

        @Test
        @DisplayName("根据ID获取部门详情 - 不存在")
        void getDepartmentById_notFound() throws Exception {
            when(departmentService.getDepartmentById(999L)).thenReturn(null);

            mockMvc.perform(get("/hr/department/get/999"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(200))
                    .andExpect(jsonPath("$.data").isEmpty());
        }
    }

    @Nested
    @DisplayName("分页查询部门测试")
    class PageQueryDepartmentTests {

        @Test
        @DisplayName("分页查询部门列表成功")
        void queryDepartmentPage_success() throws Exception {
            DepartmentQueryDTO queryDTO = new DepartmentQueryDTO();
            queryDTO.setDepartmentName("技术");

            IPage<DepartmentDTO> page = new Page<>(1, 20);
            page.setRecords(List.of(createMockDepartment(1L), createMockDepartment(2L)));
            page.setTotal(2L);
            page.setCurrent(1);
            page.setSize(20);

            when(departmentService.queryDepartmentPage(any(DepartmentQueryDTO.class))).thenReturn(page);

            mockMvc.perform(post("/hr/department/page")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(queryDTO)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(200))
                    .andExpect(jsonPath("$.data.records.length()").value(2))
                    .andExpect(jsonPath("$.data.total").value(2));
        }

        @Test
        @DisplayName("分页查询部门列表 - 空结果")
        void queryDepartmentPage_empty() throws Exception {
            DepartmentQueryDTO queryDTO = new DepartmentQueryDTO();

            IPage<DepartmentDTO> page = new Page<>(1, 20);
            page.setRecords(List.of());
            page.setTotal(0L);

            when(departmentService.queryDepartmentPage(any(DepartmentQueryDTO.class))).thenReturn(page);

            mockMvc.perform(post("/hr/department/page")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(queryDTO)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(200))
                    .andExpect(jsonPath("$.data.records.length()").value(0));
        }
    }

    @Nested
    @DisplayName("列表查询部门测试")
    class ListQueryDepartmentTests {

        @Test
        @DisplayName("查询部门列表成功")
        void queryDepartmentList_success() throws Exception {
            DepartmentQueryDTO queryDTO = new DepartmentQueryDTO();

            when(departmentService.queryDepartmentList(any(DepartmentQueryDTO.class)))
                    .thenReturn(List.of(createMockDepartment(1L), createMockDepartment(2L)));

            mockMvc.perform(post("/hr/department/list")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(queryDTO)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(200))
                    .andExpect(jsonPath("$.data.length()").value(2));
        }

        @Test
        @DisplayName("查询部门列表 - 空结果")
        void queryDepartmentList_empty() throws Exception {
            DepartmentQueryDTO queryDTO = new DepartmentQueryDTO();

            when(departmentService.queryDepartmentList(any(DepartmentQueryDTO.class)))
                    .thenReturn(List.of());

            mockMvc.perform(post("/hr/department/list")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(queryDTO)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(200))
                    .andExpect(jsonPath("$.data.length()").value(0));
        }
    }

    @Nested
    @DisplayName("部门树测试")
    class DepartmentTreeTests {

        @Test
        @DisplayName("获取部门树成功")
        void getDepartmentTree_success() throws Exception {
            DepartmentDTO dept1 = createMockDepartment(1L);
            dept1.setDepartmentName("总公司");

            DepartmentDTO dept2 = createMockDepartment(2L);
            dept2.setDepartmentName("技术部");
            dept2.setParentId(1L);

            when(departmentService.getDepartmentTree()).thenReturn(List.of(dept1, dept2));

            mockMvc.perform(get("/hr/department/tree"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(200))
                    .andExpect(jsonPath("$.data.length()").value(2));
        }

        @Test
        @DisplayName("获取部门树 - 空结果")
        void getDepartmentTree_empty() throws Exception {
            when(departmentService.getDepartmentTree()).thenReturn(List.of());

            mockMvc.perform(get("/hr/department/tree"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(200))
                    .andExpect(jsonPath("$.data.length()").value(0));
        }
    }

    @Nested
    @DisplayName("更新部门状态测试")
    class UpdateDepartmentStatusTests {

        @Test
        @DisplayName("更新部门状态成功")
        void updateDepartmentStatus_success() throws Exception {
            when(departmentService.updateDepartmentStatus(1L, 0)).thenReturn(true);

            mockMvc.perform(put("/hr/department/status/1")
                            .param("status", "0"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(200))
                    .andExpect(jsonPath("$.data").value(true));
        }

        @Test
        @DisplayName("更新部门状态失败 - 部门不存在")
        void updateDepartmentStatus_notFound() throws Exception {
            when(departmentService.updateDepartmentStatus(999L, 0))
                    .thenThrow(new RuntimeException("Department not found"));

            mockMvc.perform(put("/hr/department/status/999")
                            .param("status", "0"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(500));
        }
    }

    @Nested
    @DisplayName("根据父级查询部门测试")
    class GetDepartmentsByParentIdTests {

        @Test
        @DisplayName("根据父级ID查询子部门列表成功")
        void getDepartmentsByParentId_success() throws Exception {
            when(departmentService.getDepartmentsByParentId(0L))
                    .thenReturn(List.of(createMockDepartment(1L), createMockDepartment(2L)));

            mockMvc.perform(get("/hr/department/parent/0"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(200))
                    .andExpect(jsonPath("$.data.length()").value(2));
        }

        @Test
        @DisplayName("根据父级ID查询子部门列表 - 无子部门")
        void getDepartmentsByParentId_empty() throws Exception {
            when(departmentService.getDepartmentsByParentId(999L))
                    .thenReturn(List.of());

            mockMvc.perform(get("/hr/department/parent/999"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(200))
                    .andExpect(jsonPath("$.data.length()").value(0));
        }
    }

    @Nested
    @DisplayName("生成部门编码测试")
    class GenerateDepartmentCodeTests {

        @Test
        @DisplayName("生成部门编码成功")
        void generateDepartmentCode_success() throws Exception {
            when(departmentService.generateDepartmentCode()).thenReturn("DEPT-202504280001");

            mockMvc.perform(get("/hr/department/generate-code"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(200))
                    .andExpect(jsonPath("$.data").value("DEPT-202504280001"));
        }

        @Test
        @DisplayName("生成部门编码失败 - 服务异常")
        void generateDepartmentCode_serviceError() throws Exception {
            when(departmentService.generateDepartmentCode())
                    .thenThrow(new RuntimeException("Failed to generate department code"));

            mockMvc.perform(get("/hr/department/generate-code"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(500));
        }
    }
}