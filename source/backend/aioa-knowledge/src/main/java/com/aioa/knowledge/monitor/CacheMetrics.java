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
import java.util.concurrent.atomic.AtomicLong;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 缓存监控指标
 */
@Slf4j
@Component
public class CacheMetrics {
    
    private final MeterRegistry meterRegistry;
    
    // 缓存命中指标
    private final Counter cacheHitCounter;
    private final Counter cacheMissCounter;
    private final Counter cachePutCounter;
    private final Counter cacheEvictCounter;
    
    // 缓存访问延迟
    private final Timer cacheGetTimer;
    private final Timer cachePutTimer;
    private final Timer cacheEvictTimer;
    
    // 缓存级别指标
    private final Counter l1HitCounter;
    private final Counter l1MissCounter;
    private final Counter l2HitCounter;
    private final Counter l2MissCounter;
    
    // 错误指标
    private final Counter cacheErrorCounter;
    
    // 实时指标
    private final AtomicInteger cacheSize = new AtomicInteger(0);
    private final AtomicLong cacheMemoryUsage = new AtomicLong(0);
    private final AtomicInteger l1Size = new AtomicInteger(0);
    private final AtomicInteger l2Size = new AtomicInteger(0);
    
    // 按缓存名称统计
    private final Map<String, Counter> cacheHitCounters = new ConcurrentHashMap<>();
    private final Map<String, Counter> cacheMissCounters = new ConcurrentHashMap<>();
    private final Map<String, Counter> cacheSizeCounters = new ConcurrentHashMap<>();
    
    @Autowired
    public CacheMetrics(MeterRegistry meterRegistry) {
        this.meterRegistry = meterRegistry;
        
        // 初始化计数器
        this.cacheHitCounter = Counter.builder("cache.hits.total")
            .description("Total number of cache hits")
            .register(meterRegistry);
        
        this.cacheMissCounter = Counter.builder("cache.misses.total")
            .description("Total number of cache misses")
            .register(meterRegistry);
        
        this.cachePutCounter = Counter.builder("cache.puts.total")
            .description("Total number of cache puts")
            .register(meterRegistry);
        
        this.cacheEvictCounter = Counter.builder("cache.evicts.total")
            .description("Total number of cache evictions")
            .register(meterRegistry);
        
        this.cacheGetTimer = Timer.builder("cache.get.duration")
            .description("Time taken for cache get operations")
            .publishPercentiles(0.5, 0.95, 0.99)
            .register(meterRegistry);
        
        this.cachePutTimer = Timer.builder("cache.put.duration")
            .description("Time taken for cache put operations")
            .publishPercentiles(0.5, 0.95, 0.99)
            .register(meterRegistry);
        
        this.cacheEvictTimer = Timer.builder("cache.evict.duration")
            .description("Time taken for cache evict operations")
            .publishPercentiles(0.5, 0.95, 0.99)
            .register(meterRegistry);
        
        this.l1HitCounter = Counter.builder("cache.l1.hits.total")
            .description("Total number of L1 cache hits")
            .register(meterRegistry);
        
        this.l1MissCounter = Counter.builder("cache.l1.misses.total")
            .description("Total number of L1 cache misses")
            .register(meterRegistry);
        
        this.l2HitCounter = Counter.builder("cache.l2.hits.total")
            .description("Total number of L2 cache hits")
            .register(meterRegistry);
        
        this.l2MissCounter = Counter.builder("cache.l2.misses.total")
            .description("Total number of L2 cache misses")
            .register(meterRegistry);
        
        this.cacheErrorCounter = Counter.builder("cache.errors.total")
            .description("Total number of cache errors")
            .register(meterRegistry);
        
        // 注册实时指标
        Gauge.builder("cache.size.total", cacheSize, AtomicInteger::get)
            .description("Total number of cache entries")
            .register(meterRegistry);
        
        Gauge.builder("cache.memory.usage", cacheMemoryUsage, AtomicLong::get)
            .description("Cache memory usage in bytes")
            .register(meterRegistry);
        
        Gauge.builder("cache.l1.size", l1Size, AtomicInteger::get)
            .description("Number of L1 cache entries")
            .register(meterRegistry);
        
        Gauge.builder("cache.l2.size", l2Size, AtomicInteger::get)
            .description("Number of L2 cache entries")
            .register(meterRegistry);
        
        // 缓存命中率指标
        Gauge.builder("cache.hit.rate", this, CacheMetrics::calculateHitRate)
            .description("Cache hit rate")
            .register(meterRegistry);
        
        Gauge.builder("cache.l1.hit.rate", this, CacheMetrics::calculateL1HitRate)
            .description("L1 cache hit rate")
            .register(meterRegistry);
        
        Gauge.builder("cache.l2.hit.rate", this, CacheMetrics::calculateL2HitRate)
            .description("L2 cache hit rate")
            .register(meterRegistry);
        
        log.info("缓存监控指标初始化完成");
    }
    
