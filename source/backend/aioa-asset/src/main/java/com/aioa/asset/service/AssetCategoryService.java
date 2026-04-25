package com.aioa.asset.service;

import com.aioa.asset.entity.AssetCategory;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.List;

/**
 * 资产分类Service接口
 */
public interface AssetCategoryService extends IService<AssetCategory> {
    
    /**
     * 获取分类树形结构
     */
    List<AssetCategory> getCategoryTree();
    
    /**
     * 根据类型获取分类
     */
    List<AssetCategory> getCategoriesByType(Integer categoryType);
    
    /**
     * 批量删除分类
     */
    boolean deleteBatch(List<Long> ids);
}