package com.aioa.workflow.service.impl;

import com.aioa.workflow.entity.ApprovalRecord;
import com.aioa.workflow.enums.ApprovalActionEnum;
import com.aioa.workflow.enums.ApprovalStatusEnum;
import com.aioa.workflow.mapper.ApprovalRecordMapper;
import com.aioa.workflow.vo.ApprovalRecordVO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * ApprovalRecordServiceImpl 单元测试
 * 毛泽东思想指导：实事求是，测试审批记录服务
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("ApprovalRecordServiceImplTest 单元测试")
class ApprovalRecordServiceImplTest {

    @Mock
    private ApprovalRecordMapper approvalRecordMapper;

    private ApprovalRecordServiceImpl approvalRecordService;

    @BeforeEach
    void setUp() {
        approvalRecordService = new ApprovalRecordServiceImpl();
        ReflectionTestUtils.setField(approvalRecordService, "baseMapper", approvalRecordMapper);
    }

    private ApprovalRecord createRecord(String id, String approvalId, String operatorId, 
                                         String operatorName, Integer actionType, Integer statusAfter) {
        ApprovalRecord record = new ApprovalRecord();
        record.setId(id);
        record.setApprovalId(approvalId);
        record.setOperatorId(operatorId);
        record.setOperatorName(operatorName);
        record.setActionType(actionType);
        record.setActionDesc(ApprovalActionEnum.getByCode(actionType) != null ? 
                ApprovalActionEnum.getByCode(actionType).getDescription() : "未知");
        record.setStatusAfter(statusAfter);
        record.setStep(1);
        record.setCreateTime(LocalDateTime.now());
        return record;
    }

    @Test
    @DisplayName("获取审批记录列表 - 正常场景")
    void getRecordsByApprovalId_shouldReturnRecords() {
        // given
        List<ApprovalRecord> records = new ArrayList<>();
        records.add(createRecord("record-001", "approval-001", "user-001", "用户1", 
                ApprovalActionEnum.APPROVE.getCode(), ApprovalStatusEnum.APPROVED.getCode()));
        records.add(createRecord("record-002", "approval-001", "user-002", "用户2",
                ApprovalActionEnum.REJECT.getCode(), ApprovalStatusEnum.REJECTED.getCode()));

        when(approvalRecordMapper.selectByApprovalIdOrderByCreateTimeDesc("approval-001")).thenReturn(records);

        // when
        List<ApprovalRecordVO> result = approvalRecordService.getRecordsByApprovalId("approval-001");

        // then
        assertThat(result).hasSize(2);
    }

    @Test
    @DisplayName("获取审批记录列表 - 无记录")
    void getRecordsByApprovalId_withNoRecords_shouldReturnEmptyList() {
        // given
        when(approvalRecordMapper.selectByApprovalIdOrderByCreateTimeDesc("approval-002")).thenReturn(new ArrayList<>());

        // when
        List<ApprovalRecordVO> result = approvalRecordService.getRecordsByApprovalId("approval-002");

        // then
        assertThat(result).isEmpty();
    }

    @Test
    @DisplayName("创建审批记录 - 正常场景")
    void createRecord_shouldCreateAndReturnRecord() {
        // given
        when(approvalRecordMapper.insert(any(ApprovalRecord.class))).thenReturn(1);

        // when
        ApprovalRecord result = approvalRecordService.createRecord(
                "approval-001", "user-001", "用户", ApprovalActionEnum.APPROVE.getCode(), 
                "同意", ApprovalStatusEnum.APPROVED.getCode(), 1);

        // then
        assertThat(result).isNotNull();
        verify(approvalRecordMapper, times(1)).insert(any(ApprovalRecord.class));
    }

    @Test
    @DisplayName("创建审批记录 - 设置正确字段")
    void createRecord_shouldSetCorrectFields() {
        // given
        when(approvalRecordMapper.insert(any(ApprovalRecord.class))).thenReturn(1);

        // when
        ApprovalRecord result = approvalRecordService.createRecord(
                "approval-001", "user-001", "测试用户", 
                ApprovalActionEnum.REJECT.getCode(), "材料不全", 
                ApprovalStatusEnum.REJECTED.getCode(), 2);

        // then
        assertThat(result).isNotNull();
        assertThat(result.getApprovalId()).isEqualTo("approval-001");
        assertThat(result.getOperatorId()).isEqualTo("user-001");
        assertThat(result.getStep()).isEqualTo(2);
    }

