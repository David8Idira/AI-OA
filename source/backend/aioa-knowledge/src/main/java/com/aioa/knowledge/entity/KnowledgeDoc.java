package com.aioa.knowledge.entity;

import com.aioa.common.entity.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 知识文档实体
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class KnowledgeDoc extends BaseEntity {
    
    /**
     * 文档标题
     */
    private String title;
    
    /**
     * 文档内容
     */
    private String content;
    
    /**
     * 文档摘要
     */
    private String summary;
    
    /**
     * 分类ID
     */
    private Long categoryId;
    
    /**
     * 标签(JSON数组)
     */
    private String tags;
    
    /**
     * 文档类型: article, faq, manual
     */
    private String docType;
    
    /**
     * 状态: draft, published
     */
    private String status;
    
    /**
     * 浏览次数
     */
    private Integer viewCount;
    
    /**
     * 点赞次数
     */
    private Integer likeCount;
    
    /**
     * 向量ID(用于语义搜索)
     */
    private String vectorId;
}