    /**
     * 记录缓存命中
     */
    public void recordCacheHit(String cacheName) {
        cacheHitCounter.increment();
        cacheHitCounters.computeIfAbsent(cacheName, name ->
            Counter.builder("cache.hits.by.name")
                .tag("cache", name)
                .description("Cache hits by cache name")
                .register(meterRegistry)
        ).increment();
    }
    
    /**
     * 记录缓存未命中
     */
    public void recordCacheMiss(String cacheName) {
        cacheMissCounter.increment();
        cacheMissCounters.computeIfAbsent(cacheName, name ->
            Counter.builder("cache.misses.by.name")
                .tag("cache", name)
                .description("Cache misses by cache name")
                .register(meterRegistry)
        ).increment();
    }
    
    /**
     * 记录缓存获取时间
     */
    public void recordCacheGet(long durationMs) {
        cacheGetTimer.record(durationMs, TimeUnit.MILLISECONDS);
    }
    
    /**
     * 记录缓存写入
     */
    public void recordCachePut(String cacheName, long durationMs) {
        cachePutCounter.increment();
        cachePutTimer.record(durationMs, TimeUnit.MILLISECONDS);
        
        // 更新缓存大小计数器
        cacheSizeCounters.computeIfAbsent(cacheName, name ->
            Counter.builder("cache.puts.by.name")
                .tag("cache", name)
                .description("Cache puts by cache name")
                .register(meterRegistry)
        ).increment();
    }
    
    /**
     * 记录缓存清除
     */
    public void recordCacheEvict(String cacheName, long durationMs) {
        cacheEvictCounter.increment();
        cacheEvictTimer.record(durationMs, TimeUnit.MILLISECONDS);
    }
    
    /**
     * 记录L1缓存命中
     */
    public void recordL1Hit() {
        l1HitCounter.increment();
    }
    
    /**
     * 记录L1缓存未命中
     */
    public void recordL1Miss() {
        l1MissCounter.increment();
    }
    
    /**
     * 记录L2缓存命中
     */
    public void recordL2Hit() {
        l2HitCounter.increment();
    }
    
    /**
     * 记录L2缓存未命中
     */
    public void recordL2Miss() {
        l2MissCounter.increment();
    }
    
    /**
     * 记录缓存错误
     */
    public void recordCacheError(String errorType) {
        cacheErrorCounter.increment();
    }
    
    /**
     * 更新缓存大小
     */
    public void updateCacheSize(int size, long memoryUsage) {
        cacheSize.set(size);
        cacheMemoryUsage.set(memoryUsage);
    }
    
    /**
     * 更新L1缓存大小
     */
    public void updateL1Size(int size) {
        l1Size.set(size);
    }
    
    /**
     * 更新L2缓存大小
     */
    public void updateL2Size(int size) {
        l2Size.set(size);
    }
    
    /**
     * 计算总体缓存命中率
     */
    private double calculateHitRate() {
        long hits = (long) cacheHitCounter.count();
        long misses = (long) cacheMissCounter.count();
        long total = hits + misses;
        
        return total > 0 ? (double) hits / total : 0.0;
    }
    
    /**
     * 计算L1缓存命中率
     */
    private double calculateL1HitRate() {
        long hits = (long) l1HitCounter.count();
        long misses = (long) l1MissCounter.count();
        long total = hits + misses;
        
        return total > 0 ? (double) hits / total : 0.0;
    }
    
    /**
     * 计算L2缓存命中率
     */
    private double calculateL2HitRate() {
        long hits = (long) l2HitCounter.count();
        long misses = (long) l2MissCounter.count();
        long total = hits + misses;
        
        return total > 0 ? (double) hits / total : 0.0;
    }
    
