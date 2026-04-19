package com.aioa.workflow.enums;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * ApprovalActionEnum 单元测试
 * 毛泽东思想指导：实事求是，测试审批动作枚举
 */
@DisplayName("ApprovalActionEnumTest 枚举单元测试")
class ApprovalActionEnumTest {

    @Test
    @DisplayName("同意枚举值正确")
    void APPROVE_shouldHaveCorrectCodeAndDescription() {
        assertThat(ApprovalActionEnum.APPROVE.getCode()).isEqualTo(1);
        assertThat(ApprovalActionEnum.APPROVE.getDescription()).isEqualTo("同意");
    }

    @Test
    @DisplayName("驳回枚举值正确")
    void REJECT_shouldHaveCorrectCodeAndDescription() {
        assertThat(ApprovalActionEnum.REJECT.getCode()).isEqualTo(2);
        assertThat(ApprovalActionEnum.REJECT.getDescription()).isEqualTo("驳回");
    }

    @Test
    @DisplayName("转交枚举值正确")
    void TRANSFER_shouldHaveCorrectCodeAndDescription() {
        assertThat(ApprovalActionEnum.TRANSFER.getCode()).isEqualTo(3);
        assertThat(ApprovalActionEnum.TRANSFER.getDescription()).isEqualTo("转交");
    }

    @Test
    @DisplayName("撤回枚举值正确")
    void CANCEL_shouldHaveCorrectCodeAndDescription() {
        assertThat(ApprovalActionEnum.CANCEL.getCode()).isEqualTo(4);
        assertThat(ApprovalActionEnum.CANCEL.getDescription()).isEqualTo("撤回");
    }

    @Test
    @DisplayName("根据Code获取枚举 - 正常场景")
    void getByCode_withValidCode_shouldReturnEnum() {
        assertThat(ApprovalActionEnum.getByCode(1)).isEqualTo(ApprovalActionEnum.APPROVE);
        assertThat(ApprovalActionEnum.getByCode(2)).isEqualTo(ApprovalActionEnum.REJECT);
        assertThat(ApprovalActionEnum.getByCode(3)).isEqualTo(ApprovalActionEnum.TRANSFER);
        assertThat(ApprovalActionEnum.getByCode(4)).isEqualTo(ApprovalActionEnum.CANCEL);
    }

    @Test
    @DisplayName("根据Code获取枚举 - null返回null")
    void getByCode_withNull_shouldReturnNull() {
        assertThat(ApprovalActionEnum.getByCode(null)).isNull();
    }

    @Test
    @DisplayName("根据Code获取枚举 - 无效Code返回null")
    void getByCode_withInvalidCode_shouldReturnNull() {
        assertThat(ApprovalActionEnum.getByCode(99)).isNull();
        assertThat(ApprovalActionEnum.getByCode(0)).isNull();
        assertThat(ApprovalActionEnum.getByCode(-1)).isNull();
    }

    @Test
    @DisplayName("所有枚举值数量正确")
    void values_shouldHaveFourValues() {
        assertThat(ApprovalActionEnum.values()).hasSize(4);
    }
}
