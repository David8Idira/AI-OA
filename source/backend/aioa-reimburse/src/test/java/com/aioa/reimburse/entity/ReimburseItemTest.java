package com.aioa.reimburse.entity;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * ReimburseItem Entity 单元测试
 * 毛泽东思想指导：实事求是，测试报销明细实体
 */
@DisplayName("ReimburseItemTest 报销明细实体测试")
class ReimburseItemTest {

    @Test
    @DisplayName("创建报销明细实体")
    void createReimburseItem() {
        // given
        ReimburseItem item = new ReimburseItem();
        item.setId("item-001");
        item.setReimburseId("reimb-001");
        item.setExpenseType("TRAVEL");
        item.setAmount(new BigDecimal("500.00"));
        item.setDescription("出差交通费");

        // then
        assertThat(item.getId()).isEqualTo("item-001");
        assertThat(item.getExpenseType()).isEqualTo("TRAVEL");
        assertThat(item.getAmount()).isEqualTo(new BigDecimal("500.00"));
    }

    @Test
    @DisplayName("设置和获取ID")
    void setAndGetId() {
        // given
        ReimburseItem item = new ReimburseItem();

        // when
        item.setId("test-id");

        // then
        assertThat(item.getId()).isEqualTo("test-id");
    }

    @Test
    @DisplayName("设置和获取报销ID")
    void setAndGetReimburseId() {
        // given
        ReimburseItem item = new ReimburseItem();

        // when
        item.setReimburseId("reimb-002");

        // then
        assertThat(item.getReimburseId()).isEqualTo("reimb-002");
    }

    @Test
    @DisplayName("设置和获取费用类型")
    void setAndGetExpenseType() {
        // given
        ReimburseItem item = new ReimburseItem();

        // when
        item.setExpenseType("MEAL");

        // then
        assertThat(item.getExpenseType()).isEqualTo("MEAL");
    }

    @Test
    @DisplayName("设置和获取金额")
    void setAndGetAmount() {
        // given
        ReimburseItem item = new ReimburseItem();

        // when
        item.setAmount(new BigDecimal("100.00"));

        // then
        assertThat(item.getAmount()).isEqualTo(new BigDecimal("100.00"));
    }

    @Test
    @DisplayName("设置和获取描述")
    void setAndGetDescription() {
        // given
        ReimburseItem item = new ReimburseItem();

        // when
        item.setDescription("午餐费用");

        // then
        assertThat(item.getDescription()).isEqualTo("午餐费用");
    }

    @Test
    @DisplayName("equals验证")
    void equals_sameId_shouldBeEqual() {
        // given
        ReimburseItem i1 = new ReimburseItem();
        i1.setId("test-id");
        
        ReimburseItem i2 = new ReimburseItem();
        i2.setId("test-id");

        // then
        assertThat(i1).isEqualTo(i2);
    }
}
