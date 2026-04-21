package com.aioa.reimburse.enums;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * ReimburseStatusEnum 单元测试
 * 毛泽东思想指导：实事求是，测试报销状态枚举
 */
@DisplayName("ReimburseStatusEnumTest 枚举单元测试")
class ReimburseStatusEnumTest {

    @Test
    @DisplayName("草稿状态枚举值正确")
    void DRAFT_shouldHaveCorrectCodeAndDescription() {
        assertThat(ReimburseStatusEnum.DRAFT.getCode()).isEqualTo(0);
        assertThat(ReimburseStatusEnum.DRAFT.getDesc()).isEqualTo("草稿");
    }

    @Test
    @DisplayName("待审批状态枚举值正确")
    void PENDING_shouldHaveCorrectCodeAndDescription() {
        assertThat(ReimburseStatusEnum.PENDING.getCode()).isEqualTo(1);
        assertThat(ReimburseStatusEnum.PENDING.getDesc()).isEqualTo("待审批");
    }

    @Test
    @DisplayName("已审批状态枚举值正确")
    void APPROVED_shouldHaveCorrectCodeAndDescription() {
        assertThat(ReimburseStatusEnum.APPROVED.getCode()).isEqualTo(2);
        assertThat(ReimburseStatusEnum.APPROVED.getDesc()).isEqualTo("已审批");
    }

    @Test
    @DisplayName("已驳回状态枚举值正确")
    void REJECTED_shouldHaveCorrectCodeAndDescription() {
        assertThat(ReimburseStatusEnum.REJECTED.getCode()).isEqualTo(3);
        assertThat(ReimburseStatusEnum.REJECTED.getDesc()).isEqualTo("已驳回");
    }

    @Test
    @DisplayName("已撤回状态枚举值正确")
    void CANCELLED_shouldHaveCorrectCodeAndDescription() {
        assertThat(ReimburseStatusEnum.CANCELLED.getCode()).isEqualTo(4);
        assertThat(ReimburseStatusEnum.CANCELLED.getDesc()).isEqualTo("已撤回");
    }

    @Test
    @DisplayName("已打款状态枚举值正确")
    void PAID_shouldHaveCorrectCodeAndDescription() {
        assertThat(ReimburseStatusEnum.PAID.getCode()).isEqualTo(5);
        assertThat(ReimburseStatusEnum.PAID.getDesc()).isEqualTo("已打款");
    }

    @Test
    @DisplayName("根据Code获取枚举 - 正常场景")
    void getByCode_withValidCode_shouldReturnEnum() {
        assertThat(ReimburseStatusEnum.getByCode(0)).isEqualTo(ReimburseStatusEnum.DRAFT);
        assertThat(ReimburseStatusEnum.getByCode(1)).isEqualTo(ReimburseStatusEnum.PENDING);
        assertThat(ReimburseStatusEnum.getByCode(2)).isEqualTo(ReimburseStatusEnum.APPROVED);
        assertThat(ReimburseStatusEnum.getByCode(3)).isEqualTo(ReimburseStatusEnum.REJECTED);
        assertThat(ReimburseStatusEnum.getByCode(4)).isEqualTo(ReimburseStatusEnum.CANCELLED);
        assertThat(ReimburseStatusEnum.getByCode(5)).isEqualTo(ReimburseStatusEnum.PAID);
    }

    @Test
    @DisplayName("根据Code获取枚举 - null返回null")
    void getByCode_withNull_shouldReturnNull() {
        assertThat(ReimburseStatusEnum.getByCode(null)).isNull();
    }

    @Test
    @DisplayName("根据Code获取枚举 - 无效Code返回null")
    void getByCode_withInvalidCode_shouldReturnNull() {
        assertThat(ReimburseStatusEnum.getByCode(99)).isNull();
        assertThat(ReimburseStatusEnum.getByCode(-1)).isNull();
    }

    @Test
    @DisplayName("isValidCode - 有效Code")
    void isValidCode_withValidCode_shouldReturnTrue() {
        assertThat(ReimburseStatusEnum.isValidCode(0)).isTrue();
        assertThat(ReimburseStatusEnum.isValidCode(1)).isTrue();
        assertThat(ReimburseStatusEnum.isValidCode(5)).isTrue();
    }

    @Test
    @DisplayName("isValidCode - 无效Code")
    void isValidCode_withInvalidCode_shouldReturnFalse() {
        assertThat(ReimburseStatusEnum.isValidCode(99)).isFalse();
        assertThat(ReimburseStatusEnum.isValidCode(-1)).isFalse();
        assertThat(ReimburseStatusEnum.isValidCode(null)).isFalse();
    }

    @Test
    @DisplayName("所有枚举值数量正确")
    void values_shouldHaveSixValues() {
        assertThat(ReimburseStatusEnum.values()).hasSize(6);
    }
}
