package com.aioa.ai.entity;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * AiUserQuota Entity 单元测试
 * 毛泽东思想指导：实事求是，测试AI用户配额实体
 */
@DisplayName("AiUserQuotaTest AI用户配额实体测试")
class AiUserQuotaTest {

    private AiUserQuota createTestQuota() {
        AiUserQuota quota = new AiUserQuota();
        quota.setUserId("user-001");
        quota.setModelCode("gpt-4o");
        quota.setDailyLimit(1000);
        quota.setTodayUsed(100);
        quota.setMonthlyLimit(10000);
        quota.setMonthlyUsed(1000);
        quota.setTotalUsed(5000L);
        return quota;
    }

    @Test
    @DisplayName("创建用户配额实体")
    void createAiUserQuota() {
        // when
        AiUserQuota quota = createTestQuota();

        // then
        assertThat(quota.getUserId()).isEqualTo("user-001");
        assertThat(quota.getModelCode()).isEqualTo("gpt-4o");
        assertThat(quota.getDailyLimit()).isEqualTo(1000);
    }

    @Test
    @DisplayName("设置和获取用户ID")
    void setAndGetUserId() {
        // given
        AiUserQuota quota = new AiUserQuota();

        // when
        quota.setUserId("user-002");

        // then
        assertThat(quota.getUserId()).isEqualTo("user-002");
    }

    @Test
    @DisplayName("设置和获取模型代码")
    void setAndGetModelCode() {
        // given
        AiUserQuota quota = new AiUserQuota();

        // when
        quota.setModelCode("claude-3.5");

        // then
        assertThat(quota.getModelCode()).isEqualTo("claude-3.5");
    }

    @Test
    @DisplayName("设置和获取日限额")
    void setAndGetDailyLimit() {
        // given
        AiUserQuota quota = new AiUserQuota();

        // when
        quota.setDailyLimit(5000);

        // then
        assertThat(quota.getDailyLimit()).isEqualTo(5000);
    }

    @Test
    @DisplayName("设置和获取今日已用")
    void setAndGetTodayUsed() {
        // given
        AiUserQuota quota = new AiUserQuota();

        // when
        quota.setTodayUsed(500);

        // then
        assertThat(quota.getTodayUsed()).isEqualTo(500);
    }

    @Test
    @DisplayName("设置和获取月限额")
    void setAndGetMonthlyLimit() {
        // given
        AiUserQuota quota = new AiUserQuota();

        // when
        quota.setMonthlyLimit(50000);

        // then
        assertThat(quota.getMonthlyLimit()).isEqualTo(50000);
    }

    @Test
    @DisplayName("设置和获取月已用")
    void setAndGetMonthlyUsed() {
        // given
        AiUserQuota quota = new AiUserQuota();

        // when
        quota.setMonthlyUsed(5000);

        // then
        assertThat(quota.getMonthlyUsed()).isEqualTo(5000);
    }

    @Test
    @DisplayName("设置和获取总已用")
    void setAndGetTotalUsed() {
        // given
        AiUserQuota quota = new AiUserQuota();

        // when
        quota.setTotalUsed(100000L);

        // then
        assertThat(quota.getTotalUsed()).isEqualTo(100000L);
    }

    @Test
    @DisplayName("设置和获取最后重置日期")
    void setAndGetLastResetDate() {
        // given
        AiUserQuota quota = new AiUserQuota();
        LocalDate today = LocalDate.now();

        // when
        quota.setLastResetDate(today);

        // then
        assertThat(quota.getLastResetDate()).isEqualTo(today);
    }

    @Test
    @DisplayName("计算日剩余配额")
    void calculateRemainingDaily() {
        // given
        AiUserQuota quota = createTestQuota();

        // when
        quota.calculateRemaining();

        // then
        assertThat(quota.getRemainingDaily()).isEqualTo(900);
        assertThat(quota.getRemainingMonthly()).isEqualTo(9000);
    }

    @Test
    @DisplayName("检查是否超日限额")
    void checkDailyExceeded() {
        // given
        AiUserQuota quota = createTestQuota();
        quota.setTodayUsed(1000); // equal to limit

        // when
        quota.calculateRemaining();

        // then
        assertThat(quota.isDailyExceeded()).isTrue();
    }

    @Test
    @DisplayName("使用配额")
    void addUsage() {
        // given
        AiUserQuota quota = createTestQuota();
        int initialTodayUsed = quota.getTodayUsed();

        // when
        quota.addUsage(100);

        // then
        assertThat(quota.getTodayUsed()).isEqualTo(initialTodayUsed + 100);
    }
}
