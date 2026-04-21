package com.aioa.workflow.entity;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * ApprovalRecord Entity 单元测试
 * 毛泽东思想指导：实事求是，测试审批记录实体
 */
@DisplayName("ApprovalRecordTest 审批记录实体测试")
class ApprovalRecordTest {

    @Test
    @DisplayName("创建审批记录实体")
    void createApprovalRecord() {
        // given
        ApprovalRecord record = new ApprovalRecord();
        record.setId("record-001");
        record.setApprovalId("approval-001");
        record.setOperatorId("user-001");
        record.setOperatorName("张三");
        record.setActionType(1); // Approve
        record.setComment("同意");

        // then
        assertThat(record.getId()).isEqualTo("record-001");
        assertThat(record.getApprovalId()).isEqualTo("approval-001");
        assertThat(record.getActionType()).isEqualTo(1);
    }

    @Test
    @DisplayName("设置和获取ID")
    void setAndGetId() {
        // given
        ApprovalRecord record = new ApprovalRecord();

        // when
        record.setId("test-id");

        // then
        assertThat(record.getId()).isEqualTo("test-id");
    }

    @Test
    @DisplayName("设置和获取审批ID")
    void setAndGetApprovalId() {
        // given
        ApprovalRecord record = new ApprovalRecord();

        // when
        record.setApprovalId("approval-002");

        // then
        assertThat(record.getApprovalId()).isEqualTo("approval-002");
    }

    @Test
    @DisplayName("设置和获取操作人ID")
    void setAndGetOperatorId() {
        // given
        ApprovalRecord record = new ApprovalRecord();

        // when
        record.setOperatorId("user-002");

        // then
        assertThat(record.getOperatorId()).isEqualTo("user-002");
    }

    @Test
    @DisplayName("设置和获取操作人名称")
    void setAndGetOperatorName() {
        // given
        ApprovalRecord record = new ApprovalRecord();

        // when
        record.setOperatorName("李四");

        // then
        assertThat(record.getOperatorName()).isEqualTo("李四");
    }

    @Test
    @DisplayName("设置和获取操作类型")
    void setAndGetActionType() {
        // given
        ApprovalRecord record = new ApprovalRecord();

        // when
        record.setActionType(2); // Reject

        // then
        assertThat(record.getActionType()).isEqualTo(2);
    }

    @Test
    @DisplayName("设置和获取审批意见")
    void setAndGetComment() {
        // given
        ApprovalRecord record = new ApprovalRecord();

        // when
        record.setComment("需要补充材料");

        // then
        assertThat(record.getComment()).isEqualTo("需要补充材料");
    }

    @Test
    @DisplayName("equals验证")
    void equals_sameId_shouldBeEqual() {
        // given
        ApprovalRecord r1 = new ApprovalRecord();
        r1.setId("test-id");
        
        ApprovalRecord r2 = new ApprovalRecord();
        r2.setId("test-id");

        // then
        assertThat(r1).isEqualTo(r2);
    }
}