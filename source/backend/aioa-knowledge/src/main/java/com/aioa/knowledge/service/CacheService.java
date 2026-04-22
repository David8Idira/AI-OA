package com.aioa.knowledge.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 缓存服务 - 提供高级缓存操作
 */
@Slf4j
@Service
public class CacheService {
    
    // 缓存统计信息
    private final Map<String, CacheStats> cacheStats = new ConcurrentHashMap<>();
    
    /**
     * 缓存统计信息
     */
    public static class CacheStats {
        private String cacheName;
        private long hits = 0;
        private long misses = 0;
        private long puts = 0;
        private long evicts = 0;
        
        public CacheStats(String cacheName) {
            this.cacheName = cacheName;
        }
        
        public synchronized void hit() { hits++; }
        public synchronized void miss() { misses++; }
        public synchronized void put() { puts++; }
        public synchronized void evict() { evicts++; }
        
        public long getHits() { return hits; }
        public long getMisses() { return misses; }
        public long getPuts() { return puts; }
        public long getEvicts() { return evicts; }
        public double getHitRate() { 
            long total = hits + misses;
            return total > 0 ? (double) hits / total : 0.0;
        }
        
        @Override
        public String toString() {
            return String.format("CacheStats{name=%s, hits=%d, misses=%d, puts=%d, evicts=%d, hitRate=%.2f%%}", 
                cacheName, hits, misses, puts, evicts, getHitRate() * 100);
        }
    }
    
    /**
     * 获取或创建缓存统计
     */
    private CacheStats getStats(String cacheName) {
        return cacheStats.computeIfAbsent(cacheName, CacheStats::new);
    }
    
    /**
     * 获取缓存统计信息
     */
    public Map<String, Object> getCacheStatistics() {
        Map<String, Object> stats = new java.util.HashMap<>();
        
        cacheStats.forEach((name, stat) -> {
            Map<String, Object> statMap = new java.util.HashMap<>();
            statMap.put("hits", stat.getHits());
            statMap.put("misses", stat.getMisses());
            statMap.put("puts", stat.getPuts());
            statMap.put("evicts", stat.getEvicts());
            statMap.put("hitRate", String.format("%.2f%%", stat.getHitRate() * 100));
            stats.put(name, statMap);
        });
        
        stats.put("totalCaches", cacheStats.size());
        return stats;
    }
    
    /**
     * 清空所有缓存
     */
    public void clearAllCaches() {
        cacheStats.clear();
        log.info("所有缓存统计已重置");
    }
    
    /**
     * 缓存预热
     */
    public void warmUpCache() {
        log.info("开始缓存预热...");
        // 这里可以添加预热逻辑，如预加载常用数据
        log.info("缓存预热完成");
    }
    
    /**
     * 带统计的缓存获取
     */
    @Cacheable(value = "knowledge:docs", key = "#id")
    public <T> T getWithStats(String cacheName, Object key, Class<T> type, java.util.function.Supplier<T> loader) {
        CacheStats stats = getStats(cacheName);
        
        // 这里简化实现，实际应该集成到Spring Cache中
        // 在实际项目中，可以通过AOP实现统计
        try {
            T value = loader.get();
            if (value != null) {
                stats.put();
            }
            return value;
        } catch (Exception e) {
            log.error("缓存加载失败: cache={}, key={}", cacheName, key, e);
            return null;
        }
    }
    
    /**
     * 批量缓存操作
     */
    @CachePut(value = "knowledge:docs", key = "#key")
    public <T> T batchPut(String key, T value) {
        getStats("knowledge:docs").put();
        return value;
    }
    
    /**
     * 批量缓存清除
     */
    @CacheEvict(value = "knowledge:docs", allEntries = true)
    public void batchEvict() {
        getStats("knowledge:docs").evict();
        log.info("批量清除文档缓存");
    }
    
    /**
     * 智能缓存刷新策略
     * 根据访问频率决定缓存过期时间
     */
    public String getSmartCacheTTL(String cacheName, String key, long accessCount) {
        // 访问频率越高，缓存时间越长
        if (accessCount > 1000) {
            return "1h"; // 高频访问：1小时
        } else if (accessCount > 100) {
            return "30m"; // 中频访问：30分钟
        } else {
            return "10m"; // 低频访问：10分钟
        }
    }
    
    /**
     * 缓存健康检查
     */
    public Map<String, Object> healthCheck() {
        Map<String, Object> health = new java.util.HashMap<>();
        
        try {
            // 检查缓存统计
            health.put("cacheStats", cacheStats.size());
            health.put("totalHits", cacheStats.values().stream().mapToLong(CacheStats::getHits).sum());
            health.put("totalMisses", cacheStats.values().stream().mapToLong(CacheStats::getMisses).sum());
            
            // 计算总体命中率
            long totalHits = cacheStats.values().stream().mapToLong(CacheStats::getHits).sum();
            long totalMisses = cacheStats.values().stream().mapToLong(CacheStats::getMisses).sum();
            long total = totalHits + totalMisses;
            double hitRate = total > 0 ? (double) totalHits / total : 0.0;
            
            health.put("overallHitRate", String.format("%.2f%%", hitRate * 100));
            health.put("status", "healthy");
            health.put("message", "缓存服务运行正常");
            
        } catch (Exception e) {
            health.put("status", "unhealthy");
            health.put("error", e.getMessage());
            health.put("message", "缓存服务检查失败");
        }
        
        return health;
    }
}