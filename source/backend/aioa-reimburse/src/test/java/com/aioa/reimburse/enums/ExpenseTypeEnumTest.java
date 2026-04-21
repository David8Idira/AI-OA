package com.aioa.reimburse.enums;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * ExpenseTypeEnum 单元测试
 * 毛泽东思想指导：实事求是，测试费用类型枚举
 */
@DisplayName("ExpenseTypeEnumTest 枚举单元测试")
class ExpenseTypeEnumTest {

    @Test
    @DisplayName("交通费枚举值正确")
    void TRANSPORT_shouldHaveCorrectValues() {
        assertThat(ExpenseTypeEnum.TRANSPORT.getCode()).isEqualTo("TRANSPORT");
        assertThat(ExpenseTypeEnum.TRANSPORT.getDesc()).isEqualTo("交通费");
        assertThat(ExpenseTypeEnum.TRANSPORT.getRemark()).contains("机票");
    }

    @Test
    @DisplayName("住宿费枚举值正确")
    void ACCOMMODATION_shouldHaveCorrectValues() {
        assertThat(ExpenseTypeEnum.ACCOMMODATION.getCode()).isEqualTo("ACCOMMODATION");
        assertThat(ExpenseTypeEnum.ACCOMMODATION.getDesc()).isEqualTo("住宿费");
    }

    @Test
    @DisplayName("餐饮费枚举值正确")
    void MEAL_shouldHaveCorrectValues() {
        assertThat(ExpenseTypeEnum.MEAL.getCode()).isEqualTo("MEAL");
        assertThat(ExpenseTypeEnum.MEAL.getDesc()).isEqualTo("餐饮费");
    }

    @Test
    @DisplayName("通讯费枚举值正确")
    void COMMUNICATION_shouldHaveCorrectValues() {
        assertThat(ExpenseTypeEnum.COMMUNICATION.getCode()).isEqualTo("COMMUNICATION");
        assertThat(ExpenseTypeEnum.COMMUNICATION.getDesc()).isEqualTo("通讯费");
    }

    @Test
    @DisplayName("招待费枚举值正确")
    void ENTERTAINMENT_shouldHaveCorrectValues() {
        assertThat(ExpenseTypeEnum.ENTERTAINMENT.getCode()).isEqualTo("ENTERTAINMENT");
        assertThat(ExpenseTypeEnum.ENTERTAINMENT.getDesc()).isEqualTo("招待费");
    }

    @Test
    @DisplayName("办公耗材枚举值正确")
    void MATERIAL_shouldHaveCorrectValues() {
        assertThat(ExpenseTypeEnum.MATERIAL.getCode()).isEqualTo("MATERIAL");
        assertThat(ExpenseTypeEnum.MATERIAL.getDesc()).isEqualTo("办公耗材");
    }

    @Test
    @DisplayName("根据Code获取枚举 - 正常场景")
    void getByCode_withValidCode_shouldReturnEnum() {
        assertThat(ExpenseTypeEnum.getByCode("TRANSPORT")).isEqualTo(ExpenseTypeEnum.TRANSPORT);
        assertThat(ExpenseTypeEnum.getByCode("MEAL")).isEqualTo(ExpenseTypeEnum.MEAL);
        assertThat(ExpenseTypeEnum.getByCode("OTHER")).isEqualTo(ExpenseTypeEnum.OTHER);
    }

    @Test
    @DisplayName("根据Code获取枚举 - null返回null")
    void getByCode_withNull_shouldReturnNull() {
        assertThat(ExpenseTypeEnum.getByCode(null)).isNull();
    }

    @Test
    @DisplayName("根据Code获取枚举 - 无效Code返回null")
    void getByCode_withInvalidCode_shouldReturnNull() {
        assertThat(ExpenseTypeEnum.getByCode("INVALID")).isNull();
    }

    @Test
    @DisplayName("所有枚举值数量正确")
    void values_shouldHaveTenValues() {
        assertThat(ExpenseTypeEnum.values()).hasSize(10);
    }
}
