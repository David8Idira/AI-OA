package com.aioa.knowledge.service.impl;

import com.aioa.knowledge.entity.KnowledgeDoc;
import com.aioa.knowledge.service.KnowledgeService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.*;

/**
 * 知识库服务实现
 */
@Slf4j
@Service
public class KnowledgeServiceImpl implements KnowledgeService {
    
    // 模拟存储
    private final Map<Long, KnowledgeDoc> docs = new HashMap<>();
    private final Map<Long, String> categories = new HashMap<>();
    private long idGenerator = 1;
    
    public KnowledgeServiceImpl() {
        // 初始化示例数据
        initSampleData();
    }
    
    private void initSampleData() {
        KnowledgeDoc doc1 = new KnowledgeDoc();
        doc1.setId(1L);
        doc1.setTitle("AI-OA使用手册");
        doc1.setContent("AI-OA系统是一站式智能办公平台...");
        doc1.setSummary("智能办公平台使用指南");
        doc1.setDocType("manual");
        doc1.setStatus("published");
        doc1.setViewCount(100);
        doc1.setLikeCount(10);
        docs.put(1L, doc1);
        
        categories.put(1L, "使用指南");
        categories.put(2L, "常见问题");
        idGenerator = 2;
    }
    
    @Override
    public List<KnowledgeDoc> search(String keyword) {
        log.info("搜索知识库: {}", keyword);
        
        List<KnowledgeDoc> results = new ArrayList<>();
        for (KnowledgeDoc doc : docs.values()) {
            if (matchKeyword(doc, keyword)) {
                results.add(doc);
            }
        }
        
        return results;
    }
    
    @Override
    public List<KnowledgeDoc> semanticSearch(String query, int topN) {
        log.info("语义搜索: {}, topN={}", query, topN);
        
        // 这里调用向量数据库(生产环境)
        // 简化为关键词匹配
        List<KnowledgeDoc> results = new ArrayList<>(docs.values());
        results.sort((a, b) -> b.getViewCount() - a.getViewCount());
        
        return results.subList(0, Math.min(topN, results.size()));
    }
    
    @Override
    public Long createDoc(KnowledgeDoc doc) {
        Long id = idGenerator++;
        doc.setId(id);
        doc.setViewCount(0);
        doc.setLikeCount(0);
        doc.setStatus("published");
        
        docs.put(id, doc);
        log.info("创建知识文档: {}", id);
        
        return id;
    }
    
    @Override
    public boolean updateDoc(KnowledgeDoc doc) {
        if (doc.getId() == null || !docs.containsKey(doc.getId())) {
            return false;
        }
        
        docs.put(doc.getId(), doc);
        log.info("更新知识文档: {}", doc.getId());
        return true;
    }
    
    @Override
    public KnowledgeDoc getDoc(Long id) {
        return docs.get(id);
    }
    
    @Override
    public List<Map<String, Object>> getCategories() {
        List<Map<String, Object>> result = new ArrayList<>();
        categories.forEach((id, name) -> {
            Map<String, Object> cat = new HashMap<>();
            cat.put("id", id);
            cat.put("name", name);
            cat.put("docCount", docs.values().stream()
                .filter(d -> d.getCategoryId() != null && d.getCategoryId() == id)
                .count());
            result.add(cat);
        });
        return result;
    }
    
    @Override
    public Map<String, Object> getStatistics() {
        Map<String, Object> stats = new HashMap<>();
        stats.put("totalDocs", docs.size());
        stats.put("totalViews", docs.values().stream()
            .mapToInt(KnowledgeDoc::getViewCount).sum());
        stats.put("totalLikes", docs.values().stream()
            .mapToInt(KnowledgeDoc::getLikeCount).sum());
        stats.put("categories", categories.size());
        return stats;
    }
    
    private boolean matchKeyword(KnowledgeDoc doc, String keyword) {
        if (doc == null || keyword == null) return false;
        
        String lower = keyword.toLowerCase();
        return doc.getTitle() != null && doc.getTitle().toLowerCase().contains(lower)
            || doc.getContent() != null && doc.getContent().toLowerCase().contains(lower)
            || doc.getSummary() != null && doc.getSummary().toLowerCase().contains(lower);
    }
}