package com.aioa.asset.service.impl;

import com.aioa.asset.entity.AssetCategory;
import com.aioa.asset.mapper.AssetCategoryMapper;
import com.aioa.asset.service.AssetCategoryService;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 资产分类Service实现
 */
@Service
public class AssetCategoryServiceImpl extends ServiceImpl<AssetCategoryMapper, AssetCategory> implements AssetCategoryService {
    
    @Override
    public List<AssetCategory> getCategoryTree() {
        // 查询所有启用的分类
        LambdaQueryWrapper<AssetCategory> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(AssetCategory::getStatus, 1)
                   .orderByAsc(AssetCategory::getSortOrder);
        List<AssetCategory> allCategories = list(queryWrapper);
        
        // 构建树形结构
        return buildCategoryTree(allCategories, 0L);
    }
    
    private List<AssetCategory> buildCategoryTree(List<AssetCategory> categories, Long parentId) {
        List<AssetCategory> tree = new ArrayList<>();
        for (AssetCategory category : categories) {
            if (category.getParentId() == null && parentId == 0L) {
                tree.add(category);
                category.setChildren(buildCategoryTree(categories, category.getId()));
            } else if (category.getParentId() != null && category.getParentId().equals(parentId)) {
                tree.add(category);
                category.setChildren(buildCategoryTree(categories, category.getId()));
            }
        }
        return tree;
    }
    
    @Override
    public List<AssetCategory> getCategoriesByType(Integer categoryType) {
        LambdaQueryWrapper<AssetCategory> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(AssetCategory::getCategoryType, categoryType)
                   .eq(AssetCategory::getStatus, 1)
                   .orderByAsc(AssetCategory::getSortOrder);
        return list(queryWrapper);
    }
    
    @Override
    public boolean deleteBatch(List<Long> ids) {
        // 检查是否存在子分类
        LambdaQueryWrapper<AssetCategory> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.in(AssetCategory::getParentId, ids);
        long childCount = count(queryWrapper);
        if (childCount > 0) {
            throw new RuntimeException("存在子分类，无法删除");
        }
        
        return removeByIds(ids);
    }
}