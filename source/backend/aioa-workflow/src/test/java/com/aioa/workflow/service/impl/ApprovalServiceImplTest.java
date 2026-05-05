package com.aioa.workflow.service.impl;

import com.aioa.common.exception.BusinessException;
import com.aioa.system.entity.SysUser;
import com.aioa.system.mapper.SysUserMapper;
import com.aioa.workflow.dto.ApprovalActionDTO;
import com.aioa.workflow.entity.Approval;
import com.aioa.workflow.enums.ApprovalActionEnum;
import com.aioa.workflow.enums.ApprovalPriorityEnum;
import com.aioa.workflow.enums.ApprovalStatusEnum;
import com.aioa.workflow.mapper.ApprovalMapper;
import com.aioa.workflow.service.ApprovalRecordService;
import com.aioa.workflow.vo.ApprovalVO;
import com.aioa.common.vo.PageResult;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * ApprovalServiceImpl 单元测试
 * 毛泽东思想指导：实事求是，测试审批服务
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("ApprovalServiceImplTest 单元测试")
class ApprovalServiceImplTest {

    @Mock
    private ApprovalMapper approvalMapper;

    @Mock
    private ApprovalRecordService approvalRecordService;

    @Mock
    private SysUserMapper sysUserMapper;

    private ApprovalServiceImpl approvalService;

    @BeforeEach
    void setUp() {
        approvalService = new ApprovalServiceImpl(approvalRecordService);
        ReflectionTestUtils.setField(approvalService, "baseMapper", approvalMapper);
        ReflectionTestUtils.setField(approvalService, "sysUserMapper", sysUserMapper);
    }

    private SysUser createTestUser(String userId, String name) {
        SysUser user = new SysUser();
        user.setId(userId);
        user.setUsername(name);
        user.setNickname(name);
        user.setDeptId("dept-001");
        return user;
    }

    private Approval createApprovalEntity(String id, String applicantId, String approverId, int status) {
        Approval approval = new Approval();
        approval.setId(id);
        approval.setTitle("测试审批");
        approval.setApplicantId(applicantId);
        approval.setApproverId(approverId);
        approval.setStatus(status);
        approval.setPriority(ApprovalPriorityEnum.NORMAL.getCode());
        approval.setCurrentStep(1);
        approval.setTotalSteps(1);
        return approval;
    }

    private ApprovalActionDTO createActionDTO(int actionType, String comment) {
        ApprovalActionDTO dto = new ApprovalActionDTO();
        dto.setActionType(actionType);
        dto.setComment(comment);
        return dto;
    }

    @Test
    @DisplayName("取消审批 - 正常场景")
    void cancelApproval_withValidInput_shouldSucceed() {
        // given
        Approval approval = createApprovalEntity("approval-001", "user-001", "approver-001", ApprovalStatusEnum.PENDING.getCode());
        when(approvalMapper.selectById("approval-001")).thenReturn(approval);
        when(approvalMapper.updateById(any())).thenReturn(1);
        when(sysUserMapper.selectById("user-001")).thenReturn(createTestUser("user-001", "申请人"));
        when(approvalRecordService.createRecord(any(), any(), any(), anyInt(), any(), anyInt(), anyInt())).thenReturn(null);

        // when
        boolean result = approvalService.cancelApproval("approval-001", "user-001", "不需要了");

        // then
        assertThat(result).isTrue();
        verify(approvalMapper, times(1)).updateById(argThat(a -> 
            ApprovalStatusEnum.CANCELLED.getCode().equals(a.getStatus())));
    }

    @Test
    @DisplayName("取消审批 - 非申请人尝试取消")
    void cancelApproval_withNonApplicant_shouldThrow() {
        // given
        Approval approval = createApprovalEntity("approval-001", "user-001", "approver-001", ApprovalStatusEnum.PENDING.getCode());
        when(approvalMapper.selectById("approval-001")).thenReturn(approval);

        // when/then - 审批人尝试取消
        assertThatThrownBy(() -> approvalService.cancelApproval("approval-001", "approver-001", "测试"))
            .isInstanceOf(BusinessException.class);
    }

