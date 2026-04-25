package com.aioa.asset.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 资产信息实体
 */
@Data
@TableName("asset_info")
public class AssetInfo {
    
    /**
     * 主键ID
     */
    @TableId(type = IdType.AUTO)
    private Long id;
    
    /**
     * 资产编码
     */
    private String assetCode;
    
    /**
     * 资产名称
     */
    private String assetName;
    
    /**
     * 资产分类ID
     */
    private Long categoryId;
    
    /**
     * 资产型号
     */
    private String model;
    
    /**
     * 规格参数
     */
    private String specification;
    
    /**
     * 生产厂商
     */
    private String manufacturer;
    
    /**
     * 供应商
     */
    private String supplier;
    
    /**
     * 购买日期
     */
    private LocalDate purchaseDate;
    
    /**
     * 购买价格
     */
    private BigDecimal purchasePrice;
    
    /**
     * 计量单位
     */
    private String unit;
    
    /**
     * 当前数量
     */
    private Integer currentQuantity;
    
    /**
     * 预警数量
     */
    private Integer warningQuantity;
    
    /**
     * 存放位置
     */
    private String location;
    
    /**
     * 负责人
     */
    private String responsiblePerson;
    
    /**
     * 负责人ID
     */
    private String responsiblePersonId;
    
    /**
     * 资产状态：1-正常，2-领用中，3-维修中，4-报废
     */
    private Integer assetStatus;
    
    /**
     * 状态：0-禁用，1-启用
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