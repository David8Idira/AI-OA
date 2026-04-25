package com.aioa.asset.dto;

import lombok.Data;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

/**
 * 资产调拨DTO
 */
@Data
public class AssetTransferDTO {
    
    /**
     * 资产ID
     */
    @NotNull(message = "资产ID不能为空")
    private Long assetId;
    
    /**
     * 调拨数量
     */
    @NotNull(message = "调拨数量不能为空")
    @Min(value = 1, message = "调拨数量必须大于0")
    private Integer quantity;
    
    /**
     * 调拨人
     */
    @NotBlank(message = "调拨人不能为空")
    private String operator;
    
    /**
     * 调拨人ID
     */
    @NotBlank(message = "调拨人ID不能为空")
    private String operatorId;
    
    /**
     * 目标部门
     */
    @NotBlank(message = "目标部门不能为空")
    private String targetDepartment;
    
    /**
     * 调拨原因
     */
    @NotBlank(message = "调拨原因不能为空")
    private String reason;
}