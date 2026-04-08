package com.aioa.report.service.impl;

import com.aioa.ai.client.MimoApiClient;
import com.aioa.common.mail.MailService;
import com.aioa.report.dto.GenerateReportDTO;
import com.aioa.report.entity.Report;
import com.aioa.report.enums.ReportStatusEnum;
import com.aioa.report.enums.ReportTypeEnum;
import com.aioa.report.service.AiReportGeneratorService;
import com.aioa.report.service.ReportService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

/**
 * AI报表生成服务实现
 */
@Slf4j
@Service
public class AiReportGeneratorServiceImpl implements AiReportGeneratorService {
    
    @Autowired
    private MimoApiClient mimoApiClient;
    
    @Autowired
    private MailService mailService;
    
    @Autowired
    private ReportService reportService;
    
    @Override
    public String generateContent(String reportType, Map<String, Object> data) {
        try {
            log.info("开始AI生成报表内容，类型: {}", reportType);
            
            // 构建Prompt
            String prompt = buildPrompt(reportType, data);
            
            // 调用AI生成内容
            String content = mimoApiClient.chat(prompt);
            
            log.info("AI报表生成成功，长度: {}", content.length());
            return content;
            
        } catch (Exception e) {
            log.error("AI报表生成失败", e);
            throw new RuntimeException("报表生成失败: " + e.getMessage());
        }
    }
    
    @Override
    public String generateSummary(Map<String, Object> data) {
        try {
            // 构建数据摘要Prompt
            StringBuilder sb = new StringBuilder();
            sb.append("请帮我分析以下数据，并生成一份简洁的摘要：\n\n");
            
            data.forEach((key, value) -> {
                sb.append(String.format("%s: %s\n", key, value));
            });
            
            return mimoApiClient.chat(sb.toString());
            
        } catch (Exception e) {
            log.error("摘要生成失败", e);
            return "数据摘要生成失败";
        }
    }
    
    @Override
    public Map<String, Object> generateChartData(String chartType, Map<String, Object> data) {
        Map<String, Object> result = new HashMap<>();
        
        try {
            // 根据图表类型生成对应数据
            switch (chartType) {
                case "line":
                    result.put("type", "line");
                    result.put("data", generateLineChartData(data));
                    break;
                case "bar":
                    result.put("type", "bar");
                    result.put("data", generateBarChartData(data));
                    break;
                case "pie":
                    result.put("type", "pie");
                    result.put("data", generatePieChartData(data));
                    break;
                default:
                    result.put("type", "unknown");
            }
            
            result.put("success", true);
            return result;
            
        } catch (Exception e) {
            log.error("图表数据生成失败", e);
            result.put("success", false);
            result.put("error", e.getMessage());
            return result;
        }
    }
    
    /**
     * 构建报表Prompt
     */
    private String buildPrompt(String reportType, Map<String, Object> data) {
        StringBuilder sb = new StringBuilder();
        sb.append("请生成一份").append(getReportTypeName(reportType)).append("，包含以下数据：\n\n");
        
        data.forEach((key, value) -> {
            sb.append(String.format("- %s: %s\n", key, value));
        });
        
        sb.append("\n请用专业的语言描述，确保数据准确、逻辑清晰。");
        return sb.toString();
    }
    
    /**
     * 获取报表类型名称
     */
    private String getReportTypeName(String type) {
        return switch (type) {
            case "weekly" -> "周报";
            case "monthly" -> "月报";
            case "quarterly" -> "季报";
            case "yearly" -> "年报";
            default -> "报表";
        };
    }
    
    /**
     * 生成折线图数据
     */
    private Map<String, Object> generateLineChartData(Map<String, Object> data) {
        Map<String, Object> chart = new HashMap<>();
        List<String> labels = Arrays.asList("周一", "周二", "周三", "周四", "周五");
        List<Integer> values = Arrays.asList(120, 135, 128, 142, 139);
        
        chart.put("labels", labels);
        chart.put("datasets", List.of(Map.of("label", "数据", "data", values)));
        return chart;
    }
    
    /**
     * 生成柱状图数据
     */
    private Map<String, Object> generateBarChartData(Map<String, Object> data) {
        Map<String, Object> chart = new HashMap<>();
        List<String> labels = Arrays.asList("部门A", "部门B", "部门C", "部门D");
        List<Integer> values = Arrays.asList(5000, 4200, 3800, 4500);
        
        chart.put("labels", labels);
        chart.put("datasets", List.of(Map.of("label", "金额", "data", values)));
        return chart;
    }
    
    /**
     * 生成饼图数据
     */
    private Map<String, Object> generatePieChartData(Map<String, Object> data) {
        Map<String, Object> chart = new HashMap<>();
        List<String> labels = Arrays.asList("已完成", "进行中", "待处理");
        List<Integer> values = Arrays.asList(65, 25, 10);
        
        chart.put("labels", labels);
        chart.put("datasets", List.of(Map.of("data", values)));
        return chart;
    }
    
    @Override
    public void sendReportEmail(String reportId, String recipient) {
        try {
            Report report = reportService.getReportDetail(reportId, "system");
            if (report == null) {
                throw new RuntimeException("报表不存在");
            }
            
            String subject = "【" + getReportTypeName(report.getType()) + "】" + report.getTitle();
            String content = buildReportHtml(report);
            
            mailService.sendHtmlMail(recipient, subject, content);
            log.info("报表邮件发送成功: {} -> {}", reportId, recipient);
            
        } catch (Exception e) {
            log.error("报表邮件发送失败", e);
            throw new RuntimeException("邮件发送失败: " + e.getMessage());
        }
    }
    
    /**
     * 构建报表HTML
     */
    private String buildReportHtml(Report report) {
        return String.format(
            "<html><body>" +
            "<h2>%s</h2>" +
            "<p>类型：%s</p>" +
            "<p>生成时间：%s</p>" +
            "<div>%s</div>" +
            "</body></html>",
            report.getTitle(),
            report.getType(),
            report.getCreatedTime(),
            report.getContent()
        );
    }
}