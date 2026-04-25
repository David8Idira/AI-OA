package com.aioa.asset.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

/**
 * 办公用品申请单实体
 */
@Data
@TableName("office_supply_request")
public class OfficeSupplyRequest {
    
    /**
     * 主键ID
     */
    @TableId(type = IdType.AUTO)
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
     * 申请状态：
     * 0-草稿
     * 1-待审批
     * 2-审批通过
     * 3-审批拒绝
     * 4-部分领取
     * 5-已全部领取
     * 6-已取消
     */
    private Integer requestStatus;
    
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
     * 领用方式：1-一次性领取，2-分批领取
     */
    private Integer claimType;
    
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
     * 紧急程度：1-普通，2-加急，3-紧急
     */
    private Integer urgencyLevel;
    
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