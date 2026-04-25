package com.aioa.asset.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

/**
 * 办公用品领用记录实体
 */
@Data
@TableName("office_supply_claim")
public class OfficeSupplyClaim {
    
    /**
     * 主键ID
     */
    @TableId(type = IdType.AUTO)
    private Long id;
    
    /**
     * 领用单号
     */
    private String claimNo;
    
    /**
     * 申请单ID
     */
    private Long requestId;
    
    /**
     * 申请明细ID
     */
    private Long itemId;
    
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
     * 领用数量
     */
    private Integer claimQuantity;
    
    /**
     * 领用人ID
     */
    private String claimerId;
    
    /**
     * 领用人姓名
     */
    private String claimerName;
    
    /**
     * 领用部门ID
     */
    private String claimerDepartmentId;
    
    /**
     * 领用部门名称
     */
    private String claimerDepartmentName;
    
    /**
     * 领用时间
     */
    private LocalDateTime claimTime;
    
    /**
     * 领用方式：1-扫码领用，2-手动领用
     */
    private Integer claimMethod;
    
    /**
     * 领用二维码
     */
    private String claimQrCode;
    
    /**
     * 二维码失效时间
     */
    private LocalDateTime qrCodeExpireTime;
    
    /**
     * 签收状态：0-待签收，1-已签收，2-已取消
     */
    private Integer signStatus;
    
    /**
     * 签收人
     */
    private String signer;
    
    /**
     * 签收时间
     */
    private LocalDateTime signTime;
    
    /**
     * 仓库/前台管理员ID
     */
    private String warehouseManagerId;
    
    /**
     * 仓库/前台管理员姓名
     */
    private String warehouseManagerName;
    
    /**
     * 领用位置
     */
    private String claimLocation;
    
    /**
     * 创建时间
     */
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
    
    /**
     * 创建人
     */
    private String createBy;
    
    /**
     * 更新时间
     */
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
    
    /**
     * 更新人
     */
    private String updateBy;
    
    /**
     * 备注
     */
    private String remark;
}