    @Test
    @DisplayName("取消审批 - 审批已处理（已通过）")
    void cancelApproval_withAlreadyApproved_shouldThrow() {
        // given
        Approval approval = createApprovalEntity("approval-001", "user-001", "approver-001", ApprovalStatusEnum.APPROVED.getCode());
        when(approvalMapper.selectById("approval-001")).thenReturn(approval);

        // when/then
        assertThatThrownBy(() -> approvalService.cancelApproval("approval-001", "user-001", "测试"))
            .isInstanceOf(BusinessException.class);
    }

    @Test
    @DisplayName("取消审批 - 审批不存在")
    void cancelApproval_withNonExisting_shouldThrow() {
        // given
        when(approvalMapper.selectById("non-existing")).thenReturn(null);

        // when/then
        assertThatThrownBy(() -> approvalService.cancelApproval("non-existing", "user-001", "测试"))
            .isInstanceOf(BusinessException.class);
    }

    @Test
    @DisplayName("统计待审批数量 - 正常场景")
    void countPending_shouldReturnCount() {
        // given
        when(approvalMapper.selectCount(any(LambdaQueryWrapper.class))).thenReturn(5L);

        // when
        Long count = approvalService.countPending("approver-001");

        // then
        assertThat(count).isEqualTo(5L);
    }

    @Test
    @DisplayName("统计待审批数量 - 无待审批")
    void countPending_withNoPending_shouldReturnZero() {
        // given
        when(approvalMapper.selectCount(any(LambdaQueryWrapper.class))).thenReturn(0L);

        // when
        Long count = approvalService.countPending("approver-001");

        // then
        assertThat(count).isEqualTo(0L);
    }

    @Test
    @DisplayName("统计待审批数量 - 新用户无待审批")
    void countPending_withNewUser_shouldReturnZero() {
        // given
        when(approvalMapper.selectCount(any(LambdaQueryWrapper.class))).thenReturn(0L);

        // when
        Long count = approvalService.countPending("new-user");

        // then
        assertThat(count).isEqualTo(0L);
    }

    @Test
    @DisplayName("审批操作 - 无效操作类型")
    void doAction_withInvalidActionType_shouldThrow() {
        // given
        Approval approval = createApprovalEntity("approval-001", "user-001", "approver-001", ApprovalStatusEnum.PENDING.getCode());
        when(approvalMapper.selectById("approval-001")).thenReturn(approval);
        // Note: when action is null, the method throws BEFORE checking sysUserMapper

        ApprovalActionDTO actionDTO = createActionDTO(99, "无效操作"); // 99 is invalid

        // when/then
        assertThatThrownBy(() -> approvalService.doAction("approval-001", "approver-001", actionDTO))
            .isInstanceOf(BusinessException.class);
    }

    @Test
    @DisplayName("审批操作 - 审批不存在")
    void doAction_withNonExistingApproval_shouldThrow() {
        // given
        when(approvalMapper.selectById("non-existing")).thenReturn(null);

        ApprovalActionDTO actionDTO = createActionDTO(ApprovalActionEnum.APPROVE.getCode(), "同意");

        // when/then
        assertThatThrownBy(() -> approvalService.doAction("non-existing", "approver-001", actionDTO))
            .isInstanceOf(BusinessException.class);
    }

    @Test
    @DisplayName("审批操作 - 非审批人操作")
    void doAction_withNonApprover_shouldThrow() {
        // given
        Approval approval = createApprovalEntity("approval-001", "user-001", "approver-001", ApprovalStatusEnum.PENDING.getCode());
        when(approvalMapper.selectById("approval-001")).thenReturn(approval);
        when(sysUserMapper.selectById("user-001")).thenReturn(createTestUser("user-001", "申请人"));

        ApprovalActionDTO actionDTO = createActionDTO(ApprovalActionEnum.APPROVE.getCode(), "同意");

        // when/then - 申请人尝试审批
        assertThatThrownBy(() -> approvalService.doAction("approval-001", "user-001", actionDTO))
            .isInstanceOf(BusinessException.class);
    }

