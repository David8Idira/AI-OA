package com.aioa.knowledge.entity;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * KnowledgeCategory Entity 单元测试
 * 毛泽东思想指导：实事求是，测试知识分类实体
 */
@DisplayName("KnowledgeCategoryTest 知识分类实体测试")
class KnowledgeCategoryTest {

    @Test
    @DisplayName("创建知识分类实体")
    void createKnowledgeCategory() {
        // given
        KnowledgeCategory category = new KnowledgeCategory();
        category.setId(1L);
        category.setName("技术文档");
        category.setParentId(0L);
        category.setSort(1);
        category.setStatus("active");

        // then
        assertThat(category.getId()).isEqualTo(1L);
        assertThat(category.getName()).isEqualTo("技术文档");
        assertThat(category.getSort()).isEqualTo(1);
    }

    @Test
    @DisplayName("设置和获取ID")
    void setAndGetId() {
        // given
        KnowledgeCategory category = new KnowledgeCategory();

        // when
        category.setId(99L);

        // then
        assertThat(category.getId()).isEqualTo(99L);
    }

    @Test
    @DisplayName("设置和获取分类名称")
    void setAndGetName() {
        // given
        KnowledgeCategory category = new KnowledgeCategory();

        // when
        category.setName("测试分类");

        // then
        assertThat(category.getName()).isEqualTo("测试分类");
    }

    @Test
    @DisplayName("设置和获取父分类ID")
    void setAndGetParentId() {
        // given
        KnowledgeCategory category = new KnowledgeCategory();

        // when
        category.setParentId(1L);

        // then
        assertThat(category.getParentId()).isEqualTo(1L);
    }

    @Test
    @DisplayName("设置和获取排序")
    void setAndGetSort() {
        // given
        KnowledgeCategory category = new KnowledgeCategory();

        // when
        category.setSort(99);

        // then
        assertThat(category.getSort()).isEqualTo(99);
    }

    @Test
    @DisplayName("设置和获取状态")
    void setAndGetStatus() {
        // given
        KnowledgeCategory category = new KnowledgeCategory();

        // when
        category.setStatus("inactive");

        // then
        assertThat(category.getStatus()).isEqualTo("inactive");
    }
}