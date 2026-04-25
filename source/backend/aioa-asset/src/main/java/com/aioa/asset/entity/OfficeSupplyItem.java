package com.aioa.asset.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 办公用品申请明细实体
 */
@Data
@TableName("office_supply_item")
public class OfficeSupplyItem {
    
    /**
     * 主键ID
     */
    @TableId(type = IdType.AUTO)
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
     * 库存检查状态：0-未检查，1-库存充足，2-库存不足，3-自动排队
     */
    private Integer inventoryCheckStatus;
    
    /**
     * 库存检查备注
     */
    private String inventoryCheckComment;
    
    /**
     * 创建时间
     */
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
    
    /**
     * 更新时间
     */
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
    
    /**
     * 备注
     */
    private String remark;
}