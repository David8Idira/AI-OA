package com.aioa.workflow;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;

/**
 * AI-OA Workflow Module Application
 */
@SpringBootApplication
@ComponentScan(basePackages = {"com.aioa.workflow", "com.aioa.common"})
@MapperScan(basePackages = {"com.aioa.workflow.mapper", "com.aioa.system.mapper"})
public class WorkflowApplication {

    public static void main(String[] args) {
        SpringApplication.run(WorkflowApplication.class, args);
    }
}