    /**
     * 获取监控统计信息
     */
    public Map<String, Object> getMetrics() {
        Map<String, Object> metrics = new java.util.HashMap<>();
        
        long hits = (long) cacheHitCounter.count();
        long misses = (long) cacheMissCounter.count();
        long total = hits + misses;
        double hitRate = total > 0 ? (double) hits / total : 0.0;
        
        long l1Hits = (long) l1HitCounter.count();
        long l1Misses = (long) l1MissCounter.count();
        long l1Total = l1Hits + l1Misses;
        double l1HitRate = l1Total > 0 ? (double) l1Hits / l1Total : 0.0;
        
        long l2Hits = (long) l2HitCounter.count();
        long l2Misses = (long) l2MissCounter.count();
        long l2Total = l2Hits + l2Misses;
        double l2HitRate = l2Total > 0 ? (double) l2Hits / l2Total : 0.0;
        
        metrics.put("hits.total", hits);
        metrics.put("misses.total", misses);
        metrics.put("hitRate", hitRate);
        
        metrics.put("puts.total", cachePutCounter.count());
        metrics.put("evicts.total", cacheEvictCounter.count());
        
        metrics.put("get.avgTimeMs", cacheGetTimer.mean(TimeUnit.MILLISECONDS));
        metrics.put("put.avgTimeMs", cachePutTimer.mean(TimeUnit.MILLISECONDS));
        metrics.put("evict.avgTimeMs", cacheEvictTimer.mean(TimeUnit.MILLISECONDS));
        
        metrics.put("l1.hits", l1Hits);
        metrics.put("l1.misses", l1Misses);
        metrics.put("l1.hitRate", l1HitRate);
        metrics.put("l1.size", l1Size.get());
        
        metrics.put("l2.hits", l2Hits);
        metrics.put("l2.misses", l2Misses);
        metrics.put("l2.hitRate", l2HitRate);
        metrics.put("l2.size", l2Size.get());
        
        metrics.put("size.total", cacheSize.get());
        metrics.put("memory.usage", cacheMemoryUsage.get());
        
        metrics.put("errors.total", cacheErrorCounter.count());
        
        // 按缓存名称统计
        Map<String, Long> hitsByName = new java.util.HashMap<>();
        cacheHitCounters.forEach((name, counter) -> hitsByName.put(name, (long) counter.count()));
        metrics.put("hits.byCache", hitsByName);
        
        Map<String, Long> missesByName = new java.util.HashMap<>();
        cacheMissCounters.forEach((name, counter) -> missesByName.put(name, (long) counter.count()));
        metrics.put("misses.byCache", missesByName);
        
        Map<String, Long> putsByName = new java.util.HashMap<>();
        cacheSizeCounters.forEach((name, counter) -> putsByName.put(name, (long) counter.count()));
        metrics.put("puts.byCache", putsByName);
        
        return metrics;
    }
    
    /**
     * 健康检查
     */
    public Map<String, Object> healthCheck() {
        Map<String, Object> health = new java.util.HashMap<>();
        
        try {
            double hitRate = calculateHitRate();
            double l1HitRate = calculateL1HitRate();
            double l2HitRate = calculateL2HitRate();
            
            // 检查缓存命中率
            boolean healthy = hitRate > 0.7 && l1HitRate > 0.8 && l2HitRate > 0.6;
            
            health.put("status", healthy ? "healthy" : "degraded");
            health.put("hitRate", hitRate);
            health.put("l1HitRate", l1HitRate);
            health.put("l2HitRate", l2HitRate);
            health.put("totalSize", cacheSize.get());
            health.put("message", healthy ? "缓存运行正常" : "缓存性能可能下降");
            
        } catch (Exception e) {
            health.put("status", "unhealthy");
            health.put("error", e.getMessage());
            health.put("message", "缓存健康检查失败");
        }
        
        return health;
    }
    
    /**
     * 重置指标（测试用途）
     */
    public void resetMetrics() {
        cacheSize.set(0);
        cacheMemoryUsage.set(0);
        l1Size.set(0);
        l2Size.set(0);
    }
    
    /**
     * 获取按缓存名称的统计信息
     */
    public Map<String, Map<String, Object>> getCacheStatsByName() {
        Map<String, Map<String, Object>> stats = new java.util.HashMap<>();
        
        cacheHitCounters.keySet().forEach(cacheName -> {
            Map<String, Object> cacheStats = new java.util.HashMap<>();
            
            Counter hitsCounter = cacheHitCounters.get(cacheName);
            Counter missesCounter = cacheMissCounters.get(cacheName);
            Counter putsCounter = cacheSizeCounters.get(cacheName);
            
            long hits = hitsCounter != null ? (long) hitsCounter.count() : 0;
            long misses = missesCounter != null ? (long) missesCounter.count() : 0;
            long puts = putsCounter != null ? (long) putsCounter.count() : 0;
            long total = hits + misses;
            double hitRate = total > 0 ? (double) hits / total : 0.0;
            
            cacheStats.put("hits", hits);
            cacheStats.put("misses", misses);
            cacheStats.put("hitRate", hitRate);
            cacheStats.put("puts", puts);
            
            stats.put(cacheName, cacheStats);
        });
        
        return stats;
    }
}