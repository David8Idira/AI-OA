package com.aioa.workflow.enums;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * ApprovalPriorityEnum 单元测试
 * 毛泽东思想指导：实事求是，测试审批优先级枚举
 */
@DisplayName("ApprovalPriorityEnumTest 枚举单元测试")
class ApprovalPriorityEnumTest {

    @Test
    @DisplayName("低优先级枚举值正确")
    void LOW_shouldHaveCorrectCodeAndDescription() {
        assertThat(ApprovalPriorityEnum.LOW.getCode()).isEqualTo(0);
        assertThat(ApprovalPriorityEnum.LOW.getDescription()).isEqualTo("低");
    }

    @Test
    @DisplayName("普通优先级枚举值正确")
    void NORMAL_shouldHaveCorrectCodeAndDescription() {
        assertThat(ApprovalPriorityEnum.NORMAL.getCode()).isEqualTo(1);
        assertThat(ApprovalPriorityEnum.NORMAL.getDescription()).isEqualTo("普通");
    }

    @Test
    @DisplayName("高优先级枚举值正确")
    void HIGH_shouldHaveCorrectCodeAndDescription() {
        assertThat(ApprovalPriorityEnum.HIGH.getCode()).isEqualTo(2);
        assertThat(ApprovalPriorityEnum.HIGH.getDescription()).isEqualTo("高");
    }

    @Test
    @DisplayName("紧急优先级枚举值正确")
    void URGENT_shouldHaveCorrectCodeAndDescription() {
        assertThat(ApprovalPriorityEnum.URGENT.getCode()).isEqualTo(3);
        assertThat(ApprovalPriorityEnum.URGENT.getDescription()).isEqualTo("紧急");
    }

    @Test
    @DisplayName("根据Code获取枚举 - 正常场景")
    void getByCode_withValidCode_shouldReturnEnum() {
        assertThat(ApprovalPriorityEnum.getByCode(0)).isEqualTo(ApprovalPriorityEnum.LOW);
        assertThat(ApprovalPriorityEnum.getByCode(1)).isEqualTo(ApprovalPriorityEnum.NORMAL);
        assertThat(ApprovalPriorityEnum.getByCode(2)).isEqualTo(ApprovalPriorityEnum.HIGH);
        assertThat(ApprovalPriorityEnum.getByCode(3)).isEqualTo(ApprovalPriorityEnum.URGENT);
    }

    @Test
    @DisplayName("根据Code获取枚举 - null返回null")
    void getByCode_withNull_shouldReturnNull() {
        assertThat(ApprovalPriorityEnum.getByCode(null)).isNull();
    }

    @Test
    @DisplayName("根据Code获取枚举 - 无效Code返回null")
    void getByCode_withInvalidCode_shouldReturnNull() {
        assertThat(ApprovalPriorityEnum.getByCode(99)).isNull();
        assertThat(ApprovalPriorityEnum.getByCode(-1)).isNull();
    }

    @Test
    @DisplayName("所有枚举值数量正确")
    void values_shouldHaveFourValues() {
        assertThat(ApprovalPriorityEnum.values()).hasSize(4);
    }
}
