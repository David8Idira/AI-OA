package com.aioa.asset.service.impl;

import com.aioa.asset.entity.AssetOperation;
import com.aioa.asset.mapper.AssetOperationMapper;
import com.aioa.asset.service.AssetOperationService;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

/**
 * 资产操作记录Service实现
 */
@Service
public class AssetOperationServiceImpl extends ServiceImpl<AssetOperationMapper, AssetOperation> implements AssetOperationService {

    @Override
    public Page<AssetOperation> pageOperations(Page<AssetOperation> page, AssetOperation query) {
        return this.page(page);
    }

    @Override
    public Page<AssetOperation> getOperationsByAssetId(Page<AssetOperation> page, Long assetId) {
        return this.lambdaQuery()
                .eq(AssetOperation::getAssetId, assetId)
                .page(page);
    }
}