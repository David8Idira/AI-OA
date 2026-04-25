package com.aioa.asset.dto;

import lombok.Data;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

/**
 * 库存盘点DTO
 */
@Data
public class StockCheckDto {
    
    /**
     * 资产ID
     */
    @NotNull(message = "资产ID不能为空")
    private Long assetId;
    
    /**
     * 实际数量
     */
    @NotNull(message = "实际数量不能为空")
    @Min(value = 0, message = "实际数量不能为负数")
    private Integer actualQuantity;
    
    /**
     * 仓库/库位
     */
    @NotBlank(message = "仓库不能为空")
    private String warehouse;
    
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
     * 盘点说明
     */
    private String remark;
}