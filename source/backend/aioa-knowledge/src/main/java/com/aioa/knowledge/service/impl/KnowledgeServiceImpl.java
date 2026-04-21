package com.aioa.knowledge.service.impl;

import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.aioa.knowledge.entity.KnowledgeDoc;
import com.aioa.knowledge.mapper.KnowledgeMapper;
import com.aioa.knowledge.service.KnowledgeService;
import com.aioa.knowledge.service.VectorService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Knowledge Base Service Implementation with MyBatis Plus
 */
@Slf4j
@Service
public class KnowledgeServiceImpl implements KnowledgeService {
    
    @Autowired
    private KnowledgeMapper knowledgeMapper;
    
    @Autowired
    private VectorService vectorService;
    
    @Override
    public List<KnowledgeDoc> search(String keyword) {
        log.info("搜索知识库: {}", keyword);
        
        LambdaQueryWrapper<KnowledgeDoc> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(KnowledgeDoc::getStatus, "published")
               .and(w -> w.like(KnowledgeDoc::getTitle, keyword)
                          .or().like(KnowledgeDoc::getContent, keyword)
                          .or().like(KnowledgeDoc::getSummary, keyword))
               .orderByDesc(KnowledgeDoc::getViewCount);
        
        return knowledgeMapper.selectList(wrapper);
    }
    
    @Override
    public List<KnowledgeDoc> semanticSearch(String query, int topN) {
        log.info("语义搜索: {}, topN={}", query, topN);
        
        // 使用向量服务进行语义搜索
        List<String> vectorResults = vectorService.vectorSearch(query, topN);
        
        if (vectorResults.isEmpty()) {
            log.warn("向量搜索无结果，回退到关键词搜索");
            return search(query);
        }
        
        // 根据向量搜索结果获取文档详情
        List<KnowledgeDoc> results = new ArrayList<>();
        for (String vectorId : vectorResults) {
            // 这里简化处理，实际应该根据向量ID查询对应文档
            // 或者根据文档标题查询
            KnowledgeDoc doc = new KnowledgeDoc();
            doc.setTitle(vectorId); // 使用向量ID作为标题（演示用）
            doc.setContent("语义搜索结果: " + query);
            doc.setVectorId(vectorId);
            results.add(doc);
        }
        
        log.info("语义搜索完成，找到 {} 个结果", results.size());
        return results;
    }
    
    @Override
    @Transactional
    public String createDoc(KnowledgeDoc doc) {
        if (doc.getViewCount() == null) doc.setViewCount(0);
        if (doc.getLikeCount() == null) doc.setLikeCount(0);
        if (doc.getStatus() == null) doc.setStatus("published");
        
        // 保存文档到数据库
        knowledgeMapper.insert(doc);
        log.info("创建知识文档: {}", doc.getId());
        
        // 生成并存储向量
        if (doc.getContent() != null && !doc.getContent().trim().isEmpty()) {
            String vectorId = vectorService.storeVector(doc);
            if (vectorId != null) {
                doc.setVectorId(vectorId);
                // 更新文档的向量ID
                knowledgeMapper.updateById(doc);
                log.info("文档向量已存储: docId={}, vectorId={}", doc.getId(), vectorId);
            }
        }
        
        return doc.getId();
    }
    
    @Override
    @Transactional
    public boolean updateDoc(KnowledgeDoc doc) {
        if (doc.getId() == null) {
            return false;
        }
        
        // 获取原文档（用于获取向量ID）
        KnowledgeDoc originalDoc = knowledgeMapper.selectById(doc.getId());
        
        // 更新数据库
        int result = knowledgeMapper.updateById(doc);
        boolean dbSuccess = result > 0;
        log.info("更新知识文档: {}, result={}", doc.getId(), dbSuccess);
        
        // 更新向量（如果内容有变化）
        boolean vectorSuccess = true;
        if (originalDoc != null && originalDoc.getVectorId() != null && 
            doc.getContent() != null && !doc.getContent().equals(originalDoc.getContent())) {
            vectorSuccess = vectorService.updateVector(originalDoc.getVectorId(), doc);
            log.info("更新文档向量: docId={}, success={}", doc.getId(), vectorSuccess);
        }
        
        return dbSuccess && vectorSuccess;
    }
    
