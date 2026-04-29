package com.aioa.knowledge.controller;

import com.aioa.knowledge.entity.KnowledgeDoc;
import com.aioa.knowledge.service.KnowledgeService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;
import java.util.Map;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(KnowledgeController.class)
class KnowledgeControllerTest {
    
    @Autowired
    private MockMvc mockMvc;
    
    @MockBean
    private KnowledgeService knowledgeService;
    
    @Test
    void testSearch() throws Exception {
        KnowledgeDoc doc = new KnowledgeDoc();
        doc.setId("1");
        doc.setTitle("测试文档");
        doc.setSummary("测试摘要");
        
        when(knowledgeService.search("测试")).thenReturn(List.of(doc));
        
        mockMvc.perform(get("/api/knowledge/search")
                .param("keyword", "测试"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));
    }
    
    @Test
    void testSemanticSearch() throws Exception {
        KnowledgeDoc doc = new KnowledgeDoc();
        doc.setId("2");
        doc.setTitle("AI入门");
        
        when(knowledgeService.semanticSearch("什么是AI", 5)).thenReturn(List.of(doc));
        
        mockMvc.perform(get("/api/knowledge/semantic")
                .param("query", "什么是AI")
                .param("topN", "5"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));
    }
    
    @Test
    void testCreateDoc() throws Exception {
        KnowledgeDoc doc = new KnowledgeDoc();
        doc.setTitle("新建文档");
        doc.setContent("内容");
        
        when(knowledgeService.createDoc(any(KnowledgeDoc.class))).thenReturn("doc-123");
        
        mockMvc.perform(post("/api/knowledge/doc")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"title\":\"新建文档\",\"content\":\"内容\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));
    }
    
    @Test
    void testGetDoc() throws Exception {
        KnowledgeDoc doc = new KnowledgeDoc();
        doc.setId("1");
        doc.setTitle("测试文档");
        
        when(knowledgeService.getDoc(1L)).thenReturn(doc);
        
        mockMvc.perform(get("/api/knowledge/doc/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));
    }
    
    @Test
    void testGetCategories() throws Exception {
        when(knowledgeService.getCategories()).thenReturn(List.of(
                Map.of("id", 1, "name", "技术"),
                Map.of("id", 2, "name", "产品")
        ));
        
        mockMvc.perform(get("/api/knowledge/categories"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));
    }
    
    @Test
    void testGetStats() throws Exception {
        when(knowledgeService.getStatistics()).thenReturn(Map.of(
                "totalDocs", 100,
                "totalCategories", 10
        ));
        
        mockMvc.perform(get("/api/knowledge/stats"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));
    }
}