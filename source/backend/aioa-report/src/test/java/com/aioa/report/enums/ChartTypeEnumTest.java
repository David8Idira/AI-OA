package com.aioa.report.enums;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * ChartTypeEnum 单元测试
 * 毛泽东思想指导：实事求是，测试图表类型枚举
 */
@DisplayName("ChartTypeEnumTest 枚举单元测试")
class ChartTypeEnumTest {

    @Test
    @DisplayName("柱状图枚举值正确")
    void BAR_shouldHaveCorrectCodeAndDescription() {
        assertThat(ChartTypeEnum.BAR.getCode()).isEqualTo("BAR");
        assertThat(ChartTypeEnum.BAR.getDescription()).isEqualTo("柱状图");
    }

    @Test
    @DisplayName("折线图枚举值正确")
    void LINE_shouldHaveCorrectCodeAndDescription() {
        assertThat(ChartTypeEnum.LINE.getCode()).isEqualTo("LINE");
        assertThat(ChartTypeEnum.LINE.getDescription()).isEqualTo("折线图");
    }

    @Test
    @DisplayName("饼图枚举值正确")
    void PIE_shouldHaveCorrectCodeAndDescription() {
        assertThat(ChartTypeEnum.PIE.getCode()).isEqualTo("PIE");
        assertThat(ChartTypeEnum.PIE.getDescription()).isEqualTo("饼图");
    }

    @Test
    @DisplayName("面积图枚举值正确")
    void AREA_shouldHaveCorrectCodeAndDescription() {
        assertThat(ChartTypeEnum.AREA.getCode()).isEqualTo("AREA");
        assertThat(ChartTypeEnum.AREA.getDescription()).isEqualTo("面积图");
    }

    @Test
    @DisplayName("散点图枚举值正确")
    void SCATTER_shouldHaveCorrectCodeAndDescription() {
        assertThat(ChartTypeEnum.SCATTER.getCode()).isEqualTo("SCATTER");
        assertThat(ChartTypeEnum.SCATTER.getDescription()).isEqualTo("散点图");
    }

    @Test
    @DisplayName("数据表格枚举值正确")
    void TABLE_shouldHaveCorrectCodeAndDescription() {
        assertThat(ChartTypeEnum.TABLE.getCode()).isEqualTo("TABLE");
        assertThat(ChartTypeEnum.TABLE.getDescription()).isEqualTo("数据表格");
    }

    @Test
    @DisplayName("文本描述枚举值正确")
    void TEXT_shouldHaveCorrectCodeAndDescription() {
        assertThat(ChartTypeEnum.TEXT.getCode()).isEqualTo("TEXT");
        assertThat(ChartTypeEnum.TEXT.getDescription()).isEqualTo("文本描述");
    }

    @Test
    @DisplayName("fromCode - 正常场景")
    void fromCode_withValidCode_shouldReturnEnum() {
        assertThat(ChartTypeEnum.fromCode("BAR")).isEqualTo(ChartTypeEnum.BAR);
        assertThat(ChartTypeEnum.fromCode("LINE")).isEqualTo(ChartTypeEnum.LINE);
        assertThat(ChartTypeEnum.fromCode("PIE")).isEqualTo(ChartTypeEnum.PIE);
    }

    @Test
    @DisplayName("fromCode - null返回null")
    void fromCode_withNull_shouldReturnNull() {
        assertThat(ChartTypeEnum.fromCode(null)).isNull();
    }

    @Test
    @DisplayName("fromCode - 无效Code返回null")
    void fromCode_withInvalidCode_shouldReturnNull() {
        assertThat(ChartTypeEnum.fromCode("INVALID")).isNull();
    }

    @Test
    @DisplayName("所有枚举值数量正确")
    void values_shouldHaveSevenValues() {
        assertThat(ChartTypeEnum.values()).hasSize(7);
    }
}
