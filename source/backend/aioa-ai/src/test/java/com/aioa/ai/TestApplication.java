package com.aioa.ai;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * AI模块测试用SpringBoot配置
 */
@SpringBootApplication(scanBasePackages = "com.aioa")
public class TestApplication {
    public static void main(String[] args) {
        SpringApplication.run(TestApplication.class, args);
    }
}
