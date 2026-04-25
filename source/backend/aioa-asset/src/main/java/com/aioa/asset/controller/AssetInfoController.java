package com.aioa.asset.controller;

import com.aioa.asset.dto.*;
import com.aioa.asset.entity.AssetInfo;
import com.aioa.asset.service.AssetInfoService;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * 资产信息Controller
 */
@Tag(name = "资产管理")
@RestController
@RequestMapping("/asset/info")
public class AssetInfoController {
    
    @Autowired
    private AssetInfoService assetInfoService;
    
    @Operation(summary = "分页查询资产")
    @GetMapping("/page")
    public Page<AssetInfo> pageAssets(Page<AssetInfo> page, AssetInfo query) {
        return assetInfoService.pageAssets(page, query);
    }
    
    @Operation(summary = "获取资产详情")
    @GetMapping("/{id}")
    public AssetInfo getById(@PathVariable Long id) {
        return assetInfoService.getById(id);
    }
    
    @Operation(summary = "创建资产")
    @PostMapping
    public boolean createAsset(@Validated @RequestBody AssetInfo asset) {
        return assetInfoService.save(asset);
    }
    
    @Operation(summary = "更新资产")
    @PutMapping("/{id}")
    public boolean updateAsset(@PathVariable Long id, @Validated @RequestBody AssetInfo asset) {
        asset.setId(id);
        return assetInfoService.updateById(asset);
    }
    
    @Operation(summary = "删除资产")
    @DeleteMapping("/{id}")
    public boolean deleteAsset(@PathVariable Long id) {
        return assetInfoService.removeById(id);
    }
    
    @Operation(summary = "获取资产预警列表")
    @GetMapping("/warning")
    public List<AssetInfo> getWarningAssets() {
        return assetInfoService.getWarningAssets();
    }
    
    @Operation(summary = "领用资产")
    @PostMapping("/borrow")
    public boolean borrowAsset(@Validated @RequestBody AssetBorrowDTO dto) {
        return assetInfoService.borrowAsset(
            dto.getAssetId(),
            dto.getQuantity(),
            dto.getOperator(),
            dto.getOperatorId(),
            dto.getReason()
        );
    }
    
    @Operation(summary = "归还资产")
    @PostMapping("/return")
    public boolean returnAsset(@Validated @RequestBody AssetReturnDTO dto) {
        return assetInfoService.returnAsset(
            dto.getAssetId(),
            dto.getQuantity(),
            dto.getOperator()
        );
    }
    
    @Operation(summary = "调拨资产")
    @PostMapping("/transfer")
    public boolean transferAsset(@Validated @RequestBody AssetTransferDTO dto) {
        return assetInfoService.transferAsset(
            dto.getAssetId(),
            dto.getQuantity(),
            dto.getOperator(),
            dto.getOperatorId(),
            dto.getTargetDepartment(),
            dto.getReason()
        );
    }
    
    @Operation(summary = "报废资产")
    @PostMapping("/scrap")
    public boolean scrapAsset(@Validated @RequestBody AssetScrapDTO dto) {
        return assetInfoService.scrapAsset(
            dto.getAssetId(),
            dto.getQuantity(),
            dto.getOperator(),
            dto.getOperatorId(),
            dto.getReason()
        );
    }
    
    @Operation(summary = "获取资产统计")
    @GetMapping("/statistics")
    public Map<String, Object> getAssetStatistics() {
        return assetInfoService.getAssetStatistics();
    }
}