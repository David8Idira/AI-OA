package com.aioa.aichat.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

/**
 * AI提示词模板实体类
 */
@Entity
@Table(name = "ai_prompt_template")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PromptTemplate {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * 模板名称
     */
    @Column(nullable = false, length = 100)
    private String name;

    /**
     * 模板编码（唯一）
     */
    @Column(unique = true, length = 50)
    private String code;

    /**
     * 模板类型：general-通用，approval-审批，document-文档
     */
    @Column(nullable = false, length = 50)
    private String type;

    /**
     * 模板内容
     */
    @Column(columnDefinition = "TEXT", nullable = false)
    private String template;

    /**
     * 变量说明（JSON格式）
     */
    @Column(columnDefinition = "TEXT")
    private String variables;

    /**
     * 模板描述
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
    private java.time.LocalDateTime createTime;
}
