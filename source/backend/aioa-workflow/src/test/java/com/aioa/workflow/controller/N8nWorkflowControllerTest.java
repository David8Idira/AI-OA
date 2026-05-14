package com.aioa.workflow.controller;

import com.aioa.workflow.dto.N8nWorkflowDTO;
import com.aioa.workflow.service.N8nWorkflowService;
import com.fasterxml.jackson.databind.ObjectMapper;
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

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * N8nWorkflowController测试
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("N8nWorkflowControllerTest n8n工作流控制器测试")
class N8nWorkflowControllerTest {

    private MockMvc mockMvc;
    private ObjectMapper objectMapper;

    @Mock
    private N8nWorkflowService n8nWorkflowService;

    @InjectMocks
    private N8nWorkflowController n8nWorkflowController;

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper();
        mockMvc = MockMvcBuilders.standaloneSetup(n8nWorkflowController).build();
    }

    @Test
    @DisplayName("注册工作流成功")
    void register_success() throws Exception {
        N8nWorkflowDTO dto = new N8nWorkflowDTO();
        dto.setWorkflowId("wf-001");
        dto.setName("测试工作流");
        dto.setType("approval");
        dto.setWebhookUrl("https://n8n.example.com/webhook/test");

        when(n8nWorkflowService.registerWorkflow(any(N8nWorkflowDTO.class))).thenReturn(true);

        mockMvc.perform(post("/api/workflow/n8n/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.code").value(200));
    }

    @Test
    @DisplayName("注册工作流失败")
    void register_fail() throws Exception {
        N8nWorkflowDTO dto = new N8nWorkflowDTO();
        dto.setWorkflowId("wf-001");
        dto.setName("测试工作流");

        when(n8nWorkflowService.registerWorkflow(any(N8nWorkflowDTO.class))).thenReturn(false);

        mockMvc.perform(post("/api/workflow/n8n/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(500))
                .andExpect(jsonPath("$.code").value(500));
    }

    @Test
    @DisplayName("触发工作流成功")
    void trigger_success() throws Exception {
        when(n8nWorkflowService.triggerWorkflow(anyString(), any())).thenReturn("triggered successfully");

        mockMvc.perform(post("/api/workflow/n8n/trigger/wf-001")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"key\":\"value\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data").value("triggered successfully"));
    }

    @Test
    @DisplayName("触发工作流-无请求体")
    void trigger_noBody() throws Exception {
        when(n8nWorkflowService.triggerWorkflow(eq("wf-002"), any())).thenReturn("triggered");

        mockMvc.perform(post("/api/workflow/n8n/trigger/wf-002")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data").value("triggered"));
    }

    @Test
    @DisplayName("获取工作流状态成功")
    void getStatus_success() throws Exception {
        when(n8nWorkflowService.getWorkflowStatus("wf-001")).thenReturn("active");

        mockMvc.perform(get("/api/workflow/n8n/status/wf-001"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data").value("active"));
    }

    @Test
    @DisplayName("获取工作流状态-不存在")
    void getStatus_notFound() throws Exception {
        when(n8nWorkflowService.getWorkflowStatus("not-exist")).thenReturn("not_found");

        mockMvc.perform(get("/api/workflow/n8n/status/not-exist"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data").value("not_found"));
    }

    @Test
    @DisplayName("测试Webhook成功")
    void testWebhook_success() throws Exception {
        when(n8nWorkflowService.sendWebhook(anyString(), any())).thenReturn("webhook response");

        mockMvc.perform(post("/api/workflow/n8n/webhook/test")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"test\":true}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data").value("webhook response"));
    }

    @Test
    @DisplayName("测试Webhook-复杂数据")
    void testWebhook_complexData() throws Exception {
        String complexJson = "{\"approvalId\":\"ap-001\",\"action\":\"approve\",\"data\":{\"amount\":1000}}";
        when(n8nWorkflowService.sendWebhook(anyString(), any())).thenReturn("success");

        mockMvc.perform(post("/api/workflow/n8n/webhook/test")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(complexJson))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));
    }
}