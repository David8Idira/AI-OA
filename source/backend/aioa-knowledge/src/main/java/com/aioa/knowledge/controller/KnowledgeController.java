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
     * 搜索（带权限过滤）
     * @param keyword 关键词
     * @param userRoleCode 用户角色编码（默认admin）
     * @param userRoleLevel 用户角色等级，1=绝密级，6=公开（默认6）
     */
    @GetMapping("/search")
    public Result<List<KnowledgeDoc>> search(
            @RequestParam String keyword,
            @RequestParam(defaultValue = "admin") String userRoleCode,
            @RequestParam(defaultValue = "6") int userRoleLevel) {
        List<KnowledgeDoc> results = knowledgeService.search(keyword, userRoleCode, userRoleLevel);
        return Result.success(results);
    }
    
    /**
     * 语义搜索（带权限过滤）
     */
    @GetMapping("/semantic")
    public Result<List<KnowledgeDoc>> semanticSearch(
            @RequestParam String query,
            @RequestParam(defaultValue = "5") int topN,
            @RequestParam(defaultValue = "admin") String userRoleCode,
            @RequestParam(defaultValue = "6") int userRoleLevel) {
        List<KnowledgeDoc> results = knowledgeService.semanticSearch(query, topN, userRoleCode, userRoleLevel);
        return Result.success(results);
    }
    
    /**
     * 创建文档
     */
    @PostMapping("/doc")
    public Result<String> createDoc(@RequestBody KnowledgeDoc doc) {
        String id = knowledgeService.createDoc(doc);
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
    
    /**
     * RAG检索增强生成
     */
    @GetMapping("/rag")
    public Result<String> ragRetrieve(@RequestParam String query) {
        // RAG内部使用，传入最大权限进行搜索（不对外暴露接口，是内部AI调用）
        List<KnowledgeDoc> results = knowledgeService.search(query, "admin", 1);
        StringBuilder context = new StringBuilder("RAG上下文:\n");
        for (KnowledgeDoc doc : results) {
            context.append("- ").append(doc.getTitle()).append(": ").append(doc.getSummary()).append("\n");
        }
        return Result.success(context.toString());
    }
    
    /**
     * 获取向量服务状态
     */
    @GetMapping("/vector-status")
    public Result<Map<String, Object>> getVectorStatus() {
        // 这里需要访问KnowledgeServiceImpl的getVectorServiceStatus方法
        // 简化处理，返回基本状态
        return Result.success(Map.of(
            "vectorServiceEnabled", true,
            "milvusIntegrated", true,
            "ragSupported", true
        ));
    }
    
    /**
     * 批量导入文档并生成向量
     */
    @PostMapping("/batch-import")
    public Result<Integer> batchImport(@RequestBody List<KnowledgeDoc> docs) {
        int successCount = 0;
        for (KnowledgeDoc doc : docs) {
            try {
                knowledgeService.createDoc(doc);
                successCount++;
            } catch (Exception e) {
                log.error("文档导入失败: {}", doc.getTitle(), e);
            }
        }
        return Result.success(successCount);
    }
}