package com.aioa.knowledge.service;

import com.aioa.knowledge.entity.KnowledgeDoc;
import com.aioa.knowledge.mapper.KnowledgeMapper;
import com.aioa.knowledge.service.impl.KnowledgeServiceImpl;
import com.aioa.knowledge.service.VectorService;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.InjectMocks;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

/**
 * KnowledgeServiceImpl 单元测试
 * 毛泽东思想指导：实事求是，测试知识库服务
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("KnowledgeServiceImpl 单元测试")
class KnowledgeServiceImplTest {

    @Mock
    private KnowledgeMapper knowledgeMapper;

    @Mock
    private VectorService vectorService;

    @InjectMocks
    private KnowledgeServiceImpl knowledgeService;

    private KnowledgeDoc createTestDoc() {
        KnowledgeDoc doc = new KnowledgeDoc();
        doc.setId("doc-001");
        doc.setTitle("测试文档");
        doc.setContent("这是测试文档的内容");
        doc.setStatus("published");
        doc.setViewCount(0);
        doc.setLikeCount(0);
        return doc;
    }

    @Test
    @DisplayName("搜索知识库 - 正常场景")
    void search_shouldReturnResults() {
        // given
        KnowledgeDoc doc = createTestDoc();
        when(knowledgeMapper.selectList(any(LambdaQueryWrapper.class))).thenReturn(List.of(doc));

        // when
        List<KnowledgeDoc> results = knowledgeService.search("测试");

        // then
        assertThat(results).hasSize(1);
        assertThat(results.get(0).getTitle()).isEqualTo("测试文档");
    }

    @Test
    @DisplayName("搜索知识库 - 无结果")
    void search_withNoMatch_shouldReturnEmptyList() {
        // given
        when(knowledgeMapper.selectList(any(LambdaQueryWrapper.class))).thenReturn(List.of());

        // when
        List<KnowledgeDoc> results = knowledgeService.search("不存在");

        // then
        assertThat(results).isEmpty();
    }

    @Test
    @DisplayName("语义搜索 - 向量搜索有结果")
    void semanticSearch_withVectorResults_shouldReturnDocs() {
        // given
        when(vectorService.vectorSearch(anyString(), anyInt())).thenReturn(List.of("doc-1", "doc-2"));

        // when
        List<KnowledgeDoc> results = knowledgeService.semanticSearch("测试查询", 5);

        // then
        assertThat(results).isNotNull();
        verify(vectorService, times(1)).vectorSearch("测试查询", 5);
    }

    @Test
    @DisplayName("语义搜索 - 向量搜索无结果回退到关键词搜索")
    void semanticSearch_withNoVectorResults_shouldFallbackToKeywordSearch() {
        // given
        when(vectorService.vectorSearch(anyString(), anyInt())).thenReturn(List.of());
        KnowledgeDoc doc = createTestDoc();
        when(knowledgeMapper.selectList(any(LambdaQueryWrapper.class))).thenReturn(List.of(doc));

        // when
        List<KnowledgeDoc> results = knowledgeService.semanticSearch("测试", 5);

        // then
        assertThat(results).hasSize(1);
    }

    @Test
    @DisplayName("获取文档详情 - 正常场景")
    void getDoc_withValidId_shouldReturnDoc() {
        // given
        KnowledgeDoc doc = createTestDoc();
        when(knowledgeMapper.selectById(1L)).thenReturn(doc);

        // when
        KnowledgeDoc result = knowledgeService.getDoc(1L);

        // then
        assertThat(result).isNotNull();
        assertThat(result.getTitle()).isEqualTo("测试文档");
    }

    @Test
    @DisplayName("获取文档详情 - 不存在")
    void getDoc_withInvalidId_shouldReturnNull() {
        // given
        when(knowledgeMapper.selectById(999L)).thenReturn(null);

        // when
        KnowledgeDoc result = knowledgeService.getDoc(999L);

        // then
        assertThat(result).isNull();
    }

    @Test
    @DisplayName("统计知识库 - 正常场景")
    void getStatistics_shouldReturnStats() {
        // when
        Map<String, Object> result = knowledgeService.getStatistics();

        // then
        assertThat(result).isNotNull();
    }
}