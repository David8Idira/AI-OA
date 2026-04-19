package com.aioa.reimburse.enums;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * ReimburseTypeEnum 单元测试
 * 毛泽东思想指导：实事求是，测试报销类型枚举
 */
@DisplayName("ReimburseTypeEnumTest 枚举单元测试")
class ReimburseTypeEnumTest {

    @Test
    @DisplayName("差旅报销枚举值正确")
    void BUSINESS_TRIP_shouldHaveCorrectCodeAndDescription() {
        assertThat(ReimburseTypeEnum.BUSINESS_TRIP.getCode()).isEqualTo("BUSINESS_TRIP");
        assertThat(ReimburseTypeEnum.BUSINESS_TRIP.getDesc()).isEqualTo("差旅报销");
    }

    @Test
    @DisplayName("日常报销枚举值正确")
    void DAILY_shouldHaveCorrectCodeAndDescription() {
        assertThat(ReimburseTypeEnum.DAILY.getCode()).isEqualTo("DAILY");
        assertThat(ReimburseTypeEnum.DAILY.getDesc()).isEqualTo("日常报销");
    }

    @Test
    @DisplayName("通讯报销枚举值正确")
    void COMMUNICATION_shouldHaveCorrectCodeAndDescription() {
        assertThat(ReimburseTypeEnum.COMMUNICATION.getCode()).isEqualTo("COMMUNICATION");
        assertThat(ReimburseTypeEnum.COMMUNICATION.getDesc()).isEqualTo("通讯报销");
    }

    @Test
    @DisplayName("招待报销枚举值正确")
    void ENTERTAINMENT_shouldHaveCorrectCodeAndDescription() {
        assertThat(ReimburseTypeEnum.ENTERTAINMENT.getCode()).isEqualTo("ENTERTAINMENT");
        assertThat(ReimburseTypeEnum.ENTERTAINMENT.getDesc()).isEqualTo("招待报销");
    }

    @Test
    @DisplayName("采购报销枚举值正确")
    void PURCHASE_shouldHaveCorrectCodeAndDescription() {
        assertThat(ReimburseTypeEnum.PURCHASE.getCode()).isEqualTo("PURCHASE");
        assertThat(ReimburseTypeEnum.PURCHASE.getDesc()).isEqualTo("采购报销");
    }

    @Test
    @DisplayName("其他报销枚举值正确")
    void OTHER_shouldHaveCorrectCodeAndDescription() {
        assertThat(ReimburseTypeEnum.OTHER.getCode()).isEqualTo("OTHER");
        assertThat(ReimburseTypeEnum.OTHER.getDesc()).isEqualTo("其他报销");
    }

    @Test
    @DisplayName("根据Code获取枚举 - 正常场景")
    void getByCode_withValidCode_shouldReturnEnum() {
        assertThat(ReimburseTypeEnum.getByCode("BUSINESS_TRIP")).isEqualTo(ReimburseTypeEnum.BUSINESS_TRIP);
        assertThat(ReimburseTypeEnum.getByCode("DAILY")).isEqualTo(ReimburseTypeEnum.DAILY);
        assertThat(ReimburseTypeEnum.getByCode("OTHER")).isEqualTo(ReimburseTypeEnum.OTHER);
    }

    @Test
    @DisplayName("根据Code获取枚举 - null返回null")
    void getByCode_withNull_shouldReturnNull() {
        assertThat(ReimburseTypeEnum.getByCode(null)).isNull();
    }

    @Test
    @DisplayName("根据Code获取枚举 - 无效Code返回null")
    void getByCode_withInvalidCode_shouldReturnNull() {
        assertThat(ReimburseTypeEnum.getByCode("INVALID")).isNull();
        assertThat(ReimburseTypeEnum.getByCode("")).isNull();
    }

    @Test
    @DisplayName("所有枚举值数量正确")
    void values_shouldHaveSixValues() {
        assertThat(ReimburseTypeEnum.values()).hasSize(6);
    }
}
