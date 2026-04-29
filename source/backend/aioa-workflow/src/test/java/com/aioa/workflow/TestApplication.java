package com.aioa.workflow;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import com.baomidou.mybatisplus.autoconfigure.MybatisPlusAutoConfiguration;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import com.aioa.workflow.service.impl.*;

@SpringBootApplication(
    scanBasePackages = "com.aioa",
    exclude = {
        DataSourceAutoConfiguration.class,
        MybatisPlusAutoConfiguration.class
    }
)
@ComponentScan(
    basePackages = "com.aioa",
    excludeFilters = {
        @ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE, classes = {
            ApprovalServiceImpl.class,
            ApprovalRecordServiceImpl.class,
            N8nWorkflowServiceImpl.class,
            WorkflowMonitorServiceImpl.class
        })
    }
)
public class TestApplication {
    public static void main(String[] args) {
        SpringApplication.run(TestApplication.class, args);
    }
}
