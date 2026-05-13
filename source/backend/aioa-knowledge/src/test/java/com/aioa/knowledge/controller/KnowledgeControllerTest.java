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

import java.util.List;

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

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(knowledgeController)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
        objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());
    }

    @Test
    @DisplayName("关键词搜索成功-验证服务调用")
    void search_verifyServiceCall() throws Exception {
        when(knowledgeService.search(anyString(), anyString(), anyInt()))
                .thenReturn(List.of());

        mockMvc.perform(get("/api/knowledge/search")
                        .param("keyword", "测试"))
                .andExpect(status().isOk());

        verify(knowledgeService, times(1)).search(anyString(), anyString(), anyInt());
    }

    @Test
    @DisplayName("语义搜索成功-验证服务调用")
    void semanticSearch_verifyServiceCall() throws Exception {
        when(knowledgeService.semanticSearch(anyString(), anyInt(), anyString(), anyInt()))
                .thenReturn(List.of());

        mockMvc.perform(get("/api/knowledge/semantic")
                        .param("query", "测试")
                        .param("topN", "5"))
                .andExpect(status().isOk());

        verify(knowledgeService, times(1)).semanticSearch(anyString(), anyInt(), anyString(), anyInt());
    }

    @Test
    @DisplayName("创建文档成功-验证服务调用")
    void createDoc_verifyServiceCall() throws Exception {
        when(knowledgeService.createDoc(any(KnowledgeDoc.class)))
                .thenReturn("doc-123");

        KnowledgeDoc doc = new KnowledgeDoc();
        doc.setTitle("测试");

        mockMvc.perform(post("/api/knowledge/doc")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(doc)))
                .andExpect(status().isOk());

        verify(knowledgeService, times(1)).createDoc(any(KnowledgeDoc.class));
    }

    @Test
    @DisplayName("获取文档-验证服务调用")
    void getDoc_verifyServiceCall() throws Exception {
        when(knowledgeService.getDoc(any()))
                .thenReturn(null);

        mockMvc.perform(get("/api/knowledge/doc/1"))
                .andExpect(status().isOk());

        verify(knowledgeService, times(1)).getDoc(any());
    }

    @Test
    @DisplayName("获取分类-验证服务调用")
    void getCategories_verifyServiceCall() throws Exception {
        when(knowledgeService.getCategories())
                .thenReturn(List.of());

        mockMvc.perform(get("/api/knowledge/categories"))
                .andExpect(status().isOk());

        verify(knowledgeService, times(1)).getCategories();
    }

    @Test
    @DisplayName("获取统计-验证服务调用")
    void getStats_verifyServiceCall() throws Exception {
        when(knowledgeService.getStatistics())
                .thenReturn(null);

        mockMvc.perform(get("/api/knowledge/stats"))
                .andExpect(status().isOk());

        verify(knowledgeService, times(1)).getStatistics();
    }

    @Test
    @DisplayName("RAG检索-验证服务调用")
    void ragRetrieve_verifyServiceCall() throws Exception {
        when(knowledgeService.search(anyString(), anyString(), anyInt()))
                .thenReturn(List.of());

        mockMvc.perform(get("/api/knowledge/rag")
                        .param("query", "测试"))
                .andExpect(status().isOk());

        verify(knowledgeService, times(1)).search(anyString(), anyString(), anyInt());
    }

    @Test
    @DisplayName("批量导入-验证服务调用")
    void batchImport_verifyServiceCall() throws Exception {
        when(knowledgeService.createDoc(any(KnowledgeDoc.class)))
                .thenReturn("doc-new");

        KnowledgeDoc doc = new KnowledgeDoc();
        doc.setTitle("测试");

        mockMvc.perform(post("/api/knowledge/batch-import")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(List.of(doc))))
                .andExpect(status().isOk());

        verify(knowledgeService, times(1)).createDoc(any(KnowledgeDoc.class));
    }
}