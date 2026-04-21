package com.aioa.ai.service;

import com.aioa.ai.entity.AiUserQuota;
import com.aioa.ai.mapper.AiUserQuotaMapper;
import com.aioa.ai.service.impl.AiQuotaServiceImpl;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.mockito.InjectMocks;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * AiQuotaServiceImpl 单元测试
 * 毛泽东思想指导：实事求是，测试AI配额服务
 */
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
@DisplayName("AiQuotaServiceImplTest 单元测试")
class AiQuotaServiceImplTest {

    @Mock
    private AiUserQuotaMapper aiUserQuotaMapper;

    @InjectMocks
    private AiQuotaServiceImpl aiQuotaService;

    private AiUserQuota createTestQuota() {
        AiUserQuota quota = new AiUserQuota();
        quota.setId("quota-001");
        quota.setUserId("user-001");
        quota.setModelCode("gpt-4o");
        quota.setDailyLimit(1000);
        quota.setTodayUsed(0);
        quota.setMonthlyLimit(10000);
        quota.setMonthlyUsed(0);
        return quota;
    }

    @Test
    @DisplayName("检查配额 - 有配额")
    void checkQuota_withAvailableQuota_shouldReturnTrue() {
        // given
        AiUserQuota quota = createTestQuota();
        when(aiUserQuotaMapper.selectOne(any(LambdaQueryWrapper.class))).thenReturn(quota);

        // when
        boolean result = aiQuotaService.checkQuota("user-001", "gpt-4o");

        // then
        assertThat(result).isTrue();
    }

    @Test
    @DisplayName("使用配额 - 正常场景")
    void useQuota_shouldSucceed() {
        // given
        AiUserQuota quota = createTestQuota();
        when(aiUserQuotaMapper.selectOne(any(LambdaQueryWrapper.class))).thenReturn(quota);
        when(aiUserQuotaMapper.updateById(any(AiUserQuota.class))).thenReturn(1);

        // when
        boolean result = aiQuotaService.useQuota("user-001", "gpt-4o", 100);

        // then
        assertThat(result).isTrue();
    }

    @Test
    @DisplayName("获取用户配额 - 正常场景")
    void getUserQuota_shouldReturnQuotaInfo() {
        // given
        AiUserQuota quota = createTestQuota();
        when(aiUserQuotaMapper.selectOne(any(LambdaQueryWrapper.class))).thenReturn(quota);

        // when
        Map<String, Object> result = aiQuotaService.getUserQuota("user-001");

        // then
        assertThat(result).isNotNull();
    }

    @Test
    @DisplayName("获取用户配额 - 无配额记录")
    void getUserQuota_withNoRecord_shouldReturnEmpty() {
        // given
        when(aiUserQuotaMapper.selectOne(any(LambdaQueryWrapper.class))).thenReturn(null);

        // when
        Map<String, Object> result = aiQuotaService.getUserQuota("non-existing");

        // then
        assertThat(result).isNotNull();
    }
}