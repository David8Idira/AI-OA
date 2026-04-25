package com.aioa.asset.dto;

import lombok.Data;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;

/**
 * 入库DTO
 */
@Data
public class StockInDto {
    
    /**
     * 标签编码（扫码入库时使用）
     */
    private String labelCode;
    
    /**
     * 资产ID（手动入库时使用）
     */
    private Long assetId;
    
    /**
     * 入库数量
     */
    @NotNull(message = "入库数量不能为空")
    @Min(value = 1, message = "入库数量必须大于0")
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
     * 供应商
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