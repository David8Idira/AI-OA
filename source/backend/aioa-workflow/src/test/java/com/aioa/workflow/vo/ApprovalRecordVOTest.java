package com.aioa.workflow.vo;

import com.aioa.workflow.entity.ApprovalRecord;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

/**
 * ApprovalRecordVO测试
 */
@DisplayName("ApprovalRecordVOTest 审批记录VO测试")
class ApprovalRecordVOTest {

    @Test
    @DisplayName("默认构造")
    void defaultConstructor() {
        ApprovalRecordVO vo = new ApprovalRecordVO();
        assertNotNull(vo);
    }

    @Test
    @DisplayName("设置字段")
    void setters() {
        ApprovalRecordVO vo = new ApprovalRecordVO();
        vo.setId("record-001");
        vo.setApprovalId("approval-001");
        vo.setOperatorId("user-001");
        vo.setOperatorName("张三");
        vo.setActionType(1);
        vo.setActionDesc("同意");
        vo.setComment("符合条件");
        vo.setStatusAfter(2);
        vo.setStep(1);
        vo.setTransferToId(null);
        vo.setTransferToName(null);
        vo.setCreateTime(LocalDateTime.of(2024, 6, 15, 10, 30));
        vo.setAttachments("https://example.com/file.pdf");

        assertEquals("record-001", vo.getId());
        assertEquals("approval-001", vo.getApprovalId());
        assertEquals("user-001", vo.getOperatorId());
        assertEquals("张三", vo.getOperatorName());
        assertEquals(1, vo.getActionType());
        assertEquals("同意", vo.getActionDesc());
        assertEquals("符合条件", vo.getComment());
        assertEquals(2, vo.getStatusAfter());
        assertEquals(1, vo.getStep());
        assertNull(vo.getTransferToId());
        assertNull(vo.getTransferToName());
        assertEquals(LocalDateTime.of(2024, 6, 15, 10, 30), vo.getCreateTime());
        assertEquals("https://example.com/file.pdf", vo.getAttachments());
    }

    @Test
    @DisplayName("从实体转换-正常")
    void fromEntity_normal() {
        ApprovalRecord entity = new ApprovalRecord();
        entity.setId("record-001");
        entity.setApprovalId("approval-001");
        entity.setOperatorId("user-001");
        entity.setOperatorName("李四");
        entity.setActionType(2);
        entity.setActionDesc("拒绝");
        entity.setComment("材料不全");
        entity.setStatusAfter(3);
        entity.setStep(1);
        entity.setTransferToId(null);
        entity.setTransferToName(null);
        entity.setCreateTime(LocalDateTime.of(2024, 6, 16, 14, 0));
        entity.setAttachments(null);

        ApprovalRecordVO vo = ApprovalRecordVO.fromEntity(entity);

        assertNotNull(vo);
        assertEquals("record-001", vo.getId());
        assertEquals("approval-001", vo.getApprovalId());
        assertEquals("user-001", vo.getOperatorId());
        assertEquals("李四", vo.getOperatorName());
        assertEquals(2, vo.getActionType());
        assertEquals("拒绝", vo.getActionDesc());
        assertEquals("材料不全", vo.getComment());
        assertEquals(3, vo.getStatusAfter());
        assertEquals(1, vo.getStep());
        assertNull(vo.getTransferToId());
        assertNull(vo.getTransferToName());
        assertEquals(LocalDateTime.of(2024, 6, 16, 14, 0), vo.getCreateTime());
        assertNull(vo.getAttachments());
    }

    @Test
    @DisplayName("从实体转换-空值")
    void fromEntity_null() {
        ApprovalRecordVO vo = ApprovalRecordVO.fromEntity(null);
        assertNull(vo);
    }

    @Test
    @DisplayName("审批动作类型-同意")
    void actionTypeApprove() {
        ApprovalRecordVO vo = new ApprovalRecordVO();
        vo.setActionType(1);
        assertEquals(1, vo.getActionType());
    }

    @Test
    @DisplayName("审批动作类型-拒绝")
    void actionTypeReject() {
        ApprovalRecordVO vo = new ApprovalRecordVO();
        vo.setActionType(2);
        assertEquals(2, vo.getActionType());
    }

    @Test
    @DisplayName("审批动作类型-转交")
    void actionTypeTransfer() {
        ApprovalRecordVO vo = new ApprovalRecordVO();
        vo.setActionType(3);
        vo.setTransferToId("user-002");
        vo.setTransferToName("王五");
        assertEquals(3, vo.getActionType());
        assertEquals("user-002", vo.getTransferToId());
        assertEquals("王五", vo.getTransferToName());
    }

    @Test
    @DisplayName("审批动作类型-取消")
    void actionTypeCancel() {
        ApprovalRecordVO vo = new ApprovalRecordVO();
        vo.setActionType(4);
        assertEquals(4, vo.getActionType());
    }

    @Test
    @DisplayName("步骤编号")
    void stepNumber() {
        ApprovalRecordVO vo = new ApprovalRecordVO();
        vo.setStep(1);
        assertEquals(1, vo.getStep());
        
        vo.setStep(2);
        assertEquals(2, vo.getStep());
    }

    @Test
    @DisplayName("创建时间格式")
    void createTimeFormat() {
        ApprovalRecordVO vo = new ApprovalRecordVO();
        LocalDateTime now = LocalDateTime.now();
        vo.setCreateTime(now);
        assertEquals(now, vo.getCreateTime());
    }

    @Test
    @DisplayName("多步骤审批记录")
    void multiStepApproval() {
        // Step 1
        ApprovalRecord step1 = new ApprovalRecord();
        step1.setId("record-001");
        step1.setApprovalId("approval-001");
        step1.setOperatorId("manager-001");
        step1.setOperatorName("主管");
        step1.setActionType(1);
        step1.setActionDesc("同意");
        step1.setStatusAfter(1);
        step1.setStep(1);
        step1.setCreateTime(LocalDateTime.of(2024, 6, 15, 10, 0));

        ApprovalRecordVO vo1 = ApprovalRecordVO.fromEntity(step1);
        assertEquals(1, vo1.getStep());
        assertEquals(1, vo1.getActionType());

        // Step 2
        ApprovalRecord step2 = new ApprovalRecord();
        step2.setId("record-002");
        step2.setApprovalId("approval-001");
        step2.setOperatorId("director-001");
        step2.setOperatorName("总监");
        step2.setActionType(1);
        step2.setActionDesc("同意");
        step2.setStatusAfter(2);
        step2.setStep(2);
        step2.setCreateTime(LocalDateTime.of(2024, 6, 15, 14, 0));

        ApprovalRecordVO vo2 = ApprovalRecordVO.fromEntity(step2);
        assertEquals(2, vo2.getStep());
        assertEquals(1, vo2.getActionType());
    }
}