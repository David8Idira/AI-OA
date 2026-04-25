package com.aioa.asset.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

/**
 * 资产操作记录实体
 */
@Data
@TableName("asset_operation")
public class AssetOperation {
    
    /**
     * 主键ID
     */
    @TableId(type = IdType.AUTO)
    private Long id;
    
    /**
     * 资产ID
     */
    private Long assetId;
    
    /**
     * 操作类型：1-登记，2-领用，3-归还，4-调拨，5-报废
     */
    private Integer operationType;
    
    /**
     * 操作数量
     */
    private Integer operationQuantity;
    
    /**
     * 操作前数量
     */
    private Integer beforeQuantity;
    
    /**
     * 操作后数量
     */
    private Integer afterQuantity;
    
    /**
     * 领用人/调拨人/报废人
     */
    private String operator;
    
    /**
     * 领用人ID/调拨人ID/报废人ID
     */
    private String operatorId;
    
    /**
     * 部门/目标部门
     */
    private String department;
    
    /**
     * 原因说明
     */
    private String reason;
    
    /**
     * 审批状态：0-待审批，1-已通过，2-已拒绝
     */
    private Integer approvalStatus;
    
    /**
     * 审批意见
     */
    private String approvalComment;
    
    /**
     * 操作时间
     */
    private LocalDateTime operationTime;
    
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