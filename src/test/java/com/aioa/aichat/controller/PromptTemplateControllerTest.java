package com.aioa.aichat.controller;

import com.aioa.aichat.dto.PromptTemplateDTO;
import com.aioa.aichat.dto.PromptTemplateResponseDTO;
import com.aioa.aichat.service.PromptTemplateService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(PromptTemplateController.class)
class PromptTemplateControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private PromptTemplateService templateService;

    @Test
    void testCreate_Success_ReturnsCreated() throws Exception {
        PromptTemplateResponseDTO response = new PromptTemplateResponseDTO();
        response.setId(1L);
        response.setName("测试模板");
        response.setType("chat");
        response.setCreateTime("2024-01-01 10:00:00");

        when(templateService.create(any(PromptTemplateDTO.class))).thenReturn(response);

        mockMvc.perform(post("/chat/templates")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"name\":\"测试模板\",\"type\":\"chat\",\"template\":\"内容\"}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.name").value("测试模板"));
    }

    @Test
    void testCreate_WithValidName_ReturnsCreated() throws Exception {
        PromptTemplateResponseDTO response = new PromptTemplateResponseDTO();
        response.setId(2L);
        response.setName("模板2");

        when(templateService.create(any(PromptTemplateDTO.class))).thenReturn(response);

        mockMvc.perform(post("/chat/templates")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"name\":\"模板2\",\"type\":\"chat\",\"template\":\"test\"}"))
                .andExpect(status().isCreated());
    }

    @Test
    void testUpdate_Success_ReturnsOk() throws Exception {
        PromptTemplateResponseDTO response = new PromptTemplateResponseDTO();
        response.setId(1L);
        response.setName("更新后模板");
        response.setType("chat");

        when(templateService.update(eq(1L), any(PromptTemplateDTO.class))).thenReturn(response);

        mockMvc.perform(put("/chat/templates/1")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"name\":\"更新后模板\",\"type\":\"chat\",\"template\":\"新内容\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("更新后模板"));
    }

    @Test
    void testUpdate_NotFound_ReturnsOk() throws Exception {
        when(templateService.update(eq(999L), any(PromptTemplateDTO.class))).thenReturn(null);

        mockMvc.perform(put("/chat/templates/999")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"name\":\"更新\",\"type\":\"chat\",\"template\":\"test\"}"))
                .andExpect(status().isOk());
    }

    @Test
    void testDelete_Success_ReturnsNoContent() throws Exception {
        doNothing().when(templateService).delete(1L);

        mockMvc.perform(delete("/chat/templates/1"))
                .andExpect(status().isNoContent());

        verify(templateService).delete(1L);
    }

    @Test
    void testGetById_Found_ReturnsOk() throws Exception {
        PromptTemplateResponseDTO response = new PromptTemplateResponseDTO();
        response.setId(1L);
        response.setName("模板1");

        when(templateService.getById(1L)).thenReturn(response);

        mockMvc.perform(get("/chat/templates/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1));
    }

    @Test
    void testGetById_NotFound_ReturnsOk() throws Exception {
        when(templateService.getById(999L)).thenReturn(null);

        mockMvc.perform(get("/chat/templates/999"))
                .andExpect(status().isOk());
    }

    @Test
    void testGetAll_ReturnsList() throws Exception {
        PromptTemplateResponseDTO t1 = new PromptTemplateResponseDTO();
        t1.setId(1L);
        t1.setName("模板1");

        PromptTemplateResponseDTO t2 = new PromptTemplateResponseDTO();
        t2.setId(2L);
        t2.setName("模板2");

        when(templateService.getAll()).thenReturn(List.of(t1, t2));

        mockMvc.perform(get("/chat/templates"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2));
    }

    @Test
    void testGetByType_ReturnsFilteredList() throws Exception {
        PromptTemplateResponseDTO response = new PromptTemplateResponseDTO();
        response.setId(1L);
        response.setType("workflow");

        when(templateService.getByType("workflow")).thenReturn(List.of(response));

        mockMvc.perform(get("/chat/templates/type/workflow"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].type").value("workflow"));
    }
}
