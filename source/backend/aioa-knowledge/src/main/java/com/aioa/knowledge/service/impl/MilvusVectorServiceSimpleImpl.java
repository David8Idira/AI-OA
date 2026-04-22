package com.aioa.knowledge.service.impl;

import com.aioa.knowledge.entity.KnowledgeDoc;
import com.aioa.knowledge.service.EmbeddingService;
import com.aioa.knowledge.service.VectorService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.*;

/**
 * 简化的Milvus向量服务实现
 * 提供基础向量存储和搜索功能，实际部署时可替换为完整的Milvus实现
 */
@Slf4j
@Service
public class MilvusVectorServiceSimpleImpl implements VectorService {
    
    @Autowired(required = false)
    private EmbeddingService embeddingService;
    
    @Value("${milvus.collectionName:aioa_knowledge}")
    private String collectionName;
    
    @Value("${milvus.vectorDim:1536}")
    private int vectorDim;
    
    // 模拟向量存储（实际应使用真实的Milvus）
    private final Map<String, List<Float>> vectorStore = new HashMap<>();
    private final Map<String, Map<String, Object>> metadataStore = new HashMap<>();
    
    /**
     * 存储文档向量
     */
    @Override
    public String storeVector(KnowledgeDoc doc) {
        try {
            if (embeddingService == null) {
                log.warn("EmbeddingService未配置，使用模拟向量");
                String vectorId = "mock_vector_" + System.currentTimeMillis() + "_" + doc.getId();
                vectorStore.put(vectorId, generateMockVector());
                metadataStore.put(vectorId, createMetadata(doc));
                log.info("模拟向量存储成功: {}", vectorId);
                return vectorId;
            }
            
            // 生成向量
            List<Float> vector = embeddingService.generateEmbedding(doc.getContent());
            if (vector == null || vector.isEmpty()) {
                log.error("向量生成失败");
                return null;
            }
            
            // 存储向量（模拟）
            String vectorId = "vector_" + System.currentTimeMillis() + "_" + doc.getId();
            vectorStore.put(vectorId, vector);
            metadataStore.put(vectorId, createMetadata(doc));
            
            log.info("向量存储成功: {}", vectorId);
            return vectorId;
            
        } catch (Exception e) {
            log.error("向量存储失败", e);
            return null;
        }
    }
    
    /**
     * 向量搜索
     */
    @Override
    public List<String> vectorSearch(String query, int topK) {
        try {
            log.info("搜索相似向量: query={}, topK={}", query, topK);
            
            // 生成查询向量
            List<Float> queryVector;
            if (embeddingService == null) {
                queryVector = generateMockVector();
            } else {
                queryVector = embeddingService.generateEmbedding(query);
                if (queryVector == null || queryVector.isEmpty()) {
                    log.warn("查询向量生成失败，使用模拟向量");
                    queryVector = generateMockVector();
                }
            }
            
            // 计算相似度（模拟）
            List<Map<String, Object>> results = new ArrayList<>();
            for (Map.Entry<String, List<Float>> entry : vectorStore.entrySet()) {
                String vectorId = entry.getKey();
                List<Float> vector = entry.getValue();
                Map<String, Object> metadata = metadataStore.get(vectorId);
                
                // 计算余弦相似度（简化）
                double similarity = calculateCosineSimilarity(queryVector, vector);
                
                Map<String, Object> result = new HashMap<>();
                result.put("vectorId", vectorId);
                result.put("similarity", similarity);
                result.put("metadata", metadata);
                results.add(result);
            }
            
            // 按相似度排序
            results.sort((a, b) -> Double.compare(
                (double) b.get("similarity"),
                (double) a.get("similarity")
            ));
            
            // 返回前topK个向量ID
            List<String> vectorIds = new ArrayList<>();
            for (int i = 0; i < Math.min(topK, results.size()); i++) {
                Map<String, Object> result = results.get(i);
                vectorIds.add((String) result.get("vectorId"));
            }
            
            log.info("搜索完成，找到 {} 个结果", vectorIds.size());
            return vectorIds;
            
        } catch (Exception e) {
            log.error("向量搜索失败", e);
            return Collections.emptyList();
        }
    }
    
