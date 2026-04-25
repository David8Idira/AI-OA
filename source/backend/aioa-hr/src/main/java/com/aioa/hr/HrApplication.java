package com.aioa.hr;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

/**
 * HR管理模块启动类
 */
@SpringBootApplication
@EnableDiscoveryClient
public class HrApplication {
    
    public static void main(String[] args) {
        SpringApplication.run(HrApplication.class, args);
    }
}