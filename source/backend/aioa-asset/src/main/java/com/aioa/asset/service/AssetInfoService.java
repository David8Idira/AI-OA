package com.aioa.asset.service;

import com.aioa.asset.entity.AssetInfo;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.List;
import java.util.Map;

/**
 * 资产信息Service接口
 */
public interface AssetInfoService extends IService<AssetInfo> {
    
    /**
     * 分页查询资产
     */
    Page<AssetInfo> pageAssets(Page<AssetInfo> page, AssetInfo query);
    
    /**
     * 获取资产预警列表
     */
    List<AssetInfo> getWarningAssets();
    
    /**
     * 领用资产
     */
    boolean borrowAsset(Long assetId, Integer quantity, String operator, String operatorId, String reason);
    
    /**
     * 归还资产
     */
    boolean returnAsset(Long assetId, Integer quantity, String operator);
    
    /**
     * 调拨资产
     */
    boolean transferAsset(Long assetId, Integer quantity, String operator, String operatorId, String targetDepartment, String reason);
    
    /**
     * 报废资产
     */
    boolean scrapAsset(Long assetId, Integer quantity, String operator, String operatorId, String reason);
    
    /**
     * 统计资产数据
     */
    Map<String, Object> getAssetStatistics();
}