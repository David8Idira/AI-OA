package com.aioa.knowledge.controller;

import com.aioa.knowledge.entity.KnowledgeDoc;
import com.aioa.knowledge.service.KnowledgeService;
import com.aioa.common.result.Result;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * 知识库控制器
 */
@Slf4j
@RestController
@RequestMapping("/api/knowledge")
public class KnowledgeController {
    
    @Autowired
    private KnowledgeService knowledgeService;
    
    /**
     * 搜索
     */
    @GetMapping("/search")
    public Result<List<KnowledgeDoc>> search(@RequestParam String keyword) {
        List<KnowledgeDoc> results = knowledgeService.search(keyword);
        return Result.success(results);
    }
    
    /**
     * 语义搜索
     */
    @GetMapping("/semantic")
    public Result<List<KnowledgeDoc>> semanticSearch(
            @RequestParam String query,
            @RequestParam(defaultValue = "5") int topN) {
        List<KnowledgeDoc> results = knowledgeService.semanticSearch(query, topN);
        return Result.success(results);
    }
    
    /**
     * 创建文档
     */
    @PostMapping("/doc")
    public Result<Long> createDoc(@RequestBody KnowledgeDoc doc) {
        Long id = knowledgeService.createDoc(doc);
        return Result.success(id);
    }
    
    /**
     * 获取文档
     */
    @GetMapping("/doc/{id}")
    public Result<KnowledgeDoc> getDoc(@PathVariable Long id) {
        return Result.success(knowledgeService.getDoc(id));
    }
    
    /**
     * 获取分类
     */
    @GetMapping("/categories")
    public Result<List<Map<String, Object>>> getCategories() {
        return Result.success(knowledgeService.getCategories());
    }
    
    /**
     * 统计
     */
    @GetMapping("/stats")
    public Result<Map<String, Object>> getStats() {
        return Result.success(knowledgeService.getStatistics());
    }
}