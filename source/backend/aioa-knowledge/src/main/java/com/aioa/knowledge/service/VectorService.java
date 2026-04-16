package com.aioa.knowledge.service;

import com.aioa.knowledge.entity.KnowledgeDoc;

import java.util.List;

/**
 * 向量服务接口
 * 负责文本向量化、向量存储和向量搜索
 */
public interface VectorService {
    
    /**
     * 生成文本向量
     * @param text 文本内容
     * @return 向量数组
     */
    List<Float> generateEmbedding(String text);
    
    /**
     * 存储文档向量
     * @param doc 知识文档
     * @return 向量ID
     */
    String storeVector(KnowledgeDoc doc);
    
    /**
     * 更新文档向量
     * @param vectorId 向量ID
     * @param doc 更新后的文档
     * @return 是否成功
     */
    boolean updateVector(String vectorId, KnowledgeDoc doc);
    
    /**
     * 删除文档向量
     * @param vectorId 向量ID
     * @return 是否成功
     */
    boolean deleteVector(String vectorId);
    
    /**
     * 向量搜索
     * @param query 查询文本
     * @param topK 返回数量
     * @return 相关文档ID列表
     */
    List<String> vectorSearch(String query, int topK);
    
    /**
     * 混合搜索（向量+关键词）
     * @param query 查询文本
     * @param topK 返回数量
     * @return 相关文档ID列表
     */
    List<String> hybridSearch(String query, int topK);
    
    /**
     * 创建Milvus集合（如果不存在）
     * @return 是否成功
     */
    boolean createCollectionIfNotExists();
    
    /**
     * 健康检查
     * @return 服务是否正常
     */
    boolean healthCheck();
}