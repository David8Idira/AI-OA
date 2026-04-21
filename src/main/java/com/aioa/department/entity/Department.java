package com.aioa.department.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

/**
 * 部门实体类
 */
@Entity
@Table(name = "sys_department")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Department {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * 部门名称
     */
    @Column(nullable = false, length = 100)
    private String name;

    /**
     * 部门代码（唯一）
     */
    @Column(unique = true, length = 50)
    private String code;

    /**
     * 上级部门ID
     */
    @Column(name = "parent_id")
    private Long parentId;

    /**
     * 部门负责人ID
     */
    @Column(name = "manager_id")
    private Long managerId;

    /**
     * 部门描述
     */
    @Column(length = 500)
    private String description;

    /**
     * 排序号
     */
    @Column(nullable = false)
    private Integer sortOrder;

    /**
     * 状态：0-禁用，1-启用
     */
    @Column(nullable = false)
    private Integer status;

    /**
     * 创建时间
     */
    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private LocalDateTime createTime;

    /**
     * 更新时间
     */
    @UpdateTimestamp
    @Column(nullable = false)
    private LocalDateTime updateTime;
}
