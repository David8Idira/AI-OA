package com.aioa.report;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;

/**
 * AI-OA Report Module Application
 */
@SpringBootApplication
@ComponentScan(basePackages = {"com.aioa.report", "com.aioa.common", "com.aioa.ai"})
@MapperScan(basePackages = {"com.aioa.report.mapper", "com.aioa.system.mapper"})
public class ReportApplication {

    public static void main(String[] args) {
        SpringApplication.run(ReportApplication.class, args);
    }
}
