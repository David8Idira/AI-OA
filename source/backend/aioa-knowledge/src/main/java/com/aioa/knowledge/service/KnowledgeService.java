package com.aioa.knowledge.service;

import com.aioa.knowledge.entity.KnowledgeDoc;

import java.util.List;
import java.util.Map;

/**
 * 知识库服务接口
 */
public interface KnowledgeService {
    
    /**
     * 搜索知识库
     * @param keyword 关键词
     * @return 搜索结果
     */
    List<KnowledgeDoc> search(String keyword);
    
    /**
     * 语义搜索(基于向量)
     * @param query 查询
     * @param topN 返回数量
     * @return 结果列表
     */
    List<KnowledgeDoc> semanticSearch(String query, int topN);
    
    /**
     * 创建文档
     * @param doc 文档
     * @return ID
     */
    Long createDoc(KnowledgeDoc doc);
    
    /**
     * 更新文档
     * @param doc 文档
     * @return 是否成功
     */
    boolean updateDoc(KnowledgeDoc doc);
    
    /**
     * 获取文档详情
     * @param id 文档ID
     * @return 文档
     */
    KnowledgeDoc getDoc(Long id);
    
    /**
     * 获取分类列表
     * @return 分类列表
     */
    List<Map<String, Object>> getCategories();
    
    /**
     * 统计知识库
     * @return 统计信息
     */
    Map<String, Object> getStatistics();
}