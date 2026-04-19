package com.aioa.ai.service;

import com.aioa.ai.entity.AiModelConfig;
import com.aioa.ai.mapper.AiModelConfigMapper;
import com.aioa.ai.service.impl.AiModelConfigServiceImpl;
import com.aioa.common.exception.BusinessException;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.mockito.InjectMocks;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.*;

/**
 * AiModelConfigServiceImpl 单元测试
 * 毛泽东思想指导：实事求是，测试AI模型配置服务
 */
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
@DisplayName("AiModelConfigServiceImplTest 单元测试")
class AiModelConfigServiceImplTest {

    @Mock
    private AiModelConfigMapper aiModelConfigMapper;

    @InjectMocks
    private AiModelConfigServiceImpl aiModelConfigService;

    private AiModelConfig createTestConfig() {
        AiModelConfig config = new AiModelConfig();
        config.setId("config-001");
        config.setModelCode("gpt-4o");
        config.setModelName("GPT-4o");
        config.setProvider("openai");
        config.setEnabled(1);
        return config;
    }

    @Test
    @DisplayName("获取所有配置 - 正常场景")
    void getAllConfigs_shouldReturnList() {
        // given
        AiModelConfig config = createTestConfig();
        when(aiModelConfigMapper.selectList(isNull())).thenReturn(List.of(config));

        // when
        List<AiModelConfig> results = aiModelConfigService.getAllConfigs();

        // then
        assertThat(results).hasSize(1);
        assertThat(results.get(0).getModelCode()).isEqualTo("gpt-4o");
    }

    @Test
    @DisplayName("获取启用的配置 - 正常场景")
    void getEnabledConfigs_shouldReturnList() {
        // given
        AiModelConfig config = createTestConfig();
        when(aiModelConfigMapper.selectList(any(LambdaQueryWrapper.class))).thenReturn(List.of(config));

        // when
        List<AiModelConfig> results = aiModelConfigService.getEnabledConfigs();

        // then
        assertThat(results).hasSize(1);
        assertThat(results.get(0).getEnabled()).isEqualTo(1);
    }

    @Test
    @DisplayName("获取配置 - 空modelCode抛出异常")
    void getConfigByCode_withEmptyCode_shouldThrowException() {
        // when & then
        assertThatThrownBy(() -> aiModelConfigService.getConfigByCode(""))
                .isInstanceOf(BusinessException.class);
    }

    @Test
    @DisplayName("获取配置 - null modelCode抛出异常")
    void getConfigByCode_withNullCode_shouldThrowException() {
        // when & then
        assertThatThrownBy(() -> aiModelConfigService.getConfigByCode(null))
                .isInstanceOf(BusinessException.class);
    }

    @Test
    @DisplayName("获取配置 - 不存在抛出异常")
    void getConfigByCode_withNonExisting_shouldThrowException() {
        // given
        when(aiModelConfigMapper.selectOne(any(LambdaQueryWrapper.class))).thenReturn(null);

        // when & then
        assertThatThrownBy(() -> aiModelConfigService.getConfigByCode("non-existing"))
                .isInstanceOf(BusinessException.class);
    }

    @Test
    @DisplayName("获取配置 - 已禁用抛出异常")
    void getConfigByCode_withDisabledModel_shouldThrowException() {
        // given
        AiModelConfig config = createTestConfig();
        config.setEnabled(0);  // 禁用
        when(aiModelConfigMapper.selectOne(any(LambdaQueryWrapper.class))).thenReturn(config);

        // when & then
        assertThatThrownBy(() -> aiModelConfigService.getConfigByCode("gpt-4o"))
                .isInstanceOf(BusinessException.class);
    }
}