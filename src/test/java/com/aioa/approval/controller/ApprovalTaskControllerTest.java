package com.aioa.approval.controller;

import com.aioa.approval.dto.ApprovalTaskResponseDTO;
import com.aioa.approval.service.ApprovalTaskService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(ApprovalTaskController.class)
class ApprovalTaskControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private ApprovalTaskService taskService;

    @Test
    void testGetByInstance_Found_ReturnsOk() throws Exception {
        ApprovalTaskResponseDTO task = new ApprovalTaskResponseDTO();
        task.setId(1L);
        task.setInstanceId(100L);
        task.setInstanceTitle("审批任务1");
        task.setStatus(0);

        when(taskService.getByInstance(100L)).thenReturn(List.of(task));

        mockMvc.perform(get("/approval/tasks/instance/100"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].instanceId").value(100));
    }

    @Test
    void testGetByInstance_EmptyList_ReturnsOk() throws Exception {
        when(taskService.getByInstance(100L)).thenReturn(List.of());

        mockMvc.perform(get("/approval/tasks/instance/100"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(0));
    }

    @Test
    void testGetPendingTasks_HasPending_ReturnsOk() throws Exception {
        ApprovalTaskResponseDTO task1 = new ApprovalTaskResponseDTO();
        task1.setId(1L);
        task1.setApproverId(200L);
        task1.setStatus(0);

        ApprovalTaskResponseDTO task2 = new ApprovalTaskResponseDTO();
        task2.setId(2L);
        task2.setApproverId(200L);
        task2.setStatus(0);

        when(taskService.getPendingTasks(200L)).thenReturn(List.of(task1, task2));

        mockMvc.perform(get("/approval/tasks/pending/200"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2));
    }

    @Test
    void testGetPendingTasks_NoPending_ReturnsOk() throws Exception {
        when(taskService.getPendingTasks(200L)).thenReturn(List.of());

        mockMvc.perform(get("/approval/tasks/pending/200"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(0));
    }

    @Test
    void testGetByInstance_MultipleTasks_ReturnsAll() throws Exception {
        ApprovalTaskResponseDTO task1 = new ApprovalTaskResponseDTO();
        task1.setId(1L);
        task1.setInstanceId(100L);
        task1.setInstanceTitle("任务1");

        ApprovalTaskResponseDTO task2 = new ApprovalTaskResponseDTO();
        task2.setId(2L);
        task2.setInstanceId(100L);
        task2.setInstanceTitle("任务2");

        ApprovalTaskResponseDTO task3 = new ApprovalTaskResponseDTO();
        task3.setId(3L);
        task3.setInstanceId(100L);
        task3.setInstanceTitle("任务3");

        when(taskService.getByInstance(100L)).thenReturn(List.of(task1, task2, task3));

        mockMvc.perform(get("/approval/tasks/instance/100"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(3));
    }
}
