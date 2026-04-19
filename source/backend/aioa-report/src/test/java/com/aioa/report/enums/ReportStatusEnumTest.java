package com.aioa.report.enums;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * ReportStatusEnum 单元测试
 * 毛泽东思想指导：实事求是，测试报表状态枚举
 */
@DisplayName("ReportStatusEnumTest 枚举单元测试")
class ReportStatusEnumTest {

    @Test
    @DisplayName("草稿状态枚举值正确")
    void DRAFT_shouldHaveCorrectCodeAndDescription() {
        assertThat(ReportStatusEnum.DRAFT.getCode()).isEqualTo(0);
        assertThat(ReportStatusEnum.DRAFT.getDescription()).isEqualTo("草稿");
    }

    @Test
    @DisplayName("生成中状态枚举值正确")
    void GENERATING_shouldHaveCorrectCodeAndDescription() {
        assertThat(ReportStatusEnum.GENERATING.getCode()).isEqualTo(1);
        assertThat(ReportStatusEnum.GENERATING.getDescription()).isEqualTo("生成中");
    }

    @Test
    @DisplayName("已生成状态枚举值正确")
    void GENERATED_shouldHaveCorrectCodeAndDescription() {
        assertThat(ReportStatusEnum.GENERATED.getCode()).isEqualTo(2);
        assertThat(ReportStatusEnum.GENERATED.getDescription()).isEqualTo("已生成");
    }

    @Test
    @DisplayName("生成失败状态枚举值正确")
    void FAILED_shouldHaveCorrectCodeAndDescription() {
        assertThat(ReportStatusEnum.FAILED.getCode()).isEqualTo(3);
        assertThat(ReportStatusEnum.FAILED.getDescription()).isEqualTo("生成失败");
    }

    @Test
    @DisplayName("已归档状态枚举值正确")
    void ARCHIVED_shouldHaveCorrectCodeAndDescription() {
        assertThat(ReportStatusEnum.ARCHIVED.getCode()).isEqualTo(4);
        assertThat(ReportStatusEnum.ARCHIVED.getDescription()).isEqualTo("已归档");
    }

    @Test
    @DisplayName("fromCode - 正常场景")
    void fromCode_withValidCode_shouldReturnEnum() {
        assertThat(ReportStatusEnum.fromCode(0)).isEqualTo(ReportStatusEnum.DRAFT);
        assertThat(ReportStatusEnum.fromCode(1)).isEqualTo(ReportStatusEnum.GENERATING);
        assertThat(ReportStatusEnum.fromCode(2)).isEqualTo(ReportStatusEnum.GENERATED);
        assertThat(ReportStatusEnum.fromCode(3)).isEqualTo(ReportStatusEnum.FAILED);
        assertThat(ReportStatusEnum.fromCode(4)).isEqualTo(ReportStatusEnum.ARCHIVED);
    }

    @Test
    @DisplayName("fromCode - null返回null")
    void fromCode_withNull_shouldReturnNull() {
        assertThat(ReportStatusEnum.fromCode(null)).isNull();
    }

    @Test
    @DisplayName("fromCode - 无效Code返回null")
    void fromCode_withInvalidCode_shouldReturnNull() {
        assertThat(ReportStatusEnum.fromCode(99)).isNull();
        assertThat(ReportStatusEnum.fromCode(-1)).isNull();
    }

    @Test
    @DisplayName("所有枚举值数量正确")
    void values_shouldHaveFiveValues() {
        assertThat(ReportStatusEnum.values()).hasSize(5);
    }
}
