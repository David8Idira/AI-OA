package com.aioa.asset.vo;

import lombok.Data;
import java.time.LocalDateTime;

/**
 * 办公用品领用记录 VO
 */
@Data
public class OfficeSupplyClaimVO {
    
    /**
     * 主键ID
     */
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
     * 领用方式
     */
    private Integer claimMethod;
    
    /**
     * 领用方式名称
     */
    private String claimMethodName;
    
    /**
     * 领用二维码
     */
    private String claimQrCode;
    
    /**
     * 二维码失效时间
     */
    private LocalDateTime qrCodeExpireTime;
    
    /**
     * 签收状态
     */
    private Integer signStatus;
    
    /**
     * 签收状态名称
     */
    private String signStatusName;
    
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
    private LocalDateTime createTime;
    
    /**
     * 备注
     */
    private String remark;
}