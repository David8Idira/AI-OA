package com.aioa.approval.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

/**
 * 审批流程实体类
 */
@Entity
@Table(name = "approval_process")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ApprovalProcess {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * 流程名称
     */
    @Column(nullable = false, length = 100)
    private String name;

    /**
     * 流程编码（唯一）
     */
    @Column(unique = true, length = 50)
    private String code;

    /**
     * 流程类型：leave-请假，reimburse-报销，purchase-采购等
     */
    @Column(nullable = false, length = 50)
    private String type;

    /**
     * 表单模板JSON
     */
    @Column(columnDefinition = "TEXT")
    private String formTemplate;

    /**
     * 流程描述
     */
    @Column(length = 500)
    private String description;

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
