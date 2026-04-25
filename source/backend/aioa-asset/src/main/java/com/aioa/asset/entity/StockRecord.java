package com.aioa.asset.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 库存流水记录实体
 */
@Data
@TableName("stock_record")
public class StockRecord {
    
    /**
     * 主键ID
     */
    @TableId(type = IdType.AUTO)
    private Long id;
    
    /**
     * 流水号（唯一标识）
     */
    private String recordNo;
    
    /**
     * 资产ID
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
     * 操作类型：1-入库，2-出库，3-盘点，4-调拨
     */
    private Integer operationType;
    
    /**
     * 操作子类型：101-采购入库，102-退货入库，201-销售出库，202-领用出库，203-报废出库
     */
    private Integer subType;
    
    /**
     * 操作数量
     */
    private Integer quantity;
    
    /**
     * 操作前数量
     */
    private Integer beforeQuantity;
    
    /**
     * 操作后数量
     */
    private Integer afterQuantity;
    
    /**
     * 单价
     */
    private BigDecimal unitPrice;
    
    /**
     * 总金额
     */
    private BigDecimal totalAmount;
    
    /**
     * 批次号
     */
    private String batchNo;
    
    /**
     * 供应商/客户
     */
    private String partner;
    
    /**
     * 仓库/库位
     */
    private String warehouse;
    
    /**
     * 目标仓库/库位（调拨时使用）
     */
    private String targetWarehouse;
    
    /**
     * 经办人
     */
    private String operator;
    
    /**
     * 经办人ID
     */
    private String operatorId;
    
    /**
     * 操作时间
     */
    private LocalDateTime operationTime;
    
    /**
     * 扫描标签编码
     */
    private String scannedLabelCode;
    
    /**
     * 扫描方式：1-扫码枪，2-手机扫码，3-手动输入
     */
    private Integer scanMethod;
    
    /**
     * 审批状态：0-待审批，1-已通过，2-已拒绝
     */
    private Integer approvalStatus;
    
    /**
     * 审批意见
     */
    private String approvalComment;
    
    /**
     * 关联单据号（如采购单号、销售单号）
     */
    private String relatedOrderNo;
    
    /**
     * 状态：0-草稿，1-已确认，2-已作废
     */
    private Integer status;
    
    /**
     * 创建人
     */
    private String createBy;
    
    /**
     * 创建时间
     */
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
    
    /**
     * 更新人
     */
    private String updateBy;
    
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