package com.aioa.license;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

/**
 * 证照管理模块启动类
 */
@SpringBootApplication
@EnableDiscoveryClient
public class LicenseApplication {
    
    public static void main(String[] args) {
        SpringApplication.run(LicenseApplication.class, args);
    }
}