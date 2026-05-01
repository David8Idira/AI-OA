package com.aioa.workflow.service;

import com.aioa.common.exception.BusinessException;
import com.aioa.common.result.ResultCode;
import com.aioa.system.entity.SysUser;
import com.aioa.workflow.dto.ApprovalActionDTO;
import com.aioa.workflow.dto.ApprovalQueryDTO;
import com.aioa.workflow.dto.CreateApprovalDTO;
import com.aioa.workflow.entity.Approval;
import com.aioa.workflow.entity.ApprovalRecord;
import com.aioa.workflow.enums.ApprovalActionEnum;
import com.aioa.workflow.enums.ApprovalPriorityEnum;
import com.aioa.workflow.enums.ApprovalStatusEnum;
import com.aioa.workflow.mapper.ApprovalMapper;
import com.aioa.workflow.mapper.ApprovalRecordMapper;
import com.aioa.workflow.service.impl.ApprovalServiceImpl;
import com.aioa.workflow.vo.ApprovalRecordVO;
import com.aioa.workflow.vo.ApprovalVO;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.*;

/**
 * ApprovalServiceImpl 单元测试
 * 毛泽东思想指导：实事求是，测试审批服务
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("ApprovalServiceImpl 单元测试")
class ApprovalServiceImplTest {

    @Mock
    private ApprovalMapper approvalMapper;

    @Mock
    private ApprovalRecordMapper approvalRecordMapper;

    @Mock
    private com.aioa.system.mapper.SysUserMapper sysUserMapper;

    @Mock
    private com.aioa.workflow.service.ApprovalRecordService approvalRecordService;

    private ApprovalServiceImpl approvalService;

    private SysUser createTestUser(String userId, String name) {
        SysUser user = new SysUser();
        user.setId(userId);
        user.setUsername(name);
        user.setNickname(name);
        user.setDeptId("1");
        return user;
    }

    private Approval createTestApproval(String id, String applicantId, String approverId) {
        Approval approval = new Approval();
        approval.setId(id);
        approval.setTitle("测试审批");
        approval.setType("LEAVE");
        approval.setContent("测试内容");
        approval.setStatus(ApprovalStatusEnum.PENDING.getCode());
        approval.setPriority(ApprovalPriorityEnum.NORMAL.getCode());
        approval.setApplicantId(applicantId);
        approval.setApplicantName("申请人");
        approval.setApproverId(approverId);
        approval.setApproverName("审批人");
        approval.setCurrentStep(1);
        approval.setTotalSteps(1);
        return approval;
    }

    private CreateApprovalDTO createTestCreateDTO() {
        CreateApprovalDTO dto = new CreateApprovalDTO();
        dto.setTitle("测试审批");
        dto.setType("LEAVE");
        dto.setContent("测试内容");
        dto.setApproverId("approver-001");
        return dto;
    }

    @BeforeEach
    void setUp() throws Exception {
        // Create real ApprovalServiceImpl
        approvalService = spy(new ApprovalServiceImpl(approvalRecordService));
        
        // Use reflection to set the sysUserMapper since it's @Autowired with required=false
        var sysUserField = ApprovalServiceImpl.class.getDeclaredField("sysUserMapper");
        sysUserField.setAccessible(true);
        sysUserField.set(approvalService, sysUserMapper);
        
        // Use reflection to set the baseMapper (MyBatis Plus)
        var baseMapperField = com.baomidou.mybatisplus.extension.service.impl.ServiceImpl.class.getDeclaredField("baseMapper");
        baseMapperField.setAccessible(true);
        baseMapperField.set(approvalService, approvalMapper);
    }

    @Test
    @DisplayName("创建审批 - 正常场景")
    void createApproval_shouldSucceed() {
        // given
        String userId = "user-001";
        CreateApprovalDTO dto = createTestCreateDTO();
        SysUser applicant = createTestUser(userId, "申请人");
        SysUser approver = createTestUser("approver-001", "审批人");

        when(sysUserMapper.selectById(userId)).thenReturn(applicant);
        when(sysUserMapper.selectById("approver-001")).thenReturn(approver);
        doAnswer(inv -> {
            Approval a = inv.getArgument(0);
            a.setId("approval-001");
            return true;
        }).when(approvalService).save(any(Approval.class));

        // when
        ApprovalVO result = approvalService.createApproval(userId, dto);

        // then
        assertThat(result).isNotNull();
        assertThat(result.getTitle()).isEqualTo("测试审批");
        assertThat(result.getStatus()).isEqualTo(ApprovalStatusEnum.PENDING.getCode());
    }

    @Test
    @DisplayName("创建审批 - 申请人不存在")
    void createApproval_withNonExistentUser_shouldThrowException() {
        // given
        String userId = "non-existent";
        CreateApprovalDTO dto = createTestCreateDTO();
        when(sysUserMapper.selectById(userId)).thenReturn(null);

        // when/then
        assertThatThrownBy(() -> approvalService.createApproval(userId, dto))
                .isInstanceOf(BusinessException.class);
    }

    @Test
    @DisplayName("创建审批 - 审批人不存在")
    void createApproval_withNonExistentApprover_shouldThrowException() {
        // given
        String userId = "user-001";
        CreateApprovalDTO dto = createTestCreateDTO();
        SysUser applicant = createTestUser(userId, "申请人");

        when(sysUserMapper.selectById(userId)).thenReturn(applicant);
        when(sysUserMapper.selectById("approver-001")).thenReturn(null);

        // when/then
        assertThatThrownBy(() -> approvalService.createApproval(userId, dto))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("审批人不存在");
    }

    @Test
    @DisplayName("获取审批详情 - 正常场景")
    void getApprovalDetail_shouldSucceed() {
        // given
        String approvalId = "approval-001";
        String userId = "user-001";
        Approval approval = createTestApproval(approvalId, userId, "approver-001");

        when(approvalService.getById(approvalId)).thenReturn(approval);
        when(approvalRecordService.getRecordsByApprovalId(approvalId)).thenReturn(List.of());

        // when
        ApprovalVO result = approvalService.getApprovalDetail(approvalId, userId);

        // then
        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(approvalId);
    }

    @Test
    @DisplayName("获取审批详情 - 审批不存在")
    void getApprovalDetail_withNonExistent_shouldThrowException() {
        // given
        String approvalId = "non-existent";
        String userId = "user-001";
        when(approvalService.getById(approvalId)).thenReturn(null);

        // when/then
        assertThatThrownBy(() -> approvalService.getApprovalDetail(approvalId, userId))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("不存在");
    }

    @Test
    @DisplayName("查询审批列表 - 正常场景")
    void queryApprovals_shouldReturnResults() {
        // given
        String userId = "user-001";
        ApprovalQueryDTO query = new ApprovalQueryDTO();
        query.setMode("MY_APPLY");

        List<Approval> records = List.of(createTestApproval("approval-001", userId, "approver-001"));

        Page<Approval> resultPage = new Page<>(1, 10);
        resultPage.setRecords(records);
        resultPage.setTotal(1L);

        when(approvalService.page(any(Page.class), any(LambdaQueryWrapper.class))).thenReturn(resultPage);

        // when
        var result = approvalService.queryApprovals(userId, query);

        // then
        assertThat(result).isNotNull();
        assertThat(result.getTotal()).isEqualTo(1);
    }

    @Test
    @DisplayName("执行审批操作 - 同意")
    void doAction_withApprove_shouldSucceed() {
        // given
        String approvalId = "approval-001";
        String userId = "approver-001";
        Approval approval = createTestApproval(approvalId, "user-001", userId);

        ApprovalActionDTO dto = new ApprovalActionDTO();
        dto.setActionType(ApprovalActionEnum.APPROVE.getCode());
        dto.setComment("同意");

        SysUser operator = createTestUser(userId, "审批人");
        when(approvalService.getById(approvalId)).thenReturn(approval);
        when(sysUserMapper.selectById(userId)).thenReturn(operator);
        when(approvalRecordService.getRecordsByApprovalId(approvalId)).thenReturn(new ArrayList<>());

        // when
        ApprovalVO result = approvalService.doAction(approvalId, userId, dto);

        // then
        assertThat(result).isNotNull();
        verify(approvalService).updateById(approval);
    }

    @Test
    @DisplayName("执行审批操作 - 驳回")
    void doAction_withReject_shouldSucceed() {
        // given
        String approvalId = "approval-001";
        String userId = "approver-001";
        Approval approval = createTestApproval(approvalId, "user-001", userId);

        ApprovalActionDTO dto = new ApprovalActionDTO();
        dto.setActionType(ApprovalActionEnum.REJECT.getCode());
        dto.setComment("不符合条件");

        SysUser operator = createTestUser(userId, "审批人");
        when(approvalService.getById(approvalId)).thenReturn(approval);
        when(sysUserMapper.selectById(userId)).thenReturn(operator);
        when(approvalRecordService.getRecordsByApprovalId(approvalId)).thenReturn(new ArrayList<>());

        // when
        ApprovalVO result = approvalService.doAction(approvalId, userId, dto);

        // then
        assertThat(result).isNotNull();
        verify(approvalService).updateById(approval);
    }

    @Test
    @DisplayName("执行审批操作 - 权限不足")
    void doAction_withWrongApprover_shouldThrowException() {
        // given
        String approvalId = "approval-001";
        String userId = "wrong-user";
        Approval approval = createTestApproval(approvalId, "user-001", "approver-001");

        ApprovalActionDTO dto = new ApprovalActionDTO();
        dto.setActionType(ApprovalActionEnum.APPROVE.getCode());

        when(approvalService.getById(approvalId)).thenReturn(approval);
        // Mock the operator user lookup
        SysUser operator = createTestUser(userId, "测试用户");
        when(sysUserMapper.selectById(userId)).thenReturn(operator);

        // when/then
        assertThatThrownBy(() -> approvalService.doAction(approvalId, userId, dto))
                .isInstanceOf(BusinessException.class);
    }

    @Test
    @DisplayName("执行审批操作 - 非待审批状态")
    void doAction_withNonPendingStatus_shouldThrowException() {
        // given
        String approvalId = "approval-001";
        String userId = "approver-001";
        Approval approval = createTestApproval(approvalId, "user-001", userId);
        approval.setStatus(ApprovalStatusEnum.APPROVED.getCode());

        ApprovalActionDTO dto = new ApprovalActionDTO();
        dto.setActionType(ApprovalActionEnum.APPROVE.getCode());

        when(approvalService.getById(approvalId)).thenReturn(approval);
        // Mock the operator user lookup
        SysUser operator = createTestUser(userId, "审批人");
        when(sysUserMapper.selectById(userId)).thenReturn(operator);

        // when/then
        assertThatThrownBy(() -> approvalService.doAction(approvalId, userId, dto))
                .isInstanceOf(BusinessException.class);
    }

    @Test
    @DisplayName("获取待审批列表 - 正常场景")
    void getPendingApprovals_shouldReturnResults() {
        // given
        String approverId = "approver-001";
        List<Approval> records = List.of(createTestApproval("approval-001", "user-001", approverId));

        Page<Approval> resultPage = new Page<>(1, 10);
        resultPage.setRecords(records);
        resultPage.setTotal(1L);

        when(approvalService.page(any(Page.class), any(LambdaQueryWrapper.class))).thenReturn(resultPage);

        // when
        var result = approvalService.getPendingApprovals(approverId, 1, 10);

        // then
        assertThat(result).isNotNull();
        assertThat(result.getTotal()).isEqualTo(1);
    }

    @Test
    @DisplayName("获取统计数据 - 正常场景")
    void getStatistics_shouldReturnCorrectData() {
        // given
        String userId = "user-001";
        when(approvalService.count(any(LambdaQueryWrapper.class))).thenReturn(5L);

        // when
        Map<String, Object> stats = approvalService.getStatistics(userId);

        // then
        assertThat(stats).isNotNull();
        assertThat(stats).containsKey("totalMyApply");
        assertThat(stats).containsKey("totalPendingApprove");
        assertThat(stats).containsKey("totalApproved");
        assertThat(stats).containsKey("totalRejected");
    }

    @Test
    @DisplayName("获取统计数据 - 数据库异常")
    void getStatistics_withException_shouldPropagateException() {
        // given
        String userId = "user-001";
        when(approvalService.count(any(LambdaQueryWrapper.class)))
                .thenThrow(new RuntimeException("数据库错误"));

        // when/then
        assertThatThrownBy(() -> approvalService.getStatistics(userId))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("数据库错误");
    }

    @Test
    @DisplayName("获取我的申请 - 正常场景")
    void getMyApprovals_shouldReturnResults() {
        // given
        String applicantId = "user-001";
        List<Approval> records = List.of(createTestApproval("approval-001", applicantId, "approver-001"));

        Page<Approval> resultPage = new Page<>(1, 10);
        resultPage.setRecords(records);
        resultPage.setTotal(1L);

        when(approvalService.page(any(Page.class), any(LambdaQueryWrapper.class))).thenReturn(resultPage);

        // when
        var result = approvalService.getMyApprovals(applicantId, 1, 10);

        // then
        assertThat(result).isNotNull();
        assertThat(result.getTotal()).isEqualTo(1);
    }

    @Test
    @DisplayName("获取我的申请 - 空结果")
    void getMyApprovals_withNoResults_shouldReturnEmptyList() {
        // given
        String applicantId = "user-001";

        Page<Approval> resultPage = new Page<>(1, 10);
        resultPage.setRecords(List.of());
        resultPage.setTotal(0L);

        when(approvalService.page(any(Page.class), any(LambdaQueryWrapper.class))).thenReturn(resultPage);

        // when
        var result = approvalService.getMyApprovals(applicantId, 1, 10);

        // then
        assertThat(result).isNotNull();
        assertThat(result.getTotal()).isEqualTo(0);
    }

    @Test
    @DisplayName("计算待审批数量 - 正常场景")
    void countPending_shouldReturnCorrectCount() {
        // given
        String approverId = "approver-001";
        when(approvalService.count(any(LambdaQueryWrapper.class))).thenReturn(5L);

        // when
        Long count = approvalService.countPending(approverId);

        // then
        assertThat(count).isEqualTo(5L);
    }

    @Test
    @DisplayName("计算待审批数量 - 数据库异常")
    void countPending_withException_shouldPropagateException() {
        // given
        String approverId = "approver-001";
        when(approvalService.count(any(LambdaQueryWrapper.class)))
                .thenThrow(new RuntimeException("数据库错误"));

        // when/then
        assertThatThrownBy(() -> approvalService.countPending(approverId))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("数据库错误");
    }
}