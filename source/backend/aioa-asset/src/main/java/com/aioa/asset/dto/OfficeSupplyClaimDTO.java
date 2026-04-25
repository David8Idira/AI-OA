package com.aioa.asset.dto;

import lombok.Data;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDateTime;

/**
 * 办公用品领用 DTO
 */
@Data
public class OfficeSupplyClaimDTO {
    
    /**
     * 申请单ID
     */
    @NotNull(message = "申请单ID不能为空")
    private Long requestId;
    
    /**
     * 申请明细ID
     */
    private Long itemId;
    
    /**
     * 领用数量
     */
    @NotNull(message = "领用数量不能为空")
    @Min(value = 1, message = "领用数量必须大于0")
    private Integer claimQuantity;
    
    /**
     * 领用人ID
     */
    @NotBlank(message = "领用人ID不能为空")
    private String claimerId;
    
    /**
     * 领用人姓名
     */
    @NotBlank(message = "领用人姓名不能为空")
    private String claimerName;
    
    /**
     * 领用部门ID
     */
    private String claimerDepartmentId;
    
    /**
     * 领用部门名称
     */
    private String claimerDepartmentName;
    
    /**
     * 领用方式：1-扫码领用，2-手动领用
     */
    @NotNull(message = "领用方式不能为空")
    private Integer claimMethod;
    
    /**
     * 领用位置
     */
    private String claimLocation;
    
    /**
     * 仓库/前台管理员ID
     */
    private String warehouseManagerId;
    
    /**
     * 仓库/前台管理员姓名
     */
    private String warehouseManagerName;
    
    /**
     * 备注
     */
    private String remark;
}