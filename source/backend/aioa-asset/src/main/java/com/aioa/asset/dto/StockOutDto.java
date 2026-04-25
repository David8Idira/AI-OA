package com.aioa.asset.dto;

import lombok.Data;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;

/**
 * 出库DTO
 */
@Data
public class StockOutDto {
    
    /**
     * 标签编码（扫码出库时使用）
     */
    private String labelCode;
    
    /**
     * 资产ID（手动出库时使用）
     */
    private Long assetId;
    
    /**
     * 出库数量
     */
    @NotNull(message = "出库数量不能为空")
    @Min(value = 1, message = "出库数量必须大于0")
    private Integer quantity;
    
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
     * 批次号
     */
    private String batchNo;
    
    /**
     * 客户/领用人
     */
    private String partner;
    
    /**
     * 单价
     */
    private BigDecimal unitPrice;
    
    /**
     * 关联单据号
     */
    private String relatedOrderNo;
    
    /**
     * 备注
     */
    private String remark;
}