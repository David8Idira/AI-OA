package com.aioa.asset.dto;

import lombok.Data;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

/**
 * 办公用品审批 DTO
 */
@Data
public class OfficeSupplyApproveDTO {
    
    /**
     * 申请单ID
     */
    @NotNull(message = "申请单ID不能为空")
    private Long requestId;
    
    /**
     * 审批结果：true-通过，false-拒绝
     */
    @NotNull(message = "审批结果不能为空")
    private Boolean approveResult;
    
    /**
     * 审批意见
     */
    private String approveComment;
    
    /**
     * 审批人ID
     */
    @NotBlank(message = "审批人ID不能为空")
    private String approverId;
    
    /**
     * 审批人姓名
     */
    @NotBlank(message = "审批人姓名不能为空")
    private String approverName;
}