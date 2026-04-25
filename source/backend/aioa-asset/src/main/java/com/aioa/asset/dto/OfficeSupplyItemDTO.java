package com.aioa.asset.dto;

import lombok.Data;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;

/**
 * 办公用品申请明细 DTO
 */
@Data
public class OfficeSupplyItemDTO {
    
    /**
     * 资产ID（办公用品）
     */
    @NotNull(message = "资产ID不能为空")
    private Long assetId;
    
    /**
     * 申请数量
     */
    @NotNull(message = "申请数量不能为空")
    @Min(value = 1, message = "申请数量必须大于0")
    private Integer requestQuantity;
    
    /**
     * 备注
     */
    private String remark;
}