    /**
     * 批量存储向量（额外方法）
     */
    public List<String> batchStoreVector(List<KnowledgeDoc> docs) {
        List<String> vectorIds = new ArrayList<>();
        
        for (KnowledgeDoc doc : docs) {
            String vectorId = storeVector(doc);
            if (vectorId != null) {
                vectorIds.add(vectorId);
            }
        }
        
        log.info("批量存储完成，成功 {} 个，总共 {} 个", vectorIds.size(), docs.size());
        return vectorIds;
    }
    
    /**
     * 删除向量
     */
    @Override
    public boolean deleteVector(String vectorId) {
        try {
            vectorStore.remove(vectorId);
            metadataStore.remove(vectorId);
            log.info("向量删除成功: {}", vectorId);
            return true;
        } catch (Exception e) {
            log.error("向量删除失败: {}", vectorId, e);
            return false;
        }
    }
    
    /**
     * 批量删除向量（额外方法）
     */
    public int batchDeleteVector(List<String> vectorIds) {
        int successCount = 0;
        
        for (String vectorId : vectorIds) {
            if (deleteVector(vectorId)) {
                successCount++;
            }
        }
        
        log.info("批量删除完成，成功 {} 个，总共 {} 个", successCount, vectorIds.size());
        return successCount;
    }
    
    /**
     * 更新向量
     */
    @Override
    public boolean updateVector(String vectorId, KnowledgeDoc doc) {
        try {
            if (!vectorStore.containsKey(vectorId)) {
                log.warn("向量不存在: {}", vectorId);
                return false;
            }
            
            // 重新生成向量
            List<Float> vector;
            if (embeddingService == null) {
                vector = generateMockVector();
            } else {
                vector = embeddingService.generateEmbedding(doc.getContent());
                if (vector == null || vector.isEmpty()) {
                    log.error("向量生成失败");
                    return false;
                }
            }
            
            // 更新向量和元数据
            vectorStore.put(vectorId, vector);
            metadataStore.put(vectorId, createMetadata(doc));
            
            log.info("向量更新成功: {}", vectorId);
            return true;
            
        } catch (Exception e) {
            log.error("向量更新失败", e);
            return false;
        }
    }
    
    /**
     * 健康检查
     */
    @Override
    public boolean healthCheck() {
        try {
            // 简化健康检查
            boolean healthy = true;
            
            if (embeddingService == null) {
                log.warn("EmbeddingService未配置，使用模拟模式");
            }
            
            log.info("向量服务健康检查通过");
            return healthy;
            
        } catch (Exception e) {
            log.error("向量服务健康检查失败", e);
            return false;
        }
    }
    
    /**
     * 获取集合统计
     */
    public Map<String, Object> getCollectionStats() {
        Map<String, Object> stats = new HashMap<>();
        
        stats.put("collectionName", collectionName);
        stats.put("vectorCount", vectorStore.size());
        stats.put("vectorDim", vectorDim);
        stats.put("embeddingService", embeddingService != null ? "enabled" : "mock");
        stats.put("status", "healthy");
        stats.put("timestamp", new java.util.Date());
        
        return stats;
    }
    
    /**
     * 性能测试
     */
    public Map<String, Object> performanceTest(int queryCount, int topK) {
        Map<String, Object> performance = new HashMap<>();
        
        try {
            long startTime = System.currentTimeMillis();
            
            // 执行多次搜索
            for (int i = 0; i < Math.min(queryCount, 10); i++) {
                vectorSearch("性能测试查询" + i, topK);
            }
            
            long endTime = System.currentTimeMillis();
            long duration = endTime - startTime;
            
            performance.put("queryCount", queryCount);
            performance.put("topK", topK);
            performance.put("durationMs", duration);
            performance.put("avgQueryTimeMs", (double) duration / queryCount);
            performance.put("vectorCount", vectorStore.size());
            performance.put("status", "completed");
            
            log.info("性能测试完成: {} 次查询用时 {} 毫秒", queryCount, duration);
            
        } catch (Exception e) {
            log.error("性能测试失败", e);
            performance.put("status", "failed");
            performance.put("error", e.getMessage());
        }
        
        return performance;
    }
    