    @Override
    public KnowledgeDoc getDoc(Long id) {
        KnowledgeDoc doc = knowledgeMapper.selectById(id);
        if (doc != null) {
            // 增加浏览次数
            doc.setViewCount(doc.getViewCount() + 1);
            knowledgeMapper.updateById(doc);
        }
        return doc;
    }
    
    @Override
    public List<Map<String, Object>> getCategories() {
        // Query all published docs and group by categoryId
        LambdaQueryWrapper<KnowledgeDoc> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(KnowledgeDoc::getStatus, "published")
               .select(KnowledgeDoc::getCategoryId);
        
        List<KnowledgeDoc> docs = knowledgeMapper.selectList(wrapper);
        
        Map<Long, Long> categoryDocCount = docs.stream()
                .filter(d -> d.getCategoryId() != null)
                .collect(Collectors.groupingBy(KnowledgeDoc::getCategoryId, Collectors.counting()));
        
        List<Map<String, Object>> result = new ArrayList<>();
        categoryDocCount.forEach((categoryId, count) -> {
            Map<String, Object> cat = new HashMap<>();
            cat.put("id", categoryId);
            cat.put("name", "分类_" + categoryId);
            cat.put("docCount", count);
            result.add(cat);
        });
        
        return result;
    }
    
    @Override
    public Map<String, Object> getStatistics() {
        Map<String, Object> stats = new HashMap<>();
        
        LambdaQueryWrapper<KnowledgeDoc> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(KnowledgeDoc::getStatus, "published");
        List<KnowledgeDoc> docs = knowledgeMapper.selectList(wrapper);
        
        stats.put("totalDocs", docs.size());
        stats.put("totalViews", docs.stream().mapToInt(KnowledgeDoc::getViewCount).sum());
        stats.put("totalLikes", docs.stream().mapToInt(KnowledgeDoc::getLikeCount).sum());
        stats.put("categories", docs.stream()
                .map(KnowledgeDoc::getCategoryId)
                .filter(Objects::nonNull)
                .distinct()
                .count());
        
        return stats;
    }
    
    /**
     * RAG检索增强生成
     * @param query 用户查询
     * @return 增强的上下文
     */
    public String ragRetrieve(String query) {
        log.info("RAG检索: {}", query);
        
        // 使用向量搜索获取相关文档
        List<KnowledgeDoc> relevantDocs = semanticSearch(query, 5);
        
        if (relevantDocs.isEmpty()) {
            return "未找到相关信息";
        }
        
        // 构建上下文
        StringBuilder context = new StringBuilder();
        context.append("基于知识库的上下文信息:\n\n");
        
        for (int i = 0; i < relevantDocs.size(); i++) {
            KnowledgeDoc doc = relevantDocs.get(i);
            context.append(String.format("文档 %d: %s\n", i + 1, doc.getTitle()));
            if (doc.getSummary() != null) {
                context.append("摘要: ").append(doc.getSummary()).append("\n");
            }
            if (doc.getContent() != null && doc.getContent().length() > 200) {
                context.append("内容片段: ").append(doc.getContent().substring(0, 200)).append("...\n");
            }
            context.append("\n");
        }
        
        context.append("请基于以上上下文信息回答用户的问题。");
        
        return context.toString();
    }
    
    /**
     * 获取向量服务状态
     */
    public Map<String, Object> getVectorServiceStatus() {
        Map<String, Object> status = new HashMap<>();
        status.put("vectorServiceEnabled", vectorService != null);
        status.put("healthCheck", vectorService != null ? vectorService.healthCheck() : false);
        status.put("collectionExists", vectorService != null ? vectorService.createCollectionIfNotExists() : false);
        return status;
    }
    
    /**
     * Get paginated knowledge docs
     */
    public IPage<KnowledgeDoc> getPage(int pageNum, int pageSize, String keyword, String category) {
        Page<KnowledgeDoc> page = new Page<>(pageNum, pageSize);
        LambdaQueryWrapper<KnowledgeDoc> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(KnowledgeDoc::getStatus, "published");
        
        if (StrUtil.isNotBlank(keyword)) {
            wrapper.and(w -> w.like(KnowledgeDoc::getTitle, keyword)
                              .or().like(KnowledgeDoc::getSummary, keyword));
        }
        if (StrUtil.isNotBlank(category)) {
            wrapper.eq(KnowledgeDoc::getDocType, category);
        }
        
        wrapper.orderByDesc(KnowledgeDoc::getViewCount);
        return knowledgeMapper.selectPage(page, wrapper);
    }
}
