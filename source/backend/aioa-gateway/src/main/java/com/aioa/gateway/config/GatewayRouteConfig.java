package com.aioa.gateway.config;

import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * 网关路由配置
 */
@Configuration
public class GatewayRouteConfig {
    
    /**
     * 静态路由配置
     * 生产环境建议使用服务发现
     */
    @Bean
    public RouteLocator customRouteLocator(RouteLocatorBuilder builder) {
        return builder.routes()
            // 系统服务
            .route("system-service", r -> r
                .path("/api/system/**")
                .uri("lb://aioa-system"))
            // 工作流服务
            .route("workflow-service", r -> r
                .path("/api/workflow/**")
                .uri("lb://aioa-workflow"))
            // AI服务
            .route("ai-service", r -> r
                .path("/api/ai/**")
                .uri("lb://aioa-ai"))
            // IM服务
            .route("im-service", r -> r
                .path("/api/im/**")
                .uri("lb://aioa-im"))
            // OCR服务
            .route("ocr-service", r -> r
                .path("/api/ocr/**")
                .uri("lb://aioa-ocr"))
            // 报销服务
            .route("reimburse-service", r -> r
                .path("/api/reimburse/**")
                .uri("lb://aioa-reimburse"))
            // 报表服务
            .route("report-service", r -> r
                .path("/api/report/**")
                .uri("lb://aioa-report"))
            .build();
    }
}