package com.aioa.asset.dto;

import lombok.Data;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

/**
 * 资产归还DTO
 */
@Data
public class AssetReturnDTO {
    
    /**
     * 资产ID
     */
    @NotNull(message = "资产ID不能为空")
    private Long assetId;
    
    /**
     * 归还数量
     */
    @NotNull(message = "归还数量不能为空")
    @Min(value = 1, message = "归还数量必须大于0")
    private Integer quantity;
    
    /**
     * 归还人
     */
    @NotBlank(message = "归还人不能为空")
    private String operator;
    
    /**
     * 归还说明
     */
    private String remark;
}