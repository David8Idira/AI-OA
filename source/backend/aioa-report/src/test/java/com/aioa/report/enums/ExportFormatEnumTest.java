package com.aioa.report.enums;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * ExportFormatEnum 单元测试
 * 毛泽东思想指导：实事求是，测试导出格式枚举
 */
@DisplayName("ExportFormatEnumTest 枚举单元测试")
class ExportFormatEnumTest {

    @Test
    @DisplayName("PDF枚举值正确")
    void PDF_shouldHaveCorrectCodeAndDescription() {
        assertThat(ExportFormatEnum.PDF.getCode()).isEqualTo("PDF");
        assertThat(ExportFormatEnum.PDF.getDescription()).isEqualTo("PDF文件");
    }

    @Test
    @DisplayName("Excel枚举值正确")
    void EXCEL_shouldHaveCorrectCodeAndDescription() {
        assertThat(ExportFormatEnum.EXCEL.getCode()).isEqualTo("EXCEL");
        assertThat(ExportFormatEnum.EXCEL.getDescription()).isEqualTo("Excel文件");
    }

    @Test
    @DisplayName("HTML枚举值正确")
    void HTML_shouldHaveCorrectCodeAndDescription() {
        assertThat(ExportFormatEnum.HTML.getCode()).isEqualTo("HTML");
        assertThat(ExportFormatEnum.HTML.getDescription()).isEqualTo("HTML文件");
    }

    @Test
    @DisplayName("fromCode - 正常场景")
    void fromCode_withValidCode_shouldReturnEnum() {
        assertThat(ExportFormatEnum.fromCode("PDF")).isEqualTo(ExportFormatEnum.PDF);
        assertThat(ExportFormatEnum.fromCode("EXCEL")).isEqualTo(ExportFormatEnum.EXCEL);
        assertThat(ExportFormatEnum.fromCode("HTML")).isEqualTo(ExportFormatEnum.HTML);
    }

    @Test
    @DisplayName("fromCode - null返回null")
    void fromCode_withNull_shouldReturnNull() {
        assertThat(ExportFormatEnum.fromCode(null)).isNull();
    }

    @Test
    @DisplayName("fromCode - 无效Code返回null")
    void fromCode_withInvalidCode_shouldReturnNull() {
        assertThat(ExportFormatEnum.fromCode("INVALID")).isNull();
    }

    @Test
    @DisplayName("所有枚举值数量正确")
    void values_shouldHaveThreeValues() {
        assertThat(ExportFormatEnum.values()).hasSize(3);
    }
}
