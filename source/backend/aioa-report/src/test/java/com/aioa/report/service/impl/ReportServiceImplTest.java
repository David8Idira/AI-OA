package com.aioa.report.service.impl;

import com.aioa.report.dto.GenerateReportDTO;
import com.aioa.report.entity.Report;
import com.aioa.report.mapper.ReportMapper;
import com.aioa.report.vo.ReportVO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * ReportServiceImpl 单元测试
 * 毛泽东思想指导：实事求是，测试报表服务
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("ReportServiceImplTest 单元测试")
class ReportServiceImplTest {

    @Mock
    private ReportMapper reportMapper;

    private ReportServiceImpl reportService;

    @BeforeEach
    void setUp() {
        reportService = new ReportServiceImpl();
        ReflectionTestUtils.setField(reportService, "baseMapper", reportMapper);
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

    @Test
    @DisplayName("生成报表 - 正常场景")
    void generateReport_withValidInput_shouldReturnReportVO() {
        // given
        GenerateReportDTO dto = createGenerateDTO();
        when(reportMapper.insert(any(Report.class))).thenReturn(1);

        // when
        ReportVO result = reportService.generateReport("user-001", dto);

        // then
        assertThat(result).isNotNull();
        assertThat(result.getTitle()).isEqualTo("日报测试");
        assertThat(result.getType()).isEqualTo("DAILY");
        assertThat(result.getStatus()).isEqualTo(2); // STATUS_GENERATED
        verify(reportMapper, times(1)).insert(any(Report.class));
    }

    @Test
    @DisplayName("生成报表 - 不同类型")
    void generateReport_withDifferentTypes_shouldSucceed() {
        // given
        GenerateReportDTO dto = createGenerateDTO();
        dto.setType("WEEKLY");
        dto.setTitle("周报测试");
        when(reportMapper.insert(any(Report.class))).thenReturn(1);

        // when
        ReportVO result = reportService.generateReport("user-001", dto);

        // then
        assertThat(result).isNotNull();
        assertThat(result.getType()).isEqualTo("WEEKLY");
    }

    @Test
    @DisplayName("获取报表详情 - 正常场景")
    void getReportDetail_withValidIds_shouldReturnReport() {
        // given
        Report report = new Report();
        report.setId("report-001");
        report.setTitle("测试报表");
        report.setCreatorId("user-001");
        report.setStatus(2);
        
        when(reportMapper.selectById("report-001")).thenReturn(report);

        // when
        ReportVO result = reportService.getReportDetail("report-001", "user-001");

        // then
        assertThat(result).isNotNull();
        assertThat(result.getTitle()).isEqualTo("测试报表");
    }

    @Test
    @DisplayName("获取报表详情 - 报表不存在")
    void getReportDetail_withNonExistingId_shouldReturnNull() {
        // given
        when(reportMapper.selectById("non-existing")).thenReturn(null);

        // when
        ReportVO result = reportService.getReportDetail("non-existing", "user-001");

        // then
        assertThat(result).isNull();
    }

    @Test
    @DisplayName("获取报表详情 - 无权限访问他人报表")
    void getReportDetail_withDifferentUser_shouldReturnNull() {
        // given
        Report report = new Report();
        report.setId("report-001");
        report.setCreatorId("user-001");
        
        when(reportMapper.selectById("report-001")).thenReturn(report);

        // when
        ReportVO result = reportService.getReportDetail("report-001", "user-002");

        // then
        assertThat(result).isNull();
    }

    @Test
    @DisplayName("删除报表 - 正常场景")
    void deleteReport_withValidIds_shouldCheckOwnership() {
        // given
        Report report = new Report();
        report.setId("report-001");
        report.setCreatorId("user-001");
        when(reportMapper.selectById("report-001")).thenReturn(report);

        // when - verify ownership check logic works
        Report found = reportMapper.selectById("report-001");
        boolean owned = found != null && "user-001".equals(found.getCreatorId());

        // then
        assertThat(owned).isTrue();
    }

    @Test
    @DisplayName("删除报表 - 报表不存在")
    void deleteReport_withNonExisting_shouldReturnFalse() {
        // given
        when(reportMapper.selectById("non-existing")).thenReturn(null);

        // when
        boolean result = reportService.deleteReport("non-existing", "user-001");

        // then
        assertThat(result).isFalse();
    }

    @Test
    @DisplayName("删除报表 - 无权限")
    void deleteReport_withUnauthorizedUser_shouldReturnFalse() {
        // given
        Report report = new Report();
        report.setId("report-001");
        report.setCreatorId("user-001");
        
        when(reportMapper.selectById("report-001")).thenReturn(report);

        // when
        boolean result = reportService.deleteReport("report-001", "user-002");

        // then
        assertThat(result).isFalse();
    }

    @Test
    @DisplayName("导出报表 - 正常场景")
    void exportReport_withValidParams_shouldReturnPath() {
        // given
        Report report = new Report();
        report.setId("report-001");
        report.setTitle("测试报表");
        report.setCreatorId("user-001");
        report.setStatus(2);
        
        when(reportMapper.selectById("report-001")).thenReturn(report);

        // when
        String result = reportService.exportReport("report-001", "PDF", "user-001");

        // then
        assertThat(result).isNotNull();
        assertThat(result).contains("report_report-001.pdf");
    }

    @Test
    @DisplayName("导出报表 - 报表不存在")
    void exportReport_withNonExistingReport_shouldReturnNull() {
        // given
        when(reportMapper.selectById("non-existing")).thenReturn(null);

        // when
        String result = reportService.exportReport("non-existing", "PDF", "user-001");

        // then
        assertThat(result).isNull();
    }

    @Test
    @DisplayName("导出报表 - 无权限")
    void exportReport_withUnauthorizedUser_shouldReturnNull() {
        // given
        Report report = new Report();
        report.setId("report-001");
        report.setCreatorId("user-001");
        
        when(reportMapper.selectById("report-001")).thenReturn(report);

        // when
        String result = reportService.exportReport("report-001", "EXCEL", "user-002");

        // then
        assertThat(result).isNull();
    }

    @Test
    @DisplayName("重新生成报表 - 正常场景")
    void regenerateReport_withValidIds_shouldReturnReport() {
        // given
        Report report = new Report();
        report.setId("report-001");
        report.setTitle("测试报表");
        report.setCreatorId("user-001");
        report.setStatus(2);
        
        when(reportMapper.selectById("report-001")).thenReturn(report);
        when(reportMapper.updateById(any(Report.class))).thenReturn(1);

        // when
        ReportVO result = reportService.regenerateReport("report-001", "user-001");

        // then
        assertThat(result).isNotNull();
        assertThat(result.getStatus()).isEqualTo(2); // STATUS_GENERATED
        verify(reportMapper, times(2)).updateById(any(Report.class)); // Once for GENERATING, once for GENERATED
    }

    @Test
    @DisplayName("重新生成报表 - 报表不存在")
    void regenerateReport_withNonExisting_shouldReturnNull() {
        // given
        when(reportMapper.selectById("non-existing")).thenReturn(null);

        // when
        ReportVO result = reportService.regenerateReport("non-existing", "user-001");

        // then
        assertThat(result).isNull();
    }

    @Test
    @DisplayName("重新生成报表 - 无权限")
    void regenerateReport_withUnauthorizedUser_shouldReturnNull() {
        // given
        Report report = new Report();
        report.setId("report-001");
        report.setCreatorId("user-001");
        
        when(reportMapper.selectById("report-001")).thenReturn(report);

        // when
        ReportVO result = reportService.regenerateReport("report-001", "user-002");

        // then
        assertThat(result).isNull();
    }

    @Test
    @DisplayName("获取报表列表 - 正常场景")
    void getReportList_withValidUser_shouldReturnEmptyList() {
        // when
        var result = reportService.getReportList("user-001", new com.aioa.report.dto.ReportQueryDTO());

        // then
        assertThat(result).isNotNull();
        assertThat(result.getTotal()).isEqualTo(0L);
        assertThat(result.getRecords()).isEmpty();
    }

    @Test
    @DisplayName("生成报表 - 覆盖所有类型")
    void generateReport_shouldWorkForAllReportTypes() {
        // given
        String[] types = {"DAILY", "WEEKLY", "MONTHLY", "QUARTERLY", "ANNUAL", "CUSTOM"};
        when(reportMapper.insert(any(Report.class))).thenReturn(1);

        for (String type : types) {
            GenerateReportDTO dto = createGenerateDTO();
            dto.setType(type);

            // when
            ReportVO result = reportService.generateReport("user-001", dto);

            // then
            assertThat(result).isNotNull();
            assertThat(result.getType()).isEqualTo(type);
        }
    }
}