package com.aioa.knowledge.monitor;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.Gauge;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

/**
 * 知识库监控指标
 * 监控文档数量、搜索频率、向量操作等关键指标
 */
@Slf4j
@Component
public class KnowledgeMetrics {
    
    private final MeterRegistry meterRegistry;
    
    // 文档相关指标
    private final Counter docCreateCounter;
    private final Counter docUpdateCounter;
    private final Counter docDeleteCounter;
    private final Counter docViewCounter;
    
    // 搜索相关指标
    private final Counter searchCounter;
    private final Counter semanticSearchCounter;
    private final Timer searchTimer;
    private final Timer semanticSearchTimer;
    
    // 向量相关指标
    private final Counter vectorStoreCounter;
    private final Counter vectorSearchCounter;
    private final Timer vectorStoreTimer;
    private final Timer vectorSearchTimer;
    
    // RAG相关指标
    private final Counter ragRetrieveCounter;
    private final Timer ragRetrieveTimer;
    
    // 批量操作指标
    private final Counter batchCreateCounter;
    private final Counter batchUpdateCounter;
    private final Counter batchDeleteCounter;
    private final Timer batchOperationTimer;
    
    // 实时统计指标
    private final AtomicInteger activeDocuments = new AtomicInteger(0);
    private final AtomicInteger activeSearches = new AtomicInteger(0);
    private final AtomicLong totalVectorSize = new AtomicLong(0);
    private final AtomicInteger cacheHitCount = new AtomicInteger(0);
    private final AtomicInteger cacheMissCount = new AtomicInteger(0);
    
    // 错误指标
    private final Counter errorCounter;
    private final Map<String, Counter> errorCounters = new ConcurrentHashMap<>();
    
    @Autowired
    public KnowledgeMetrics(MeterRegistry meterRegistry) {
        this.meterRegistry = meterRegistry;
        
        // 初始化计数器
        this.docCreateCounter = Counter.builder("knowledge.docs.create.total")
            .description("Total number of documents created")
            .register(meterRegistry);
        
        this.docUpdateCounter = Counter.builder("knowledge.docs.update.total")
            .description("Total number of documents updated")
            .register(meterRegistry);
        
        this.docDeleteCounter = Counter.builder("knowledge.docs.delete.total")
            .description("Total number of documents deleted")
            .register(meterRegistry);
        
        this.docViewCounter = Counter.builder("knowledge.docs.view.total")
            .description("Total number of document views")
            .register(meterRegistry);
        
        this.searchCounter = Counter.builder("knowledge.search.total")
            .description("Total number of keyword searches")
            .register(meterRegistry);
        
        this.semanticSearchCounter = Counter.builder("knowledge.search.semantic.total")
            .description("Total number of semantic searches")
            .register(meterRegistry);
        
        this.searchTimer = Timer.builder("knowledge.search.duration")
            .description("Time taken for keyword searches")
            .publishPercentiles(0.5, 0.95, 0.99)
            .register(meterRegistry);
        
        this.semanticSearchTimer = Timer.builder("knowledge.search.semantic.duration")
            .description("Time taken for semantic searches")
            .publishPercentiles(0.5, 0.95, 0.99)
            .register(meterRegistry);
        
        this.vectorStoreCounter = Counter.builder("knowledge.vector.store.total")
            .description("Total number of vectors stored")
            .register(meterRegistry);
        
        this.vectorSearchCounter = Counter.builder("knowledge.vector.search.total")
            .description("Total number of vector searches")
            .register(meterRegistry);
        
        this.vectorStoreTimer = Timer.builder("knowledge.vector.store.duration")
            .description("Time taken to store vectors")
            .publishPercentiles(0.5, 0.95, 0.99)
            .register(meterRegistry);
        
        this.vectorSearchTimer = Timer.builder("knowledge.vector.search.duration")
            .description("Time taken for vector searches")
            .publishPercentiles(0.5, 0.95, 0.99)
            .register(meterRegistry);
        
        this.ragRetrieveCounter = Counter.builder("knowledge.rag.retrieve.total")
            .description("Total number of RAG retrievals")
            .register(meterRegistry);
        
        this.ragRetrieveTimer = Timer.builder("knowledge.rag.retrieve.duration")
            .description("Time taken for RAG retrievals")
            .publishPercentiles(0.5, 0.95, 0.99)
            .register(meterRegistry);
        
        this.batchCreateCounter = Counter.builder("knowledge.batch.create.total")
            .description("Total number of batch document creations")
            .register(meterRegistry);
        
        this.batchUpdateCounter = Counter.builder("knowledge.batch.update.total")
            .description("Total number of batch document updates")
            .register(meterRegistry);
        
        this.batchDeleteCounter = Counter.builder("knowledge.batch.delete.total")
            .description("Total number of batch document deletions")
            .register(meterRegistry);
        
        this.batchOperationTimer = Timer.builder("knowledge.batch.operation.duration")
            .description("Time taken for batch operations")
            .publishPercentiles(0.5, 0.95, 0.99)
            .register(meterRegistry);
        
        this.errorCounter = Counter.builder("knowledge.errors.total")
            .description("Total number of errors")
            .register(meterRegistry);
        
        // 注册实时指标
        Gauge.builder("knowledge.docs.active", activeDocuments, AtomicInteger::get)
            .description("Number of active documents")
            .register(meterRegistry);
        
        Gauge.builder("knowledge.search.active", activeSearches, AtomicInteger::get)
            .description("Number of active searches")
            .register(meterRegistry);
        
        Gauge.builder("knowledge.vector.size.total", totalVectorSize, AtomicLong::get)
            .description("Total vector storage size in bytes")
            .register(meterRegistry);
        
        Gauge.builder("knowledge.cache.hit.rate", this, KnowledgeMetrics::calculateCacheHitRate)
            .description("Cache hit rate")
            .register(meterRegistry);
        
        log.info("知识库监控指标初始化完成");
    }
    
