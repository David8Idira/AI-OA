package com.aioa.knowledge.service.impl;

import cn.hutool.core.util.IdUtil;
import com.aioa.knowledge.entity.KnowledgeDoc;
import com.aioa.knowledge.service.VectorService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * 简化版向量服务实现
 * 使用内存存储+关键词匹配实现语义搜索
 * 不依赖外部向量数据库（如Milvus）
 * 
 * 毛泽东思想：实事求是，简单实用
 */
@Slf4j
@Service
public class VectorServiceImpl implements VectorService {

    // 内存向量存储: vectorId -> VectorEntry
    private final Map<String, VectorEntry> vectorStore = new ConcurrentHashMap<>();
    
    // 文档向量ID映射: docId -> vectorId
    private final Map<String, String> docVectorMap = new ConcurrentHashMap<>();

    /**
     * 内部向量条目
     */
    private static class VectorEntry {
        String id;
        String docId;  // 修改为String，因为BaseEntity的id是String类型
        String title;
        String content;
        List<Float> embedding;
        long createTime;

        VectorEntry(String id, String docId, String title, String content, List<Float> embedding) {
            this.id = id;
            this.docId = docId;
            this.title = title;
            this.content = content;
            this.embedding = embedding;
            this.createTime = System.currentTimeMillis();
        }
    }

    @Override
    public List<Float> generateEmbedding(String text) {
        // 简化实现：生成伪向量
        // 实际应该调用EmbeddingService获取真实向量
        if (text == null || text.isEmpty()) {
            return Collections.emptyList();
        }
        
        // 生成一个简单的伪向量（基于文本长度和字符）
        List<Float> embedding = new ArrayList<>();
        int dim = 128; // 向量维度
        for (int i = 0; i < dim; i++) {
            float value = (float) (Math.random() * 2 - 1); // -1 到 1 之间
            embedding.add(value);
        }
        
        return embedding;
    }

    @Override
    public String storeVector(KnowledgeDoc doc) {
        if (doc == null || doc.getContent() == null) {
            log.error("文档内容为空，无法生成向量");
            return null;
        }
        
        String vectorId = IdUtil.fastSimpleUUID();
        
        try {
            // 生成向量
            List<Float> embedding = generateEmbedding(doc.getContent());
            
            // 存储向量
            VectorEntry entry = new VectorEntry(vectorId, doc.getId().toString(), doc.getTitle(), doc.getContent(), embedding);
            vectorStore.put(vectorId, entry);
            docVectorMap.put(doc.getId(), vectorId);
            
            log.info("向量存储成功: vectorId={}, docId={}", vectorId, doc.getId());
            return vectorId;
            
        } catch (Exception e) {
            log.error("向量存储异常", e);
            return null;
        }
    }

    @Override
    public boolean updateVector(String vectorId, KnowledgeDoc doc) {
        if (vectorId == null || doc == null) {
            return false;
        }
        
        try {
            // 删除旧向量
            deleteVector(vectorId);
            
            // 存储新向量
            String newVectorId = storeVector(doc);
            return newVectorId != null;
            
        } catch (Exception e) {
            log.error("向量更新异常", e);
            return false;
        }
    }

    @Override
    public boolean deleteVector(String vectorId) {
        if (vectorId == null) {
            return false;
        }
        
        try {
            VectorEntry entry = vectorStore.remove(vectorId);
            if (entry != null) {
                docVectorMap.remove(entry.docId);
                log.info("向量删除成功: vectorId={}", vectorId);
                return true;
            }
            return false;
            
        } catch (Exception e) {
            log.error("向量删除异常", e);
            return false;
        }
    }

    @Override
    public List<String> vectorSearch(String query, int topK) {
        if (query == null || query.trim().isEmpty()) {
            return new ArrayList<>();
        }
        
        try {
            // 简化实现：基于关键词匹配返回结果
            // 实际应该使用向量相似度搜索
            
            String[] keywords = query.toLowerCase().split("\\s+");
            
            // 计算每个向量的匹配分数
            List<Map.Entry<String, Double>> scores = new ArrayList<>();
            
            for (Map.Entry<String, VectorEntry> entry : vectorStore.entrySet()) {
                double score = calculateMatchScore(entry.getValue(), keywords);
                if (score > 0) {
                    scores.add(new AbstractMap.SimpleEntry<>(entry.getKey(), score));
                }
            }
            
            // 按分数排序
            scores.sort((a, b) -> Double.compare(b.getValue(), a.getValue()));
            
            // 取前topK个
            List<String> results = scores.stream()
                    .limit(topK)
                    .map(Map.Entry::getKey)
                    .collect(Collectors.toList());
            
            log.info("向量搜索完成: query='{}', topK={}, 结果数={}", query, topK, results.size());
            return results;
            
        } catch (Exception e) {
            log.error("向量搜索异常", e);
            return new ArrayList<>();
        }
    }

    /**
     * 计算匹配分数
     */
    private double calculateMatchScore(VectorEntry entry, String[] keywords) {
        double score = 0;
        String titleLower = entry.title != null ? entry.title.toLowerCase() : "";
        String contentLower = entry.content != null ? entry.content.toLowerCase() : "";
        
        for (String keyword : keywords) {
            // 标题匹配权重更高
            if (titleLower.contains(keyword)) {
                score += 2.0;
            }
            if (contentLower.contains(keyword)) {
                score += 1.0;
            }
        }
        
        return score;
    }

    @Override
    public List<String> hybridSearch(String query, int topK) {
        // 混合搜索：结合向量搜索和关键词搜索
        // 这里简化实现，直接使用向量搜索
        return vectorSearch(query, topK);
    }

    @Override
    public boolean createCollectionIfNotExists() {
        // 内存存储不需要创建集合
        log.info("向量集合初始化完成，存储类型: 内存");
        return true;
    }

    @Override
    public boolean healthCheck() {
        try {
            log.info("向量服务健康检查: 内存存储, 向量数={}", vectorStore.size());
            return true;
        } catch (Exception e) {
            log.error("向量服务健康检查失败", e);
            return false;
        }
    }

    /**
     * 获取集合统计信息
     */
    public Map<String, Object> getCollectionStats() {
        Map<String, Object> stats = new HashMap<>();
        stats.put("vectorCount", vectorStore.size());
        stats.put("docCount", docVectorMap.size());
        stats.put("storageType", "memory");
        stats.put("status", "healthy");
        stats.put("embeddingService", "simple");
        return stats;
    }

    /**
     * 批量存储向量
     */
    public List<String> batchStoreVectors(List<KnowledgeDoc> docs) {
        if (docs == null || docs.isEmpty()) {
            return new ArrayList<>();
        }
        
        List<String> vectorIds = new ArrayList<>();
        for (KnowledgeDoc doc : docs) {
            String vectorId = storeVector(doc);
            if (vectorId != null) {
                vectorIds.add(vectorId);
            }
        }
        
        log.info("批量存储向量完成，成功数: {}/{}", vectorIds.size(), docs.size());
        return vectorIds;
    }
}