package com.aioa.system;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * AI-OA System Application for testing
 * This class is only used for Controller tests that need @SpringBootTest
 */
@SpringBootApplication(scanBasePackages = "com.aioa")
public class TestApplication {
    public static void main(String[] args) {
        SpringApplication.run(TestApplication.class, args);
    }
}