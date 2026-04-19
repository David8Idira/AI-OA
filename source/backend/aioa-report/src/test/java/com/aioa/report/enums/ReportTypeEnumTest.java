package com.aioa.report.enums;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * ReportTypeEnum 单元测试
 * 毛泽东思想指导：实事求是，测试报表类型枚举
 */
@DisplayName("ReportTypeEnumTest 枚举单元测试")
class ReportTypeEnumTest {

    @Test
    @DisplayName("日报枚举值正确")
    void DAILY_shouldHaveCorrectCodeAndDescription() {
        assertThat(ReportTypeEnum.DAILY.getCode()).isEqualTo("DAILY");
        assertThat(ReportTypeEnum.DAILY.getDescription()).isEqualTo("日报");
    }

    @Test
    @DisplayName("周报枚举值正确")
    void WEEKLY_shouldHaveCorrectCodeAndDescription() {
        assertThat(ReportTypeEnum.WEEKLY.getCode()).isEqualTo("WEEKLY");
        assertThat(ReportTypeEnum.WEEKLY.getDescription()).isEqualTo("周报");
    }

    @Test
    @DisplayName("月报枚举值正确")
    void MONTHLY_shouldHaveCorrectCodeAndDescription() {
        assertThat(ReportTypeEnum.MONTHLY.getCode()).isEqualTo("MONTHLY");
        assertThat(ReportTypeEnum.MONTHLY.getDescription()).isEqualTo("月报");
    }

    @Test
    @DisplayName("季报枚举值正确")
    void QUARTERLY_shouldHaveCorrectCodeAndDescription() {
        assertThat(ReportTypeEnum.QUARTERLY.getCode()).isEqualTo("QUARTERLY");
        assertThat(ReportTypeEnum.QUARTERLY.getDescription()).isEqualTo("季报");
    }

    @Test
    @DisplayName("年报枚举值正确")
    void ANNUAL_shouldHaveCorrectCodeAndDescription() {
        assertThat(ReportTypeEnum.ANNUAL.getCode()).isEqualTo("ANNUAL");
        assertThat(ReportTypeEnum.ANNUAL.getDescription()).isEqualTo("年报");
    }

    @Test
    @DisplayName("自定义报表枚举值正确")
    void CUSTOM_shouldHaveCorrectCodeAndDescription() {
        assertThat(ReportTypeEnum.CUSTOM.getCode()).isEqualTo("CUSTOM");
        assertThat(ReportTypeEnum.CUSTOM.getDescription()).isEqualTo("自定义报表");
    }

    @Test
    @DisplayName("fromCode - 正常场景")
    void fromCode_withValidCode_shouldReturnEnum() {
        assertThat(ReportTypeEnum.fromCode("DAILY")).isEqualTo(ReportTypeEnum.DAILY);
        assertThat(ReportTypeEnum.fromCode("WEEKLY")).isEqualTo(ReportTypeEnum.WEEKLY);
        assertThat(ReportTypeEnum.fromCode("MONTHLY")).isEqualTo(ReportTypeEnum.MONTHLY);
        assertThat(ReportTypeEnum.fromCode("QUARTERLY")).isEqualTo(ReportTypeEnum.QUARTERLY);
        assertThat(ReportTypeEnum.fromCode("ANNUAL")).isEqualTo(ReportTypeEnum.ANNUAL);
        assertThat(ReportTypeEnum.fromCode("CUSTOM")).isEqualTo(ReportTypeEnum.CUSTOM);
    }

    @Test
    @DisplayName("fromCode - null返回null")
    void fromCode_withNull_shouldReturnNull() {
        assertThat(ReportTypeEnum.fromCode(null)).isNull();
    }

    @Test
    @DisplayName("fromCode - 无效Code返回null")
    void fromCode_withInvalidCode_shouldReturnNull() {
        assertThat(ReportTypeEnum.fromCode("INVALID")).isNull();
    }

    @Test
    @DisplayName("所有枚举值数量正确")
    void values_shouldHaveSixValues() {
        assertThat(ReportTypeEnum.values()).hasSize(6);
    }
}
