package com.aioa.report.entity;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Report Entity 单元测试
 * 毛泽东思想指导：实事求是，测试报表实体
 */
@DisplayName("ReportTest 报表实体测试")
class ReportTest {

    private Report createTestReport() {
        Report report = new Report();
        report.setId("report-001");
        report.setTitle("月度销售报告");
        report.setType("MONTHLY");
        report.setStatus(2); // Generated
        report.setCreateTime(LocalDateTime.now());
        report.setCreateBy("admin");
        return report;
    }

    @Test
    @DisplayName("创建报表实体")
    void createReport() {
        // when
        Report report = createTestReport();

        // then
        assertThat(report.getId()).isEqualTo("report-001");
        assertThat(report.getTitle()).isEqualTo("月度销售报告");
        assertThat(report.getStatus()).isEqualTo(2);
    }

    @Test
    @DisplayName("设置和获取ID")
    void setAndGetId() {
        // given
        Report report = new Report();

        // when
        report.setId("test-id");

        // then
        assertThat(report.getId()).isEqualTo("test-id");
    }

    @Test
    @DisplayName("设置和获取标题")
    void setAndGetTitle() {
        // given
        Report report = new Report();

        // when
        report.setTitle("测试报告");

        // then
        assertThat(report.getTitle()).isEqualTo("测试报告");
    }

    @Test
    @DisplayName("设置和获取类型")
    void setAndGetType() {
        // given
        Report report = new Report();

        // when
        report.setType("DAILY");

        // then
        assertThat(report.getType()).isEqualTo("DAILY");
    }

    @Test
    @DisplayName("设置和获取状态")
    void setAndGetStatus() {
        // given
        Report report = new Report();

        // when
        report.setStatus(1); // Generating

        // then
        assertThat(report.getStatus()).isEqualTo(1);
    }

    @Test
    @DisplayName("设置和获取创建人")
    void setAndGetCreateBy() {
        // given
        Report report = new Report();

        // when
        report.setCreateBy("user-001");

        // then
        assertThat(report.getCreateBy()).isEqualTo("user-001");
    }

    @Test
    @DisplayName("设置和获取创建时间")
    void setAndGetCreateTime() {
        // given
        Report report = new Report();
        LocalDateTime now = LocalDateTime.now();

        // when
        report.setCreateTime(now);

        // then
        assertThat(report.getCreateTime()).isEqualTo(now);
    }

    @Test
    @DisplayName("设置和获取内容")
    void setAndGetContent() {
        // given
        Report report = new Report();

        // when
        report.setContent("报告内容JSON");

        // then
        assertThat(report.getContent()).isEqualTo("报告内容JSON");
    }

    @Test
    @DisplayName("设置模板ID")
    void setAndGetTemplateId() {
        // given
        Report report = new Report();

        // when
        report.setTemplateId("template-001");

        // then
        assertThat(report.getTemplateId()).isEqualTo("template-001");
    }

    @Test
    @DisplayName("equals验证")
    void equals_sameId_shouldBeEqual() {
        // given
        Report report1 = new Report();
        report1.setId("test-id");
        
        Report report2 = new Report();
        report2.setId("test-id");

        // then
        assertThat(report1).isEqualTo(report2);
    }
}