    /**
     * 记录文档创建
     */
    public void recordDocCreate(long durationMs) {
        docCreateCounter.increment();
        searchTimer.record(durationMs, TimeUnit.MILLISECONDS);
    }
    
    /**
     * 记录文档更新
     */
    public void recordDocUpdate(long durationMs) {
        docUpdateCounter.increment();
        searchTimer.record(durationMs, TimeUnit.MILLISECONDS);
    }
    
    /**
     * 记录文档删除
     */
    public void recordDocDelete(long durationMs) {
        docDeleteCounter.increment();
        searchTimer.record(durationMs, TimeUnit.MILLISECONDS);
    }
    
    /**
     * 记录文档查看
     */
    public void recordDocView() {
        docViewCounter.increment();
    }
    
    /**
     * 记录关键词搜索
     */
    public void recordSearch(long durationMs) {
        searchCounter.increment();
        searchTimer.record(durationMs, TimeUnit.MILLISECONDS);
        activeSearches.incrementAndGet();
    }
    
    /**
     * 记录语义搜索
     */
    public void recordSemanticSearch(long durationMs) {
        semanticSearchCounter.increment();
        semanticSearchTimer.record(durationMs, TimeUnit.MILLISECONDS);
        activeSearches.incrementAndGet();
    }
    
    /**
     * 记录搜索完成
     */
    public void recordSearchComplete() {
        activeSearches.decrementAndGet();
    }
    
    /**
     * 记录向量存储
     */
    public void recordVectorStore(long durationMs, int vectorSize) {
        vectorStoreCounter.increment();
        vectorStoreTimer.record(durationMs, TimeUnit.MILLISECONDS);
        totalVectorSize.addAndGet(vectorSize);
    }
    
    /**
     * 记录向量搜索
     */
    public void recordVectorSearch(long durationMs) {
        vectorSearchCounter.increment();
        vectorSearchTimer.record(durationMs, TimeUnit.MILLISECONDS);
    }
    
    /**
     * 记录RAG检索
     */
    public void recordRagRetrieve(long durationMs) {
        ragRetrieveCounter.increment();
        ragRetrieveTimer.record(durationMs, TimeUnit.MILLISECONDS);
    }
    
    /**
     * 记录批量创建
     */
    public void recordBatchCreate(int count, long durationMs) {
        batchCreateCounter.increment(count);
        batchOperationTimer.record(durationMs, TimeUnit.MILLISECONDS);
    }
    
    /**
     * 记录批量更新
     */
    public void recordBatchUpdate(int count, long durationMs) {
        batchUpdateCounter.increment(count);
        batchOperationTimer.record(durationMs, TimeUnit.MILLISECONDS);
    }
    
    /**
     * 记录批量删除
     */
    public void recordBatchDelete(int count, long durationMs) {
        batchDeleteCounter.increment(count);
        batchOperationTimer.record(durationMs, TimeUnit.MILLISECONDS);
    }
    
    /**
     * 记录错误
     */
    public void recordError(String errorType) {
        errorCounter.increment();
        
        // 按错误类型统计
        Counter typeCounter = errorCounters.computeIfAbsent(errorType, type -> 
            Counter.builder("knowledge.errors.by.type")
                .tag("type", type)
                .description("Errors by type")
                .register(meterRegistry)
        );
        typeCounter.increment();
    }
    
    /**
     * 记录缓存命中
     */
    public void recordCacheHit() {
        cacheHitCount.incrementAndGet();
    }
    
