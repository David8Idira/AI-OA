package com.aioa.knowledge.monitor;

import io.micrometer.core.instrument.Clock;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.binder.jvm.*;
import io.micrometer.core.instrument.binder.system.ProcessorMetrics;
import io.micrometer.core.instrument.binder.system.UptimeMetrics;
import io.micrometer.prometheus.PrometheusConfig;
import io.micrometer.prometheus.PrometheusMeterRegistry;
import io.prometheus.client.CollectorRegistry;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * 监控系统配置
 * 集成Micrometer + Prometheus进行系统监控
 */
@Configuration
public class MonitorConfig {
    
    /**
     * Prometheus监控注册表
     */
    @Bean
    public PrometheusMeterRegistry prometheusMeterRegistry() {
        PrometheusMeterRegistry registry = new PrometheusMeterRegistry(PrometheusConfig.DEFAULT, CollectorRegistry.defaultRegistry, Clock.SYSTEM);
        
        // 注册JVM监控
        new JvmMemoryMetrics().bindTo(registry);
        new JvmGcMetrics().bindTo(registry);
        new JvmThreadMetrics().bindTo(registry);
        new ClassLoaderMetrics().bindTo(registry);
        
        // 注册系统监控
        new ProcessorMetrics().bindTo(registry);
        new UptimeMetrics().bindTo(registry);
        
        return registry;
    }
    
    /**
     * 知识库监控指标
     */
    @Bean
    public KnowledgeMetrics knowledgeMetrics(MeterRegistry meterRegistry) {
        return new KnowledgeMetrics(meterRegistry);
    }
    
    /**
     * 缓存监控指标
     */
    @Bean
    public CacheMetrics cacheMetrics(MeterRegistry meterRegistry) {
        return new CacheMetrics(meterRegistry);
    }
    
    /**
     * API监控指标
     */
    @Bean
    public ApiMetrics apiMetrics(MeterRegistry meterRegistry) {
        return new ApiMetrics(meterRegistry);
    }
}