package com.aioa.reimburse.entity;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Reimburse Entity 单元测试
 * 毛泽东思想指导：实事求是，测试报销实体
 */
@DisplayName("ReimburseTest 报销实体测试")
class ReimburseTest {

    @Test
    @DisplayName("创建报销实体")
    void createReimburse() {
        // given
        Reimburse reimburse = new Reimburse();
        reimburse.setId("reimb-001");
        reimburse.setTitle("差旅费报销");
        reimburse.setType("BUSINESS_TRIP");
        reimburse.setTotalAmount(new BigDecimal("1000.00"));
        reimburse.setCurrency("CNY");
        reimburse.setStatus(0);
        reimburse.setPriority(1);
        reimburse.setApplicantId("user-001");

        // then
        assertThat(reimburse.getId()).isEqualTo("reimb-001");
        assertThat(reimburse.getTitle()).isEqualTo("差旅费报销");
        assertThat(reimburse.getTotalAmount()).isEqualTo(new BigDecimal("1000.00"));
    }

    @Test
    @DisplayName("设置和获取ID")
    void setAndGetId() {
        // given
        Reimburse reimburse = new Reimburse();

        // when
        reimburse.setId("test-id");

        // then
        assertThat(reimburse.getId()).isEqualTo("test-id");
    }

    @Test
    @DisplayName("设置和获取标题")
    void setAndGetTitle() {
        // given
        Reimburse reimburse = new Reimburse();

        // when
        reimburse.setTitle("测试报销");

        // then
        assertThat(reimburse.getTitle()).isEqualTo("测试报销");
    }

    @Test
    @DisplayName("设置和获取类型")
    void setAndGetType() {
        // given
        Reimburse reimburse = new Reimburse();

        // when
        reimburse.setType("DAILY");

        // then
        assertThat(reimburse.getType()).isEqualTo("DAILY");
    }

    @Test
    @DisplayName("设置和获取总金额")
    void setAndGetTotalAmount() {
        // given
        Reimburse reimburse = new Reimburse();

        // when
        reimburse.setTotalAmount(new BigDecimal("500.50"));

        // then
        assertThat(reimburse.getTotalAmount()).isEqualTo(new BigDecimal("500.50"));
    }

    @Test
    @DisplayName("设置和获取货币")
    void setAndGetCurrency() {
        // given
        Reimburse reimburse = new Reimburse();

        // when
        reimburse.setCurrency("USD");

        // then
        assertThat(reimburse.getCurrency()).isEqualTo("USD");
    }

    @Test
    @DisplayName("设置和获取状态")
    void setAndGetStatus() {
        // given
        Reimburse reimburse = new Reimburse();

        // when
        reimburse.setStatus(1); // Pending

        // then
        assertThat(reimburse.getStatus()).isEqualTo(1);
    }

    @Test
    @DisplayName("设置和获取优先级")
    void setAndGetPriority() {
        // given
        Reimburse reimburse = new Reimburse();

        // when
        reimburse.setPriority(2); // High

        // then
        assertThat(reimburse.getPriority()).isEqualTo(2);
    }

    @Test
    @DisplayName("设置和获取申请人ID")
    void setAndGetApplicantId() {
        // given
        Reimburse reimburse = new Reimburse();

        // when
        reimburse.setApplicantId("user-002");

        // then
        assertThat(reimburse.getApplicantId()).isEqualTo("user-002");
    }

    @Test
    @DisplayName("equals验证")
    void equals_sameId_shouldBeEqual() {
        // given
        Reimburse r1 = new Reimburse();
        r1.setId("test-id");
        
        Reimburse r2 = new Reimburse();
        r2.setId("test-id");

        // then
        assertThat(r1).isEqualTo(r2);
    }
}