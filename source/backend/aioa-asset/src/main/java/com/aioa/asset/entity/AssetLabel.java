package com.aioa.asset.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

/**
 * 资产标签实体
 */
@Data
@TableName("asset_label")
public class AssetLabel {
    
    /**
     * 主键ID
     */
    @TableId(type = IdType.AUTO)
    private Long id;
    
    /**
     * 标签编码（唯一标识）
     */
    private String labelCode;
    
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
     * 二维码内容
     */
    private String qrContent;
    
    /**
     * 二维码图片路径
     */
    private String qrImagePath;
    
    /**
     * 条码内容
     */
    private String barcodeContent;
    
    /**
     * 条码图片路径
     */
    private String barcodeImagePath;
    
    /**
     * 打印模板ID
     */
    private Long templateId;
    
    /**
     * 打印模板名称
     */
    private String templateName;
    
    /**
     * 打印时间
     */
    private LocalDateTime printTime;
    
    /**
     * 打印人
     */
    private String printer;
    
    /**
     * 打印人ID
     */
    private String printerId;
    
    /**
     * 打印状态：0-未打印，1-已打印，2-打印失败
     */
    private Integer printStatus;
    
    /**
     * 打印次数
     */
    private Integer printCount;
    
    /**
     * 最后打印时间
     */
    private LocalDateTime lastPrintTime;
    
    /**
     * 标签状态：0-禁用，1-启用，2-作废
     */
    private Integer labelStatus;
    
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