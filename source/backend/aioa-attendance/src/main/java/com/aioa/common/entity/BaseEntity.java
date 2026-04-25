package com.aioa.common.entity;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * Base entity class
 */
@Data
public class BaseEntity implements Serializable {
    
    private static final long serialVersionUID = 1L;
    
    /**
     * Primary key ID
     */
    @TableId(type = IdType.AUTO)
    private Long id;
    
    /**
     * Deleted flag: 0-Not deleted, 1-Deleted
     */
    private Integer deleted = 0;
    
    /**
     * Creator
     */
    @TableField(fill = FieldFill.INSERT)
    private String createBy;
    
    /**
     * Create time
     */
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
    
    /**
     * Updater
     */
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private String updateBy;
    
    /**
     * Update time
     */
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
}