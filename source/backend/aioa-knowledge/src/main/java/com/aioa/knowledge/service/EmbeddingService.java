package com.aioa.knowledge.service;

import java.util.List;

/**
 * 向量化服务接口
 */
public interface EmbeddingService {
    
    /**
     * 生成单个文本的向量
     */
    List<Float> generateEmbedding(String text);
    
    /**
     * 批量生成文本向量
     */
    List<List<Float>> batchGenerateEmbedding(List<String> texts);
}
