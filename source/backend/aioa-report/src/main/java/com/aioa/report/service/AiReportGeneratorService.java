package com.aioa.report.service;

import com.aioa.report.dto.GenerateReportDTO;
import java.util.Map;

/**
 * AI Report Generator Service Interface
 * Responsible for calling GPT-4o to generate report content and chart data
 */
public interface AiReportGeneratorService {

    /**
     * Generate report content using AI
     *
     * @param dto          generation parameters
     * @param templatePrompt template prompt (if using template)
     * @param userId       current user ID
     * @return generated report content (JSON string with text and chart data)
     */
    String generateReportContent(GenerateReportDTO dto, String templatePrompt, String userId);

    /**
     * Generate chart data using AI
     * Supports BAR, LINE, PIE chart data generation
     *
     * @param chartType  chart type (BAR, LINE, PIE)
     * @param title      chart title
     * @param xField     X-axis field
     * @param yFields    Y-axis fields (comma-separated)
     * @param dataSource data source description
     * @param userId     current user ID
     * @return chart data as JSON string
     */
    String generateChartData(String chartType, String title, String xField, String yFields, String dataSource, String userId);

    /**
     * Generate report summary using AI
     *
     * @param reportContent generated report content
     * @param userId        current user ID
     * @return summary text
     */
    String generateSummary(String reportContent, String userId);

    /**
     * Call AI model and return response
     *
     * @param prompt   prompt text
     * @param model    AI model name
     * @param userId   user ID for tracking
     * @return AI response text
     */
    String callAiModel(String prompt, String model, String userId);
}
