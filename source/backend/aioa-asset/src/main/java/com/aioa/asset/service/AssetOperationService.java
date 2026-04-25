package com.aioa.asset.service;

import com.aioa.asset.entity.AssetOperation;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;

/**
 * 资产操作记录Service接口
 */
public interface AssetOperationService extends IService<AssetOperation> {
    
    /**
     * 分页查询操作记录
     */
    Page<AssetOperation> pageOperations(Page<AssetOperation> page, AssetOperation query);
    
    /**
     * 根据资产ID查询操作记录
     */
    Page<AssetOperation> getOperationsByAssetId(Page<AssetOperation> page, Long assetId);
}