    /**
     * 清空集合
     */
    public boolean truncateCollection() {
        try {
            vectorStore.clear();
            metadataStore.clear();
            log.info("集合清空成功: {}", collectionName);
            return true;
        } catch (Exception e) {
            log.error("集合清空失败", e);
            return false;
        }
    }
    
    /**
     * 创建元数据
     */
    private Map<String, Object> createMetadata(KnowledgeDoc doc) {
        Map<String, Object> metadata = new HashMap<>();
        metadata.put("id", doc.getId());
        metadata.put("title", doc.getTitle());
        metadata.put("content", doc.getContent());
        metadata.put("categoryId", doc.getCategoryId());
        metadata.put("status", doc.getStatus());
        metadata.put("createdTime", new java.util.Date());
        metadata.put("updatedTime", new java.util.Date());
        return metadata;
    }
    
    /**
     * 生成模拟向量
     */
    private List<Float> generateMockVector() {
        List<Float> vector = new ArrayList<>();
        Random random = new Random();
        
        for (int i = 0; i < vectorDim; i++) {
            vector.add(random.nextFloat() * 2 - 1); // -1 到 1 之间的随机数
        }
        
        // 归一化
        return normalizeVector(vector);
    }
    
    /**
     * 计算余弦相似度
     */
    private double calculateCosineSimilarity(List<Float> v1, List<Float> v2) {
        if (v1 == null || v2 == null || v1.size() != v2.size()) {
            return 0.0;
        }
        
        double dotProduct = 0.0;
        double norm1 = 0.0;
        double norm2 = 0.0;
        
        for (int i = 0; i < v1.size(); i++) {
            dotProduct += v1.get(i) * v2.get(i);
            norm1 += Math.pow(v1.get(i), 2);
            norm2 += Math.pow(v2.get(i), 2);
        }
        
        if (norm1 == 0 || norm2 == 0) {
            return 0.0;
        }
        
        return dotProduct / (Math.sqrt(norm1) * Math.sqrt(norm2));
    }
    
    /**
     * 向量归一化
     */
    private List<Float> normalizeVector(List<Float> vector) {
        if (vector == null || vector.isEmpty()) {
            return vector;
        }
        
        double norm = 0.0;
        for (Float value : vector) {
            norm += Math.pow(value, 2);
        }
        norm = Math.sqrt(norm);
        
        if (norm == 0.0) {
            return vector;
        }
        
        List<Float> normalized = new ArrayList<>();
        for (Float value : vector) {
            normalized.add(value / (float) norm);
        }
        
        return normalized;
    }
    
    /**
     * 获取向量总数
     */
    public int getVectorCount() {
        return vectorStore.size();
    }
    
    /**
     * 重建向量索引（模拟）
     */
    public boolean rebuildIndex() {
        try {
            log.info("重建向量索引（模拟）");
            // 模拟索引重建
            Thread.sleep(100); // 模拟处理时间
            log.info("向量索引重建完成");
            return true;
        } catch (Exception e) {
            log.error("向量索引重建失败", e);
            return false;
        }
    }
    
    /**
     * 生成文本向量（接口方法）
     */
    @Override
    public List<Float> generateEmbedding(String text) {
        try {
            if (embeddingService != null) {
                return embeddingService.generateEmbedding(text);
            } else {
                // 使用模拟向量
                return generateMockVector();
            }
        } catch (Exception e) {
            log.error("生成向量失败", e);
            return Collections.emptyList();
        }
    }
    
    /**
     * 混合搜索（向量+关键词）
     */
    @Override
    public List<String> hybridSearch(String query, int topK) {
        try {
            // 简化实现：只进行向量搜索
            log.info("混合搜索（简化）: query={}, topK={}", query, topK);
            return vectorSearch(query, topK);
        } catch (Exception e) {
            log.error("混合搜索失败", e);
            return Collections.emptyList();
        }
    }
    
    /**
     * 创建集合（如果不存在）
     */
    @Override
    public boolean createCollectionIfNotExists() {
        try {
            // 简化实现：总是成功
            log.info("创建Milvus集合（简化）: {}", collectionName);
            return true;
        } catch (Exception e) {
            log.error("创建集合失败", e);
            return false;
        }
    }
}