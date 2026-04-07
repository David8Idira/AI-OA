package com.aioa.workflow.vo;

import com.aioa.workflow.entity.ApprovalRecord;
import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * Approval Record View Object
 */
@Data
@Schema(name = "ApprovalRecordVO", description = "Approval record view object")
public class ApprovalRecordVO {

    @Schema(description = "Record ID")
    private String id;

    @Schema(description = "Approval ID")
    private String approvalId;

    @Schema(description = "Operator ID")
    private String operatorId;

    @Schema(description = "Operator name")
    private String operatorName;

    @Schema(description = "Action type: 1-Approve, 2-Reject, 3-Transfer, 4-Cancel")
    private Integer actionType;

    @Schema(description = "Action description")
    private String actionDesc;

    @Schema(description = "Comment")
    private String comment;

    @Schema(description = "Status after action")
    private Integer statusAfter;

    @Schema(description = "Step number")
    private Integer step;

    @Schema(description = "Transfer target user ID")
    private String transferToId;

    @Schema(description = "Transfer target user name")
    private String transferToName;

    @Schema(description = "Create time")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createTime;

    @Schema(description = "Attachments")
    private String attachments;

    /**
     * Build ApprovalRecordVO from ApprovalRecord entity
     */
    public static ApprovalRecordVO fromEntity(ApprovalRecord record) {
        if (record == null) {
            return null;
        }
        ApprovalRecordVO vo = new ApprovalRecordVO();
        vo.setId(record.getId());
        vo.setApprovalId(record.getApprovalId());
        vo.setOperatorId(record.getOperatorId());
        vo.setOperatorName(record.getOperatorName());
        vo.setActionType(record.getActionType());
        vo.setActionDesc(record.getActionDesc());
        vo.setComment(record.getComment());
        vo.setStatusAfter(record.getStatusAfter());
        vo.setStep(record.getStep());
        vo.setTransferToId(record.getTransferToId());
        vo.setTransferToName(record.getTransferToName());
        vo.setCreateTime(record.getCreateTime());
        vo.setAttachments(record.getAttachments());
        return vo;
    }
}
