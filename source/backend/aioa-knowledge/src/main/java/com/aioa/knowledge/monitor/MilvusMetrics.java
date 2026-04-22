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

/**
 * Milvus向量数据库监控指标
 */
@Slf4j
@Component
public class MilvusMetrics {
    
    private final MeterRegistry meterRegistry;
    
    // 连接相关指标
    private final Counter connectionSuccessCounter;
    private final Counter connectionFailureCounter;
    private final Timer connectionTimer;
    
    // 向量操作指标
    private final Counter vectorInsertCounter;
    private final Counter vectorSearchCounter;
    private final Counter vectorDeleteCounter;
    private final Counter vectorUpdateCounter;
    
    private final Timer vectorInsertTimer;
    private final Timer vectorSearchTimer;
    private final Timer vectorDeleteTimer;
    private final Timer vectorUpdateTimer;
    
    // 集合操作指标
    private final Counter collectionCreateCounter;
    private final Counter collectionDropCounter;
    private final Timer collectionOperationTimer;
    
    // 错误指标
    private final Counter milvusErrorCounter;
    
    // 实时指标
    private final AtomicInteger activeConnections = new AtomicInteger(0);
    private final AtomicLong totalVectors = new AtomicLong(0);
    private final AtomicInteger searchLatencyMs = new AtomicInteger(0);
    
    @Autowired
    public MilvusMetrics(MeterRegistry meterRegistry) {
        this.meterRegistry = meterRegistry;
        
        // 初始化计数器
        this.connectionSuccessCounter = Counter.builder("milvus.connections.success.total")
            .description("Total number of successful Milvus connections")
            .register(meterRegistry);
        
        this.connectionFailureCounter = Counter.builder("milvus.connections.failure.total")
            .description("Total number of failed Milvus connections")
            .register(meterRegistry);
        
        this.connectionTimer = Timer.builder("milvus.connections.duration")
            .description("Time taken to establish Milvus connections")
            .publishPercentiles(0.5, 0.95, 0.99)
            .register(meterRegistry);
        
        this.vectorInsertCounter = Counter.builder("milvus.vectors.insert.total")
            .description("Total number of vectors inserted")
            .register(meterRegistry);
        
        this.vectorSearchCounter = Counter.builder("milvus.vectors.search.total")
            .description("Total number of vector searches")
            .register(meterRegistry);
        
        this.vectorDeleteCounter = Counter.builder("milvus.vectors.delete.total")
            .description("Total number of vectors deleted")
            .register(meterRegistry);
        
        this.vectorUpdateCounter = Counter.builder("milvus.vectors.update.total")
            .description("Total number of vectors updated")
            .register(meterRegistry);
        
        this.vectorInsertTimer = Timer.builder("milvus.vectors.insert.duration")
            .description("Time taken to insert vectors")
            .publishPercentiles(0.5, 0.95, 0.99)
            .register(meterRegistry);
        
        this.vectorSearchTimer = Timer.builder("milvus.vectors.search.duration")
            .description("Time taken for vector searches")
            .publishPercentiles(0.5, 0.95, 0.99)
            .register(meterRegistry);
        
        this.vectorDeleteTimer = Timer.builder("milvus.vectors.delete.duration")
            .description("Time taken to delete vectors")
            .publishPercentiles(0.5, 0.95, 0.99)
            .register(meterRegistry);
        
        this.vectorUpdateTimer = Timer.builder("milvus.vectors.update.duration")
            .description("Time taken to update vectors")
            .publishPercentiles(0.5, 0.95, 0.99)
            .register(meterRegistry);
        
        this.collectionCreateCounter = Counter.builder("milvus.collections.create.total")
            .description("Total number of collections created")
            .register(meterRegistry);
        
        this.collectionDropCounter = Counter.builder("milvus.collections.drop.total")
            .description("Total number of collections dropped")
            .register(meterRegistry);
        
        this.collectionOperationTimer = Timer.builder("milvus.collections.operation.duration")
            .description("Time taken for collection operations")
            .publishPercentiles(0.5, 0.95, 0.99)
            .register(meterRegistry);
        
        this.milvusErrorCounter = Counter.builder("milvus.errors.total")
            .description("Total number of Milvus errors")
            .register(meterRegistry);
        
        // 注册实时指标
        Gauge.builder("milvus.connections.active", activeConnections, AtomicInteger::get)
            .description("Number of active Milvus connections")
            .register(meterRegistry);
        
        Gauge.builder("milvus.vectors.total", totalVectors, AtomicLong::get)
            .description("Total number of vectors in Milvus")
            .register(meterRegistry);
        
        Gauge.builder("milvus.search.latency", searchLatencyMs, AtomicInteger::get)
            .description("Average search latency in milliseconds")
            .register(meterRegistry);
        
        log.info("Milvus监控指标初始化完成");
    }
    
    /**
     * 记录连接成功
     */
    public void recordConnectionSuccess(long durationMs) {
        connectionSuccessCounter.increment();
        connectionTimer.record(durationMs, TimeUnit.MILLISECONDS);
        activeConnections.incrementAndGet();
    }
    
