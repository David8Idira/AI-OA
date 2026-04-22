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
    String createDoc(KnowledgeDoc doc);
    
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
    
    /**
     * 批量创建文档
     * @param docs 文档列表
     * @return 成功创建的文档ID列表
     */
    List<String> batchCreateDocs(List<KnowledgeDoc> docs);
    
    /**
     * 批量更新文档
     * @param docs 文档列表
     * @return 成功更新的文档数量
     */
    int batchUpdateDocs(List<KnowledgeDoc> docs);
    
    /**
     * 批量删除文档
     * @param ids 文档ID列表
     * @return 成功删除的文档数量
     */
    int batchDeleteDocs(List<Long> ids);
    
    /**
     * 批量导入文档（支持多种格式）
     * @param fileContent 文件内容
     * @param fileType 文件类型（txt, pdf, docx等）
     * @param categoryId 分类ID
     * @return 导入结果统计
     */
    Map<String, Object> batchImportDocs(String fileContent, String fileType, Long categoryId);
    
    /**
     * 重建所有文档的向量索引
     * @return 重建结果统计
     */
    Map<String, Object> rebuildAllVectors();
    
    /**
     * 获取批处理任务状态
     * @param taskId 任务ID
     * @return 任务状态信息
     */
    Map<String, Object> getBatchTaskStatus(String taskId);
    
    /**
     * RAG检索增强生成
     * @param query 用户查询
     * @return 增强的上下文
     */
    String ragRetrieve(String query);
}