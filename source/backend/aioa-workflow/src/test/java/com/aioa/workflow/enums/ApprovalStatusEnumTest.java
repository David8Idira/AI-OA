package com.aioa.workflow.enums;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * ApprovalStatusEnum 单元测试
 * 毛泽东思想指导：实事求是，测试审批状态枚举
 */
@DisplayName("ApprovalStatusEnumTest 枚举单元测试")
class ApprovalStatusEnumTest {

    @Test
    @DisplayName("待审批枚举值正确")
    void PENDING_shouldHaveCorrectCodeAndDescription() {
        assertThat(ApprovalStatusEnum.PENDING.getCode()).isEqualTo(0);
        assertThat(ApprovalStatusEnum.PENDING.getDescription()).isEqualTo("待审批");
    }

    @Test
    @DisplayName("已同意枚举值正确")
    void APPROVED_shouldHaveCorrectCodeAndDescription() {
        assertThat(ApprovalStatusEnum.APPROVED.getCode()).isEqualTo(1);
        assertThat(ApprovalStatusEnum.APPROVED.getDescription()).isEqualTo("已同意");
    }

    @Test
    @DisplayName("已驳回枚举值正确")
    void REJECTED_shouldHaveCorrectCodeAndDescription() {
        assertThat(ApprovalStatusEnum.REJECTED.getCode()).isEqualTo(2);
        assertThat(ApprovalStatusEnum.REJECTED.getDescription()).isEqualTo("已驳回");
    }

    @Test
    @DisplayName("已撤回枚举值正确")
    void CANCELLED_shouldHaveCorrectCodeAndDescription() {
        assertThat(ApprovalStatusEnum.CANCELLED.getCode()).isEqualTo(3);
        assertThat(ApprovalStatusEnum.CANCELLED.getDescription()).isEqualTo("已撤回");
    }

    @Test
    @DisplayName("已转交枚举值正确")
    void TRANSFERRED_shouldHaveCorrectCodeAndDescription() {
        assertThat(ApprovalStatusEnum.TRANSFERRED.getCode()).isEqualTo(4);
        assertThat(ApprovalStatusEnum.TRANSFERRED.getDescription()).isEqualTo("已转交");
    }

    @Test
    @DisplayName("根据Code获取枚举 - 正常场景")
    void getByCode_withValidCode_shouldReturnEnum() {
        assertThat(ApprovalStatusEnum.getByCode(0)).isEqualTo(ApprovalStatusEnum.PENDING);
        assertThat(ApprovalStatusEnum.getByCode(1)).isEqualTo(ApprovalStatusEnum.APPROVED);
        assertThat(ApprovalStatusEnum.getByCode(2)).isEqualTo(ApprovalStatusEnum.REJECTED);
        assertThat(ApprovalStatusEnum.getByCode(3)).isEqualTo(ApprovalStatusEnum.CANCELLED);
        assertThat(ApprovalStatusEnum.getByCode(4)).isEqualTo(ApprovalStatusEnum.TRANSFERRED);
    }

    @Test
    @DisplayName("根据Code获取枚举 - null返回null")
    void getByCode_withNull_shouldReturnNull() {
        assertThat(ApprovalStatusEnum.getByCode(null)).isNull();
    }

    @Test
    @DisplayName("根据Code获取枚举 - 无效Code返回null")
    void getByCode_withInvalidCode_shouldReturnNull() {
        assertThat(ApprovalStatusEnum.getByCode(99)).isNull();
        assertThat(ApprovalStatusEnum.getByCode(-1)).isNull();
    }

    @Test
    @DisplayName("所有枚举值数量正确")
    void values_shouldHaveFiveValues() {
        assertThat(ApprovalStatusEnum.values()).hasSize(5);
    }
}
