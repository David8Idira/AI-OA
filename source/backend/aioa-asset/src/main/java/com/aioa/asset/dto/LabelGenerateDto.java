package com.aioa.asset.dto;

import lombok.Data;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.util.List;

/**
 * 标签生成DTO
 */
@Data
public class LabelGenerateDto {
    
    /**
     * 资产ID（单个生成时使用）
     */
    private Long assetId;
    
    /**
     * 资产ID列表（批量生成时使用）
     */
    private List<Long> assetIds;
    
    /**
     * 打印模板ID
     */
    private Long templateId;
    
    /**
     * 创建人
     */
    @NotBlank(message = "创建人不能为空")
    private String createBy;
    
    /**
     * 备注
     */
    private String remark;
}