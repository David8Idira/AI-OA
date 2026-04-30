package com.aioa.workflow.controller;

import com.aioa.workflow.dto.ApprovalActionDTO;
import com.aioa.workflow.dto.ApprovalQueryDTO;
import com.aioa.workflow.dto.CreateApprovalDTO;
import com.aioa.workflow.service.ApprovalService;
import com.aioa.workflow.vo.ApprovalVO;
import com.aioa.common.vo.PageResult;

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
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("ApprovalControllerTest 审批控制器测试")
class ApprovalControllerTest {

    private MockMvc mockMvc;
    private ObjectMapper objectMapper;

    @Mock
    private ApprovalService approvalService;

    @InjectMocks
    private ApprovalController approvalController;

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());
        MappingJackson2HttpMessageConverter converter = new MappingJackson2HttpMessageConverter();
        converter.setObjectMapper(objectMapper);
        mockMvc = MockMvcBuilders.standaloneSetup(approvalController)
                .setMessageConverters(converter)
                .build();
    }

    private ApprovalVO createMockApprovalVO(String id, String title) {
        ApprovalVO vo = new ApprovalVO();
        vo.setId(id);
        vo.setTitle(title);
        vo.setType("LEAVE");
        vo.setStatus(0);
        return vo;
    }

    @Test
    @DisplayName("查询审批列表成功")
    void listApprovals_success() throws Exception {
        PageResult<ApprovalVO> pageResult = new PageResult<>();
        pageResult.setRecords(List.of(createMockApprovalVO("approval-001", "请假申请")));
        pageResult.setTotal(1L);
        when(approvalService.queryApprovals(anyString(), any(ApprovalQueryDTO.class))).thenReturn(pageResult);

        mockMvc.perform(get("/api/v1/approvals")
                        .requestAttr("userId", "user-001")
                        .param("mode", "MY_APPLY"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.records.length()").value(1));
    }

    @Test
    @DisplayName("查询审批列表为空")
    void listApprovals_empty() throws Exception {
        PageResult<ApprovalVO> pageResult = new PageResult<>();
        pageResult.setRecords(List.of());
        pageResult.setTotal(0L);
        when(approvalService.queryApprovals(anyString(), any(ApprovalQueryDTO.class))).thenReturn(pageResult);

        mockMvc.perform(get("/api/v1/approvals")
                        .requestAttr("userId", "user-001"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.records.length()").value(0));
    }

    @Test
    @DisplayName("获取审批详情成功")
    void getApprovalDetail_success() throws Exception {
        when(approvalService.getApprovalDetail(eq("approval-001"), anyString()))
                .thenReturn(createMockApprovalVO("approval-001", "请假申请"));

        mockMvc.perform(get("/api/v1/approvals/approval-001")
                        .requestAttr("userId", "user-001"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.id").value("approval-001"));
    }

    @Test
    @DisplayName("获取审批详情-未找到")
    void getApprovalDetail_notFound() throws Exception {
        when(approvalService.getApprovalDetail(eq("not-exist"), anyString()))
                .thenReturn(null);

        mockMvc.perform(get("/api/v1/approvals/not-exist")
                        .requestAttr("userId", "user-001"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));
    }

    @Test
    @DisplayName("创建审批成功")
    void createApproval_success() throws Exception {
        CreateApprovalDTO dto = new CreateApprovalDTO();
        dto.setTitle("请假申请");
        dto.setType("LEAVE");
        dto.setContent("因病请假2天");
        dto.setPriority(1);
        dto.setApproverId("approver-001");

        ApprovalVO created = createMockApprovalVO("new-approval-id", "请假申请");
        when(approvalService.createApproval(anyString(), any(CreateApprovalDTO.class)))
                .thenReturn(created);

        mockMvc.perform(post("/api/v1/approvals")
                        .requestAttr("userId", "user-001")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.id").value("new-approval-id"));
    }

    @Test
    @DisplayName("审批操作-同意")
    void doAction_success_approve() throws Exception {
        ApprovalActionDTO dto = new ApprovalActionDTO();
        dto.setActionType(1);
        dto.setComment("同意");

        ApprovalVO resultVO = createMockApprovalVO("approval-001", "请假申请");
        when(approvalService.doAction(eq("approval-001"), anyString(), any(ApprovalActionDTO.class)))
                .thenReturn(resultVO);

        mockMvc.perform(post("/api/v1/approvals/approval-001/action")
                        .requestAttr("userId", "approver-001")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));
    }

    @Test
    @DisplayName("审批操作-拒绝")
    void doAction_success_reject() throws Exception {
        ApprovalActionDTO dto = new ApprovalActionDTO();
        dto.setActionType(2);
        dto.setComment("材料不全");

        ApprovalVO resultVO = createMockApprovalVO("approval-001", "请假申请");
        when(approvalService.doAction(eq("approval-001"), anyString(), any(ApprovalActionDTO.class)))
                .thenReturn(resultVO);

        mockMvc.perform(post("/api/v1/approvals/approval-001/action")
                        .requestAttr("userId", "approver-001")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));
    }

    @Test
    @DisplayName("获取待审批列表成功")
    void getPendingApprovals_success() throws Exception {
        PageResult<ApprovalVO> pageResult = new PageResult<>();
        pageResult.setRecords(List.of(createMockApprovalVO("pending-001", "报销申请")));
        pageResult.setTotal(1L);
        when(approvalService.getPendingApprovals(anyString(), any(Integer.class), any(Integer.class)))
                .thenReturn(pageResult);

        mockMvc.perform(get("/api/v1/approvals/pending")
                        .requestAttr("userId", "approver-001"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.records.length()").value(1));
    }

    @Test
    @DisplayName("获取我发起的审批列表成功")
    void getMyApprovals_success() throws Exception {
        PageResult<ApprovalVO> pageResult = new PageResult<>();
        pageResult.setRecords(List.of(createMockApprovalVO("my-001", "出差申请")));
        pageResult.setTotal(1L);
        when(approvalService.getMyApprovals(anyString(), any(Integer.class), any(Integer.class)))
                .thenReturn(pageResult);

        mockMvc.perform(get("/api/v1/approvals/my")
                        .requestAttr("userId", "user-001"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.records.length()").value(1));
    }

    @Test
    @DisplayName("获取审批统计")
    void getStatistics_success() throws Exception {
        when(approvalService.getStatistics(anyString()))
                .thenReturn(java.util.Map.of("pending", 5, "approved", 10, "rejected", 2));

        mockMvc.perform(get("/api/v1/approvals/statistics")
                        .requestAttr("userId", "user-001"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.pending").value(5));
    }

    @Test
    @DisplayName("获取待审批数量")
    void getPendingCount_success() throws Exception {
        when(approvalService.countPending(anyString())).thenReturn(3L);

        mockMvc.perform(get("/api/v1/approvals/pending/count")
                        .requestAttr("userId", "approver-001"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data").value(3));
    }

    @Test
    @DisplayName("取消审批成功")
    void cancelApproval_success() throws Exception {
        when(approvalService.cancelApproval(anyString(), anyString(), anyString()))
                .thenReturn(true);

        mockMvc.perform(post("/api/v1/approvals/approval-001/cancel")
                        .requestAttr("userId", "user-001")
                        .param("reason", "不需要了"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));
    }

    @Test
    @DisplayName("取消审批-已处理不能取消")
    void cancelApproval_fail_alreadyProcessed() throws Exception {
        when(approvalService.cancelApproval(anyString(), anyString(), any()))
                .thenReturn(false);

        mockMvc.perform(post("/api/v1/approvals/approval-001/cancel")
                        .requestAttr("userId", "user-001"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(500));
    }

    @Test
    @DisplayName("转交审批人成功")
    void reassignApprover_success() throws Exception {
        ApprovalVO resultVO = createMockApprovalVO("approval-001", "请假申请");
        when(approvalService.reassignApprover(any(), anyString(), anyString(), any()))
                .thenReturn(resultVO);

        mockMvc.perform(post("/api/v1/approvals/approval-001/reassign")
                        .requestAttr("userId", "admin-001")
                        .param("newApproverId", "approver-002")
                        .param("reason", "审批人出差"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));
    }
}
