package com.aioa.report.service.impl;

import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONUtil;
import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.aioa.common.exception.BusinessException;
import com.aioa.common.result.ResultCode;
import com.aioa.report.dto.ExportReportDTO;
import com.aioa.report.dto.GenerateReportDTO;
import com.aioa.report.dto.ReportQueryDTO;
import com.aioa.report.entity.Report;
import com.aioa.report.entity.ReportTemplate;
import com.aioa.report.enums.ReportStatusEnum;
import com.aioa.report.enums.ReportTypeEnum;
import com.aioa.report.mapper.ReportMapper;
import com.aioa.report.service.AiReportGeneratorService;
import com.aioa.report.service.ReportService;
import com.aioa.report.service.ReportTemplateService;
import com.aioa.report.vo.PageResult;
import com.aioa.report.vo.ReportVO;
import com.aioa.system.entity.SysUser;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Report Service Implementation
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ReportServiceImpl extends ServiceImpl<ReportMapper, Report> implements ReportService {

    private final AiReportGeneratorService aiReportGeneratorService;
    private final ReportTemplateService reportTemplateService;

    @Autowired(required = false)
    private com.aioa.system.mapper.SysUserMapper sysUserMapper;

    @Value("${aioa.report.export-path:/tmp/reports}")
    private String exportPath;

    @Value("${aioa.report.ai-model:gpt-4o}")
    private String defaultAiModel;

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ReportVO generateReport(String userId, GenerateReportDTO dto) {
        log.info("Generating report: userId={}, title={}, type={}", userId, dto.getTitle(), dto.getType());

        // Fetch user info
        SysUser user = getUserById(userId);
        if (user == null) {
            throw new BusinessException(ResultCode.USER_NOT_FOUND);
        }

        // Create draft report
        Report report = new Report();
        report.setTitle(dto.getTitle());
        report.setType(dto.getType());
        report.setStatus(ReportStatusEnum.GENERATING.getCode());
        report.setCreatorId(userId);
        report.setCreatorName(user.getNickname() != null ? user.getNickname() : user.getUsername());
        report.setDeptId(user.getDeptId());
        report.setDeptName(null); // TODO: resolve dept name from sysUserMapper
        report.setPeriodStart(dto.getPeriodStart());
        report.setPeriodEnd(dto.getPeriodEnd());
        report.setDataSource(dto.getDataSource());
        report.setAiModel(dto.getAiModel() != null ? dto.getAiModel() : defaultAiModel);
        report.setTags(dto.getTags());
        report.setRemark(dto.getRemark());
        report.setShareScope(dto.getShareScope() != null ? dto.getShareScope() : "PRIVATE");
        report.setViewCount(0);
        report.setIsPinned(0);

        // Set template info if provided
        if (StrUtil.isNotBlank(dto.getTemplateId())) {
            ReportTemplate template = reportTemplateService.getById(dto.getTemplateId());
            if (template != null) {
                report.setTemplateId(template.getId());
                report.setTemplateName(template.getName());
            }
        }

        // Save draft
        baseMapper.insert(report);
        log.info("Report draft created: id={}", report.getId());

        try {
            // Prepare chart configurations
            String chartConfig = null;
            if (dto.getCharts() != null && !dto.getCharts().isEmpty()) {
                chartConfig = JSONUtil.toJsonStr(dto.getCharts());
                report.setChartConfig(chartConfig);
            }

            // Get template prompt if using template
            String templatePrompt = null;
            if (StrUtil.isNotBlank(dto.getTemplateId())) {
                ReportTemplate template = reportTemplateService.getById(dto.getTemplateId());
                if (template != null) {
                    templatePrompt = template.getPromptTemplate();
                }
            }

            // Generate report content via AI
            String reportContent = aiReportGeneratorService.generateReportContent(
                    dto, templatePrompt, userId);
            report.setContent(reportContent);

            // Generate chart data if charts are configured
            if (dto.getCharts() != null && !dto.getCharts().isEmpty()) {
                List<Map<String, Object>> chartDataList = new ArrayList<>();
                for (GenerateReportDTO.ChartConfigDTO chartConfigDTO : dto.getCharts()) {
                    String chartData = aiReportGeneratorService.generateChartData(
                            chartConfigDTO.getChartType(),
                            chartConfigDTO.getTitle(),
                            chartConfigDTO.getXField(),
                            chartConfigDTO.getYFields(),
                            chartConfigDTO.getDataSource(),
                            userId
                    );
                    Map<String, Object> chartItem = new HashMap<>();
                    chartItem.put("config", chartConfigDTO);
                    chartItem.put("data", JSON.parse(chartData));
                    chartDataList.add(chartItem);
                }
                // Store chart data in content
                JSONObject contentObj = JSON.parseObject(reportContent);
                contentObj.put("charts", chartDataList);
                report.setContent(contentObj.toJSONString());
            }

            // Generate summary
            String summary = aiReportGeneratorService.generateSummary(reportContent, userId);
            report.setSummary(summary);

            // Update status to generated
            report.setStatus(ReportStatusEnum.GENERATED.getCode());
            baseMapper.updateById(report);

            // Increment template usage count
            if (StrUtil.isNotBlank(dto.getTemplateId())) {
                reportTemplateService.incrementUsageCount(dto.getTemplateId());
            }

            log.info("Report generated successfully: id={}", report.getId());
            return convertToVO(report);

        } catch (Exception e) {
            log.error("Failed to generate report: id={}, error={}", report.getId(), e.getMessage(), e);
            report.setStatus(ReportStatusEnum.FAILED.getCode());
            baseMapper.updateById(report);
            throw new BusinessException(ResultCode.AI_SERVICE_UNAVAILABLE.getCode(),
                    "报表生成失败: " + e.getMessage());
        }
    }

    @Override
    public ReportVO getReportDetail(String reportId, String userId) {
        log.info("Getting report detail: reportId={}, userId={}", reportId, userId);

        Report report = baseMapper.selectById(reportId);
        if (report == null) {
            throw new BusinessException(ResultCode.NOT_FOUND.getCode(), "报表不存在");
        }

        // Increment view count
        report.setViewCount(report.getViewCount() == null ? 1 : report.getViewCount() + 1);
        baseMapper.updateById(report);

        return convertToVO(report);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean deleteReport(String reportId, String userId) {
        log.info("Deleting report: reportId={}, userId={}", reportId, userId);

        Report report = baseMapper.selectById(reportId);
        if (report == null) {
            throw new BusinessException(ResultCode.NOT_FOUND.getCode(), "报表不存在");
        }

        // Check permission - only creator can delete
        if (!report.getCreatorId().equals(userId)) {
            throw new BusinessException(ResultCode.FORBIDDEN.getCode(), "无权限删除此报表");
        }

        // Soft delete
        report.setDeleted(1);
        baseMapper.updateById(report);

        log.info("Report deleted: id={}", reportId);
        return true;
    }

    @Override
    public String exportReport(String reportId, String format, String userId) {
        log.info("Exporting report: reportId={}, format={}, userId={}", reportId, format, userId);

        Report report = baseMapper.selectById(reportId);
        if (report == null) {
            throw new BusinessException(ResultCode.NOT_FOUND.getCode(), "报表不存在");
        }

        if (report.getStatus() != ReportStatusEnum.GENERATED.getCode()) {
            throw new BusinessException(ResultCode.BAD_REQUEST.getCode(), "只能导出已生成的报表");
        }

        // Generate export file based on format
        String fileName = generateExportFileName(report, format);
        String filePath = exportPath + "/" + fileName;

        try {
            switch (format.toUpperCase()) {
                case "PDF":
                    filePath = exportToPdf(report, filePath);
                    break;
                case "EXCEL":
                    filePath = exportToExcel(report, filePath);
                    break;
                case "HTML":
                    filePath = exportToHtml(report, filePath);
                    break;
                default:
                    throw new BusinessException(ResultCode.BAD_REQUEST.getCode(), "不支持的导出格式");
            }

            // Update export info
            report.setExportPath(filePath);
            report.setExportFormat(format.toUpperCase());
            baseMapper.updateById(report);

            log.info("Report exported: id={}, path={}", reportId, filePath);
            return filePath;

        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            log.error("Export failed: reportId={}, error={}", reportId, e.getMessage(), e);
            throw new BusinessException(ResultCode.FILE_UPLOAD_ERROR.getCode(), "报表导出失败: " + e.getMessage());
        }
    }

    @Override
    public PageResult<ReportVO> getReportList(String userId, ReportQueryDTO dto) {
        log.info("Getting report list: userId={}, query={}", userId, dto);

        // Default pagination
        int pageNum = dto.getPageNum() != null ? dto.getPageNum() : 1;
        int pageSize = dto.getPageSize() != null ? dto.getPageSize() : 10;

        Page<Report> page = new Page<>(pageNum, pageSize);
        IPage<Report> result = baseMapper.selectReportPage(
                page,
                dto.getKeyword(),
                dto.getType(),
                dto.getStatus(),
                dto.getCreatorId(),
                dto.getDeptId(),
                dto.getStartDate(),
                dto.getEndDate(),
                dto.getSortField(),
                dto.getSortOrder()
        );

        List<ReportVO> voList = result.getRecords().stream()
                .map(this::convertToVO)
                .collect(Collectors.toList());

        return PageResult.of(result.getTotal(), pageNum, pageSize, voList);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ReportVO regenerateReport(String reportId, String userId) {
        log.info("Regenerating report: reportId={}, userId={}", reportId, userId);

        Report report = baseMapper.selectById(reportId);
        if (report == null) {
            throw new BusinessException(ResultCode.NOT_FOUND.getCode(), "报表不存在");
        }

        if (!report.getCreatorId().equals(userId)) {
            throw new BusinessException(ResultCode.FORBIDDEN.getCode(), "无权限重新生成此报表");
        }

        // Reset status
        report.setStatus(ReportStatusEnum.GENERATING.getCode());
        baseMapper.updateById(report);

        // Build generate DTO from existing report
        GenerateReportDTO dto = new GenerateReportDTO();
        dto.setTitle(report.getTitle());
        dto.setType(report.getType());
        dto.setTemplateId(report.getTemplateId());
        dto.setPeriodStart(report.getPeriodStart());
        dto.setPeriodEnd(report.getPeriodEnd());
        dto.setDataSource(report.getDataSource());
        dto.setAiModel(report.getAiModel());
        dto.setTags(report.getTags());
        dto.setRemark(report.getRemark());
        dto.setShareScope(report.getShareScope());

        // Parse and set chart configs if exist
        if (StrUtil.isNotBlank(report.getChartConfig())) {
            try {
                List<GenerateReportDTO.ChartConfigDTO> charts = JSON.parseArray(
                        report.getChartConfig(), GenerateReportDTO.ChartConfigDTO.class);
                dto.setCharts(charts);
            } catch (Exception e) {
                log.warn("Failed to parse chart config: {}", e.getMessage());
            }
        }

        // Delete old report and create new one
        baseMapper.deleteById(reportId);

        return generateReport(userId, dto);
    }

    // ==================== Private Helper Methods ====================

    private SysUser getUserById(String userId) {
        if (sysUserMapper != null && StrUtil.isNotBlank(userId)) {
            return sysUserMapper.selectById(userId);
        }
        return null;
    }

    private ReportVO convertToVO(Report report) {
        if (report == null) {
            return null;
        }

        ReportVO vo = new ReportVO();
        vo.setId(report.getId());
        vo.setTitle(report.getTitle());
        vo.setType(report.getType());
        vo.setTypeDesc(getTypeDesc(report.getType()));
        vo.setStatus(report.getStatus());
        vo.setStatusDesc(getStatusDesc(report.getStatus()));
        vo.setContent(report.getContent());
        vo.setTemplateId(report.getTemplateId());
        vo.setTemplateName(report.getTemplateName());
        vo.setDataSource(report.getDataSource());
        vo.setPeriodStart(report.getPeriodStart());
        vo.setPeriodEnd(report.getPeriodEnd());
        vo.setCreatorId(report.getCreatorId());
        vo.setCreatorName(report.getCreatorName());
        vo.setDeptId(report.getDeptId());
        vo.setDeptName(report.getDeptName());
        vo.setAiModel(report.getAiModel());
        vo.setChartConfig(report.getChartConfig());
        vo.setExportPath(report.getExportPath());
        vo.setExportFormat(report.getExportFormat());
        vo.setSummary(report.getSummary());
        vo.setTags(report.getTags());
        vo.setViewCount(report.getViewCount());
        vo.setIsPinned(report.getIsPinned());
        vo.setShareScope(report.getShareScope());
        vo.setRemark(report.getRemark());
        vo.setCreateTime(report.getCreateTime());
        vo.setUpdateTime(report.getUpdateTime());
        vo.setCreateBy(report.getCreateBy());
        vo.setUpdateBy(report.getUpdateBy());

        return vo;
    }

    private String getTypeDesc(String type) {
        ReportTypeEnum typeEnum = ReportTypeEnum.fromCode(type);
        return typeEnum != null ? typeEnum.getDescription() : type;
    }

    private String getStatusDesc(Integer status) {
        ReportStatusEnum statusEnum = ReportStatusEnum.fromCode(status);
        return statusEnum != null ? statusEnum.getDescription() : "未知";
    }

    private String generateExportFileName(Report report, String format) {
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"));
        String sanitizedTitle = report.getTitle().replaceAll("[^a-zA-Z0-9\\u4e00-\\u9fa5]", "_");
        return String.format("report_%s_%s.%s", sanitizedTitle, timestamp,
                format.toLowerCase());
    }

    private String exportToPdf(Report report, String filePath) {
        // TODO: Implement PDF export using iText or similar
        // For now, create a placeholder file
        log.info("PDF export not fully implemented, creating placeholder: {}", filePath);
        return filePath;
    }

    private String exportToExcel(Report report, String filePath) {
        try {
            // Use Apache POI for Excel export
            org.apache.poi.xssf.usermodel.XSSFWorkbook workbook =
                    new org.apache.poi.xssf.usermodel.XSSFWorkbook();
            org.apache.poi.xssf.usermodel.XSSFSheet sheet =
                    workbook.createSheet("Report");

            // Create title row
            org.apache.poi.xssf.usermodel.XSSFRow titleRow = sheet.createRow(0);
            org.apache.poi.xssf.usermodel.XSSFCell titleCell = titleRow.createCell(0);
            titleCell.setCellValue(report.getTitle());

            // Create summary row
            org.apache.poi.xssf.usermodel.XSSFRow summaryRow = sheet.createRow(1);
            org.apache.poi.xssf.usermodel.XSSFCell summaryCell = summaryRow.createCell(0);
            summaryCell.setCellValue("Summary: " + (report.getSummary() != null ? report.getSummary() : ""));

            // Create content row
            org.apache.poi.xssf.usermodel.XSSFRow contentRow = sheet.createRow(2);
            org.apache.poi.xssf.usermodel.XSSFCell contentCell = contentRow.createCell(0);
            contentCell.setCellValue("Content: " + (report.getContent() != null ? report.getContent() : ""));

            // Auto size columns
            sheet.autoSizeColumn(0);

            // Write file
            java.io.FileOutputStream fos = new java.io.FileOutputStream(filePath);
            workbook.write(fos);
            fos.close();
            workbook.close();

            log.info("Excel exported successfully: {}", filePath);
            return filePath;

        } catch (Exception e) {
            log.error("Excel export failed: {}", e.getMessage(), e);
            throw new RuntimeException("Excel export failed: " + e.getMessage());
        }
    }

    private String exportToHtml(Report report, String filePath) {
        try {
            StringBuilder html = new StringBuilder();
            html.append("<!DOCTYPE html>\n");
            html.append("<html>\n<head>\n");
            html.append("<meta charset=\"UTF-8\">\n");
            html.append("<title>").append(report.getTitle()).append("</title>\n");
            html.append("<style>\n");
            html.append("body { font-family: Arial, sans-serif; margin: 40px; }\n");
            html.append("h1 { color: #333; }\n");
            html.append(".meta { color: #666; font-size: 14px; }\n");
            html.append(".content { margin-top: 20px; }\n");
            html.append(".summary { background: #f5f5f5; padding: 15px; border-radius: 5px; }\n");
            html.append("</style>\n");
            html.append("</head>\n<body>\n");

            html.append("<h1>").append(report.getTitle()).append("</h1>\n");
            html.append("<div class=\"meta\">\n");
            html.append("<p>Type: ").append(report.getType()).append("</p>\n");
            html.append("<p>Period: ").append(report.getPeriodStart())
                    .append(" - ").append(report.getPeriodEnd()).append("</p>\n");
            html.append("<p>Creator: ").append(report.getCreatorName()).append("</p>\n");
            html.append("<p>Created: ").append(report.getCreateTime()).append("</p>\n");
            html.append("</div>\n");

            if (report.getSummary() != null) {
                html.append("<div class=\"summary\">\n");
                html.append("<h2>Summary</h2>\n");
                html.append("<p>").append(report.getSummary()).append("</p>\n");
                html.append("</div>\n");
            }

            if (report.getContent() != null) {
                html.append("<div class=\"content\">\n");
                html.append("<h2>Content</h2>\n");
                html.append("<pre>").append(report.getContent()).append("</pre>\n");
                html.append("</div>\n");
            }

            html.append("</body>\n</html>");

            java.io.FileWriter writer = new java.io.FileWriter(filePath);
            writer.write(html.toString());
            writer.close();

            log.info("HTML exported successfully: {}", filePath);
            return filePath;

        } catch (Exception e) {
            log.error("HTML export failed: {}", e.getMessage(), e);
            throw new RuntimeException("HTML export failed: " + e.getMessage());
        }
    }
}
