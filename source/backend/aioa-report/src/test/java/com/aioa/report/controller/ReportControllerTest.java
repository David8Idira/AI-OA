package com.aioa.report.controller;

import com.aioa.common.result.Result;
import com.aioa.report.dto.ExportReportDTO;
import com.aioa.report.dto.GenerateReportDTO;
import com.aioa.report.dto.ReportQueryDTO;
import com.aioa.report.entity.ReportTemplate;
import com.aioa.report.service.ReportService;
import com.aioa.report.service.ReportTemplateService;
import com.aioa.report.vo.PageResult;
import com.aioa.report.vo.ReportVO;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * ReportController 单元测试
 * 测试报表控制器所有REST API端点
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("ReportControllerTest 报表控制器测试")
class ReportControllerTest {

    private MockMvc mockMvc;
    private ObjectMapper objectMapper;

    @Mock
    private ReportService reportService;

    @Mock
    private ReportTemplateService reportTemplateService;

    @InjectMocks
    private ReportController reportController;

    private ReportVO createTestReportVO() {
        ReportVO vo = new ReportVO();
        vo.setId("report-001");
        vo.setTitle("测试报表");
        vo.setType("DAILY");
        vo.setStatus(2);
        vo.setCreatorId("user-001");
        return vo;
    }

    private GenerateReportDTO createGenerateDTO() {
        GenerateReportDTO dto = new GenerateReportDTO();
        dto.setType("DAILY");
        dto.setTitle("日报测试");
        dto.setPeriodStart(LocalDateTime.now().minusDays(1));
        dto.setPeriodEnd(LocalDateTime.now());
        dto.setDataSource("测试数据源");
        dto.setAiModel("gpt-4o-mini");
        dto.setShareScope("PRIVATE");
        return dto;
    }

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(reportController).build();
        objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());
    }

    // ==================== Report APIs ====================

    @Test
    @DisplayName("POST /api/v1/report/generate - 生成报表成功")
    void generateReport_success() throws Exception {
        ReportVO reportVO = createTestReportVO();
        when(reportService.generateReport(anyString(), any(GenerateReportDTO.class))).thenReturn(reportVO);

        mockMvc.perform(post("/api/v1/report/generate")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createGenerateDTO()))
                        .requestAttr("userId", "user-001"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.title").value("测试报表"));
    }

    @Test
    @DisplayName("POST /api/v1/report/regenerate/{reportId} - 重新生成报表成功")
    void regenerateReport_success() throws Exception {
        ReportVO reportVO = createTestReportVO();
        when(reportService.regenerateReport(anyString(), anyString())).thenReturn(reportVO);

        mockMvc.perform(post("/api/v1/report/regenerate/report-001")
                        .requestAttr("userId", "user-001"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.id").value("report-001"));
    }

    @Test
    @DisplayName("POST /api/v1/report/regenerate/{reportId} - 报表不存在")
    void regenerateReport_notFound() throws Exception {
        when(reportService.regenerateReport(anyString(), anyString())).thenReturn(null);

        mockMvc.perform(post("/api/v1/report/regenerate/non-existing")
                        .requestAttr("userId", "user-001"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data").isEmpty());
    }

    @Test
    @DisplayName("GET /api/v1/report/list - 获取报表列表成功")
    void getReportList_success() throws Exception {
        ReportVO reportVO = createTestReportVO();
        PageResult<ReportVO> pageResult = new PageResult<>();
        pageResult.setRecords(Arrays.asList(reportVO));
        pageResult.setTotal(1L);

        when(reportService.getReportList(anyString(), any(ReportQueryDTO.class))).thenReturn(pageResult);

        mockMvc.perform(get("/api/v1/report/list")
                        .requestAttr("userId", "user-001"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.records[0].title").value("测试报表"));
    }

    @Test
    @DisplayName("GET /api/v1/report/list - 空列表")
    void getReportList_empty() throws Exception {
        PageResult<ReportVO> pageResult = new PageResult<>();
        pageResult.setRecords(Arrays.asList());
        pageResult.setTotal(0L);

        when(reportService.getReportList(anyString(), any(ReportQueryDTO.class))).thenReturn(pageResult);

        mockMvc.perform(get("/api/v1/report/list")
                        .requestAttr("userId", "user-001"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.records").isEmpty());
    }

    @Test
    @DisplayName("GET /api/v1/report/{reportId} - 获取报表详情成功")
    void getReportDetail_success() throws Exception {
        ReportVO reportVO = createTestReportVO();
        when(reportService.getReportDetail(anyString(), anyString())).thenReturn(reportVO);

        mockMvc.perform(get("/api/v1/report/report-001")
                        .requestAttr("userId", "user-001"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.id").value("report-001"));
    }

    @Test
    @DisplayName("GET /api/v1/report/{reportId} - 报表不存在")
    void getReportDetail_notFound() throws Exception {
        when(reportService.getReportDetail(anyString(), anyString())).thenReturn(null);

        mockMvc.perform(get("/api/v1/report/non-existing")
                        .requestAttr("userId", "user-001"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data").isEmpty());
    }

    @Test
    @DisplayName("DELETE /api/v1/report/{reportId} - 删除报表成功")
    void deleteReport_success() throws Exception {
        when(reportService.deleteReport(anyString(), anyString())).thenReturn(true);

        mockMvc.perform(delete("/api/v1/report/report-001")
                        .requestAttr("userId", "user-001"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data").value(true));
    }

    @Test
    @DisplayName("DELETE /api/v1/report/{reportId} - 无权限删除")
    void deleteReport_unauthorized() throws Exception {
        when(reportService.deleteReport(anyString(), anyString())).thenReturn(false);

        mockMvc.perform(delete("/api/v1/report/report-001")
                        .requestAttr("userId", "user-002"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data").value(false));
    }

    @Test
    @DisplayName("POST /api/v1/report/export - 导出报表成功")
    void exportReport_success() throws Exception {
        when(reportService.exportReport(anyString(), anyString(), anyString()))
                .thenReturn("/exports/report_report-001.pdf");

        ExportReportDTO dto = new ExportReportDTO();
        dto.setReportId("report-001");
        dto.setFormat("PDF");

        mockMvc.perform(post("/api/v1/report/export")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto))
                        .requestAttr("userId", "user-001"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data").value("/exports/report_report-001.pdf"));
    }

    @Test
    @DisplayName("POST /api/v1/report/export - 导出失败")
    void exportReport_failed() throws Exception {
        when(reportService.exportReport(anyString(), anyString(), anyString())).thenReturn(null);

        ExportReportDTO dto = new ExportReportDTO();
        dto.setReportId("non-existing");
        dto.setFormat("PDF");

        mockMvc.perform(post("/api/v1/report/export")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto))
                        .requestAttr("userId", "user-001"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data").isEmpty());
    }

    // ==================== Template APIs ====================

    @Test
    @DisplayName("GET /api/v1/report/templates - 获取所有模板")
    void getTemplates_success() throws Exception {
        ReportTemplate template = new ReportTemplate();
        template.setId("template-001");
        template.setName("日报模板");
        template.setType("DAILY");

        when(reportTemplateService.list()).thenReturn(Arrays.asList(template));

        mockMvc.perform(get("/api/v1/report/templates"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data[0].name").value("日报模板"));
    }

    @Test
    @DisplayName("GET /api/v1/report/templates?type=DAILY - 按类型筛选模板")
    void getTemplates_byType() throws Exception {
        ReportTemplate template = new ReportTemplate();
        template.setId("template-001");
        template.setName("日报模板");
        template.setType("DAILY");

        when(reportTemplateService.getActiveByType("DAILY")).thenReturn(Arrays.asList(template));

        mockMvc.perform(get("/api/v1/report/templates")
                        .param("type", "DAILY"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data[0].type").value("DAILY"));
    }

    @Test
    @DisplayName("GET /api/v1/report/templates/{templateId} - 获取模板详情成功")
    void getTemplateDetail_success() throws Exception {
        ReportTemplate template = new ReportTemplate();
        template.setId("template-001");
        template.setName("日报模板");

        when(reportTemplateService.getById("template-001")).thenReturn(template);

        mockMvc.perform(get("/api/v1/report/templates/template-001"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.name").value("日报模板"));
    }

    @Test
    @DisplayName("GET /api/v1/report/templates/{templateId} - 模板不存在")
    void getTemplateDetail_notFound() throws Exception {
        when(reportTemplateService.getById("non-existing")).thenReturn(null);

        mockMvc.perform(get("/api/v1/report/templates/non-existing"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(500))
                .andExpect(jsonPath("$.message").value("模板不存在"));
    }

    @Test
    @DisplayName("POST /api/v1/report/templates - 创建模板成功")
    void createTemplate_success() throws Exception {
        ReportTemplate template = new ReportTemplate();
        template.setId("template-001");
        template.setName("新模板");

        when(reportTemplateService.save(any(ReportTemplate.class))).thenReturn(true);

        mockMvc.perform(post("/api/v1/report/templates")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(template))
                        .requestAttr("userId", "user-001"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));
    }

    @Test
    @DisplayName("PUT /api/v1/report/templates/{templateId} - 更新模板成功")
    void updateTemplate_success() throws Exception {
        ReportTemplate template = new ReportTemplate();
        template.setId("template-001");
        template.setName("更新后的模板");

        when(reportTemplateService.updateById(any(ReportTemplate.class))).thenReturn(true);

        mockMvc.perform(put("/api/v1/report/templates/template-001")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(template)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));
    }

    @Test
    @DisplayName("DELETE /api/v1/report/templates/{templateId} - 删除模板成功")
    void deleteTemplate_success() throws Exception {
        when(reportTemplateService.removeById("template-001")).thenReturn(true);

        mockMvc.perform(delete("/api/v1/report/templates/template-001"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data").value(true));
    }

    @Test
    @DisplayName("DELETE /api/v1/report/templates/{templateId} - 模板不存在")
    void deleteTemplate_notFound() throws Exception {
        when(reportTemplateService.removeById("non-existing")).thenReturn(false);

        mockMvc.perform(delete("/api/v1/report/templates/non-existing"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data").value(false));
    }
}