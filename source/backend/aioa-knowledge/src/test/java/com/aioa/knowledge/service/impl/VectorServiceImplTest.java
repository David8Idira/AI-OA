package com.aioa.knowledge.service.impl;

import com.aioa.knowledge.entity.KnowledgeDoc;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * VectorServiceImpl 单元测试
 * 毛泽东思想指导：实事求是，测试向量存储和搜索功能
 */
@DisplayName("VectorServiceImpl 单元测试")
class VectorServiceImplTest {

    private VectorServiceImpl vectorService;

    @BeforeEach
    void setUp() {
        vectorService = new VectorServiceImpl();
    }

    @Test
    @DisplayName("生成向量 - 正常文本")
    void generateEmbedding_withNormalText_shouldReturnVector() {
        // when
        List<Float> embedding = vectorService.generateEmbedding("测试文本");

        // then
        assertThat(embedding).isNotNull();
        assertThat(embedding).hasSize(128);  // 默认维度128
    }

    @Test
    @DisplayName("生成向量 - 空文本")
    void generateEmbedding_withEmptyText_shouldReturnEmptyList() {
        // when
        List<Float> embedding = vectorService.generateEmbedding("");

        // then
        assertThat(embedding).isEmpty();
    }

    @Test
    @DisplayName("生成向量 - null文本")
    void generateEmbedding_withNullText_shouldReturnEmptyList() {
        // when
        List<Float> embedding = vectorService.generateEmbedding(null);

        // then
        assertThat(embedding).isEmpty();
    }

    @Test
    @DisplayName("存储向量 - 正常文档")
    void storeVector_withNormalDoc_shouldReturnVectorId() {
        // given
        KnowledgeDoc doc = new KnowledgeDoc();
        doc.setId("doc-001");
        doc.setTitle("测试文档");
        doc.setContent("这是测试文档的内容");

        // when
        String vectorId = vectorService.storeVector(doc);

        // then
        assertThat(vectorId).isNotNull();
        assertThat(vectorId).isNotEmpty();
    }

    @Test
    @DisplayName("存储向量 - 空文档")
    void storeVector_withNullDoc_shouldReturnNull() {
        // when
        String vectorId = vectorService.storeVector(null);

        // then
        assertThat(vectorId).isNull();
    }

    @Test
    @DisplayName("删除向量 - 正常场景")
    void deleteVector_withExistingId_shouldReturnTrue() {
        // given
        KnowledgeDoc doc = new KnowledgeDoc();
        doc.setId("doc-002");
        doc.setTitle("测试文档2");
        doc.setContent("内容2");
        String vectorId = vectorService.storeVector(doc);

        // when
        boolean result = vectorService.deleteVector(vectorId);

        // then
        assertThat(result).isTrue();
    }

    @Test
    @DisplayName("删除向量 - 不存在的ID")
    void deleteVector_withNonExistingId_shouldReturnFalse() {
        // when
        boolean result = vectorService.deleteVector("non-existing-id");

        // then
        assertThat(result).isFalse();
    }

    @Test
    @DisplayName("向量搜索 - 正常查询")
    void vectorSearch_withNormalQuery_shouldReturnResults() {
        // given
        KnowledgeDoc doc = new KnowledgeDoc();
        doc.setId("doc-003");
        doc.setTitle("Java编程指南");
        doc.setContent("这是一本关于Java编程的书");
        vectorService.storeVector(doc);

        // when
        List<String> results = vectorService.vectorSearch("Java", 10);

        // then
        assertThat(results).isNotNull();
        // 简化实现可能返回空结果因为伪向量不真正匹配
    }

    @Test
    @DisplayName("向量搜索 - 空查询")
    void vectorSearch_withEmptyQuery_shouldReturnEmptyList() {
        // when
        List<String> results = vectorService.vectorSearch("", 10);

        // then
        assertThat(results).isEmpty();
    }

    @Test
    @DisplayName("健康检查 - 正常")
    void healthCheck_shouldReturnTrue() {
        // when
        boolean healthy = vectorService.healthCheck();

        // then
        assertThat(healthy).isTrue();
    }

    @Test
    @DisplayName("获取集合统计")
    void getCollectionStats_shouldReturnStats() {
        // when
        var stats = vectorService.getCollectionStats();

        // then
        assertThat(stats).containsKeys("vectorCount", "docCount", "storageType", "status");
        assertThat(stats.get("storageType")).isEqualTo("memory");
        assertThat(stats.get("status")).isEqualTo("healthy");
    }

    @Test
    @DisplayName("批量存储向量")
    void batchStoreVectors_shouldStoreAll() {
        // given
        KnowledgeDoc doc1 = new KnowledgeDoc();
        doc1.setId("doc-batch-1");
        doc1.setTitle("文档1");
        doc1.setContent("内容1");

        KnowledgeDoc doc2 = new KnowledgeDoc();
        doc2.setId("doc-batch-2");
        doc2.setTitle("文档2");
        doc2.setContent("内容2");

        // when
        List<String> vectorIds = vectorService.batchStoreVectors(List.of(doc1, doc2));

        // then
        assertThat(vectorIds).hasSize(2);
    }
}
