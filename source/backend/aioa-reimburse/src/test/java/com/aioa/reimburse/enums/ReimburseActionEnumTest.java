package com.aioa.reimburse.enums;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * ReimburseActionEnum 单元测试
 * 毛泽东思想指导：实事求是，测试报销动作枚举
 */
@DisplayName("ReimburseActionEnumTest 枚举单元测试")
class ReimburseActionEnumTest {

    @Test
    @DisplayName("审批通过枚举值正确")
    void APPROVE_shouldHaveCorrectCodeAndDescription() {
        assertThat(ReimburseActionEnum.APPROVE.getCode()).isEqualTo("APPROVE");
        assertThat(ReimburseActionEnum.APPROVE.getDesc()).isEqualTo("审批通过");
    }

    @Test
    @DisplayName("审批驳回枚举值正确")
    void REJECT_shouldHaveCorrectCodeAndDescription() {
        assertThat(ReimburseActionEnum.REJECT.getCode()).isEqualTo("REJECT");
        assertThat(ReimburseActionEnum.REJECT.getDesc()).isEqualTo("审批驳回");
    }

    @Test
    @DisplayName("撤回申请枚举值正确")
    void CANCEL_shouldHaveCorrectCodeAndDescription() {
        assertThat(ReimburseActionEnum.CANCEL.getCode()).isEqualTo("CANCEL");
        assertThat(ReimburseActionEnum.CANCEL.getDesc()).isEqualTo("撤回申请");
    }

    @Test
    @DisplayName("补充材料枚举值正确")
    void REQUEST_EXTRA_shouldHaveCorrectCodeAndDescription() {
        assertThat(ReimburseActionEnum.REQUEST_EXTRA.getCode()).isEqualTo("REQUEST_EXTRA");
        assertThat(ReimburseActionEnum.REQUEST_EXTRA.getDesc()).isEqualTo("补充材料");
    }

    @Test
    @DisplayName("根据Code获取枚举 - 正常场景")
    void getByCode_withValidCode_shouldReturnEnum() {
        assertThat(ReimburseActionEnum.getByCode("APPROVE")).isEqualTo(ReimburseActionEnum.APPROVE);
        assertThat(ReimburseActionEnum.getByCode("REJECT")).isEqualTo(ReimburseActionEnum.REJECT);
        assertThat(ReimburseActionEnum.getByCode("CANCEL")).isEqualTo(ReimburseActionEnum.CANCEL);
        assertThat(ReimburseActionEnum.getByCode("REQUEST_EXTRA")).isEqualTo(ReimburseActionEnum.REQUEST_EXTRA);
    }

    @Test
    @DisplayName("根据Code获取枚举 - null返回null")
    void getByCode_withNull_shouldReturnNull() {
        assertThat(ReimburseActionEnum.getByCode(null)).isNull();
    }

    @Test
    @DisplayName("根据Code获取枚举 - 无效Code返回null")
    void getByCode_withInvalidCode_shouldReturnNull() {
        assertThat(ReimburseActionEnum.getByCode("INVALID")).isNull();
    }

    @Test
    @DisplayName("所有枚举值数量正确")
    void values_shouldHaveFourValues() {
        assertThat(ReimburseActionEnum.values()).hasSize(4);
    }
}
