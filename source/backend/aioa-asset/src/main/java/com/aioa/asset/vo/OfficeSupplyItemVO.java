package com.aioa.asset.vo;

import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 办公用品申请明细 VO
 */
@Data
public class OfficeSupplyItemVO {
    
    /**
     * 主键ID
     */
    private Long id;
    
    /**
     * 申请单ID
     */
    private Long requestId;
    
    /**
     * 资产ID（办公用品）
     */
    private Long assetId;
    
    /**
     * 资产编码
     */
    private String assetCode;
    
    /**
     * 资产名称
     */
    private String assetName;
    
    /**
     * 规格型号
     */
    private String specification;
    
    /**
     * 申请数量
     */
    private Integer requestQuantity;
    
    /**
     * 已领取数量
     */
    private Integer claimedQuantity;
    
    /**
     * 单价
     */
    private BigDecimal unitPrice;
    
    /**
     * 总价
     */
    private BigDecimal totalPrice;
    
    /**
     * 库存检查状态
     */
    private Integer inventoryCheckStatus;
    
    /**
     * 库存检查状态名称
     */
    private String inventoryCheckStatusName;
    
    /**
     * 库存检查备注
     */
    private String inventoryCheckComment;
    
    /**
     * 当前库存数量
     */
    private Integer currentInventory;
    
    /**
     * 创建时间
     */
    private LocalDateTime createTime;
    
    /**
     * 备注
     */
    private String remark;
}