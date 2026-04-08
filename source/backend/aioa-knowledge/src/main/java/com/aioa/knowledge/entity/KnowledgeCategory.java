package com.aioa.knowledge.entity;

import lombok.Data;

/**
 * 知识分类实体
 */
@Data
public class KnowledgeCategory {
    
    /**
     * 分类ID
     */
    private Long id;
    
    /**
     * 分类名称
     */
    private String name;
    
    /**
     * 父分类ID
     */
    private Long parentId;
    
    /**
     * 排序
     */
    private Integer sort;
    
    /**
     * 状态
     */
    private String status;
}