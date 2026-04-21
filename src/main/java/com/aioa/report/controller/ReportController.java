package com.aioa.report.controller;

import com.aioa.common.ApiResponse;
import com.aioa.report.dto.ReportDefinitionDTO;
import com.aioa.report.dto.ReportExecuteDTO;
import com.aioa.report.entity.ReportDefinition;
import com.aioa.report.entity.ReportExecution;
import com.aioa.report.service.ReportService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/reports")
@Tag(name = "报表模块", description = "智能报表与数据可视化API")
public class ReportController {

    private final ReportService reportService;

    public ReportController(ReportService reportService) {
        this.reportService = reportService;
    }

    @PostMapping
    @Operation(summary = "创建报表定义")
    public ApiResponse<ReportDefinition> createDefinition(@Valid @RequestBody ReportDefinitionDTO dto) {
        return ApiResponse.success(reportService.createDefinition(dto));
    }

    @PutMapping("/{id}")
    @Operation(summary = "更新报表定义")
    public ApiResponse<ReportDefinition> updateDefinition(
            @Parameter(description = "报表ID") @PathVariable Long id,
            @Valid @RequestBody ReportDefinitionDTO dto) {
        return ApiResponse.success(reportService.updateDefinition(id, dto));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "删除报表定义")
    public ApiResponse<Void> deleteDefinition(@Parameter(description = "报表ID") @PathVariable Long id) {
        reportService.deleteDefinition(id);
        return ApiResponse.success("删除成功", null);
    }

    @GetMapping
    @Operation(summary = "获取报表定义列表")
    public ApiResponse<List<ReportDefinition>> listDefinitions() {
        return ApiResponse.success(reportService.listDefinitions());
    }

    @GetMapping("/{id}")
    @Operation(summary = "获取单个报表定义")
    public ApiResponse<ReportDefinition> getDefinition(@Parameter(description = "报表ID") @PathVariable Long id) {
        return ApiResponse.success(reportService.getDefinition(id));
    }

    @PostMapping("/execute")
    @Operation(summary = "执行报表")
    public ApiResponse<ReportExecution> executeReport(@Valid @RequestBody ReportExecuteDTO dto) {
        return ApiResponse.success(reportService.executeReport(dto));
    }

    @GetMapping("/{id}/history")
    @Operation(summary = "获取报表执行历史")
    public ApiResponse<List<ReportExecution>> getExecutionHistory(@Parameter(description = "报表ID") @PathVariable Long id) {
        return ApiResponse.success(reportService.getExecutionHistory(id));
    }
}
