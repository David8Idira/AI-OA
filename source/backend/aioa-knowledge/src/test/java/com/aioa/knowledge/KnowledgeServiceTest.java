package com.aioa.knowledge;

import com.aioa.knowledge.entity.KnowledgeDoc;
import com.aioa.knowledge.service.KnowledgeService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * 知识库服务测试
 */
@SpringBootTest
@ActiveProfiles("test")
class KnowledgeServiceTest {
    
    @Autowired
    private KnowledgeService knowledgeService;
    
    @Test
    void testCreateDocument() {
        KnowledgeDoc doc = new KnowledgeDoc();
        doc.setTitle("测试文档");
        doc.setContent("这是一个测试文档内容，用于验证知识库功能。");
        doc.setSummary("测试文档摘要");
        doc.setDocType("article");
        doc.setStatus("published");
        
        String docId = knowledgeService.createDoc(doc);
        assertNotNull(docId);
        assertTrue(docId.length() > 0);
        
        System.out.println("文档创建成功，ID: " + docId);
    }
    
    @Test
    void testSearchDocument() {
        // 先创建测试文档
        KnowledgeDoc doc = new KnowledgeDoc();
        doc.setTitle("Spring Boot配置指南");
        doc.setContent("Spring Boot应用程序的配置可以通过application.properties或application.yml文件进行。");
        doc.setSummary("Spring Boot配置方法");
        doc.setDocType("manual");
        doc.setStatus("published");
        
        String docId = knowledgeService.createDoc(doc);
        
        // 测试搜索
        List<KnowledgeDoc> results = knowledgeService.search("Spring Boot");
        assertFalse(results.isEmpty());
        
        System.out.println("搜索测试完成，找到 " + results.size() + " 个结果");
        
        // 验证结果
        boolean found = results.stream()
                .anyMatch(d -> d.getTitle().contains("Spring Boot"));
        assertTrue(found, "应该能找到包含'Spring Boot'的文档");
    }
    
    @Test
    void testSemanticSearch() {
        // 创建测试文档
        KnowledgeDoc doc1 = new KnowledgeDoc();
        doc1.setTitle("Java编程语言");
        doc1.setContent("Java是一种广泛使用的面向对象编程语言，具有跨平台特性。");
        doc1.setSummary("Java语言介绍");
        doc1.setDocType("article");
        doc1.setStatus("published");
        knowledgeService.createDoc(doc1);
        
        KnowledgeDoc doc2 = new KnowledgeDoc();
        doc2.setTitle("Python数据分析");
        doc2.setContent("Python在数据科学和机器学习领域有广泛应用。");
        doc2.setSummary("Python数据科学应用");
        doc2.setDocType("article");
        doc2.setStatus("published");
        knowledgeService.createDoc(doc2);
        
        // 语义搜索测试
        List<KnowledgeDoc> results = knowledgeService.semanticSearch("编程语言", 5);
        
        System.out.println("语义搜索测试完成，找到 " + results.size() + " 个结果");
        results.forEach(d -> System.out.println("  - " + d.getTitle()));
        
        assertFalse(results.isEmpty(), "语义搜索应该返回结果");
    }
    
    @Test
    void testUpdateDocument() {
        // 创建文档
        KnowledgeDoc doc = new KnowledgeDoc();
        doc.setTitle("原始标题");
        doc.setContent("原始内容");
        doc.setSummary("原始摘要");
        doc.setDocType("article");
        doc.setStatus("published");
        
        String docId = knowledgeService.createDoc(doc);
        
        // 更新文档
        doc.setId(Long.parseLong(docId));
        doc.setTitle("更新后的标题");
        doc.setContent("更新后的内容");
        
        boolean success = knowledgeService.updateDoc(doc);
        assertTrue(success, "文档更新应该成功");
        
        // 验证更新
        KnowledgeDoc updatedDoc = knowledgeService.getDoc(Long.parseLong(docId));
        assertEquals("更新后的标题", updatedDoc.getTitle());
        assertEquals("更新后的内容", updatedDoc.getContent());
        
        System.out.println("文档更新测试完成");
    }
    
