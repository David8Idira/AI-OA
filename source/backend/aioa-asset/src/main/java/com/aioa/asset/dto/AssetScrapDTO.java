package com.aioa.asset.dto;

import lombok.Data;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

/**
 * 资产报废DTO
 */
@Data
public class AssetScrapDTO {
    
    /**
     * 资产ID
     */
    @NotNull(message = "资产ID不能为空")
    private Long assetId;
    
    /**
     * 报废数量
     */
    @NotNull(message = "报废数量不能为空")
    @Min(value = 1, message = "报废数量必须大于0")
    private Integer quantity;
    
    /**
     * 报废人
     */
    @NotBlank(message = "报废人不能为空")
    private String operator;
    
    /**
     * 报废人ID
     */
    @NotBlank(message = "报废人ID不能为空")
    private String operatorId;
    
    /**
     * 报废原因
     */
    @NotBlank(message = "报废原因不能为空")
    private String reason;
    
    /**
     * 报废证明
     */
    private String proof;
}