package com.aioa.knowledge.entity;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * KnowledgeDoc Entity 单元测试
 * 毛泽东思想指导：实事求是，测试知识文档实体
 */
@DisplayName("KnowledgeDocTest 知识文档实体测试")
class KnowledgeDocTest {

    private KnowledgeDoc createTestDoc() {
        KnowledgeDoc doc = new KnowledgeDoc();
        doc.setId("doc-001");
        doc.setTitle("测试文档");
        doc.setContent("这是测试内容");
        doc.setCategoryId(1L);
        doc.setStatus("published");
        doc.setViewCount(0);
        doc.setLikeCount(0);
        doc.setCreateTime(LocalDateTime.now());
        return doc;
    }

    @Test
    @DisplayName("创建知识文档实体")
    void createKnowledgeDoc() {
        // when
        KnowledgeDoc doc = createTestDoc();

        // then
        assertThat(doc.getId()).isEqualTo("doc-001");
        assertThat(doc.getTitle()).isEqualTo("测试文档");
        assertThat(doc.getStatus()).isEqualTo("published");
    }

    @Test
    @DisplayName("设置和获取ID")
    void setAndGetId() {
        // given
        KnowledgeDoc doc = new KnowledgeDoc();

        // when
        doc.setId("test-id");

        // then
        assertThat(doc.getId()).isEqualTo("test-id");
    }

    @Test
    @DisplayName("设置和获取标题")
    void setAndGetTitle() {
        // given
        KnowledgeDoc doc = new KnowledgeDoc();

        // when
        doc.setTitle("测试标题");

        // then
        assertThat(doc.getTitle()).isEqualTo("测试标题");
    }

    @Test
    @DisplayName("设置和获取内容")
    void setAndGetContent() {
        // given
        KnowledgeDoc doc = new KnowledgeDoc();

        // when
        doc.setContent("测试内容正文");

        // then
        assertThat(doc.getContent()).isEqualTo("测试内容正文");
    }

    @Test
    @DisplayName("设置和获取分类ID")
    void setAndGetCategoryId() {
        // given
        KnowledgeDoc doc = new KnowledgeDoc();

        // when
        doc.setCategoryId(1L);

        // then
        assertThat(doc.getCategoryId()).isEqualTo(1L);
    }

    @Test
    @DisplayName("设置和获取状态")
    void setAndGetStatus() {
        // given
        KnowledgeDoc doc = new KnowledgeDoc();

        // when
        doc.setStatus("draft");

        // then
        assertThat(doc.getStatus()).isEqualTo("draft");
    }

    @Test
    @DisplayName("设置和获取浏览数")
    void setAndGetViewCount() {
        // given
        KnowledgeDoc doc = new KnowledgeDoc();

        // when
        doc.setViewCount(100);

        // then
        assertThat(doc.getViewCount()).isEqualTo(100);
    }

    @Test
    @DisplayName("设置和获取点赞数")
    void setAndGetLikeCount() {
        // given
        KnowledgeDoc doc = new KnowledgeDoc();

        // when
        doc.setLikeCount(50);

        // then
        assertThat(doc.getLikeCount()).isEqualTo(50);
    }

    @Test
    @DisplayName("增加浏览数")
    void incrementViewCount() {
        // given
        KnowledgeDoc doc = createTestDoc();
        int initialCount = doc.getViewCount();

        // when
        doc.setViewCount(initialCount + 1);

        // then
        assertThat(doc.getViewCount()).isEqualTo(initialCount + 1);
    }

    @Test
    @DisplayName("增加点赞数")
    void incrementLikeCount() {
        // given
        KnowledgeDoc doc = createTestDoc();
        int initialCount = doc.getLikeCount();

        // when
        doc.setLikeCount(initialCount + 1);

        // then
        assertThat(doc.getLikeCount()).isEqualTo(initialCount + 1);
    }
}