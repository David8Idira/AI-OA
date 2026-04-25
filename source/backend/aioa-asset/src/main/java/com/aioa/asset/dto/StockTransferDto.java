package com.aioa.asset.dto;

import lombok.Data;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

/**
 * 库存调拨DTO
 */
@Data
public class StockTransferDto {
    
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
     * 源仓库
     */
    @NotBlank(message = "源仓库不能为空")
    private String fromWarehouse;
    
    /**
     * 目标仓库
     */
    @NotBlank(message = "目标仓库不能为空")
    private String toWarehouse;
    
    /**
     * 经办人
     */
    @NotBlank(message = "经办人不能为空")
    private String operator;
    
    /**
     * 经办人ID
     */
    @NotBlank(message = "经办人ID不能为空")
    private String operatorId;
    
    /**
     * 调拨原因
     */
    @NotBlank(message = "调拨原因不能为空")
    private String reason;
    
    /**
     * 备注
     */
    private String remark;
}