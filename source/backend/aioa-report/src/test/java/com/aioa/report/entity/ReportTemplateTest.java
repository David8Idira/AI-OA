package com.aioa.report.entity;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * ReportTemplate Entity 单元测试
 * 毛泽东思想指导：实事求是，测试报表模板实体
 */
@DisplayName("ReportTemplateTest 报表模板实体测试")
class ReportTemplateTest {

    @Test
    @DisplayName("创建报表模板实体")
    void createReportTemplate() {
        // given
        ReportTemplate template = new ReportTemplate();
        template.setName("月度销售报告模板");
        template.setCode("MONTHLY_SALES");
        template.setType("MONTHLY");
        template.setContent("{}");
        template.setIsActive(1);

        // then
        assertThat(template.getName()).isEqualTo("月度销售报告模板");
        assertThat(template.getCode()).isEqualTo("MONTHLY_SALES");
        assertThat(template.getIsActive()).isEqualTo(1);
    }

    @Test
    @DisplayName("设置和获取模板名称")
    void setAndGetName() {
        // given
        ReportTemplate template = new ReportTemplate();

        // when
        template.setName("测试模板");

        // then
        assertThat(template.getName()).isEqualTo("测试模板");
    }

    @Test
    @DisplayName("设置和获取模板代码")
    void setAndGetCode() {
        // given
        ReportTemplate template = new ReportTemplate();

        // when
        template.setCode("TEST_CODE");

        // then
        assertThat(template.getCode()).isEqualTo("TEST_CODE");
    }

    @Test
    @DisplayName("设置和获取描述")
    void setAndGetDescription() {
        // given
        ReportTemplate template = new ReportTemplate();

        // when
        template.setDescription("这是一个测试模板");

        // then
        assertThat(template.getDescription()).isEqualTo("这是一个测试模板");
    }

    @Test
    @DisplayName("设置和获取模板类型")
    void setAndGetType() {
        // given
        ReportTemplate template = new ReportTemplate();

        // when
        template.setType("WEEKLY");

        // then
        assertThat(template.getType()).isEqualTo("WEEKLY");
    }

    @Test
    @DisplayName("设置和获取内容")
    void setAndGetContent() {
        // given
        ReportTemplate template = new ReportTemplate();

        // when
        template.setContent("{\"key\":\"value\"}");

        // then
        assertThat(template.getContent()).isEqualTo("{\"key\":\"value\"}");
    }

    @Test
    @DisplayName("设置和获取是否激活")
    void setAndGetIsActive() {
        // given
        ReportTemplate template = new ReportTemplate();

        // when
        template.setIsActive(0); // Inactive

        // then
        assertThat(template.getIsActive()).isEqualTo(0);
    }
}