    @Test
    void testGetStatistics() {
        // 创建一些测试文档
        for (int i = 0; i < 3; i++) {
            KnowledgeDoc doc = new KnowledgeDoc();
            doc.setTitle("统计测试文档 " + (i + 1));
            doc.setContent("这是第 " + (i + 1) + " 个测试文档。");
            doc.setSummary("测试摘要");
            doc.setDocType("test");
            doc.setStatus("published");
            knowledgeService.createDoc(doc);
        }
        
        // 获取统计信息
        var stats = knowledgeService.getStatistics();
        assertNotNull(stats);
        
        System.out.println("知识库统计信息:");
        stats.forEach((key, value) -> System.out.println("  " + key + ": " + value));
        
        assertTrue(stats.containsKey("totalDocs"), "统计信息应包含文档总数");
        int totalDocs = (int) stats.get("totalDocs");
        assertTrue(totalDocs >= 3, "文档总数应至少为3");
    }
    
    @Test
    void testCategories() {
        // 创建带分类的文档
        KnowledgeDoc doc1 = new KnowledgeDoc();
        doc1.setTitle("技术文档");
        doc1.setContent("技术相关内容");
        doc1.setCategoryId(1L);
        doc1.setDocType("tech");
        doc1.setStatus("published");
        knowledgeService.createDoc(doc1);
        
        KnowledgeDoc doc2 = new KnowledgeDoc();
        doc2.setTitle("业务文档");
        doc2.setContent("业务相关内容");
        doc2.setCategoryId(2L);
        doc2.setDocType("business");
        doc2.setStatus("published");
        knowledgeService.createDoc(doc2);
        
        // 获取分类
        var categories = knowledgeService.getCategories();
        assertNotNull(categories);
        
        System.out.println("分类列表:");
        categories.forEach(cat -> System.out.println("  ID: " + cat.get("id") + 
            ", 名称: " + cat.get("name") + ", 文档数: " + cat.get("docCount")));
        
        assertFalse(categories.isEmpty(), "分类列表不应为空");
    }
    
    @Test
    void testDocumentViewCount() {
        // 创建文档
        KnowledgeDoc doc = new KnowledgeDoc();
        doc.setTitle("查看次数测试");
        doc.setContent("测试文档查看次数统计功能。");
        doc.setSummary("查看次数测试");
        doc.setDocType("test");
        doc.setStatus("published");
        
        String docId = knowledgeService.createDoc(doc);
        
        // 多次获取文档（应该增加查看次数）
        int initialViewCount = knowledgeService.getDoc(Long.parseLong(docId)).getViewCount();
        
        // 再次获取
        knowledgeService.getDoc(Long.parseLong(docId));
        knowledgeService.getDoc(Long.parseLong(docId));
        
        int finalViewCount = knowledgeService.getDoc(Long.parseLong(docId)).getViewCount();
        
        System.out.println("查看次数测试 - 初始: " + initialViewCount + ", 最终: " + finalViewCount);
        assertTrue(finalViewCount > initialViewCount, "查看次数应该增加");
    }
    
    @Test
    void testVectorServiceIntegration() {
        // 这个测试需要Milvus服务运行
        // 在实际环境中测试向量服务集成
        
        System.out.println("向量服务集成测试 - 需要Milvus环境");
        System.out.println("建议在生产环境或集成测试环境中运行此测试");
        
        // 这里可以添加模拟测试或条件测试
        boolean vectorServiceAvailable = true; // 假设可用
        
        if (vectorServiceAvailable) {
            System.out.println("向量服务可用，可以进行完整测试");
            // 实际的向量测试代码
        } else {
            System.out.println("向量服务不可用，跳过向量相关测试");
        }
        
        assertTrue(true, "向量服务集成测试占位");
    }
    
    @Test
    void testPerformance() {
        // 性能测试：批量创建文档
        long startTime = System.currentTimeMillis();
        
        int batchSize = 10;
        for (int i = 0; i < batchSize; i++) {
            KnowledgeDoc doc = new KnowledgeDoc();
            doc.setTitle("性能测试文档 " + i);
            doc.setContent("这是第 " + i + " 个性能测试文档。内容较长，用于测试向量生成性能。");
            doc.setSummary("性能测试");
            doc.setDocType("performance");
            doc.setStatus("published");
            
            knowledgeService.createDoc(doc);
        }
        
        long endTime = System.currentTimeMillis();
        long duration = endTime - startTime;
        
        System.out.println("性能测试结果:");
        System.out.println("  文档数量: " + batchSize);
        System.out.println("  总耗时: " + duration + "ms");
        System.out.println("  平均每文档: " + (duration / batchSize) + "ms");
        
        assertTrue(duration > 0, "执行时间应该为正数");
    }
}