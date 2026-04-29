package com.aioa.aichat.controller;

import com.aioa.aichat.dto.ChatSessionDTO;
import com.aioa.aichat.dto.ChatSessionResponseDTO;
import com.aioa.aichat.service.ChatSessionService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(ChatSessionController.class)
class ChatSessionControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private ChatSessionService sessionService;

    @Test
    void testCreateSession_Success_ReturnsCreated() throws Exception {
        ChatSessionResponseDTO response = new ChatSessionResponseDTO();
        response.setId(1L);
        response.setSessionId("sess-001");
        response.setTitle("新会话");
        response.setCreateTime("2024-01-01 10:00:00");

        when(sessionService.createSession(any(ChatSessionDTO.class))).thenReturn(response);

        mockMvc.perform(post("/chat/sessions")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"userId\":1,\"title\":\"新会话\"}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.sessionId").value("sess-001"));
    }

    @Test
    void testCreateSession_WithEmptyTitle_ReturnsCreated() throws Exception {
        ChatSessionResponseDTO response = new ChatSessionResponseDTO();
        response.setId(1L);
        response.setTitle("");

        when(sessionService.createSession(any(ChatSessionDTO.class))).thenReturn(response);

        mockMvc.perform(post("/chat/sessions")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"userId\":1,\"title\":\"\"}"))
                .andExpect(status().isCreated());
    }

    @Test
    void testGetSession_Found_ReturnsOk() throws Exception {
        ChatSessionResponseDTO response = new ChatSessionResponseDTO();
        response.setId(1L);
        response.setSessionId("sess-001");

        when(sessionService.getSessionById(1L)).thenReturn(response);

        mockMvc.perform(get("/chat/sessions/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1));
    }

    @Test
    void testGetSession_NotFound_ReturnsOk() throws Exception {
        when(sessionService.getSessionById(999L)).thenReturn(null);

        mockMvc.perform(get("/chat/sessions/999"))
                .andExpect(status().isOk());
    }

    @Test
    void testGetSessionBySid_Success_ReturnsOk() throws Exception {
        ChatSessionResponseDTO response = new ChatSessionResponseDTO();
        response.setId(1L);
        response.setSessionId("sess-001");

        when(sessionService.getSessionBySessionId("sess-001")).thenReturn(response);

        mockMvc.perform(get("/chat/sessions/sid/sess-001"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.sessionId").value("sess-001"));
    }

    @Test
    void testGetUserSessions_ReturnsList() throws Exception {
        ChatSessionResponseDTO session1 = new ChatSessionResponseDTO();
        session1.setId(1L);
        session1.setUserId(100L);

        ChatSessionResponseDTO session2 = new ChatSessionResponseDTO();
        session2.setId(2L);
        session2.setUserId(100L);

        when(sessionService.getUserSessions(100L)).thenReturn(List.of(session1, session2));

        mockMvc.perform(get("/chat/sessions/user/100"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2));
    }

    @Test
    void testGetUserSessions_EmptyList_ReturnsOk() throws Exception {
        when(sessionService.getUserSessions(100L)).thenReturn(List.of());

        mockMvc.perform(get("/chat/sessions/user/100"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(0));
    }

    @Test
    void testCloseSession_Success_ReturnsOk() throws Exception {
        doNothing().when(sessionService).closeSession(1L);

        mockMvc.perform(put("/chat/sessions/1/close"))
                .andExpect(status().isOk());

        verify(sessionService).closeSession(1L);
    }

    @Test
    void testDeleteSession_Success_ReturnsNoContent() throws Exception {
        doNothing().when(sessionService).deleteSession(1L);

        mockMvc.perform(delete("/chat/sessions/1"))
                .andExpect(status().isNoContent());

        verify(sessionService).deleteSession(1L);
    }
}