    @Test
    @DisplayName("审批操作 - 审批已驳回再次审批")
    void doAction_withAlreadyRejected_shouldThrow() {
        // given
        Approval approval = createApprovalEntity("approval-001", "user-001", "approver-001", ApprovalStatusEnum.REJECTED.getCode());
        when(approvalMapper.selectById("approval-001")).thenReturn(approval);
        when(sysUserMapper.selectById("approver-001")).thenReturn(createTestUser("approver-001", "审批人"));

        ApprovalActionDTO actionDTO = createActionDTO(ApprovalActionEnum.APPROVE.getCode(), "同意");

        // when/then
        assertThatThrownBy(() -> approvalService.doAction("approval-001", "approver-001", actionDTO))
            .isInstanceOf(BusinessException.class);
    }

    @Test
    @DisplayName("审批操作 - 审批已取消")
    void doAction_withAlreadyCancelled_shouldThrow() {
        // given
        Approval approval = createApprovalEntity("approval-001", "user-001", "approver-001", ApprovalStatusEnum.CANCELLED.getCode());
        when(approvalMapper.selectById("approval-001")).thenReturn(approval);
        when(sysUserMapper.selectById("approver-001")).thenReturn(createTestUser("approver-001", "审批人"));

        ApprovalActionDTO actionDTO = createActionDTO(ApprovalActionEnum.APPROVE.getCode(), "同意");

        // when/then
        assertThatThrownBy(() -> approvalService.doAction("approval-001", "approver-001", actionDTO))
            .isInstanceOf(BusinessException.class);
    }

    @Test
    @DisplayName("审批操作 - 同意时记录操作并设置完成时间")
    void doAction_withApprove_shouldRecordActionAndSetFinishTime() {
        // given
        Approval approval = createApprovalEntity("approval-001", "user-001", "approver-001", ApprovalStatusEnum.PENDING.getCode());
        when(approvalMapper.selectById("approval-001")).thenReturn(approval);
        when(approvalMapper.updateById(any())).thenReturn(1);
        when(sysUserMapper.selectById("approver-001")).thenReturn(createTestUser("approver-001", "审批人"));
        when(approvalRecordService.createRecord(any(), any(), any(), anyInt(), any(), anyInt(), anyInt())).thenReturn(null);

        ApprovalActionDTO actionDTO = createActionDTO(ApprovalActionEnum.APPROVE.getCode(), "同意");

        // when
        ApprovalVO result = approvalService.doAction("approval-001", "approver-001", actionDTO);

        // then
        assertThat(result).isNotNull();
        verify(approvalRecordService, times(1)).createRecord(
            eq("approval-001"), eq("approver-001"), any(),
            eq(ApprovalActionEnum.APPROVE.getCode()),
            eq("同意"),
            eq(ApprovalStatusEnum.APPROVED.getCode()),
            eq(1)
        );
        verify(approvalMapper).updateById(argThat(a -> a.getFinishTime() != null));
    }

    @Test
    @DisplayName("审批操作 - 驳回时状态变更")
    void doAction_withReject_shouldChangeStatusToRejected() {
        // given
        Approval approval = createApprovalEntity("approval-001", "user-001", "approver-001", ApprovalStatusEnum.PENDING.getCode());
        when(approvalMapper.selectById("approval-001")).thenReturn(approval);
        when(approvalMapper.updateById(any())).thenReturn(1);
        when(sysUserMapper.selectById("approver-001")).thenReturn(createTestUser("approver-001", "审批人"));
        when(approvalRecordService.createRecord(any(), any(), any(), anyInt(), any(), anyInt(), anyInt())).thenReturn(null);

        ApprovalActionDTO actionDTO = createActionDTO(ApprovalActionEnum.REJECT.getCode(), "条件不符");

        // when
        ApprovalVO result = approvalService.doAction("approval-001", "approver-001", actionDTO);

        // then
        assertThat(result).isNotNull();
        verify(approvalMapper).updateById(argThat(a -> 
            ApprovalStatusEnum.REJECTED.getCode().equals(a.getStatus())));
    }

