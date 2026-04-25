package com.aioa.asset.controller;

import com.aioa.asset.entity.AssetCategory;
import com.aioa.asset.service.AssetCategoryService;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 资产分类Controller
 */
@Tag(name = "资产分类管理")
@RestController
@RequestMapping("/asset/category")
public class AssetCategoryController {
    
    @Autowired
    private AssetCategoryService assetCategoryService;
    
    @Operation(summary = "获取分类树")
    @GetMapping("/tree")
    public List<AssetCategory> getCategoryTree() {
        return assetCategoryService.getCategoryTree();
    }
    
    @Operation(summary = "分页查询分类")
    @GetMapping("/page")
    public Page<AssetCategory> pageCategory(Page<AssetCategory> page, AssetCategory query) {
        return assetCategoryService.page(page);
    }
    
    @Operation(summary = "根据类型获取分类")
    @GetMapping("/type/{type}")
    public List<AssetCategory> getByType(@PathVariable Integer type) {
        return assetCategoryService.getCategoriesByType(type);
    }
    
    @Operation(summary = "获取分类详情")
    @GetMapping("/{id}")
    public AssetCategory getById(@PathVariable Long id) {
        return assetCategoryService.getById(id);
    }
    
    @Operation(summary = "创建分类")
    @PostMapping
    public boolean createCategory(@Validated @RequestBody AssetCategory category) {
        return assetCategoryService.save(category);
    }
    
    @Operation(summary = "更新分类")
    @PutMapping("/{id}")
    public boolean updateCategory(@PathVariable Long id, @Validated @RequestBody AssetCategory category) {
        category.setId(id);
        return assetCategoryService.updateById(category);
    }
    
    @Operation(summary = "删除分类")
    @DeleteMapping("/{id}")
    public boolean deleteCategory(@PathVariable Long id) {
        return assetCategoryService.removeById(id);
    }
    
    @Operation(summary = "批量删除分类")
    @DeleteMapping("/batch")
    public boolean deleteBatch(@RequestBody List<Long> ids) {
        return assetCategoryService.deleteBatch(ids);
    }
}