package com.aioa.knowledge.service.impl;

import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.aioa.knowledge.entity.KnowledgeDoc;
import com.aioa.knowledge.mapper.KnowledgeMapper;
import com.aioa.knowledge.service.KnowledgeService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

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
        
        // In production, this would call Milvus vector database for semantic search
        // For now, fallback to keyword search
        return search(query);
    }
    
    @Override
    public String createDoc(KnowledgeDoc doc) {
        if (doc.getViewCount() == null) doc.setViewCount(0);
        if (doc.getLikeCount() == null) doc.setLikeCount(0);
        if (doc.getStatus() == null) doc.setStatus("published");
        
        knowledgeMapper.insert(doc);
        log.info("创建知识文档: {}", doc.getId());
        
        return doc.getId();
    }
    
    @Override
    public boolean updateDoc(KnowledgeDoc doc) {
        if (doc.getId() == null) {
            return false;
        }
        
        int result = knowledgeMapper.updateById(doc);
        log.info("更新知识文档: {}, result={}", doc.getId(), result > 0);
        return result > 0;
    }
    
    @Override
    public KnowledgeDoc getDoc(Long id) {
        return knowledgeMapper.selectById(id);
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
