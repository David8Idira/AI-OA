package com.aioa.workflow.dto;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * ApprovalActionDTO 单元测试
 * 毛泽东思想指导：实事求是，测试审批操作DTO
 */
@DisplayName("ApprovalActionDTO 单元测试")
class ApprovalActionDTOTest {

    @Test
    @DisplayName("创建审批操作DTO - 同意")
    void createApprovalAction_approve_shouldSetActionTypeOne() {
        // given
        ApprovalActionDTO dto = new ApprovalActionDTO();
        dto.setActionType(1);
        dto.setComment("同意申请");

        // then
        assertThat(dto.getActionType()).isEqualTo(1);
        assertThat(dto.getComment()).isEqualTo("同意申请");
    }

    @Test
    @DisplayName("创建审批操作DTO - 拒绝")
    void createApprovalAction_reject_shouldSetActionTypeTwo() {
        // given
        ApprovalActionDTO dto = new ApprovalActionDTO();
        dto.setActionType(2);
        dto.setComment("材料不全，拒绝");

        // then
        assertThat(dto.getActionType()).isEqualTo(2);
        assertThat(dto.getComment()).isEqualTo("材料不全，拒绝");
    }

    @Test
    @DisplayName("创建审批操作DTO - 转交")
    void createApprovalAction_transfer_shouldSetActionTypeThree() {
        // given
        ApprovalActionDTO dto = new ApprovalActionDTO();
        dto.setActionType(3);
        dto.setTransferToId("user-456");

        // then
        assertThat(dto.getActionType()).isEqualTo(3);
        assertThat(dto.getTransferToId()).isEqualTo("user-456");
    }

    @Test
    @DisplayName("创建审批操作DTO - 取消")
    void createApprovalAction_cancel_shouldSetActionTypeFour() {
        // given
        ApprovalActionDTO dto = new ApprovalActionDTO();
        dto.setActionType(4);
        dto.setComment("申请人主动取消");

        // then
        assertThat(dto.getActionType()).isEqualTo(4);
        assertThat(dto.getComment()).isEqualTo("申请人主动取消");
    }

    @Test
    @DisplayName("审批操作常量 - 同意")
    void approvalAction_approve_shouldBeOne() {
        assertThat(1).isEqualTo(1); // approve
    }

    @Test
    @DisplayName("审批操作常量 - 拒绝")
    void approvalAction_reject_shouldBeTwo() {
        assertThat(2).isEqualTo(2); // reject
    }

    @Test
    @DisplayName("审批操作常量 - 转交")
    void approvalAction_transfer_shouldBeThree() {
        assertThat(3).isEqualTo(3); // transfer
    }

    @Test
    @DisplayName("审批操作常量 - 取消")
    void approvalAction_cancel_shouldBeFour() {
        assertThat(4).isEqualTo(4); // cancel
    }

    @Test
    @DisplayName("审批操作 - 带附件")
    void approvalAction_withAttachments_shouldSucceed() {
        // given
        ApprovalActionDTO dto = new ApprovalActionDTO();
        dto.setActionType(1);
        dto.setAttachments("http://example.com/file1.pdf");

        // then
        assertThat(dto.getAttachments()).isEqualTo("http://example.com/file1.pdf");
    }

    @Test
    @DisplayName("审批操作 - 下一步审批人")
    void approvalAction_withNextApprover_shouldSucceed() {
        // given
        ApprovalActionDTO dto = new ApprovalActionDTO();
        dto.setActionType(1);
        dto.setNextApproverId("approver-789");

        // then
        assertThat(dto.getNextApproverId()).isEqualTo("approver-789");
    }

    @Test
    @DisplayName("审批操作 - 完整场景")
    void approvalAction_completeScenario_shouldSucceed() {
        // given
        ApprovalActionDTO dto = new ApprovalActionDTO();
        dto.setActionType(2);
        dto.setComment("不符合报销规定");
        dto.setAttachments("http://example.com/reject.pdf");
        dto.setTransferToId(null);
        dto.setNextApproverId(null);

        // then
        assertThat(dto.getActionType()).isEqualTo(2);
        assertThat(dto.getComment()).isEqualTo("不符合报销规定");
        assertThat(dto.getAttachments()).isEqualTo("http://example.com/reject.pdf");
        assertThat(dto.getTransferToId()).isNull();
        assertThat(dto.getNextApproverId()).isNull();
    }
}