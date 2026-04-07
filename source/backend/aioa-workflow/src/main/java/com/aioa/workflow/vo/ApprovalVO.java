package com.aioa.workflow.vo;

import com.aioa.workflow.entity.Approval;
import com.aioa.workflow.entity.ApprovalRecord;
import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Approval View Object
 */
@Data
@Schema(name = "ApprovalVO", description = "Approval view object")
public class ApprovalVO {

    @Schema(description = "Approval ID")
    private String id;

    @Schema(description = "Title")
    private String title;

    @Schema(description = "Type")
    private String type;

    @Schema(description = "Type description")
    private String typeDesc;

    @Schema(description = "Content")
    private String content;

    @Schema(description = "Status: 0-Pending, 1-Approved, 2-Rejected, 3-Cancelled, 4-Transferred")
    private Integer status;

    @Schema(description = "Status description")
    private String statusDesc;

    @Schema(description = "Priority: 0-Low, 1-Normal, 2-High, 3-Urgent")
    private Integer priority;

    @Schema(description = "Priority description")
    private String priorityDesc;

    @Schema(description = "Applicant ID")
    private String applicantId;

    @Schema(description = "Applicant name")
    private String applicantName;

    @Schema(description = "Approver ID")
    private String approverId;

    @Schema(description = "Approver name")
    private String approverName;

    @Schema(description = "Department ID")
    private String deptId;

    @Schema(description = "Department name")
    private String deptName;

    @Schema(description = "CC users")
    private String ccUsers;

    @Schema(description = "Expected finish time")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime expectFinishTime;

    @Schema(description = "Actual finish time")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime finishTime;

    @Schema(description = "Current step")
    private Integer currentStep;

    @Schema(description = "Total steps")
    private Integer totalSteps;

    @Schema(description = "Attachments")
    private String attachments;

    @Schema(description = "Form data")
    private String formData;

    @Schema(description = "Remark")
    private String remark;

    @Schema(description = "Approval comment")
    private String approvalComment;

    @Schema(description = "Create time")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createTime;

    @Schema(description = "Update time")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime updateTime;

    @Schema(description = "Approval history records")
    private List<ApprovalRecordVO> records;

    /**
     * Build ApprovalVO from Approval entity
     */
    public static ApprovalVO fromEntity(Approval approval) {
        if (approval == null) {
            return null;
        }
        ApprovalVO vo = new ApprovalVO();
        vo.setId(approval.getId());
        vo.setTitle(approval.getTitle());
        vo.setType(approval.getType());
        vo.setContent(approval.getContent());
        vo.setStatus(approval.getStatus());
        vo.setPriority(approval.getPriority());
        vo.setApplicantId(approval.getApplicantId());
        vo.setApplicantName(approval.getApplicantName());
        vo.setApproverId(approval.getApproverId());
        vo.setApproverName(approval.getApproverName());
        vo.setDeptId(approval.getDeptId());
        vo.setDeptName(approval.getDeptName());
        vo.setCcUsers(approval.getCcUsers());
        vo.setExpectFinishTime(approval.getExpectFinishTime());
        vo.setFinishTime(approval.getFinishTime());
        vo.setCurrentStep(approval.getCurrentStep());
        vo.setTotalSteps(approval.getTotalSteps());
        vo.setAttachments(approval.getAttachments());
        vo.setFormData(approval.getFormData());
        vo.setRemark(approval.getRemark());
        vo.setApprovalComment(approval.getApprovalComment());
        vo.setCreateTime(approval.getCreateTime());
        vo.setUpdateTime(approval.getUpdateTime());
        return vo;
    }
}
