package com.aioa.workflow.test;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import com.baomidou.mybatisplus.autoconfigure.MybatisPlusAutoConfiguration;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import org.springframework.context.annotation.Configuration;
import com.aioa.workflow.mapper.ApprovalMapper;
import com.aioa.workflow.mapper.ApprovalRecordMapper;

@Configuration
@ComponentScan(
    basePackages = {"com.aioa.workflow", "com.aioa.common"},
    excludeFilters = {
        @ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE, classes = {
            ApprovalMapper.class,
            ApprovalRecordMapper.class
        })
    }
)
public class TestApplication {
    public static void main(String[] args) {
        SpringApplication.run(TestApplication.class, args);
    }
}