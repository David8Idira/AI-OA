package com.aioa.workflow.controller;
import com.aioa.workflow.TestApplication;

import com.aioa.common.result.Result;
import com.aioa.workflow.dto.ApprovalActionDTO;
import com.aioa.workflow.dto.ApprovalQueryDTO;
import com.aioa.workflow.dto.CreateApprovalDTO;
import com.aioa.workflow.service.ApprovalService;
import com.aioa.workflow.vo.ApprovalVO;
import com.aioa.common.vo.PageResult;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * ApprovalController 单元测试
 */
@DisplayName("ApprovalControllerTest 审批控制器测试")
@SpringBootTest(classes = TestApplication.class)
@AutoConfigureMockMvc
class ApprovalControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private ApprovalService approvalService;

    private ApprovalVO createMockApprovalVO(String id, String title) {
        ApprovalVO vo = new ApprovalVO();
        vo.setId(id);
        vo.setTitle(title);
        vo.setType("LEAVE");
        vo.setStatus(0);
        return vo;
    }

    // ==================== List Approvals ====================

    @Test
    @DisplayName("查询审批列表成功")
    void listApprovals_success() throws Exception {
        // given
        PageResult<ApprovalVO> pageResult = new PageResult<>();
        pageResult.setRecords(List.of(createMockApprovalVO("approval-001", "请假申请")));
        pageResult.setTotal(1L);
        when(approvalService.queryApprovals(anyString(), any(ApprovalQueryDTO.class))).thenReturn(pageResult);

        // when & then
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
        // given
        PageResult<ApprovalVO> pageResult = new PageResult<>();
        pageResult.setRecords(List.of());
        pageResult.setTotal(0L);
        when(approvalService.queryApprovals(anyString(), any(ApprovalQueryDTO.class))).thenReturn(pageResult);

        // when & then
        mockMvc.perform(get("/api/v1/approvals")
                        .requestAttr("userId", "user-001"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.records.length()").value(0));
    }

    // ==================== Get Approval Detail ====================

    @Test
    @DisplayName("获取审批详情成功")
    void getApprovalDetail_success() throws Exception {
        // given
        ApprovalVO vo = createMockApprovalVO("approval-001", "请假申请");
        when(approvalService.getApprovalDetail("approval-001", "user-001")).thenReturn(vo);

        // when & then
        mockMvc.perform(get("/api/v1/approvals/approval-001")
                        .requestAttr("userId", "user-001"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.title").value("请假申请"));
    }

    @Test
    @DisplayName("获取审批详情 - 不存在")
    void getApprovalDetail_notFound() throws Exception {
        // given
        when(approvalService.getApprovalDetail("nonexist", "user-001")).thenReturn(null);

        // when & then
        mockMvc.perform(get("/api/v1/approvals/nonexist")
                        .requestAttr("userId", "user-001"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(500));
    }

    // ==================== Create Approval ====================

    @Test
    @DisplayName("创建审批成功")
    void createApproval_success() throws Exception {
        // given
        CreateApprovalDTO dto = new CreateApprovalDTO();
        dto.setType("LEAVE");
        dto.setTitle("请假申请");
        dto.setContent("{\"days\":3}");
        dto.setApproverId("approver-001");

        ApprovalVO vo = createMockApprovalVO("approval-new", "请假申请");
        when(approvalService.createApproval(anyString(), any(CreateApprovalDTO.class))).thenReturn(vo);

        // when & then
        mockMvc.perform(post("/api/v1/approvals")
                        .requestAttr("userId", "user-001")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.title").value("请假申请"));
    }

    @Test
    @DisplayName("创建审批 - 标题为空")
    void createApproval_validationTitleBlank() throws Exception {
        // given
        CreateApprovalDTO dto = new CreateApprovalDTO();
        dto.setType("LEAVE");
        dto.setTitle("");
        dto.setApproverId("approver-001");

        // when & then
        mockMvc.perform(post("/api/v1/approvals")
                        .requestAttr("userId", "user-001")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isBadRequest());
    }

    // ==================== Perform Action ====================

    @Test
    @DisplayName("执行审批动作成功 - 同意")
    void doAction_success_approve() throws Exception {
        // given
        ApprovalActionDTO dto = new ApprovalActionDTO();
        dto.setActionType(1);
        dto.setComment("同意");

        ApprovalVO vo = createMockApprovalVO("approval-001", "请假申请");
        vo.setStatus(1);
        when(approvalService.doAction(anyString(), anyString(), any(ApprovalActionDTO.class))).thenReturn(vo);

        // when & then
        mockMvc.perform(post("/api/v1/approvals/approval-001/action")
                        .requestAttr("userId", "approver-001")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));
    }

    @Test
    @DisplayName("执行审批动作成功 - 拒绝")
    void doAction_success_reject() throws Exception {
        // given
        ApprovalActionDTO dto = new ApprovalActionDTO();
        dto.setActionType(2);
        dto.setComment("材料不全");

        ApprovalVO vo = createMockApprovalVO("approval-001", "请假申请");
        vo.setStatus(2);
        when(approvalService.doAction(anyString(), anyString(), any(ApprovalActionDTO.class))).thenReturn(vo);

        // when & then
        mockMvc.perform(post("/api/v1/approvals/approval-001/action")
                        .requestAttr("userId", "approver-001")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));
    }

    // ==================== Cancel Approval ====================

    @Test
    @DisplayName("撤回审批成功")
    void cancelApproval_success() throws Exception {
        // given
        when(approvalService.cancelApproval("approval-001", "user-001", "不想审批了")).thenReturn(true);

        // when & then
        mockMvc.perform(post("/api/v1/approvals/approval-001/cancel")
                        .requestAttr("userId", "user-001")
                        .param("reason", "不想审批了"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));
    }

    @Test
    @DisplayName("撤回审批失败 - 已被处理")
    void cancelApproval_fail_alreadyProcessed() throws Exception {
        // given
        when(approvalService.cancelApproval("approval-001", "user-001", null)).thenReturn(false);

        // when & then
        mockMvc.perform(post("/api/v1/approvals/approval-001/cancel")
                        .requestAttr("userId", "user-001"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(500));
    }

    // ==================== Get Pending Approvals ====================

    @Test
    @DisplayName("获取待我审批列表成功")
    void getPendingApprovals_success() throws Exception {
        // given
        PageResult<ApprovalVO> pageResult = new PageResult<>();
        pageResult.setRecords(List.of(createMockApprovalVO("approval-001", "请假申请")));
        pageResult.setTotal(1L);
        when(approvalService.getPendingApprovals("approver-001", 1, 10)).thenReturn(pageResult);

        // when & then
        mockMvc.perform(get("/api/v1/approvals/pending")
                        .requestAttr("userId", "approver-001")
                        .param("pageNum", "1")
                        .param("pageSize", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.records.length()").value(1));
    }

    // ==================== Get My Approvals ====================

    @Test
    @DisplayName("获取我发起的审批列表成功")
    void getMyApprovals_success() throws Exception {
        // given
        PageResult<ApprovalVO> pageResult = new PageResult<>();
        pageResult.setRecords(List.of(createMockApprovalVO("approval-001", "请假申请")));
        pageResult.setTotal(1L);
        when(approvalService.getMyApprovals("user-001", 1, 10)).thenReturn(pageResult);

        // when & then
        mockMvc.perform(get("/api/v1/approvals/my")
                        .requestAttr("userId", "user-001")
                        .param("pageNum", "1")
                        .param("pageSize", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.records.length()").value(1));
    }

    // ==================== Get Statistics ====================

    @Test
    @DisplayName("获取审批统计成功")
    void getStatistics_success() throws Exception {
        // given
        Map<String, Object> stats = new HashMap<>();
        stats.put("total", 10);
        stats.put("pending", 3);
        stats.put("approved", 5);
        stats.put("rejected", 2);
        when(approvalService.getStatistics("user-001")).thenReturn(stats);

        // when & then
        mockMvc.perform(get("/api/v1/approvals/statistics")
                        .requestAttr("userId", "user-001"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.total").value(10));
    }

    // ==================== Get Pending Count ====================

    @Test
    @DisplayName("获取待审批数量成功")
    void getPendingCount_success() throws Exception {
        // given
        when(approvalService.countPending("user-001")).thenReturn(5L);

        // when & then
        mockMvc.perform(get("/api/v1/approvals/pending/count")
                        .requestAttr("userId", "user-001"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data").value(5));
    }

    // ==================== Reassign Approver ====================

    @Test
    @DisplayName("变更审批人成功")
    void reassignApprover_success() throws Exception {
        // given
        ApprovalVO vo = createMockApprovalVO("approval-001", "请假申请");
        when(approvalService.reassignApprover("approval-001", "user-001", "new-approver", null)).thenReturn(vo);

        // when & then
        mockMvc.perform(post("/api/v1/approvals/approval-001/reassign")
                        .requestAttr("userId", "user-001")
                        .param("newApproverId", "new-approver"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));
    }
}
