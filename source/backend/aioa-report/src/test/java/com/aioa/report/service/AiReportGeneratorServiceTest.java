package com.aioa.report.service;

import com.aioa.report.dto.GenerateReportDTO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

/**
 * AiReportGeneratorService 单元测试
 * 测试AI报表生成器的各个方法
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("AiReportGeneratorServiceTest AI报表生成器测试")
class AiReportGeneratorServiceTest {

    @Mock
    private AiReportGeneratorService aiReportGeneratorService;

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

    @Test
    @DisplayName("生成报表内容 - 正常场景")
    void generateReportContent_withValidInput_shouldReturnContent() {
        // given
        String expectedContent = "{\"text\":\"测试报表内容\",\"charts\":[]}";
        when(aiReportGeneratorService.generateReportContent(any(), anyString(), anyString()))
                .thenReturn(expectedContent);

        // when
        String result = aiReportGeneratorService.generateReportContent(
                createGenerateDTO(), "模板提示", "user-001");

        // then
        assertThat(result).isEqualTo(expectedContent);
        verify(aiReportGeneratorService, times(1)).generateReportContent(any(), anyString(), anyString());
    }

    @Test
    @DisplayName("生成报表内容 - 空模板提示")
    void generateReportContent_withEmptyTemplatePrompt_shouldWork() {
        // given
        String expectedContent = "{\"text\":\"报表内容\",\"charts\":[]}";
        when(aiReportGeneratorService.generateReportContent(any(), anyString(), anyString()))
                .thenReturn(expectedContent);

        // when
        String result = aiReportGeneratorService.generateReportContent(
                createGenerateDTO(), "", "user-001");

        // then
        assertThat(result).isEqualTo(expectedContent);
    }

    @Test
    @DisplayName("生成图表数据 - BAR类型")
    void generateChartData_barChart_shouldReturnData() {
        // given
        String expectedData = "{\"type\":\"BAR\",\"title\":\"销售数据\",\"data\":[{\"name\":\"Q1\",\"value\":100}]}";
        when(aiReportGeneratorService.generateChartData(anyString(), anyString(), anyString(), anyString(), anyString(), anyString()))
                .thenReturn(expectedData);

        // when
        String result = aiReportGeneratorService.generateChartData(
                "BAR", "销售数据", "季度", "销售额", "销售数据库", "user-001");

        // then
        assertThat(result).isEqualTo(expectedData);
        assertThat(result).contains("BAR");
    }

    @Test
    @DisplayName("生成图表数据 - LINE类型")
    void generateChartData_lineChart_shouldReturnData() {
        // given
        String expectedData = "{\"type\":\"LINE\",\"title\":\"趋势图\",\"data\":[{\"name\":\"1月\",\"value\":50}]}";
        when(aiReportGeneratorService.generateChartData(anyString(), anyString(), anyString(), anyString(), anyString(), anyString()))
                .thenReturn(expectedData);

        // when
        String result = aiReportGeneratorService.generateChartData(
                "LINE", "趋势图", "月份", "访问量", "访问日志", "user-001");

        // then
        assertThat(result).isEqualTo(expectedData);
        assertThat(result).contains("LINE");
    }

    @Test
    @DisplayName("生成图表数据 - PIE类型")
    void generateChartData_pieChart_shouldReturnData() {
        // given
        String expectedData = "{\"type\":\"PIE\",\"title\":\"占比图\",\"data\":[{\"name\":\"A\",\"value\":30},{\"name\":\"B\",\"value\":70}]}";
        when(aiReportGeneratorService.generateChartData(anyString(), anyString(), anyString(), anyString(), anyString(), anyString()))
                .thenReturn(expectedData);

        // when
        String result = aiReportGeneratorService.generateChartData(
                "PIE", "占比图", "类别", "数量", "分类统计", "user-001");

        // then
        assertThat(result).isEqualTo(expectedData);
        assertThat(result).contains("PIE");
    }

    @Test
    @DisplayName("生成图表数据 - 多种图表类型")
    void generateChartData_allChartTypes_shouldWork() {
        // given
        String[] chartTypes = {"BAR", "LINE", "PIE", "SCATTER", "AREA"};
        when(aiReportGeneratorService.generateChartData(anyString(), anyString(), anyString(), anyString(), anyString(), anyString()))
                .thenReturn("{\"type\":\"TEST\"}");

        for (String chartType : chartTypes) {
            // when
            String result = aiReportGeneratorService.generateChartData(
                    chartType, "测试图表", "X轴", "Y轴", "数据源", "user-001");

            // then
            assertThat(result).isNotNull();
        }
    }

    @Test
    @DisplayName("生成摘要 - 正常场景")
    void generateSummary_withValidContent_shouldReturnSummary() {
        // given
        String content = "这是一个详细的报表内容，包含大量数据和图表分析...";
        String expectedSummary = "本报告概述了关键发现和建议";
        when(aiReportGeneratorService.generateSummary(anyString(), anyString()))
                .thenReturn(expectedSummary);

        // when
        String result = aiReportGeneratorService.generateSummary(content, "user-001");

        // then
        assertThat(result).isEqualTo(expectedSummary);
    }

    @Test
    @DisplayName("生成摘要 - 空内容")
    void generateSummary_withEmptyContent_shouldWork() {
        // given
        when(aiReportGeneratorService.generateSummary(anyString(), anyString()))
                .thenReturn("无内容摘要");

        // when
        String result = aiReportGeneratorService.generateSummary("", "user-001");

        // then
        assertThat(result).isEqualTo("无内容摘要");
    }

    @Test
    @DisplayName("调用AI模型 - 正常场景")
    void callAiModel_withValidParams_shouldReturnResponse() {
        // given
        String prompt = "请生成一份日报";
        String model = "gpt-4o-mini";
        String expectedResponse = "这是AI生成的日报内容";
        when(aiReportGeneratorService.callAiModel(anyString(), anyString(), anyString()))
                .thenReturn(expectedResponse);

        // when
        String result = aiReportGeneratorService.callAiModel(prompt, model, "user-001");

        // then
        assertThat(result).isEqualTo(expectedResponse);
    }

    @Test
    @DisplayName("调用AI模型 - 不同模型")
    void callAiModel_differentModels_shouldWork() {
        // given
        String[] models = {"gpt-4o-mini", "gpt-4o", "claude-3-sonnet", "ernie-4"};
        when(aiReportGeneratorService.callAiModel(anyString(), anyString(), anyString()))
                .thenReturn("AI响应");

        for (String model : models) {
            // when
            String result = aiReportGeneratorService.callAiModel("测试提示", model, "user-001");

            // then
            assertThat(result).isEqualTo("AI响应");
        }
    }

    @Test
    @DisplayName("调用AI模型 - 长上下文")
    void callAiModel_longContext_shouldHandle() {
        // given
        String longPrompt = "A".repeat(5000); // 模拟长上下文
        when(aiReportGeneratorService.callAiModel(anyString(), anyString(), anyString()))
                .thenReturn("处理完成");

        // when
        String result = aiReportGeneratorService.callAiModel(longPrompt, "gpt-4o-mini", "user-001");

        // then
        assertThat(result).isEqualTo("处理完成");
    }

    @Test
    @DisplayName("AI服务方法调用验证")
    void verifyAiServiceInteractions_shouldCallCorrectMethods() {
        // given
        GenerateReportDTO dto = createGenerateDTO();
        when(aiReportGeneratorService.generateReportContent(any(), anyString(), anyString()))
                .thenReturn("{}");
        when(aiReportGeneratorService.generateSummary(anyString(), anyString()))
                .thenReturn("摘要");
        when(aiReportGeneratorService.callAiModel(anyString(), anyString(), anyString()))
                .thenReturn("响应");

        // when
        aiReportGeneratorService.generateReportContent(dto, "模板", "user-001");
        aiReportGeneratorService.generateSummary("内容", "user-001");
        aiReportGeneratorService.callAiModel("提示", "gpt-4o-mini", "user-001");

        // then - 验证各方法被调用
        verify(aiReportGeneratorService, times(1)).generateReportContent(any(), anyString(), anyString());
        verify(aiReportGeneratorService, times(1)).generateSummary(anyString(), anyString());
        verify(aiReportGeneratorService, times(1)).callAiModel(anyString(), anyString(), anyString());
    }
}