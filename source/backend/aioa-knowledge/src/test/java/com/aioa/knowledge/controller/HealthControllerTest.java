package com.aioa.knowledge.controller;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;

/**
 * HealthController 单元测试
 */
@WebMvcTest(HealthController.class)
@DisplayName("HealthControllerTest 健康检查控制器测试")
class HealthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    @DisplayName("健康检查返回正确文本")
    void health_returnsCorrectText() throws Exception {
        mockMvc.perform(get("/health"))
                .andExpect(status().isOk())
                .andExpect(content().string("Knowledge Service is running"));
    }

    @Test
    @DisplayName("向量服务状态返回正确文本")
    void vectorStatus_returnsCorrectText() throws Exception {
        mockMvc.perform(get("/vector/status"))
                .andExpect(status().isOk())
                .andExpect(content().string("Vector service: Ready (Mock mode due to network issues)"));
    }
}