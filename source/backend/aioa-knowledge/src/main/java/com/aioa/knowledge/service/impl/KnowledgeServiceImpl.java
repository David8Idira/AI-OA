package com.aioa.knowledge.service.impl;

import cn.hutool.core.util.IdUtil;
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
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
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
    @CacheEvict(value = {"knowledge:docs", "knowledge:stats", "knowledge:categories"}, allEntries = true)
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
        
        // 清除相关缓存
        clearSearchCache();
        
        return doc.getId();
    }
    
    @Override
    @Transactional
    @CacheEvict(value = {"knowledge:docs", "knowledge:stats", "knowledge:search"}, key = "#doc.id")
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
        
        // 清除相关缓存
        clearSearchCache();
        
        return dbSuccess && vectorSuccess;
    }
    
    @Override
    @Cacheable(value = "knowledge:docs", key = "#id", unless = "#result == null")
    public KnowledgeDoc getDoc(Long id) {
        KnowledgeDoc doc = knowledgeMapper.selectById(id);
        if (doc != null) {
            // 增加浏览次数
            doc.setViewCount(doc.getViewCount() + 1);
            knowledgeMapper.updateById(doc);
            
            // 清除缓存以更新浏览次数
            evictDocCache(id);
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
    
    @Override
    @Transactional
    public List<String> batchCreateDocs(List<KnowledgeDoc> docs) {
        if (docs == null || docs.isEmpty()) {
            return new ArrayList<>();
        }
        
        List<String> createdIds = new ArrayList<>();
        int successCount = 0;
        int failCount = 0;
        
        for (KnowledgeDoc doc : docs) {
            try {
                String docId = createDoc(doc);
                if (docId != null) {
                    createdIds.add(docId);
                    successCount++;
                } else {
                    failCount++;
                }
            } catch (Exception e) {
                log.error("批量创建文档失败: {}", e.getMessage());
                failCount++;
            }
        }
        
        log.info("批量创建文档完成: 成功 {} 个, 失败 {} 个", successCount, failCount);
        return createdIds;
    }
    
    @Override
    @Transactional
    public int batchUpdateDocs(List<KnowledgeDoc> docs) {
        if (docs == null || docs.isEmpty()) {
            return 0;
        }
        
        int successCount = 0;
        for (KnowledgeDoc doc : docs) {
            try {
                if (updateDoc(doc)) {
                    successCount++;
                }
            } catch (Exception e) {
                log.error("批量更新文档失败: docId={}, {}", doc.getId(), e.getMessage());
            }
        }
        
        log.info("批量更新文档完成: 成功 {} 个, 总共 {} 个", successCount, docs.size());
        return successCount;
    }
    
    @Override
    @Transactional
    public int batchDeleteDocs(List<Long> ids) {
        if (ids == null || ids.isEmpty()) {
            return 0;
        }
        
        int successCount = 0;
        for (Long id : ids) {
            try {
                // 获取文档以获取向量ID
                KnowledgeDoc doc = knowledgeMapper.selectById(id);
                if (doc != null && doc.getVectorId() != null) {
                    // 删除向量
                    vectorService.deleteVector(doc.getVectorId());
                }
                
                // 删除数据库记录
                int result = knowledgeMapper.deleteById(id);
                if (result > 0) {
                    successCount++;
                }
            } catch (Exception e) {
                log.error("批量删除文档失败: docId={}, {}", id, e.getMessage());
            }
        }
        
        log.info("批量删除文档完成: 成功 {} 个, 总共 {} 个", successCount, ids.size());
        return successCount;
    }
    
    @Override
    public Map<String, Object> batchImportDocs(String fileContent, String fileType, Long categoryId) {
        Map<String, Object> result = new HashMap<>();
        result.put("taskId", IdUtil.fastSimpleUUID());
        result.put("startTime", new Date());
        
        try {
            List<KnowledgeDoc> docs = new ArrayList<>();
            
            switch (fileType.toLowerCase()) {
                case "txt":
                    // 处理TXT文件：按行分割
                    String[] lines = fileContent.split("\\r?\\n");
                    int lineNum = 1;
                    for (String line : lines) {
                        if (!line.trim().isEmpty()) {
                            KnowledgeDoc doc = new KnowledgeDoc();
                            doc.setTitle("导入文档_" + lineNum);
                            doc.setContent(line);
                            doc.setCategoryId(categoryId);
                            doc.setStatus("published");
                            docs.add(doc);
                            lineNum++;
                        }
                    }
                    break;
                case "csv":
                    // 处理CSV文件：按逗号分割
                    String[] csvLines = fileContent.split("\\r?\\n");
                    if (csvLines.length > 0) {
                        // 跳过表头
                        for (int i = 1; i < csvLines.length; i++) {
                            String[] fields = csvLines[i].split(",");
                            if (fields.length >= 2) {
                                KnowledgeDoc doc = new KnowledgeDoc();
                                doc.setTitle(fields[0].trim());
                                doc.setContent(fields[1].trim());
                                if (fields.length >= 3) {
                                    doc.setSummary(fields[2].trim());
                                }
                                doc.setCategoryId(categoryId);
                                doc.setStatus("published");
                                docs.add(doc);
                            }
                        }
                    }
                    break;
                default:
                    // 默认按文本处理
                    KnowledgeDoc doc = new KnowledgeDoc();
                    doc.setTitle("导入文档");
                    doc.setContent(fileContent);
                    doc.setCategoryId(categoryId);
                    doc.setStatus("published");
                    docs.add(doc);
                    break;
            }
            
            // 批量创建文档
            List<String> createdIds = batchCreateDocs(docs);
            
            result.put("status", "completed");
            result.put("totalDocs", docs.size());
            result.put("successCount", createdIds.size());
            result.put("failCount", docs.size() - createdIds.size());
            result.put("endTime", new Date());
            
        } catch (Exception e) {
            log.error("批量导入文档失败: {}", e.getMessage(), e);
            result.put("status", "failed");
            result.put("error", e.getMessage());
            result.put("endTime", new Date());
        }
        
        return result;
    }
    
    @Override
    public Map<String, Object> rebuildAllVectors() {
        Map<String, Object> result = new HashMap<>();
        String taskId = IdUtil.fastSimpleUUID();
        
        result.put("taskId", taskId);
        result.put("startTime", new Date());
        
        try {
            // 获取所有已发布的文档
            LambdaQueryWrapper<KnowledgeDoc> wrapper = new LambdaQueryWrapper<>();
            wrapper.eq(KnowledgeDoc::getStatus, "published");
            List<KnowledgeDoc> docs = knowledgeMapper.selectList(wrapper);
            
            int total = docs.size();
            int success = 0;
            int fail = 0;
            
            for (KnowledgeDoc doc : docs) {
                try {
                    // 重新生成向量
                    String vectorId = vectorService.storeVector(doc);
                    if (vectorId != null) {
                        doc.setVectorId(vectorId);
                        knowledgeMapper.updateById(doc);
                        success++;
                    } else {
                        fail++;
                    }
                } catch (Exception e) {
                    log.error("重建向量失败: docId={}, {}", doc.getId(), e.getMessage());
                    fail++;
                }
            }
            
            result.put("status", "completed");
            result.put("totalDocs", total);
            result.put("successCount", success);
            result.put("failCount", fail);
            result.put("endTime", new Date());
            
            log.info("重建所有向量完成: 总共 {} 个, 成功 {} 个, 失败 {} 个", total, success, fail);
            
        } catch (Exception e) {
            log.error("重建向量任务失败: {}", e.getMessage(), e);
            result.put("status", "failed");
            result.put("error", e.getMessage());
            result.put("endTime", new Date());
        }
        
        return result;
    }
    
    @Override
    public Map<String, Object> getBatchTaskStatus(String taskId) {
        Map<String, Object> status = new HashMap<>();
        status.put("taskId", taskId);
        status.put("status", "completed"); // 简化实现
        status.put("timestamp", new Date());
        
        // 在实际应用中，这里应该查询任务队列或数据库中的任务状态
        // 这里简化返回完成状态
        
        return status;
    }
    
    /**
     * 清除文档缓存
     */
    @CacheEvict(value = "knowledge:docs", key = "#id")
    public void evictDocCache(Long id) {
        log.debug("清除文档缓存: id={}", id);
    }
    
    /**
     * 清除搜索缓存
     */
    @CacheEvict(value = "knowledge:search", allEntries = true)
    public void clearSearchCache() {
        log.debug("清除搜索缓存");
    }
    
    /**
     * 清除统计缓存
     */
    @CacheEvict(value = "knowledge:stats", allEntries = true)
    public void clearStatsCache() {
        log.debug("清除统计缓存");
    }
    
    /**
     * 缓存优化：预加载热门文档
     */
    public void preloadHotDocs(int limit) {
        LambdaQueryWrapper<KnowledgeDoc> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(KnowledgeDoc::getStatus, "published")
               .orderByDesc(KnowledgeDoc::getViewCount)
               .last("LIMIT " + limit);
        
        List<KnowledgeDoc> hotDocs = knowledgeMapper.selectList(wrapper);
        log.info("预加载 {} 个热门文档到缓存", hotDocs.size());
        
        // 预加载到缓存
        for (KnowledgeDoc doc : hotDocs) {
            // 这里可以调用缓存服务进行预加载
            log.debug("预加载文档到缓存: id={}, title={}", doc.getId(), doc.getTitle());
        }
    }
    
    /**
     * 获取缓存状态信息
     */
    public Map<String, Object> getCacheStatus() {
        Map<String, Object> status = new HashMap<>();
        
        // 获取文档数量
        LambdaQueryWrapper<KnowledgeDoc> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(KnowledgeDoc::getStatus, "published");
        long totalDocs = knowledgeMapper.selectCount(wrapper);
        
        status.put("totalDocs", totalDocs);
        status.put("cacheEnabled", true);
        status.put("cacheStrategy", "L1(Caffeine) + L2(Redis)");
        status.put("cacheTime", new Date());
        
        return status;
    }
    
    /**
     * RAG检索增强生成
     * @param query 用户查询
     * @return 增强的上下文
     */
    @Cacheable(value = "knowledge:search", key = "#query", unless = "#result == null || #result.contains('未找到')")
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
