package com.aioa.asset.dto;

import lombok.Data;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 办公用品申请单 DTO
 */
@Data
public class OfficeSupplyRequestDTO {
    
    /**
     * 申请人ID
     */
    @NotBlank(message = "申请人ID不能为空")
    private String applicantId;
    
    /**
     * 申请人姓名
     */
    @NotBlank(message = "申请人姓名不能为空")
    private String applicantName;
    
    /**
     * 部门ID
     */
    @NotBlank(message = "部门ID不能为空")
    private String departmentId;
    
    /**
     * 部门名称
     */
    @NotBlank(message = "部门名称不能为空")
    private String departmentName;
    
    /**
     * 申请原因
     */
    private String reason;
    
    /**
     * 领用方式：1-一次性领取，2-分批领取
     */
    @NotNull(message = "领用方式不能为空")
    private Integer claimType;
    
    /**
     * 预计领取时间
     */
    private LocalDateTime expectedClaimTime;
    
    /**
     * 紧急程度：1-普通，2-加急，3-紧急
     */
    @NotNull(message = "紧急程度不能为空")
    private Integer urgencyLevel;
    
    /**
     * 备注
     */
    private String remark;
    
    /**
     * 办公用品明细列表
     */
    @NotNull(message = "办公用品明细不能为空")
    private List<OfficeSupplyItemDTO> items;
}