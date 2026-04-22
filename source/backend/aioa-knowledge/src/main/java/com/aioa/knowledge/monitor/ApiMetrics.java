package com.aioa.knowledge.monitor;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.Gauge;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * API监控指标
 */
@Slf4j
@Component
public class ApiMetrics {
    
    private final MeterRegistry meterRegistry;
    
    // API请求指标
    private final Counter apiRequestCounter;
    private final Counter apiSuccessCounter;
    private final Counter apiErrorCounter;
    private final Timer apiResponseTimer;
    
    // API方法指标
    private final Map<String, Counter> apiMethodCounters = new ConcurrentHashMap<>();
    private final Map<String, Counter> apiMethodErrorCounters = new ConcurrentHashMap<>();
    private final Map<String, Timer> apiMethodTimers = new ConcurrentHashMap<>();
    
    // HTTP状态码指标
    private final Map<Integer, Counter> httpStatusCounters = new ConcurrentHashMap<>();
    
    // 实时指标
    private final AtomicInteger activeRequests = new AtomicInteger(0);
    private final AtomicInteger concurrentRequests = new AtomicInteger(0);
    private final AtomicInteger maxConcurrentRequests = new AtomicInteger(0);
    
    // 响应时间统计
    private final AtomicInteger avgResponseTime = new AtomicInteger(0);
    private final AtomicInteger p95ResponseTime = new AtomicInteger(0);
    private final AtomicInteger p99ResponseTime = new AtomicInteger(0);
    
    @Autowired
    public ApiMetrics(MeterRegistry meterRegistry) {
        this.meterRegistry = meterRegistry;
        
        // 初始化计数器
        this.apiRequestCounter = Counter.builder("api.requests.total")
            .description("Total number of API requests")
            .register(meterRegistry);
        
        this.apiSuccessCounter = Counter.builder("api.requests.success.total")
            .description("Total number of successful API requests")
            .register(meterRegistry);
        
        this.apiErrorCounter = Counter.builder("api.requests.error.total")
            .description("Total number of failed API requests")
            .register(meterRegistry);
        
        this.apiResponseTimer = Timer.builder("api.response.duration")
            .description("API response time distribution")
            .publishPercentiles(0.5, 0.95, 0.99)
            .register(meterRegistry);
        
        // 注册实时指标
        Gauge.builder("api.requests.active", activeRequests, AtomicInteger::get)
            .description("Number of active API requests")
            .register(meterRegistry);
        
        Gauge.builder("api.requests.concurrent", concurrentRequests, AtomicInteger::get)
            .description("Current number of concurrent API requests")
            .register(meterRegistry);
        
        Gauge.builder("api.requests.concurrent.max", maxConcurrentRequests, AtomicInteger::get)
            .description("Maximum number of concurrent API requests")
            .register(meterRegistry);
        
        Gauge.builder("api.response.time.avg", avgResponseTime, AtomicInteger::get)
            .description("Average API response time in milliseconds")
            .register(meterRegistry);
        
        Gauge.builder("api.response.time.p95", p95ResponseTime, AtomicInteger::get)
            .description("95th percentile API response time")
            .register(meterRegistry);
        
        Gauge.builder("api.response.time.p99", p99ResponseTime, AtomicInteger::get)
            .description("99th percentile API response time")
            .register(meterRegistry);
        
        Gauge.builder("api.success.rate", this, ApiMetrics::calculateSuccessRate)
            .description("API success rate")
            .register(meterRegistry);
        
        log.info("API监控指标初始化完成");
    }
    
    /**
     * 记录API请求开始
     */
    public void recordApiRequestStart(String method, String endpoint) {
        String key = getMethodKey(method, endpoint);
        
        apiRequestCounter.increment();
        activeRequests.incrementAndGet();
        
        int currentConcurrent = concurrentRequests.incrementAndGet();
        if (currentConcurrent > maxConcurrentRequests.get()) {
            maxConcurrentRequests.set(currentConcurrent);
        }
        
        // 记录方法调用次数
        apiMethodCounters.computeIfAbsent(key, k ->
            Counter.builder("api.requests.by.method")
                .tag("method", method)
                .tag("endpoint", endpoint)
                .description("API requests by method and endpoint")
                .register(meterRegistry)
        ).increment();
    }
    
