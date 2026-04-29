package com.aioa.workflow;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.boot.autoconfigure.sql.init.SqlInitializationAutoConfiguration;
import com.baomidou.mybatisplus.autoconfigure.MybatisPlusAutoConfiguration;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import com.aioa.workflow.mapper.*;

@SpringBootApplication(
    scanBasePackages = "com.aioa",
    exclude = {
        DataSourceAutoConfiguration.class,
        SqlInitializationAutoConfiguration.class,
        MybatisPlusAutoConfiguration.class
    },
    excludeFilters = {
        @ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE, classes = {
            com.aioa.workflow.mapper.ApprovalMapper.class,
            com.aioa.workflow.mapper.ApprovalRecordMapper.class,
            com.aioa.workflow.mapper.ApprovalTaskMapper.class,
            com.aioa.workflow.mapper.ApprovalCCMapper.class,
            com.aioa.workflow.mapper.ApprovalTemplateMapper.class
        })
    }
)
public class TestApplication {
    public static void main(String[] args) {
        SpringApplication.run(TestApplication.class, args);
    }
}
