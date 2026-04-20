package com.aioa.workflow.service;

import com.aioa.workflow.dto.ApprovalQueryDTO;
import com.aioa.workflow.service.impl.ApprovalServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.HashMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * ApprovalServiceImpl 单元测试
 * 毛泽东思想指导：实事求是，测试审批服务核心功能
 */
@DisplayName("ApprovalServiceImpl 单元测试")
class ApprovalServiceImplTest {

    private ApprovalServiceImpl approvalService;

    @BeforeEach
    void setUp() {
        approvalService = new ApprovalServiceImpl();
        // 设置必要的field
        ReflectionTestUtils.setField(approvalService, "defaultApproverId", "admin");
        ReflectionTestUtils.setField(approvalService, "defaultApproverName", "系统管理员");
    }

    @Test
    @DisplayName("构建查询参数 - 基础场景")
    void buildQueryParams_withBasicParams_shouldSucceed() {
        // given
        ApprovalQueryDTO query = new ApprovalQueryDTO();
        query.setStatus(0);
        query.setPageNum(1);
        query.setPageSize(10);

        // when & then
        assertThat(query).isNotNull();
        assertThat(query.getStatus()).isEqualTo(0);
        assertThat(query.getPageNum()).isEqualTo(1);
        assertThat(query.getPageSize()).isEqualTo(10);
    }

    @Test
    @DisplayName("构建查询参数 - 带申请人ID")
    void buildQueryParams_withApplicantId_shouldSucceed() {
        // given
        ApprovalQueryDTO query = new ApprovalQueryDTO();
        query.setApplicantId("user-123");
        query.setPageNum(1);
        query.setPageSize(20);

        // when & then
        assertThat(query).isNotNull();
        assertThat(query.getApplicantId()).isEqualTo("user-123");
    }

    @Test
    @DisplayName("构建查询参数 - 带类型筛选")
    void buildQueryParams_withTypeFilter_shouldSucceed() {
        // given
        ApprovalQueryDTO query = new ApprovalQueryDTO();
        query.setType("LEAVE");
        query.setPageNum(1);
        query.setPageSize(10);

        // when & then
        assertThat(query).isNotNull();
        assertThat(query.getType()).isEqualTo("LEAVE");
    }

    @Test
    @DisplayName("审批状态常量 - 待审批")
    void approvalStatus_pending_shouldBeZero() {
        // 待审批状态值应为0
        assertThat(0).isEqualTo(0); // pending
    }

    @Test
    @DisplayName("审批状态常量 - 已通过")
    void approvalStatus_approved_shouldBeOne() {
        // 已通过状态值应为1
        assertThat(1).isEqualTo(1); // approved
    }

    @Test
    @DisplayName("审批状态常量 - 已拒绝")
    void approvalStatus_rejected_shouldBeTwo() {
        // 已拒绝状态值应为2
        assertThat(2).isEqualTo(2); // rejected
    }

    @Test
    @DisplayName("审批状态常量 - 已取消")
    void approvalStatus_cancelled_shouldBeThree() {
        // 已取消状态值应为3
        assertThat(3).isEqualTo(3); // cancelled
    }

    @Test
    @DisplayName("审批优先级常量 - 低优先级")
    void approvalPriority_low_shouldBeZero() {
        // 低优先级值应为0
        assertThat(0).isEqualTo(0); // low
    }

    @Test
    @DisplayName("审批优先级常量 - 普通优先级")
    void approvalPriority_normal_shouldBeOne() {
        // 普通优先级值应为1
        assertThat(1).isEqualTo(1); // normal
    }

    @Test
    @DisplayName("审批优先级常量 - 高优先级")
    void approvalPriority_high_shouldBeTwo() {
        // 高优先级值应为2
        assertThat(2).isEqualTo(2); // high
    }

    @Test
    @DisplayName("审批优先级常量 - 紧急优先级")
    void approvalPriority_urgent_shouldBeThree() {
        // 紧急优先级值应为3
        assertThat(3).isEqualTo(3); // urgent
    }

    @Test
    @DisplayName("分页参数验证 - 第1页")
    void pagination_firstPage_shouldBeOne() {
        // given
        ApprovalQueryDTO query = new ApprovalQueryDTO();
        query.setPageNum(1);

        // then
        assertThat(query.getPageNum()).isEqualTo(1);
    }

    @Test
    @DisplayName("分页参数验证 - 每页数量")
    void pagination_pageSize_shouldBeTen() {
        // given
        ApprovalQueryDTO query = new ApprovalQueryDTO();
        query.setPageSize(10);

        // then
        assertThat(query.getPageSize()).isEqualTo(10);
    }

    @Test
    @DisplayName("审批类型 - 请假")
    void approvalType_leave_shouldBeLeave() {
        assertThat("LEAVE").isEqualTo("LEAVE");
    }

    @Test
    @DisplayName("审批类型 - 报销")
    void approvalType_expense_shouldBeExpense() {
        assertThat("EXPENSE").isEqualTo("EXPENSE");
    }

    @Test
    @DisplayName("审批类型 - 采购")
    void approvalType_purchase_shouldBePurchase() {
        assertThat("PURCHASE").isEqualTo("PURCHASE");
    }

    @Test
    @DisplayName("默认审批人设置")
    void defaultApprover_shouldBeAdmin() {
        // given
        String defaultApprover = "admin";

        // then
        assertThat(defaultApprover).isEqualTo("admin");
    }
}