    /**
     * 记录API请求完成
     */
    public void recordApiRequestComplete(String method, String endpoint, int statusCode, long durationMs) {
        String key = getMethodKey(method, endpoint);
        
        apiResponseTimer.record(durationMs, TimeUnit.MILLISECONDS);
        activeRequests.decrementAndGet();
        concurrentRequests.decrementAndGet();
        
        // 更新响应时间统计
        updateResponseTimeStats(durationMs);
        
        // 记录HTTP状态码
        if (statusCode >= 200 && statusCode < 400) {
            apiSuccessCounter.increment();
        } else {
            apiErrorCounter.increment();
            
            // 记录方法错误
            apiMethodErrorCounters.computeIfAbsent(key, k ->
                Counter.builder("api.errors.by.method")
                    .tag("method", method)
                    .tag("endpoint", endpoint)
                    .description("API errors by method and endpoint")
                    .register(meterRegistry)
            ).increment();
        }
        
        // 记录HTTP状态码
        httpStatusCounters.computeIfAbsent(statusCode, code ->
            Counter.builder("api.responses.by.status")
                .tag("status", String.valueOf(code))
                .description("API responses by HTTP status code")
                .register(meterRegistry)
        ).increment();
        
        // 记录方法响应时间
        apiMethodTimers.computeIfAbsent(key, k ->
            Timer.builder("api.response.duration.by.method")
                .tag("method", method)
                .tag("endpoint", endpoint)
                .description("API response time by method and endpoint")
                .publishPercentiles(0.5, 0.95, 0.99)
                .register(meterRegistry)
        ).record(durationMs, TimeUnit.MILLISECONDS);
    }
    
    /**
     * 记录API错误
     */
    public void recordApiError(String method, String endpoint, String errorType) {
        apiErrorCounter.increment();
        
        String key = getMethodKey(method, endpoint);
        apiMethodErrorCounters.computeIfAbsent(key, k ->
            Counter.builder("api.errors.by.method")
                .tag("method", method)
                .tag("endpoint", endpoint)
                .description("API errors by method and endpoint")
                .register(meterRegistry)
        ).increment();
    }
    
    /**
     * 计算API成功率
     */
    private double calculateSuccessRate() {
        long success = (long) apiSuccessCounter.count();
        long error = (long) apiErrorCounter.count();
        long total = success + error;
        
        return total > 0 ? (double) success / total : 0.0;
    }
    
    /**
     * 更新响应时间统计
     */
    private void updateResponseTimeStats(long durationMs) {
        // 简化实现，实际应该基于历史数据计算
        int currentAvg = avgResponseTime.get();
        int newAvg = (int) ((currentAvg * 0.9) + (durationMs * 0.1));
        avgResponseTime.set(newAvg);
        
        // 对于分位数，这里简化处理
        if (durationMs > p95ResponseTime.get()) {
            p95ResponseTime.set((int) durationMs);
        }
        if (durationMs > p99ResponseTime.get()) {
            p99ResponseTime.set((int) durationMs);
        }
    }
    
    /**
     * 获取方法键
     */
    private String getMethodKey(String method, String endpoint) {
        return method + ":" + endpoint;
    }
    
    /**
     * 获取监控统计信息
     */
    public Map<String, Object> getMetrics() {
        Map<String, Object> metrics = new java.util.HashMap<>();
        
        long total = (long) apiRequestCounter.count();
        long success = (long) apiSuccessCounter.count();
        long error = (long) apiErrorCounter.count();
        double successRate = total > 0 ? (double) success / total : 0.0;
        
        metrics.put("requests.total", total);
        metrics.put("requests.success", success);
        metrics.put("requests.error", error);
        metrics.put("successRate", successRate);
        
        metrics.put("response.avgTimeMs", apiResponseTimer.mean(TimeUnit.MILLISECONDS));
        metrics.put("response.p50TimeMs", apiResponseTimer.percentile(0.5, TimeUnit.MILLISECONDS));
        metrics.put("response.p95TimeMs", apiResponseTimer.percentile(0.95, TimeUnit.MILLISECONDS));
        metrics.put("response.p99TimeMs", apiResponseTimer.percentile(0.99, TimeUnit.MILLISECONDS));
        
        metrics.put("requests.active", activeRequests.get());
        metrics.put("requests.concurrent", concurrentRequests.get());
        metrics.put("requests.concurrent.max", maxConcurrentRequests.get());
        
        metrics.put("response.time.avg", avgResponseTime.get());
        metrics.put("response.time.p95", p95ResponseTime.get());
        metrics.put("response.time.p99", p99ResponseTime.get());
        
        // 按方法统计
        Map<String, Map<String, Object>> methodStats = new java.util.HashMap<>();
        apiMethodCounters.forEach((key, counter) -> {
            String[] parts = key.split(":");
            if (parts.length == 2) {
                String method = parts[0];
                String endpoint = parts[1];
                
                Map<String, Object> stats = new java.util.HashMap<>();
                stats.put("total", counter.count());
                
                Counter errorCounter = apiMethodErrorCounters.get(key);
                if (errorCounter != null) {
                    stats.put("errors", errorCounter.count());
                    double methodTotal = counter.count();
                    double methodError = errorCounter.count();
                    double methodSuccessRate = methodTotal > 0 ? (methodTotal - methodError) / methodTotal : 1.0;
                    stats.put("successRate", methodSuccessRate);
                }
                
                Timer methodTimer = apiMethodTimers.get(key);
                if (methodTimer != null) {
                    stats.put("avgTimeMs", methodTimer.mean(TimeUnit.MILLISECONDS));
                    stats.put("p95TimeMs", methodTimer.percentile(0.95, TimeUnit.MILLISECONDS));
                }
                
                methodStats.put(method + " " + endpoint, stats);
            }
        });
        metrics.put("byMethod", methodStats);
        
        // 按HTTP状态码统计
        Map<Integer, Long> statusStats = new java.util.HashMap<>();
        httpStatusCounters.forEach((status, counter) -> {
            statusStats.put(status, (long) counter.count());
        });
        metrics.put("byStatus", statusStats);
        
        return metrics;
    }
    
