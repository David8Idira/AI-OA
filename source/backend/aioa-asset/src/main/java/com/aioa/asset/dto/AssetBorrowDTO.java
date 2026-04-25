package com.aioa.asset.dto;

import lombok.Data;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

/**
 * 资产领用DTO
 */
@Data
public class AssetBorrowDTO {
    
    /**
     * 资产ID
     */
    @NotNull(message = "资产ID不能为空")
    private Long assetId;
    
    /**
     * 领用数量
     */
    @NotNull(message = "领用数量不能为空")
    @Min(value = 1, message = "领用数量必须大于0")
    private Integer quantity;
    
    /**
     * 领用人
     */
    @NotBlank(message = "领用人不能为空")
    private String operator;
    
    /**
     * 领用人ID
     */
    @NotBlank(message = "领用人ID不能为空")
    private String operatorId;
    
    /**
     * 领用原因
     */
    @NotBlank(message = "领用原因不能为空")
    private String reason;
    
    /**
     * 预计归还时间
     */
    private String expectedReturnTime;
}