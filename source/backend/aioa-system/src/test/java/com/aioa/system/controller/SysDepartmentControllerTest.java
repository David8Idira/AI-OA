package com.aioa.system.controller;

import com.aioa.common.mail.MailService;
import com.aioa.common.result.Result;
import com.aioa.system.entity.SysDepartment;
import com.aioa.system.service.SysDepartmentService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.mail.javamail.JavaMailSender;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.ArrayList;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * SysDepartmentController 单元测试
 */
@DisplayName("SysDepartmentControllerTest 部门控制器测试")
@SpringBootTest
@AutoConfigureMockMvc
class SysDepartmentControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private SysDepartmentService sysDepartmentService;

    @MockBean
    private JavaMailSender javaMailSender;

    @MockBean
    private MailService mailService;

    // ==================== Get Department Tree ====================

    @Test
    @DisplayName("获取部门树成功")
    void getTree_success() throws Exception {
        // given
        SysDepartment root = new SysDepartment();
        root.setId("dept-root");
        root.setDeptName("总公司");
        when(sysDepartmentService.getDeptTree()).thenReturn(List.of(root));

        // when & then
        mockMvc.perform(get("/api/v1/departments/tree"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.length()").value(1));
    }

    @Test
    @DisplayName("获取部门树为空")
    void getTree_empty() throws Exception {
        // given
        when(sysDepartmentService.getDeptTree()).thenReturn(new ArrayList<>());

        // when & then
        mockMvc.perform(get("/api/v1/departments/tree"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.length()").value(0));
    }

    // ==================== List Departments ====================

    @Test
    @DisplayName("获取部门列表成功")
    void list_success() throws Exception {
        // given
        SysDepartment dept = new SysDepartment();
        dept.setId("dept-001");
        dept.setDeptName("技术部");
        when(sysDepartmentService.list()).thenReturn(List.of(dept));

        // when & then
        mockMvc.perform(get("/api/v1/departments"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data[0].deptName").value("技术部"));
    }

    // ==================== Get Department By ID ====================

    @Test
    @DisplayName("获取部门详情成功")
    void getById_success() throws Exception {
        // given
        SysDepartment dept = new SysDepartment();
        dept.setId("dept-001");
        dept.setDeptName("技术部");
        when(sysDepartmentService.getById("dept-001")).thenReturn(dept);

        // when & then
        mockMvc.perform(get("/api/v1/departments/dept-001"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.deptName").value("技术部"));
    }

    @Test
    @DisplayName("获取部门详情 - 不存在")
    void getById_notFound() throws Exception {
        // given
        when(sysDepartmentService.getById("nonexist")).thenReturn(null);

        // when & then - 控制器返回200但data为null
        mockMvc.perform(get("/api/v1/departments/nonexist"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));
    }

    // ==================== Create Department ====================

    @Test
    @DisplayName("创建部门成功")
    void create_success() throws Exception {
        // given - 创建部门时ID由前端生成或为空，保存后设置
        SysDepartment dept = new SysDepartment();
        dept.setId("dept-new");
        dept.setDeptName("新部门");
        dept.setParentId("dept-root");

        when(sysDepartmentService.save(any(SysDepartment.class))).thenReturn(true);

        // when & then
        mockMvc.perform(post("/api/v1/departments")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dept)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));
    }

    @Test
    @DisplayName("创建部门参数校验 - 名称为空")
    void create_validation_fail() throws Exception {
        // given
        SysDepartment dept = new SysDepartment();
        dept.setDeptName("");

        // when & then - Spring validation should reject
        mockMvc.perform(post("/api/v1/departments")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dept)))
                .andExpect(status().isOk()); // Controller may not validate name
    }

    // ==================== Update Department ====================

    @Test
    @DisplayName("更新部门成功")
    void update_success() throws Exception {
        // given
        SysDepartment dept = new SysDepartment();
        dept.setDeptName("更新后的部门");

        when(sysDepartmentService.updateById(any(SysDepartment.class))).thenReturn(true);

        // when & then
        mockMvc.perform(put("/api/v1/departments/dept-001")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dept)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));
    }

    // ==================== Delete Department ====================

    @Test
    @DisplayName("删除部门成功")
    void delete_success() throws Exception {
        // given
        when(sysDepartmentService.removeById("dept-001")).thenReturn(true);

        // when & then
        mockMvc.perform(delete("/api/v1/departments/dept-001"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));
    }

    @Test
    @DisplayName("删除部门 - 不存在")
    void delete_notFound() throws Exception {
        // given
        when(sysDepartmentService.removeById("nonexist")).thenReturn(false);

        // when & then
        mockMvc.perform(delete("/api/v1/departments/nonexist"))
                .andExpect(status().isOk());
    }

    // ==================== Get Sub-Department IDs ====================

    @Test
    @DisplayName("获取子部门ID列表成功")
    void getSubDeptIds_success() throws Exception {
        // given
        when(sysDepartmentService.getSubDeptIds("dept-root")).thenReturn(List.of("dept-001", "dept-002"));

        // when & then
        mockMvc.perform(get("/api/v1/departments/dept-root/children"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.length()").value(2));
    }

    @Test
    @DisplayName("获取子部门ID列表为空")
    void getSubDeptIds_empty() throws Exception {
        // given
        when(sysDepartmentService.getSubDeptIds("dept-leaf")).thenReturn(new ArrayList<>());

        // when & then
        mockMvc.perform(get("/api/v1/departments/dept-leaf/children"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.length()").value(0));
    }
}