    /**
     * 获取热门API端点
     */
    public Map<String, Long> getTopEndpoints(int limit) {
        Map<String, Long> topEndpoints = new java.util.HashMap<>();
        
        apiMethodCounters.entrySet().stream()
            .sorted((e1, e2) -> Long.compare((long) e2.getValue().count(), (long) e1.getValue().count()))
            .limit(limit)
            .forEach(entry -> {
                String[] parts = entry.getKey().split(":");
                if (parts.length == 2) {
                    topEndpoints.put(parts[1], (long) entry.getValue().count());
                }
            });
        
        return topEndpoints;
    }
    
    /**
     * 获取错误最多的API端点
     */
    public Map<String, Long> getErrorEndpoints(int limit) {
        Map<String, Long> errorEndpoints = new java.util.HashMap<>();
        
        apiMethodErrorCounters.entrySet().stream()
            .sorted((e1, e2) -> Long.compare((long) e2.getValue().count(), (long) e1.getValue().count()))
            .limit(limit)
            .forEach(entry -> {
                String[] parts = entry.getKey().split(":");
                if (parts.length == 2) {
                    errorEndpoints.put(parts[1], (long) entry.getValue().count());
                }
            });
        
        return errorEndpoints;
    }
    
    /**
     * 健康检查
     */
    public Map<String, Object> healthCheck() {
        Map<String, Object> health = new java.util.HashMap<>();
        
        try {
            double successRate = calculateSuccessRate();
            double avgResponseTimeMs = apiResponseTimer.mean(TimeUnit.MILLISECONDS);
            
            boolean healthy = successRate > 0.95 && avgResponseTimeMs < 1000;
            
            health.put("status", healthy ? "healthy" : "degraded");
            health.put("successRate", successRate);
            health.put("avgResponseTimeMs", avgResponseTimeMs);
            health.put("activeRequests", activeRequests.get());
            health.put("concurrentRequests", concurrentRequests.get());
            health.put("message", healthy ? "API运行正常" : "API性能可能下降");
            
        } catch (Exception e) {
            health.put("status", "unhealthy");
            health.put("error", e.getMessage());
            health.put("message", "API健康检查失败");
        }
        
        return health;
    }
    
    /**
     * 重置指标（测试用途）
     */
    public void resetMetrics() {
        activeRequests.set(0);
        concurrentRequests.set(0);
        maxConcurrentRequests.set(0);
        avgResponseTime.set(0);
        p95ResponseTime.set(0);
        p99ResponseTime.set(0);
        
        // 重置方法计数器
        apiMethodCounters.clear();
        apiMethodErrorCounters.clear();
        apiMethodTimers.clear();
        httpStatusCounters.clear();
    }
    
    /**
     * 获取API性能趋势
     */
    public Map<String, Object> getPerformanceTrend() {
        Map<String, Object> trend = new java.util.HashMap<>();
        
        try {
            double successRate = calculateSuccessRate();
            double avgTime = apiResponseTimer.mean(TimeUnit.MILLISECONDS);
            double p95Time = apiResponseTimer.percentile(0.95, TimeUnit.MILLISECONDS);
            double p99Time = apiResponseTimer.percentile(0.99, TimeUnit.MILLISECONDS);
            
            trend.put("successRate", successRate);
            trend.put("avgResponseTimeMs", avgTime);
            trend.put("p95ResponseTimeMs", p95Time);
            trend.put("p99ResponseTimeMs", p99Time);
            trend.put("requestRate", (long) apiRequestCounter.count() / 3600.0); // 每小时请求率
            
            // 性能评级
            String performanceGrade = "A";
            if (successRate < 0.99 || p95Time > 1000) {
                performanceGrade = "B";
            }
            if (successRate < 0.95 || p95Time > 2000) {
                performanceGrade = "C";
            }
            if (successRate < 0.9 || p95Time > 5000) {
                performanceGrade = "D";
            }
            
            trend.put("performanceGrade", performanceGrade);
            trend.put("timestamp", new java.util.Date());
            
        } catch (Exception e) {
            trend.put("error", e.getMessage());
        }
        
        return trend;
    }
}