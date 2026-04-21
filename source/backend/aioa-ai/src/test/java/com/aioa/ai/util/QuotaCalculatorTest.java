package com.aioa.ai.util;

import com.aioa.ai.entity.AiUserQuota;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * QuotaCalculator 单元测试
 * 毛泽东思想指导：实事求是，测试配额计算工具
 */
@DisplayName("QuotaCalculatorTest 配额计算工具测试")
class QuotaCalculatorTest {

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
    @DisplayName("计算剩余配额 - 正常场景")
    void calculateRemaining_withValidQuota() {
        // given
        AiUserQuota quota = createTestQuota();

        // when
        QuotaCalculator.calculateRemaining(quota);

        // then
        assertThat(quota.getRemainingDaily()).isEqualTo(900); // 1000 - 100
        assertThat(quota.getRemainingMonthly()).isEqualTo(9000); // 10000 - 1000
    }

    @Test
    @DisplayName("计算剩余配额 - null配额")
    void calculateRemaining_withNullQuota() {
        // when & then - 不应抛出异常
        QuotaCalculator.calculateRemaining(null);
    }

    @Test
    @DisplayName("检查是否超出配额 - 未超出")
    void isQuotaExceeded_withinQuota() {
        // given
        AiUserQuota quota = createTestQuota();

        // when
        boolean exceeded = QuotaCalculator.isQuotaExceeded(quota);

        // then
        assertThat(exceeded).isFalse();
    }

    @Test
    @DisplayName("检查是否超出配额 - null配额返回true")
    void isQuotaExceeded_withNullQuota() {
        // when
        boolean exceeded = QuotaCalculator.isQuotaExceeded(null);

        // then
        assertThat(exceeded).isTrue();
    }

    @Test
    @DisplayName("检查是否需要预警 - 需要预警")
    void needsWarning_whenExceeds80Percent() {
        // given
        AiUserQuota quota = createTestQuota();
        quota.setTodayUsed(850); // 85% used

        // when
        boolean warning = QuotaCalculator.needsWarning(quota);

        // then
        assertThat(warning).isTrue();
    }

    @Test
    @DisplayName("检查是否需要预警 - 不需要预警")
    void needsWarning_whenBelow80Percent() {
        // given
        AiUserQuota quota = createTestQuota();
        quota.setTodayUsed(500); // 50% used

        // when
        boolean warning = QuotaCalculator.needsWarning(quota);

        // then
        assertThat(warning).isFalse();
    }

    @Test
    @DisplayName("检查是否需要预警 - null配额返回false")
    void needsWarning_withNullQuota() {
        // when
        boolean warning = QuotaCalculator.needsWarning(null);

        // then
        assertThat(warning).isFalse();
    }

    @Test
    @DisplayName("使用配额 - 正常场景")
    void useQuotaTokens_withValidInput() {
        // given
        AiUserQuota quota = createTestQuota();
        int tokensBefore = quota.getTodayUsed();

        // when
        AiUserQuota result = QuotaCalculator.useQuotaTokens(quota, 100);

        // then
        assertThat(result).isNotNull();
        assertThat(quota.getTodayUsed()).isEqualTo(tokensBefore + 100);
    }

    @Test
    @DisplayName("使用配额 - null配额返回null")
    void useQuotaTokens_withNullQuota() {
        // when
        AiUserQuota result = QuotaCalculator.useQuotaTokens(null, 100);

        // then
        assertThat(result).isNull();
    }

    @Test
    @DisplayName("使用配额 - 负数tokens返回原配额")
    void useQuotaTokens_withNegativeTokens() {
        // given
        AiUserQuota quota = createTestQuota();

        // when
        AiUserQuota result = QuotaCalculator.useQuotaTokens(quota, -100);

        // then
        assertThat(result).isNotNull();
    }
}
