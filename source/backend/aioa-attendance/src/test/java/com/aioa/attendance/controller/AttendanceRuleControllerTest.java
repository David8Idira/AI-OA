package com.aioa.attendance.controller;

import com.aioa.attendance.dto.AttendanceRuleDTO;
import com.aioa.attendance.entity.AttendanceRule;
import com.aioa.attendance.service.AttendanceRuleService;
import com.aioa.common.vo.PageResult;
import com.aioa.common.vo.Result;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
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

import java.time.LocalTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * AttendanceRuleController 单元测试
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("AttendanceRuleControllerTest 考勤规则控制器测试")
class AttendanceRuleControllerTest {

    private MockMvc mockMvc;
    private ObjectMapper objectMapper;

    @Mock
    private AttendanceRuleService attendanceRuleService;

    @InjectMocks
    private AttendanceRuleController attendanceRuleController;

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());
        MappingJackson2HttpMessageConverter converter = new MappingJackson2HttpMessageConverter();
        converter.setObjectMapper(objectMapper);
        mockMvc = MockMvcBuilders.standaloneSetup(attendanceRuleController)
                .setMessageConverters(converter)
                .build();
    }

    private AttendanceRule createMockRule(Long id, String ruleCode) {
        AttendanceRule rule = new AttendanceRule();
        rule.setId(id);
        rule.setRuleCode(ruleCode);
        rule.setRuleName("标准考勤规则");
        rule.setStatus(1);
        rule.setWorkStartTime(LocalTime.of(9, 0));
        rule.setWorkEndTime(LocalTime.of(18, 0));
        rule.setAllowLateMinutes(5);
        rule.setAllowLeaveEarlyMinutes(5);
        rule.setOvertimeRule(1);
        rule.setMinOvertimeDuration(30);
        return rule;
    }

    private AttendanceRuleDTO createMockRuleDTO() {
        AttendanceRuleDTO dto = new AttendanceRuleDTO();
        dto.setRuleCode("RULE-001");
        dto.setRuleName("标准考勤规则");
        dto.setStatus(1);
        dto.setWorkStartTime(LocalTime.of(9, 0));
        dto.setWorkEndTime(LocalTime.of(18, 0));
        dto.setAllowLateMinutes(5);
        dto.setAllowLeaveEarlyMinutes(5);
        dto.setOvertimeRule(1);
        dto.setMinOvertimeDuration(30);
        return dto;
    }

    @Nested
    @DisplayName("创建考勤规则测试")
    class CreateRuleTests {

        @Test
        @DisplayName("创建考勤规则成功")
        void createRule_success() throws Exception {
            AttendanceRuleDTO dto = createMockRuleDTO();
            AttendanceRule rule = createMockRule(1L, "RULE-001");

            when(attendanceRuleService.createRule(any(AttendanceRuleDTO.class)))
                    .thenReturn(rule);

            mockMvc.perform(post("/api/attendance/rules")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(dto)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(200))
                    .andExpect(jsonPath("$.data.ruleCode").value("RULE-001"));
        }

        @Test
        @DisplayName("创建考勤规则失败 - 服务异常")
        void createRule_serviceError() throws Exception {
            AttendanceRuleDTO dto = createMockRuleDTO();

            when(attendanceRuleService.createRule(any(AttendanceRuleDTO.class)))
                    .thenThrow(new RuntimeException("Rule code already exists"));

            mockMvc.perform(post("/api/attendance/rules")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(dto)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(500));
        }
    }

    @Nested
    @DisplayName("更新考勤规则测试")
    class UpdateRuleTests {

        @Test
        @DisplayName("更新考勤规则成功")
        void updateRule_success() throws Exception {
            AttendanceRuleDTO dto = createMockRuleDTO();
            AttendanceRule rule = createMockRule(1L, "RULE-001");
            rule.setRuleName("更新后的规则名称");

            when(attendanceRuleService.updateRule(eq(1L), any(AttendanceRuleDTO.class)))
                    .thenReturn(rule);

            mockMvc.perform(put("/api/attendance/rules/1")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(dto)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(200))
                    .andExpect(jsonPath("$.data.ruleName").value("更新后的规则名称"));
        }

        @Test
        @DisplayName("更新考勤规则失败 - 规则不存在")
        void updateRule_notFound() throws Exception {
            AttendanceRuleDTO dto = createMockRuleDTO();

            when(attendanceRuleService.updateRule(eq(999L), any(AttendanceRuleDTO.class)))
                    .thenThrow(new RuntimeException("Rule not found"));

            mockMvc.perform(put("/api/attendance/rules/999")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(dto)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(500));
        }
    }

    @Nested
    @DisplayName("删除考勤规则测试")
    class DeleteRuleTests {

        @Test
        @DisplayName("删除考勤规则成功")
        void deleteRule_success() throws Exception {
            when(attendanceRuleService.deleteRule(1L)).thenReturn(true);

            mockMvc.perform(delete("/api/attendance/rules/1"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(200))
                    .andExpect(jsonPath("$.data").value(true));
        }

        @Test
        @DisplayName("删除考勤规则失败 - 规则不存在")
        void deleteRule_notFound() throws Exception {
            when(attendanceRuleService.deleteRule(999L))
                    .thenThrow(new RuntimeException("Rule not found"));

            mockMvc.perform(delete("/api/attendance/rules/999"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(500));
        }
    }

    @Nested
    @DisplayName("查询考勤规则测试")
    class QueryRuleTests {

        @Test
        @DisplayName("根据ID获取考勤规则成功")
        void getRuleById_success() throws Exception {
            AttendanceRule rule = createMockRule(1L, "RULE-001");
            when(attendanceRuleService.getRuleById(1L)).thenReturn(rule);

            mockMvc.perform(get("/api/attendance/rules/1"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(200))
                    .andExpect(jsonPath("$.data.id").value(1))
                    .andExpect(jsonPath("$.data.ruleCode").value("RULE-001"));
        }

        @Test
        @DisplayName("根据ID获取考勤规则 - 不存在")
        void getRuleById_notFound() throws Exception {
            when(attendanceRuleService.getRuleById(999L)).thenReturn(null);

            mockMvc.perform(get("/api/attendance/rules/999"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(500))
                    .andExpect(jsonPath("$.message").value("Rule not found"));
        }

        @Test
        @DisplayName("根据编码获取考勤规则成功")
        void getRuleByCode_success() throws Exception {
            AttendanceRule rule = createMockRule(1L, "RULE-001");
            when(attendanceRuleService.getRuleByCode("RULE-001")).thenReturn(rule);

            mockMvc.perform(get("/api/attendance/rules/code/RULE-001"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(200))
                    .andExpect(jsonPath("$.data.ruleCode").value("RULE-001"));
        }

        @Test
        @DisplayName("根据编码获取考勤规则 - 不存在")
        void getRuleByCode_notFound() throws Exception {
            when(attendanceRuleService.getRuleByCode("NOT-EXIST")).thenReturn(null);

            mockMvc.perform(get("/api/attendance/rules/code/NOT-EXIST"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(500))
                    .andExpect(jsonPath("$.message").value("Rule not found"));
        }
    }

    @Nested
    @DisplayName("规则列表查询测试")
    class ListRulesTests {

        @Test
        @DisplayName("分页查询考勤规则列表成功")
        void listRules_success() throws Exception {
            PageResult<AttendanceRule> pageResult = new PageResult<>();
            pageResult.setRecords(List.of(
                    createMockRule(1L, "RULE-001"),
                    createMockRule(2L, "RULE-002")
            ));
            pageResult.setTotal(2L);

            when(attendanceRuleService.listRules(anyInt(), anyInt(), any(), any()))
                    .thenReturn(pageResult);

            mockMvc.perform(get("/api/attendance/rules/list")
                            .param("pageNum", "1")
                            .param("pageSize", "20"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(200))
                    .andExpect(jsonPath("$.data.records.length()").value(2));
        }

        @Test
        @DisplayName("分页查询考勤规则列表 - 带查询条件")
        void listRules_withFilters() throws Exception {
            PageResult<AttendanceRule> pageResult = new PageResult<>();
            pageResult.setRecords(List.of(createMockRule(1L, "RULE-001")));
            pageResult.setTotal(1L);

            when(attendanceRuleService.listRules(eq(1), eq(10), eq("标准"), eq(1)))
                    .thenReturn(pageResult);

            mockMvc.perform(get("/api/attendance/rules/list")
                            .param("pageNum", "1")
                            .param("pageSize", "10")
                            .param("keyword", "标准")
                            .param("status", "1"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(200))
                    .andExpect(jsonPath("$.data.records.length()").value(1));
        }

        @Test
        @DisplayName("分页查询考勤规则列表 - 空结果")
        void listRules_empty() throws Exception {
            PageResult<AttendanceRule> pageResult = new PageResult<>();
            pageResult.setRecords(List.of());
            pageResult.setTotal(0L);

            when(attendanceRuleService.listRules(anyInt(), anyInt(), any(), any()))
                    .thenReturn(pageResult);

            mockMvc.perform(get("/api/attendance/rules/list")
                            .param("pageNum", "1")
                            .param("pageSize", "20"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(200))
                    .andExpect(jsonPath("$.data.records.length()").value(0));
        }
    }

    @Nested
    @DisplayName("获取适用规则测试")
    class GetApplicableRulesTests {

        @Test
        @DisplayName("获取用户适用规则成功")
        void getApplicableRules_success() throws Exception {
            AttendanceRule rule = createMockRule(1L, "RULE-001");
            when(attendanceRuleService.getApplicableRules(anyString(), any(), any()))
                    .thenReturn(List.of(rule));

            mockMvc.perform(get("/api/attendance/rules/applicable")
                            .param("userId", "user-001")
                            .param("deptId", "dept-001")
                            .param("positionId", "pos-001"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(200))
                    .andExpect(jsonPath("$.data.length()").value(1));
        }

        @Test
        @DisplayName("获取用户适用规则 - 无适用规则")
        void getApplicableRules_noRules() throws Exception {
            when(attendanceRuleService.getApplicableRules(anyString(), any(), any()))
                    .thenReturn(List.of());

            mockMvc.perform(get("/api/attendance/rules/applicable")
                            .param("userId", "user-new"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(200))
                    .andExpect(jsonPath("$.data.length()").value(0));
        }
    }

    @Nested
    @DisplayName("规则状态切换测试")
    class ToggleRuleStatusTests {

        @Test
        @DisplayName("启用规则成功")
        void enableRule_success() throws Exception {
            when(attendanceRuleService.toggleRuleStatus(1L, 1)).thenReturn(true);

            mockMvc.perform(put("/api/attendance/rules/1/status")
                            .param("status", "1"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(200))
                    .andExpect(jsonPath("$.data").value(true));
        }

        @Test
        @DisplayName("禁用规则成功")
        void disableRule_success() throws Exception {
            when(attendanceRuleService.toggleRuleStatus(1L, 0)).thenReturn(true);

            mockMvc.perform(put("/api/attendance/rules/1/status")
                            .param("status", "0"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(200))
                    .andExpect(jsonPath("$.data").value(true));
        }

        @Test
        @DisplayName("切换规则状态 - 无效状态值")
        void toggleRuleStatus_invalidStatus() throws Exception {
            mockMvc.perform(put("/api/attendance/rules/1/status")
                            .param("status", "2"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(500))
                    .andExpect(jsonPath("$.message").value("Invalid status, must be 0 or 1"));
        }
    }
}