package com.aioa.asset.vo;

import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 办公用品申请单 VO
 */
@Data
public class OfficeSupplyRequestVO {
    
    /**
     * 主键ID
     */
    private Long id;
    
    /**
     * 申请单号
     */
    private String requestNo;
    
    /**
     * 申请人ID
     */
    private String applicantId;
    
    /**
     * 申请人姓名
     */
    private String applicantName;
    
    /**
     * 部门ID
     */
    private String departmentId;
    
    /**
     * 部门名称
     */
    private String departmentName;
    
    /**
     * 申请原因
     */
    private String reason;
    
    /**
     * 申请状态
     */
    private Integer requestStatus;
    
    /**
     * 申请状态名称
     */
    private String requestStatusName;
    
    /**
     * 审批人ID
     */
    private String approverId;
    
    /**
     * 审批人姓名
     */
    private String approverName;
    
    /**
     * 审批时间
     */
    private LocalDateTime approveTime;
    
    /**
     * 审批意见
     */
    private String approveComment;
    
    /**
     * 领用方式
     */
    private Integer claimType;
    
    /**
     * 领用方式名称
     */
    private String claimTypeName;
    
    /**
     * 总数量
     */
    private Integer totalQuantity;
    
    /**
     * 已领取数量
     */
    private Integer claimedQuantity;
    
    /**
     * 预计领取时间
     */
    private LocalDateTime expectedClaimTime;
    
    /**
     * 紧急程度
     */
    private Integer urgencyLevel;
    
    /**
     * 紧急程度名称
     */
    private String urgencyLevelName;
    
    /**
     * 创建时间
     */
    private LocalDateTime createTime;
    
    /**
     * 更新时间
     */
    private LocalDateTime updateTime;
    
    /**
     * 备注
     */
    private String remark;
    
    /**
     * 办公用品明细列表
     */
    private List<OfficeSupplyItemVO> items;
    
    /**
     * 领用记录列表
     */
    private List<OfficeSupplyClaimVO> claims;
}