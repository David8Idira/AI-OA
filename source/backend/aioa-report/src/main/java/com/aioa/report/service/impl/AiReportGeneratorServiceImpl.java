package com.aioa.report.service.impl;

import cn.hutool.json.JSONUtil;
import com.aioa.ai.client.MimoApiClient;
import com.aioa.report.dto.GenerateReportDTO;
import com.aioa.report.enums.ReportTypeEnum;
import com.aioa.report.service.AiReportGeneratorService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.*;

/**
 * AI Report Generator Service Implementation
 * Uses MimoApiClient to call AI models for report content, chart data, and summary generation.
 */
@Slf4j
@Service
public class AiReportGeneratorServiceImpl implements AiReportGeneratorService {
    
    @Autowired
    private MimoApiClient mimoApiClient;
    
    @Value("${aioa.report.ai-model:gpt-4o}")
    private String defaultAiModel;
    
    @Override
    public String generateReportContent(GenerateReportDTO dto, String templatePrompt, String userId) {
        try {
            log.info("开始AI生成报表内容，用户: {}, 标题: {}, 类型: {}", userId, dto.getTitle(), dto.getType());
            
            // 构建Prompt
            String prompt = buildReportPrompt(dto, templatePrompt);
            
            // 调用AI生成内容
            String model = dto.getAiModel() != null ? dto.getAiModel() : defaultAiModel;
            String content = mimoApiClient.chat(prompt, model);
            
            log.info("AI报表生成成功，长度: {}", content.length());
            return content;
            
        } catch (Exception e) {
            log.error("AI报表生成失败", e);
            throw new RuntimeException("报表生成失败: " + e.getMessage());
        }
    }
    
    @Override
    public String generateChartData(String chartType, String title, String xField, String yFields, String dataSource, String userId) {
        try {
            log.info("开始生成图表数据，类型: {}, 标题: {}", chartType, title);
            
            String prompt = String.format(
                "请生成一份%s类型的图表数据，标题为'%s'。\n" +
                "X轴字段: %s\n" +
                "Y轴字段: %s\n" +
                "数据源: %s\n\n" +
                "请以JSON格式返回，包含labels数组和datasets数组，datasets中包含label和data数组。",
                chartType, title, xField, yFields, dataSource
            );
            
            String result = mimoApiClient.chat(prompt, defaultAiModel);
            log.info("图表数据生成成功");
            return result;
            
        } catch (Exception e) {
            log.error("图表数据生成失败", e);
            // 返回默认图表数据
            return generateDefaultChartData(chartType);
        }
    }
    
    @Override
    public String generateSummary(String reportContent, String userId) {
        try {
            log.info("开始生成报表摘要");
            
            String prompt = String.format(
                "请为以下报表内容生成一段简洁的摘要（200字以内）：\n\n%s",
                reportContent.length() > 3000 ? reportContent.substring(0, 3000) + "..." : reportContent
            );
            
            String summary = mimoApiClient.chat(prompt, defaultAiModel);
            log.info("报表摘要生成成功，长度: {}", summary.length());
            return summary;
            
        } catch (Exception e) {
            log.error("摘要生成失败", e);
            return "数据摘要生成失败";
        }
    }
    
    @Override
    public String callAiModel(String prompt, String model, String userId) {
        try {
            log.info("调用AI模型: {}, 用户: {}", model, userId);
            return mimoApiClient.chat(prompt, model);
        } catch (Exception e) {
            log.error("AI模型调用失败", e);
            throw new RuntimeException("AI服务调用失败: " + e.getMessage());
        }
    }
    
    /**
     * 构建报表Prompt
     */
    private String buildReportPrompt(GenerateReportDTO dto, String templatePrompt) {
        StringBuilder sb = new StringBuilder();
        
        String reportTypeName = getReportTypeName(dto.getType());
        
        sb.append("请生成一份").append(reportTypeName);
        if (dto.getTitle() != null) {
            sb.append("，标题为'").append(dto.getTitle()).append("'");
        }
        sb.append("。\n\n");
        
        // 时间范围
        if (dto.getPeriodStart() != null && dto.getPeriodEnd() != null) {
            sb.append("时间范围: ").append(dto.getPeriodStart()).append(" 至 ").append(dto.getPeriodEnd()).append("\n");
        }
        
        // 数据源
        if (dto.getDataSource() != null) {
            sb.append("数据源: ").append(dto.getDataSource()).append("\n");
        }
        
        // 自定义提示词
        if (dto.getRemark() != null) {
            sb.append("补充要求: ").append(dto.getRemark()).append("\n");
        }
        
        // 模板提示词
        if (templatePrompt != null) {
            sb.append("\n模板要求:\n").append(templatePrompt).append("\n");
        }
        
        // 图表要求
        if (dto.getCharts() != null && !dto.getCharts().isEmpty()) {
            sb.append("\n需要包含以下图表:\n");
            for (GenerateReportDTO.ChartConfigDTO chart : dto.getCharts()) {
                sb.append("- ").append(chart.getTitle()).append(" (").append(chart.getChartType()).append(")\n");
            }
        }
        
        sb.append("\n请用专业的语言描述，确保数据准确、逻辑清晰、结构完整。");
        sb.append("返回JSON格式，包含title、sections数组（每个section包含heading和content）、和summary字段。");
        
        return sb.toString();
    }
    
    /**
     * 获取报表类型名称
     */
    private String getReportTypeName(String type) {
        ReportTypeEnum typeEnum = ReportTypeEnum.fromCode(type);
        if (typeEnum != null) {
            return typeEnum.getDescription();
        }
        return switch (type != null ? type.toLowerCase() : "") {
            case "weekly" -> "周报";
            case "monthly" -> "月报";
            case "quarterly" -> "季报";
            case "yearly" -> "年报";
            default -> "报表";
        };
    }
    
    /**
     * 生成默认图表数据（AI调用失败时的fallback）
     */
    private String generateDefaultChartData(String chartType) {
        Map<String, Object> result = new HashMap<>();
        result.put("type", chartType);
        
        switch (chartType.toLowerCase()) {
            case "line":
                result.put("labels", Arrays.asList("一月", "二月", "三月", "四月", "五月"));
                result.put("datasets", List.of(Map.of("label", "趋势", "data", Arrays.asList(120, 135, 128, 142, 139))));
                break;
            case "bar":
                result.put("labels", Arrays.asList("部门A", "部门B", "部门C", "部门D"));
                result.put("datasets", List.of(Map.of("label", "金额", "data", Arrays.asList(5000, 4200, 3800, 4500))));
                break;
            case "pie":
                result.put("labels", Arrays.asList("已完成", "进行中", "待处理"));
                result.put("datasets", List.of(Map.of("data", Arrays.asList(65, 25, 10))));
                break;
            default:
                result.put("labels", Collections.emptyList());
                result.put("datasets", Collections.emptyList());
        }
        
        return JSONUtil.toJsonStr(result);
    }
}