    /**
     * 记录连接失败
     */
    public void recordConnectionFailure() {
        connectionFailureCounter.increment();
    }
    
    /**
     * 记录连接关闭
     */
    public void recordConnectionClosed() {
        activeConnections.decrementAndGet();
    }
    
    /**
     * 记录向量插入
     */
    public void recordVectorInsert(int count, long durationMs) {
        vectorInsertCounter.increment(count);
        vectorInsertTimer.record(durationMs, TimeUnit.MILLISECONDS);
        totalVectors.addAndGet(count);
    }
    
    /**
     * 记录向量搜索
     */
    public void recordVectorSearch(long durationMs) {
        vectorSearchCounter.increment();
        vectorSearchTimer.record(durationMs, TimeUnit.MILLISECONDS);
        searchLatencyMs.set((int) durationMs);
    }
    
    /**
     * 记录向量删除
     */
    public void recordVectorDelete(int count, long durationMs) {
        vectorDeleteCounter.increment(count);
        vectorDeleteTimer.record(durationMs, TimeUnit.MILLISECONDS);
        totalVectors.addAndGet(-count);
    }
    
    /**
     * 记录向量更新
     */
    public void recordVectorUpdate(int count, long durationMs) {
        vectorUpdateCounter.increment(count);
        vectorUpdateTimer.record(durationMs, TimeUnit.MILLISECONDS);
    }
    
    /**
     * 记录集合创建
     */
    public void recordCollectionCreate(long durationMs) {
        collectionCreateCounter.increment();
        collectionOperationTimer.record(durationMs, TimeUnit.MILLISECONDS);
    }
    
    /**
     * 记录集合删除
     */
    public void recordCollectionDrop(long durationMs) {
        collectionDropCounter.increment();
        collectionOperationTimer.record(durationMs, TimeUnit.MILLISECONDS);
    }
    
    /**
     * 记录Milvus错误
     */
    public void recordError(String errorType) {
        milvusErrorCounter.increment();
    }
    
    /**
     * 更新向量总数
     */
    public void updateTotalVectors(long count) {
        totalVectors.set(count);
    }
    
    /**
     * 获取监控统计信息
     */
    public java.util.Map<String, Object> getMetrics() {
        java.util.Map<String, Object> metrics = new java.util.HashMap<>();
        
        metrics.put("connections.active", activeConnections.get());
        metrics.put("connections.success", connectionSuccessCounter.count());
        metrics.put("connections.failure", connectionFailureCounter.count());
        metrics.put("connections.avgTimeMs", connectionTimer.mean(TimeUnit.MILLISECONDS));
        
        metrics.put("vectors.total", totalVectors.get());
        metrics.put("vectors.inserted", vectorInsertCounter.count());
        metrics.put("vectors.searched", vectorSearchCounter.count());
        metrics.put("vectors.deleted", vectorDeleteCounter.count());
        metrics.put("vectors.updated", vectorUpdateCounter.count());
        
        metrics.put("insert.avgTimeMs", vectorInsertTimer.mean(TimeUnit.MILLISECONDS));
        metrics.put("search.avgTimeMs", vectorSearchTimer.mean(TimeUnit.MILLISECONDS));
        metrics.put("delete.avgTimeMs", vectorDeleteTimer.mean(TimeUnit.MILLISECONDS));
        metrics.put("update.avgTimeMs", vectorUpdateTimer.mean(TimeUnit.MILLISECONDS));
        
        metrics.put("collections.created", collectionCreateCounter.count());
        metrics.put("collections.dropped", collectionDropCounter.count());
        metrics.put("collection.avgTimeMs", collectionOperationTimer.mean(TimeUnit.MILLISECONDS));
        
        metrics.put("errors.total", milvusErrorCounter.count());
        
        return metrics;
    }
    
    /**
     * 健康检查
     */
    public java.util.Map<String, Object> healthCheck() {
        java.util.Map<String, Object> health = new java.util.HashMap<>();
        
        try {
            // 检查连接状态
            boolean healthy = activeConnections.get() > 0 && 
                             connectionFailureCounter.count() < connectionSuccessCounter.count();
            
            health.put("status", healthy ? "healthy" : "unhealthy");
            health.put("activeConnections", activeConnections.get());
            health.put("errorRate", connectionFailureCounter.count() / 
                Math.max(connectionSuccessCounter.count() + connectionFailureCounter.count(), 1.0));
            health.put("message", healthy ? "Milvus连接正常" : "Milvus连接异常");
            
        } catch (Exception e) {
            health.put("status", "unhealthy");
            health.put("error", e.getMessage());
            health.put("message", "Milvus健康检查失败");
        }
        
        return health;
    }
    
    /**
     * 重置指标（测试用途）
     */
    public void resetMetrics() {
        activeConnections.set(0);
        totalVectors.set(0);
        searchLatencyMs.set(0);
    }
}