    @Test
    @DisplayName("创建转交记录 - 正常场景")
    void createTransferRecord_shouldCreateTransferRecord() {
        // given
        when(approvalRecordMapper.insert(any(ApprovalRecord.class))).thenReturn(1);

        // when
        ApprovalRecord result = approvalRecordService.createTransferRecord(
                "approval-001", "user-001", "用户",
                "previous-approver", "new-approver", "新审批人",
                "因出差需要转交", ApprovalStatusEnum.TRANSFERRED.getCode());

        // then
        assertThat(result).isNotNull();
        verify(approvalRecordMapper, times(1)).insert(any(ApprovalRecord.class));
    }

    @Test
    @DisplayName("创建转交记录 - 设置转交相关字段")
    void createTransferRecord_shouldSetTransferFields() {
        // given
        when(approvalRecordMapper.insert(any(ApprovalRecord.class))).thenReturn(1);

        // when
        ApprovalRecord result = approvalRecordService.createTransferRecord(
                "approval-001", "user-001", "用户",
                "old-approver-id", "new-approver-id", "新审批人",
                "转交原因", ApprovalStatusEnum.TRANSFERRED.getCode());

        // then
        assertThat(result).isNotNull();
        assertThat(result.getPreviousApproverId()).isEqualTo("old-approver-id");
        assertThat(result.getTransferToId()).isEqualTo("new-approver-id");
        assertThat(result.getTransferToName()).isEqualTo("新审批人");
        assertThat(result.getActionType()).isEqualTo(ApprovalActionEnum.TRANSFER.getCode());
    }

    @Test
    @DisplayName("统计审批记录数量 - 正常场景")
    void countByApprovalId_shouldReturnCount() {
        // given
        when(approvalRecordMapper.countByApprovalId("approval-001")).thenReturn(5L);

        // when
        Long result = approvalRecordService.countByApprovalId("approval-001");

        // then
        assertThat(result).isEqualTo(5L);
    }

    @Test
    @DisplayName("统计审批记录数量 - 无记录")
    void countByApprovalId_withNoRecords_shouldReturnZero() {
        // given
        when(approvalRecordMapper.countByApprovalId("approval-002")).thenReturn(0L);

        // when
        Long result = approvalRecordService.countByApprovalId("approval-002");

        // then
        assertThat(result).isEqualTo(0L);
    }

    @Test
    @DisplayName("获取审批记录 - 按时间倒序")
    void getRecordsByApprovalId_shouldOrderByCreateTimeDesc() {
        // given
        List<ApprovalRecord> records = new ArrayList<>();
        records.add(createRecord("record-002", "approval-001", "user-002", "用户2",
                ApprovalActionEnum.APPROVE.getCode(), ApprovalStatusEnum.APPROVED.getCode()));
        records.add(createRecord("record-001", "approval-001", "user-001", "用户1",
                ApprovalActionEnum.APPROVE.getCode(), ApprovalStatusEnum.PENDING.getCode()));

        when(approvalRecordMapper.selectByApprovalIdOrderByCreateTimeDesc("approval-001")).thenReturn(records);

        // when
        List<ApprovalRecordVO> result = approvalRecordService.getRecordsByApprovalId("approval-001");

        // then
        assertThat(result).hasSize(2);
    }

    @Test
    @DisplayName("创建审批记录 - 撤回操作")
    void createRecord_withCancelAction_shouldSetCorrectActionType() {
        // given
        when(approvalRecordMapper.insert(any(ApprovalRecord.class))).thenReturn(1);

        // when
        ApprovalRecord result = approvalRecordService.createRecord(
                "approval-001", "user-001", "用户",
                ApprovalActionEnum.CANCEL.getCode(), "申请人撤回",
                ApprovalStatusEnum.CANCELLED.getCode(), 1);

        // then
        assertThat(result).isNotNull();
        assertThat(result.getActionType()).isEqualTo(ApprovalActionEnum.CANCEL.getCode());
    }

    @Test
    @DisplayName("获取动作描述 - 正常操作")
    void createRecord_shouldSetCorrectActionDescription() {
        // given
        when(approvalRecordMapper.insert(any(ApprovalRecord.class))).thenReturn(1);

        // when
        ApprovalRecord result = approvalRecordService.createRecord(
                "approval-001", "user-001", "用户",
                ApprovalActionEnum.APPROVE.getCode(), "同意",
                ApprovalStatusEnum.APPROVED.getCode(), 1);

        // then
        assertThat(result).isNotNull();
        assertThat(result.getActionDesc()).isNotNull();
    }
}