    /**
     * 记录缓存未命中
     */
    public void recordCacheMiss() {
        cacheMissCount.incrementAndGet();
    }
    
    /**
     * 更新活动文档数量
     */
    public void updateActiveDocuments(int count) {
        activeDocuments.set(count);
    }
    
    /**
     * 计算缓存命中率
     */
    private double calculateCacheHitRate() {
        int hits = cacheHitCount.get();
        int misses = cacheMissCount.get();
        int total = hits + misses;
        
        return total > 0 ? (double) hits / total : 0.0;
    }
    
    /**
     * 获取监控统计信息
     */
    public Map<String, Object> getMetrics() {
        Map<String, Object> metrics = new java.util.HashMap<>();
        
        // 文档统计
        metrics.put("docs.created", docCreateCounter.count());
        metrics.put("docs.updated", docUpdateCounter.count());
        metrics.put("docs.deleted", docDeleteCounter.count());
        metrics.put("docs.viewed", docViewCounter.count());
        metrics.put("docs.active", activeDocuments.get());
        
        // 搜索统计
        metrics.put("searches.total", searchCounter.count());
        metrics.put("searches.semantic", semanticSearchCounter.count());
        metrics.put("searches.active", activeSearches.get());
        
        // 搜索性能
        metrics.put("search.avgTimeMs", searchTimer.mean(TimeUnit.MILLISECONDS));
        metrics.put("search.semantic.avgTimeMs", semanticSearchTimer.mean(TimeUnit.MILLISECONDS));
        
        // 向量统计
        metrics.put("vectors.stored", vectorStoreCounter.count());
        metrics.put("vectors.searched", vectorSearchCounter.count());
        metrics.put("vectors.totalSize", totalVectorSize.get());
        
        // 向量性能
        metrics.put("vector.store.avgTimeMs", vectorStoreTimer.mean(TimeUnit.MILLISECONDS));
        metrics.put("vector.search.avgTimeMs", vectorSearchTimer.mean(TimeUnit.MILLISECONDS));
        
        // RAG统计
        metrics.put("rag.retrievals", ragRetrieveCounter.count());
        metrics.put("rag.avgTimeMs", ragRetrieveTimer.mean(TimeUnit.MILLISECONDS));
        
        // 批量操作统计
        metrics.put("batch.creates", batchCreateCounter.count());
        metrics.put("batch.updates", batchUpdateCounter.count());
        metrics.put("batch.deletes", batchDeleteCounter.count());
        metrics.put("batch.avgTimeMs", batchOperationTimer.mean(TimeUnit.MILLISECONDS));
        
        // 缓存统计
        int hits = cacheHitCount.get();
        int misses = cacheMissCount.get();
        int total = hits + misses;
        double hitRate = total > 0 ? (double) hits / total : 0.0;
        
        metrics.put("cache.hits", hits);
        metrics.put("cache.misses", misses);
        metrics.put("cache.hitRate", hitRate);
        
        // 错误统计
        metrics.put("errors.total", errorCounter.count());
        
        // 错误类型统计
        Map<String, Double> errorTypes = new java.util.HashMap<>();
        errorCounters.forEach((type, counter) -> {
            errorTypes.put(type, counter.count());
        });
        metrics.put("errors.byType", errorTypes);
        
        // 系统时间
        metrics.put("timestamp", new java.util.Date());
        metrics.put("uptime", System.currentTimeMillis());
        
        return metrics;
    }
    
    /**
     * 重置监控指标（用于测试）
     */
    public void resetMetrics() {
        // 注意：实际使用中应谨慎重置监控指标
        cacheHitCount.set(0);
        cacheMissCount.set(0);
        activeDocuments.set(0);
        activeSearches.set(0);
        totalVectorSize.set(0);
        
        log.info("监控指标已重置");
    }
    
    /**
     * 健康检查
     */
    public Map<String, Object> healthCheck() {
        Map<String, Object> health = new java.util.HashMap<>();
        
        try {
            // 检查指标收集是否正常
            double searchRate = searchTimer.count() > 0 ? 1.0 / searchTimer.mean(TimeUnit.SECONDS) : 0;
            double errorRate = errorCounter.count() / Math.max(searchTimer.count(), 1.0);
            
            health.put("status", "healthy");
            health.put("searchRate", searchRate);
            health.put("errorRate", errorRate);
            health.put("cacheHitRate", calculateCacheHitRate());
            health.put("activeSearches", activeSearches.get());
            health.put("message", "知识库监控运行正常");
            
        } catch (Exception e) {
            health.put("status", "unhealthy");
            health.put("error", e.getMessage());
            health.put("message", "知识库监控检查失败");
        }
        
        return health;
    }
}