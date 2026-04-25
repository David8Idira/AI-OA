package com.aioa.asset.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

/**
 * 资产盘点实体
 */
@Data
@TableName("asset_inventory")
public class AssetInventory {
    
    /**
     * 主键ID
     */
    @TableId(type = IdType.AUTO)
    private Long id;
    
    /**
     * 盘点单号
     */
    private String inventoryNo;
    
    /**
     * 盘点名称
     */
    private String inventoryName;
    
    /**
     * 盘点范围：1-全部，2-按分类，3-按位置
     */
    private Integer inventoryScope;
    
    /**
     * 盘点分类ID
     */
    private Long categoryId;
    
    /**
     * 盘点位置
     */
    private String location;
    
    /**
     * 盘点负责人
     */
    private String responsiblePerson;
    
    /**
     * 盘点负责人ID
     */
    private String responsiblePersonId;
    
    /**
     * 盘点开始时间
     */
    private LocalDateTime startTime;
    
    /**
     * 盘点结束时间
     */
    private LocalDateTime endTime;
    
    /**
     * 盘点状态：0-待开始，1-进行中，2-已完成
     */
    private Integer inventoryStatus;
    
    /**
     * 资产总数
     */
    private Integer totalAssets;
    
    /**
     * 已盘点数
     */
    private Integer inventoriedCount;
    
    /**
     * 盘盈数
     */
    private Integer surplusCount;
    
    /**
     * 盘亏数
     */
    private Integer shortageCount;
    
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