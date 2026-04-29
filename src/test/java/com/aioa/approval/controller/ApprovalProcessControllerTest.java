package com.aioa.approval.controller;

import com.aioa.approval.dto.ApprovalProcessDTO;
import com.aioa.approval.dto.ApprovalProcessResponseDTO;
import com.aioa.approval.service.ApprovalProcessService;
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

@WebMvcTest(ApprovalProcessController.class)
class ApprovalProcessControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private ApprovalProcessService processService;

    @Test
    void testCreate_Success_ReturnsCreated() throws Exception {
        ApprovalProcessResponseDTO response = new ApprovalProcessResponseDTO();
        response.setId(1L);
        response.setName("测试流程");
        response.setType("leave");
        response.setCreateTime("2024-01-01 10:00:00");
        response.setUpdateTime("2024-01-01 10:00:00");

        when(processService.create(any(ApprovalProcessDTO.class))).thenReturn(response);

        mockMvc.perform(post("/approval/processes")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"name\":\"测试流程\",\"type\":\"leave\"}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.name").value("测试流程"));
    }

    @Test
    void testCreate_WithValidData_ReturnsCreated() throws Exception {
        ApprovalProcessResponseDTO response = new ApprovalProcessResponseDTO();
        response.setId(2L);
        response.setName("流程2");
        response.setType("reimbursement");

        when(processService.create(any(ApprovalProcessDTO.class))).thenReturn(response);

        mockMvc.perform(post("/approval/processes")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"name\":\"流程2\",\"type\":\"reimbursement\"}"))
                .andExpect(status().isCreated());
    }

    @Test
    void testUpdate_Success_ReturnsOk() throws Exception {
        ApprovalProcessResponseDTO response = new ApprovalProcessResponseDTO();
        response.setId(1L);
        response.setName("更新后流程");
        response.setType("leave");

        when(processService.update(eq(1L), any(ApprovalProcessDTO.class))).thenReturn(response);

        mockMvc.perform(put("/approval/processes/1")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"name\":\"更新后流程\",\"type\":\"leave\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("更新后流程"));
    }

    @Test
    void testUpdate_NotFound_ReturnsOk() throws Exception {
        when(processService.update(eq(999L), any(ApprovalProcessDTO.class))).thenReturn(null);

        mockMvc.perform(put("/approval/processes/999")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"name\":\"更新\",\"type\":\"leave\"}"))
                .andExpect(status().isOk());
    }

    @Test
    void testDelete_Success_ReturnsNoContent() throws Exception {
        doNothing().when(processService).delete(1L);

        mockMvc.perform(delete("/approval/processes/1"))
                .andExpect(status().isNoContent());

        verify(processService).delete(1L);
    }

    @Test
    void testGetById_Found_ReturnsOk() throws Exception {
        ApprovalProcessResponseDTO response = new ApprovalProcessResponseDTO();
        response.setId(1L);
        response.setName("流程1");
        response.setType("leave");

        when(processService.getById(1L)).thenReturn(response);

        mockMvc.perform(get("/approval/processes/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1));
    }

    @Test
    void testGetById_NotFound_ReturnsOk() throws Exception {
        when(processService.getById(999L)).thenReturn(null);

        mockMvc.perform(get("/approval/processes/999"))
                .andExpect(status().isOk());
    }

    @Test
    void testGetAll_ReturnsList() throws Exception {
        ApprovalProcessResponseDTO p1 = new ApprovalProcessResponseDTO();
        p1.setId(1L);
        p1.setName("流程1");

        ApprovalProcessResponseDTO p2 = new ApprovalProcessResponseDTO();
        p2.setId(2L);
        p2.setName("流程2");

        when(processService.getAll()).thenReturn(List.of(p1, p2));

        mockMvc.perform(get("/approval/processes"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2));
    }

    @Test
    void testGetByType_ReturnsFilteredList() throws Exception {
        ApprovalProcessResponseDTO response = new ApprovalProcessResponseDTO();
        response.setId(1L);
        response.setType("reimbursement");

        when(processService.getByType("reimbursement")).thenReturn(List.of(response));

        mockMvc.perform(get("/approval/processes/type/reimbursement"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].type").value("reimbursement"));
    }
}