    @Test
    @DisplayName("审批操作 - 驳回时记录驳回原因")
    void doAction_withReject_shouldRecordComment() {
        // given
        Approval approval = createApprovalEntity("approval-001", "user-001", "approver-001", ApprovalStatusEnum.PENDING.getCode());
        when(approvalMapper.selectById("approval-001")).thenReturn(approval);
        when(approvalMapper.updateById(any())).thenReturn(1);
        when(sysUserMapper.selectById("approver-001")).thenReturn(createTestUser("approver-001", "审批人"));
        when(approvalRecordService.createRecord(any(), any(), any(), anyInt(), any(), anyInt(), anyInt())).thenReturn(null);

        ApprovalActionDTO actionDTO = createActionDTO(ApprovalActionEnum.REJECT.getCode(), "材料不全");

        // when
        approvalService.doAction("approval-001", "approver-001", actionDTO);

        // then
        verify(approvalRecordService).createRecord(
            eq("approval-001"), eq("approver-001"), any(),
            eq(ApprovalActionEnum.REJECT.getCode()),
            eq("材料不全"),
            eq(ApprovalStatusEnum.REJECTED.getCode()),
            eq(1)
        );
    }

    @Test
    @DisplayName("审批操作 - 同意时完成时间被设置")
    void doAction_withApprove_finishTimeShouldBeSet() {
        // given
        Approval approval = createApprovalEntity("approval-001", "user-001", "approver-001", ApprovalStatusEnum.PENDING.getCode());
        when(approvalMapper.selectById("approval-001")).thenReturn(approval);
        when(approvalMapper.updateById(any())).thenReturn(1);
        when(sysUserMapper.selectById("approver-001")).thenReturn(createTestUser("approver-001", "审批人"));
        when(approvalRecordService.createRecord(any(), any(), any(), anyInt(), any(), anyInt(), anyInt())).thenReturn(null);

        ApprovalActionDTO actionDTO = createActionDTO(ApprovalActionEnum.APPROVE.getCode(), "同意");

        // when
        approvalService.doAction("approval-001", "approver-001", actionDTO);

        // then
        verify(approvalMapper).updateById(argThat(a -> a.getFinishTime() != null));
    }

    @Test
    @DisplayName("取消审批 - 理由为空")
    void cancelApproval_withEmptyReason_shouldStillSucceed() {
        // given
        Approval approval = createApprovalEntity("approval-001", "user-001", "approver-001", ApprovalStatusEnum.PENDING.getCode());
        when(approvalMapper.selectById("approval-001")).thenReturn(approval);
        when(approvalMapper.updateById(any())).thenReturn(1);
        when(sysUserMapper.selectById("user-001")).thenReturn(createTestUser("user-001", "申请人"));
        when(approvalRecordService.createRecord(any(), any(), any(), anyInt(), any(), anyInt(), anyInt())).thenReturn(null);

        // when
        boolean result = approvalService.cancelApproval("approval-001", "user-001", null);

        // then
        assertThat(result).isTrue();
    }

    @Test
    @DisplayName("审批操作 - 操作人用户不存在")
    void doAction_withNonExistingOperator_shouldThrow() {
        // given
        Approval approval = createApprovalEntity("approval-001", "user-001", "approver-001", ApprovalStatusEnum.PENDING.getCode());
        when(approvalMapper.selectById("approval-001")).thenReturn(approval);
        when(sysUserMapper.selectById("approver-001")).thenReturn(null); // 用户不存在

        ApprovalActionDTO actionDTO = createActionDTO(ApprovalActionEnum.APPROVE.getCode(), "同意");

        // when/then
        assertThatThrownBy(() -> approvalService.doAction("approval-001", "approver-001", actionDTO))
            .isInstanceOf(BusinessException.class);
    }
}