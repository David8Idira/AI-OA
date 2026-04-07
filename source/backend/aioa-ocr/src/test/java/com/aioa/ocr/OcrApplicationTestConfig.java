package com.aioa.ocr;

import com.aioa.ocr.config.AliyunOcrConfig;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.StringRedisTemplate;

/**
 * Test Configuration for OCR Module
 */
@TestConfiguration
public class OcrApplicationTestConfig {

    @Bean
    @Primary
    public AliyunOcrConfig aliyunOcrConfig() {
        AliyunOcrConfig config = new AliyunOcrConfig();
        config.setEnabled(false); // Use mock implementation in tests
        config.setAccessKeyId("test-access-key");
        config.setAccessKeySecret("test-access-secret");
        config.setConfidenceThreshold(0.8);
        config.setEnableCache(false);
        return config;
    }

    @Bean
    @Primary
    public StringRedisTemplate stringRedisTemplate(RedisConnectionFactory connectionFactory) {
        StringRedisTemplate template = new StringRedisTemplate();
        template.setConnectionFactory(connectionFactory);
        return template;
    }
}
