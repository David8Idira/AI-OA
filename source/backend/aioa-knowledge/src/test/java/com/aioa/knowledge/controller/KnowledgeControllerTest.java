package com.aioa.knowledge.controller;

import com.aioa.common.exception.GlobalExceptionHandler;
import com.aioa.knowledge.entity.KnowledgeDoc;
import com.aioa.knowledge.service.KnowledgeService;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * KnowledgeController 单元测试
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("KnowledgeControllerTest 知识库控制器测试")
class KnowledgeControllerTest {

    private MockMvc mockMvc;
    private ObjectMapper objectMapper;

    @Mock
    private KnowledgeService knowledgeService;

    @InjectMocks
    private KnowledgeController knowledgeController;

    private KnowledgeDoc testDoc;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(knowledgeController)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
        objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());

        testDoc = new KnowledgeDoc();
        // ID由BaseEntity管理，无需设置
        testDoc.setTitle("测试文档");
        testDoc.setContent("这是测试内容");
        testDoc.setSummary("测试摘要");
        testDoc.setSecurityLevel("public");
        testDoc.setStatus("published");
    }

    // ==================== 搜索 ====================

    @Test
    @DisplayName("关键词搜索成功")
    void search_success() throws Exception {
        when(knowledgeService.search(anyString(), anyString(), anyInt()))
                .thenReturn(List.of(testDoc));

        mockMvc.perform(get("/api/knowledge/search")
                        .param("keyword", "测试"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data[0].title").value("测试文档"))
                .andExpect(jsonPath("$.code").value(200));

        verify(knowledgeService, times(1)).search("测试", "admin", 6);
    }

    @Test
    @DisplayName("关键词搜索为空结果")
    void search_emptyResult() throws Exception {
        when(knowledgeService.search(anyString(), anyString(), anyInt()))
                .thenReturn(List.of());

        mockMvc.perform(get("/api/knowledge/search")
                        .param("keyword", "nonexistent"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.length()").value(0));
    }

    @Test
    @DisplayName("关键词搜索-指定用户角色")
    void search_withUserRole() throws Exception {
        when(knowledgeService.search(eq("文档"), eq("editor"), eq(4)))
                .thenReturn(List.of());

        mockMvc.perform(get("/api/knowledge/search")
                        .param("keyword", "文档")
                        .param("userRoleCode", "editor")
                        .param("userRoleLevel", "4"))
                .andExpect(status().isOk());

        verify(knowledgeService, times(1)).search("文档", "editor", 4);
    }

    // ==================== 语义搜索 ====================

    @Test
    @DisplayName("语义搜索成功")
    void semanticSearch_success() throws Exception {
        when(knowledgeService.semanticSearch(anyString(), anyInt(), anyString(), anyInt()))
                .thenReturn(List.of(testDoc));

        mockMvc.perform(get("/api/knowledge/semantic")
                        .param("query", "如何实现")
                        .param("topN", "5"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data[0].title").value("测试文档"));

        verify(knowledgeService, times(1)).semanticSearch("如何实现", 5, "admin", 6);
    }

    @Test
    @DisplayName("语义搜索-自定义topN")
    void semanticSearch_customTopN() throws Exception {
        when(knowledgeService.semanticSearch(anyString(), eq(10), anyString(), anyInt()))
                .thenReturn(List.of());

        mockMvc.perform(get("/api/knowledge/semantic")
                        .param("query", "测试查询")
                        .param("topN", "10")
                        .param("userRoleCode", "admin")
                        .param("userRoleLevel", "6"))
                .andExpect(status().isOk());

        verify(knowledgeService, times(1)).semanticSearch("测试查询", 10, "admin", 6);
    }

    // ==================== 创建文档 ====================

    @Test
    @DisplayName("创建文档成功")
    void createDoc_success() throws Exception {
        when(knowledgeService.createDoc(any(KnowledgeDoc.class)))
                .thenReturn("doc-123");

        mockMvc.perform(post("/api/knowledge/doc")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(testDoc)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data").value("doc-123"));

        verify(knowledgeService, times(1)).createDoc(any(KnowledgeDoc.class));
    }

    @Test
    @DisplayName("创建文档失败")
    void createDoc_failure() throws Exception {
        when(knowledgeService.createDoc(any(KnowledgeDoc.class)))
                .thenThrow(new RuntimeException("Database error"));

        mockMvc.perform(post("/api/knowledge/doc")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(testDoc)))
                .andExpect(status().is5xxServerError())
                .andExpect(jsonPath("$.code").value(500));
    }

    // ==================== 获取文档 ====================

    @Test
    @DisplayName("获取文档成功")
    void getDoc_success() throws Exception {
        when(knowledgeService.getDoc(1L))
                .thenReturn(testDoc);

        mockMvc.perform(get("/api/knowledge/doc/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.title").value("测试文档"))
                .andExpect(jsonPath("$.code").value(200));

        verify(knowledgeService, times(1)).getDoc(1L);
    }

    @Test
    @DisplayName("获取文档-文档不存在")
    void getDoc_notFound() throws Exception {
        when(knowledgeService.getDoc(999L))
                .thenReturn(null);

        mockMvc.perform(get("/api/knowledge/doc/999"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data").doesNotExist());
    }

    // ==================== 获取分类 ====================

    @Test
    @DisplayName("获取分类成功")
    void getCategories_success() throws Exception {
        Map<String, Object> category = new HashMap<>();
        category.put("id", 1);
        category.put("name", "技术文档");
        category.put("count", 10);

        when(knowledgeService.getCategories())
                .thenReturn(List.of(category));

        mockMvc.perform(get("/api/knowledge/categories"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data[0].name").value("技术文档"))
                .andExpect(jsonPath("$.code").value(200));

        verify(knowledgeService, times(1)).getCategories();
    }

    // ==================== 统计 ====================

    @Test
    @DisplayName("获取统计信息成功")
    void getStats_success() throws Exception {
        Map<String, Object> stats = new HashMap<>();
        stats.put("totalDocs", 100);
        stats.put("totalCategories", 5);
        stats.put("totalViews", 5000);

        when(knowledgeService.getStatistics())
                .thenReturn(stats);

        mockMvc.perform(get("/api/knowledge/stats"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.totalDocs").value(100))
                .andExpect(jsonPath("$.code").value(200));

        verify(knowledgeService, times(1)).getStatistics();
    }

    // ==================== RAG检索 ====================

    @Test
    @DisplayName("RAG检索成功")
    void ragRetrieve_success() throws Exception {
        when(knowledgeService.search(eq("AI是什么"), eq("admin"), eq(1)))
                .thenReturn(List.of(testDoc));

        mockMvc.perform(get("/api/knowledge/rag")
                        .param("query", "AI是什么"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data").value(org.hamcrest.Matchers.containsString("RAG上下文")))
                .andExpect(jsonPath("$.data").value(org.hamcrest.Matchers.containsString("测试文档")));

        verify(knowledgeService, times(1)).search("AI是什么", "admin", 1);
    }

    // ==================== 向量服务状态 ====================

    @Test
    @DisplayName("获取向量服务状态")
    void getVectorStatus_success() throws Exception {
        mockMvc.perform(get("/api/knowledge/vector-status"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.vectorServiceEnabled").value(true))
                .andExpect(jsonPath("$.data.milvusIntegrated").value(true));
    }

    // ==================== 批量导入 ====================

    @Test
    @DisplayName("批量导入成功")
    void batchImport_success() throws Exception {
        when(knowledgeService.createDoc(any(KnowledgeDoc.class)))
                .thenReturn("doc-new")
                .thenReturn("doc-new-2");

        List<KnowledgeDoc> docs = Arrays.asList(testDoc, testDoc);
        mockMvc.perform(post("/api/knowledge/batch-import")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(docs)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data").value(2));
    }

    @Test
    @DisplayName("批量导入-部分失败")
    void batchImport_partialFailure() throws Exception {
        when(knowledgeService.createDoc(any(KnowledgeDoc.class)))
                .thenReturn("doc-1")
                .thenThrow(new RuntimeException("DB error"))
                .thenReturn("doc-3");

        List<KnowledgeDoc> docs = Arrays.asList(testDoc, testDoc, testDoc);
        mockMvc.perform(post("/api/knowledge/batch-import")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(docs)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data").value(2));
    }

    // ==================== 异常场景 ====================

    @Test
    @DisplayName("搜索-服务异常")
    void search_serviceException() throws Exception {
        when(knowledgeService.search(anyString(), anyString(), anyInt()))
                .thenThrow(new RuntimeException("Search engine unavailable"));

        mockMvc.perform(get("/api/knowledge/search")
                        .param("keyword", "测试"))
                .andExpect(status().is5xxServerError())
                .andExpect(jsonPath("$.code").value(500));
    }
}