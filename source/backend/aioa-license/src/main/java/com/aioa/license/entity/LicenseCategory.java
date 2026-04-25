package com.aioa.license.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

/**
 * 证照分类实体
 */
@Data
@TableName("license_category")
public class LicenseCategory {
    
    /**
     * 主键ID
     */
    @TableId(type = IdType.AUTO)
    private Long id;
    
    /**
     * 分类编码
     */
    private String categoryCode;
    
    /**
     * 分类名称
     */
    private String categoryName;
    
    /**
     * 分类类型：1-营业执照，2-资质证书，3-许可证
     */
    private Integer categoryType;
    
    /**
     * 父级分类ID
     */
    private Long parentId;
    
    /**
     * 分类级别
     */
    private Integer level;
    
    /**
     * 排序号
     */
    private Integer sortOrder;
    
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