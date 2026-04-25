package com.aioa.hr.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 部门实体
 */
@Data
@TableName("department")
public class Department {
    
    /**
     * 主键ID
     */
    @TableId(type = IdType.AUTO)
    private Long id;
    
    /**
     * 部门编码
     */
    private String departmentCode;
    
    /**
     * 部门名称
     */
    private String departmentName;
    
    /**
     * 父级部门ID
     */
    private Long parentId;
    
    /**
     * 部门负责人
     */
    private String manager;
    
    /**
     * 部门负责人ID
     */
    private String managerId;
    
    /**
     * 部门级别
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
    
    /**
     * 子部门列表（非数据库字段）
     */
    @TableField(exist = false)
    private List<Department> children;
}