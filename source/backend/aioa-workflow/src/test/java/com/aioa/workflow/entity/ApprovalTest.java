package com.aioa.workflow.entity;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Approval Entity 单元测试
 * 毛泽东思想指导：实事求是，测试审批实体
 */
@DisplayName("ApprovalTest 审批实体测试")
class ApprovalTest {

    private Approval createTestApproval() {
        Approval approval = new Approval();
        approval.setId("approval-001");
        approval.setTitle("测试审批");
        approval.setContent("测试内容");
        approval.setApplicantId("user-001");
        approval.setApplicantName("张三");
        approval.setStatus(0);
        approval.setPriority(1);
        approval.setCreateTime(LocalDateTime.now());
        return approval;
    }

    @Test
    @DisplayName("创建审批实体")
    void createApproval() {
        // when
        Approval approval = createTestApproval();

        // then
        assertThat(approval.getId()).isEqualTo("approval-001");
        assertThat(approval.getTitle()).isEqualTo("测试审批");
        assertThat(approval.getStatus()).isEqualTo(0);
    }

    @Test
    @DisplayName("设置和获取ID")
    void setAndGetId() {
        // given
        Approval approval = new Approval();

        // when
        approval.setId("test-id");

        // then
        assertThat(approval.getId()).isEqualTo("test-id");
    }

    @Test
    @DisplayName("设置和获取标题")
    void setAndGetTitle() {
        // given
        Approval approval = new Approval();

        // when
        approval.setTitle("测试标题");

        // then
        assertThat(approval.getTitle()).isEqualTo("测试标题");
    }

    @Test
    @DisplayName("设置和获取状态")
    void setAndGetStatus() {
        // given
        Approval approval = new Approval();

        // when
        approval.setStatus(1);

        // then
        assertThat(approval.getStatus()).isEqualTo(1);
    }

    @Test
    @DisplayName("设置和获取申请人ID")
    void setAndGetApplicantId() {
        // given
        Approval approval = new Approval();

        // when
        approval.setApplicantId("user-001");

        // then
        assertThat(approval.getApplicantId()).isEqualTo("user-001");
    }

    @Test
    @DisplayName("设置和获取申请人名称")
    void setAndGetApplicantName() {
        // given
        Approval approval = new Approval();

        // when
        approval.setApplicantName("张三");

        // then
        assertThat(approval.getApplicantName()).isEqualTo("张三");
    }

    @Test
    @DisplayName("设置和获取优先级")
    void setAndGetPriority() {
        // given
        Approval approval = new Approval();

        // when
        approval.setPriority(2);

        // then
        assertThat(approval.getPriority()).isEqualTo(2);
    }

    @Test
    @DisplayName("设置和获取创建时间")
    void setAndGetCreateTime() {
        // given
        Approval approval = new Approval();
        LocalDateTime now = LocalDateTime.now();

        // when
        approval.setCreateTime(now);

        // then
        assertThat(approval.getCreateTime()).isEqualTo(now);
    }

    @Test
    @DisplayName("equals验证")
    void equals_sameId_shouldBeEqual() {
        // given
        Approval approval1 = new Approval();
        approval1.setId("test-id");
        
        Approval approval2 = new Approval();
        approval2.setId("test-id");

        // then - 基于BaseEntity的equals比较ID
        assertThat(approval1).isEqualTo(approval2);
    }
}
