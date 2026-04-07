package com.aioa.workflow.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.aioa.workflow.entity.ApprovalRecord;
import com.aioa.workflow.enums.ApprovalActionEnum;
import com.aioa.workflow.enums.ApprovalStatusEnum;
import com.aioa.workflow.mapper.ApprovalRecordMapper;
import com.aioa.workflow.service.ApprovalRecordService;
import com.aioa.workflow.vo.ApprovalRecordVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Approval Record Service Implementation
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ApprovalRecordServiceImpl extends ServiceImpl<ApprovalRecordMapper, ApprovalRecord>
    implements ApprovalRecordService {

    @Override
    public List<ApprovalRecordVO> getRecordsByApprovalId(String approvalId) {
        List<ApprovalRecord> records = baseMapper.selectByApprovalIdOrderByCreateTimeDesc(approvalId);
        return records.stream()
            .map(ApprovalRecordVO::fromEntity)
            .collect(Collectors.toList());
    }

    @Override
    public ApprovalRecord createRecord(String approvalId, String operatorId, String operatorName,
                                        Integer actionType, String comment, Integer statusAfter, Integer step) {
        ApprovalRecord record = new ApprovalRecord();
        record.setApprovalId(approvalId);
        record.setOperatorId(operatorId);
        record.setOperatorName(operatorName);
        record.setActionType(actionType);
        record.setActionDesc(getActionDescription(actionType));
        record.setComment(comment);
        record.setStatusAfter(statusAfter);
        record.setStep(step);
        record.setCreateTime(java.time.LocalDateTime.now());

        this.save(record);
        log.debug("Approval record created: id={}, approvalId={}, actionType={}", record.getId(), approvalId, actionType);
        return record;
    }

    @Override
    public ApprovalRecord createTransferRecord(String approvalId, String operatorId, String operatorName,
                                                 String fromApproverId, String toApproverId,
                                                 String toApproverName, String comment, Integer statusAfter) {
        ApprovalRecord record = new ApprovalRecord();
        record.setApprovalId(approvalId);
        record.setOperatorId(operatorId);
        record.setOperatorName(operatorName);
        record.setActionType(ApprovalActionEnum.TRANSFER.getCode());
        record.setActionDesc(ApprovalActionEnum.TRANSFER.getDescription());
        record.setComment(comment);
        record.setStatusAfter(statusAfter);
        record.setPreviousApproverId(fromApproverId);
        record.setTransferToId(toApproverId);
        record.setTransferToName(toApproverName);
        record.setStep(0); // Transfer doesn't count as a step
        record.setCreateTime(java.time.LocalDateTime.now());

        this.save(record);
        log.debug("Transfer record created: id={}, approvalId={}, from={} to={}",
                  record.getId(), approvalId, fromApproverId, toApproverId);
        return record;
    }

    @Override
    public Long countByApprovalId(String approvalId) {
        return baseMapper.countByApprovalId(approvalId);
    }

    /**
     * Get human-readable action description
     */
    private String getActionDescription(Integer actionType) {
        ApprovalActionEnum action = ApprovalActionEnum.getByCode(actionType);
        if (action != null) {
            return action.getDescription();
        }

        // Fallback for "submit" which reuses CANCEL code
        ApprovalStatusEnum status = ApprovalStatusEnum.getByCode(actionType);
        if (status != null) {
            return status.getDescription();
        }

        return "未知操作";
    }
}
