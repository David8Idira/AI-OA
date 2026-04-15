package com.aioa.report.controller;

import com.aioa.common.annotation.Login;
import com.aioa.common.result.Result;
import com.aioa.report.dto.ExportReportDTO;
import com.aioa.report.dto.GenerateReportDTO;
import com.aioa.report.dto.ReportQueryDTO;
import com.aioa.report.entity.ReportTemplate;
import com.aioa.report.service.ReportService;
import com.aioa.report.service.ReportTemplateService;
import com.aioa.report.vo.PageResult;
import com.aioa.report.vo.ReportVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Report Controller
 * Provides REST APIs for intelligent report generation and management
 */
@Slf4j
@RestController
@RequestMapping("/api/v1/report")
@RequiredArgsConstructor
@Tag(name = "Report Management", description = "AI-powered report generation and management APIs")
public class ReportController {

    private final ReportService reportService;
    private final ReportTemplateService reportTemplateService;

    /**
     * POST /api/v1/report/generate
     * Generate a new AI-powered report
     */
    @PostMapping("/generate")
    @Operation(summary = "Generate report", description = "Generate a new AI-powered report")
    @Login
    public Result<ReportVO> generateReport(
            @RequestAttribute("userId") String userId,
            @Valid @RequestBody GenerateReportDTO dto) {
        log.info("Generate report: userId={}, title={}, type={}", userId, dto.getTitle(), dto.getType());
        ReportVO vo = reportService.generateReport(userId, dto);
        return Result.success("报表生成成功", vo);
    }

    /**
     * POST /api/v1/report/regenerate/{reportId}
     * Regenerate a failed or existing report
     */
    @PostMapping("/regenerate/{reportId}")
    @Operation(summary = "Regenerate report", description = "Regenerate an existing report")
    @Login
    public Result<ReportVO> regenerateReport(
            @RequestAttribute("userId") String userId,
            @PathVariable String reportId) {
        log.info("Regenerate report: userId={}, reportId={}", userId, reportId);
        ReportVO vo = reportService.regenerateReport(reportId, userId);
        return Result.success("报表重新生成成功", vo);
    }

    /**
     * GET /api/v1/report/list
     * Get user's report list with pagination
     */
    @GetMapping("/list")
    @Operation(summary = "Get report list", description = "Get paginated report list for the user")
    @Login
    public Result<PageResult<ReportVO>> getReportList(
            @RequestAttribute("userId") String userId,
            @ModelAttribute ReportQueryDTO dto) {
        log.info("Get report list: userId={}, query={}", userId, dto);
        // Set creatorId if not set
        if (dto.getCreatorId() == null) {
            dto.setCreatorId(userId);
        }
        PageResult<ReportVO> result = reportService.getReportList(userId, dto);
        return Result.success(result);
    }

    /**
     * GET /api/v1/report/{reportId}
     * Get report details by ID
     */
    @GetMapping("/{reportId}")
    @Operation(summary = "Get report detail", description = "Get detailed information of a report")
    @Login
    public Result<ReportVO> getReportDetail(
            @RequestAttribute("userId") String userId,
            @PathVariable String reportId) {
        log.info("Get report detail: userId={}, reportId={}", userId, reportId);
        ReportVO vo = reportService.getReportDetail(reportId, userId);
        return Result.success(vo);
    }

    /**
     * DELETE /api/v1/report/{reportId}
     * Delete a report
     */
    @DeleteMapping("/{reportId}")
    @Operation(summary = "Delete report", description = "Delete a report (soft delete)")
    @Login
    public Result<Boolean> deleteReport(
            @RequestAttribute("userId") String userId,
            @PathVariable String reportId) {
        log.info("Delete report: userId={}, reportId={}", userId, reportId);
        boolean result = reportService.deleteReport(reportId, userId);
        return Result.success("报表删除成功", result);
    }

    /**
     * POST /api/v1/report/export
     * Export report to specified format
     */
    @PostMapping("/export")
    @Operation(summary = "Export report", description = "Export report to PDF, Excel, or HTML format")
    @Login
    public Result<String> exportReport(
            @RequestAttribute("userId") String userId,
            @Valid @RequestBody ExportReportDTO dto) {
        log.info("Export report: userId={}, reportId={}, format={}", userId, dto.getReportId(), dto.getFormat());
        String filePath = reportService.exportReport(dto.getReportId(), dto.getFormat(), userId);
        return Result.success("报表导出成功", filePath);
    }

    // ==================== Report Template APIs ====================

    /**
     * GET /api/v1/report/templates
     * Get all active report templates
     */
    @GetMapping("/templates")
    @Operation(summary = "Get template list", description = "Get all active report templates")
    @Login
    public Result<List<ReportTemplate>> getTemplates(
            @Parameter(description = "Filter by type") @RequestParam(required = false) String type) {
        log.info("Get templates: type={}", type);
        if (type != null) {
            return Result.success(reportTemplateService.getActiveByType(type));
        }
        return Result.success(reportTemplateService.list());
    }

    /**
     * GET /api/v1/report/templates/{templateId}
     * Get template by ID
     */
    @GetMapping("/templates/{templateId}")
    @Operation(summary = "Get template detail", description = "Get detailed information of a template")
    @Login
    public Result<ReportTemplate> getTemplateDetail(@PathVariable String templateId) {
        log.info("Get template detail: templateId={}", templateId);
        ReportTemplate template = reportTemplateService.getById(templateId);
        if (template == null) {
            return Result.fail("模板不存在");
        }
        return Result.success(template);
    }

    /**
     * POST /api/v1/report/templates
     * Create a new report template
     */
    @PostMapping("/templates")
    @Operation(summary = "Create template", description = "Create a new report template")
    @Login
    public Result<ReportTemplate> createTemplate(
            @RequestAttribute("userId") String userId,
            @RequestBody ReportTemplate template) {
        log.info("Create template: userId={}, name={}", userId, template.getName());
        template.setCreatorId(userId);
        template.setIsActive(1);
        template.setIsBuiltIn(0);
        template.setUsageCount(0);
        reportTemplateService.save(template);
        return Result.success("模板创建成功", template);
    }

    /**
     * PUT /api/v1/report/templates/{templateId}
     * Update a report template
     */
    @PutMapping("/templates/{templateId}")
    @Operation(summary = "Update template", description = "Update an existing report template")
    @Login
    public Result<ReportTemplate> updateTemplate(
            @PathVariable String templateId,
            @RequestBody ReportTemplate template) {
        log.info("Update template: templateId={}", templateId);
        template.setId(templateId);
        reportTemplateService.updateById(template);
        return Result.success("模板更新成功", template);
    }

    /**
     * DELETE /api/v1/report/templates/{templateId}
     * Delete a report template
     */
    @DeleteMapping("/templates/{templateId}")
    @Operation(summary = "Delete template", description = "Delete a report template")
    @Login
    public Result<Boolean> deleteTemplate(@PathVariable String templateId) {
        log.info("Delete template: templateId={}", templateId);
        boolean result = reportTemplateService.removeById(templateId);
        return Result.success("模板删除成功", result);
    }
}
