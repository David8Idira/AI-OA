package com.aioa.asset.service.impl;

import com.aioa.asset.entity.AssetInfo;
import com.aioa.asset.entity.AssetOperation;
import com.aioa.asset.mapper.AssetInfoMapper;
import com.aioa.asset.mapper.AssetOperationMapper;
import com.aioa.asset.service.AssetInfoService;
import com.aioa.asset.service.AssetOperationService;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 资产信息Service实现
 */
@Slf4j
@Service
public class AssetInfoServiceImpl extends ServiceImpl<AssetInfoMapper, AssetInfo> implements AssetInfoService {
    
    @Autowired
    private AssetOperationService assetOperationService;
    
    @Autowired
    private AssetOperationMapper assetOperationMapper;
    
    @Override
    public Page<AssetInfo> pageAssets(Page<AssetInfo> page, AssetInfo query) {
        LambdaQueryWrapper<AssetInfo> queryWrapper = new LambdaQueryWrapper<>();
        
        if (query.getAssetName() != null) {
            queryWrapper.like(AssetInfo::getAssetName, query.getAssetName());
        }
        if (query.getCategoryId() != null) {
            queryWrapper.eq(AssetInfo::getCategoryId, query.getCategoryId());
        }
        if (query.getAssetStatus() != null) {
            queryWrapper.eq(AssetInfo::getAssetStatus, query.getAssetStatus());
        }
        if (query.getStatus() != null) {
            queryWrapper.eq(AssetInfo::getStatus, query.getStatus());
        }
        if (query.getResponsiblePerson() != null) {
            queryWrapper.like(AssetInfo::getResponsiblePerson, query.getResponsiblePerson());
        }
        if (query.getLocation() != null) {
            queryWrapper.like(AssetInfo::getLocation, query.getLocation());
        }
        
        queryWrapper.orderByDesc(AssetInfo::getCreateTime);
        
        return page(page, queryWrapper);
    }
    
    @Override
    public List<AssetInfo> getWarningAssets() {
        return baseMapper.selectWarningAssets();
    }
    
    @Transactional(rollbackFor = Exception.class)
    @Override
    public boolean borrowAsset(Long assetId, Integer quantity, String operator, String operatorId, String reason) {
        AssetInfo asset = getById(assetId);
        if (asset == null) {
            throw new RuntimeException("资产不存在");
        }
        if (asset.getCurrentQuantity() < quantity) {
            throw new RuntimeException("库存不足");
        }
        
        // 更新资产数量
        int beforeQuantity = asset.getCurrentQuantity();
        int afterQuantity = beforeQuantity - quantity;
        asset.setCurrentQuantity(afterQuantity);
        asset.setAssetStatus(2); // 领用中状态
        updateById(asset);
        
        // 记录操作
        AssetOperation operation = new AssetOperation();
        operation.setAssetId(assetId);
        operation.setOperationType(2); // 领用
        operation.setOperationQuantity(quantity);
        operation.setBeforeQuantity(beforeQuantity);
        operation.setAfterQuantity(afterQuantity);
        operation.setOperator(operator);
        operation.setOperatorId(operatorId);
        operation.setOperationTime(LocalDateTime.now());
        operation.setReason(reason);
        operation.setApprovalStatus(1); // 已通过
        assetOperationService.save(operation);
        
        return true;
    }
    
    @Transactional(rollbackFor = Exception.class)
    @Override
    public boolean returnAsset(Long assetId, Integer quantity, String operator) {
        AssetInfo asset = getById(assetId);
        if (asset == null) {
            throw new RuntimeException("资产不存在");
        }
        
        // 更新资产数量
        int beforeQuantity = asset.getCurrentQuantity();
        int afterQuantity = beforeQuantity + quantity;
        asset.setCurrentQuantity(afterQuantity);
        asset.setAssetStatus(1); // 正常状态
        updateById(asset);
        
        // 记录操作
        AssetOperation operation = new AssetOperation();
        operation.setAssetId(assetId);
        operation.setOperationType(3); // 归还
        operation.setOperationQuantity(quantity);
        operation.setBeforeQuantity(beforeQuantity);
        operation.setAfterQuantity(afterQuantity);
        operation.setOperator(operator);
        operation.setOperationTime(LocalDateTime.now());
        operation.setApprovalStatus(1); // 已通过
        assetOperationService.save(operation);
        
        return true;
    }
    
    @Transactional(rollbackFor = Exception.class)
    @Override
    public boolean transferAsset(Long assetId, Integer quantity, String operator, String operatorId, String targetDepartment, String reason) {
        AssetInfo asset = getById(assetId);
        if (asset == null) {
            throw new RuntimeException("资产不存在");
        }
        if (asset.getCurrentQuantity() < quantity) {
            throw new RuntimeException("库存不足");
        }
        
        // 更新资产数量
        int beforeQuantity = asset.getCurrentQuantity();
        int afterQuantity = beforeQuantity - quantity;
        asset.setCurrentQuantity(afterQuantity);
        updateById(asset);
        
        // 记录操作
        AssetOperation operation = new AssetOperation();
        operation.setAssetId(assetId);
        operation.setOperationType(4); // 调拨
        operation.setOperationQuantity(quantity);
        operation.setBeforeQuantity(beforeQuantity);
        operation.setAfterQuantity(afterQuantity);
        operation.setOperator(operator);
        operation.setOperatorId(operatorId);
        operation.setDepartment(targetDepartment);
        operation.setOperationTime(LocalDateTime.now());
        operation.setReason(reason);
        operation.setApprovalStatus(1); // 已通过
        assetOperationService.save(operation);
        
        return true;
    }
    
    @Transactional(rollbackFor = Exception.class)
    @Override
    public boolean scrapAsset(Long assetId, Integer quantity, String operator, String operatorId, String reason) {
        AssetInfo asset = getById(assetId);
        if (asset == null) {
            throw new RuntimeException("资产不存在");
        }
        if (asset.getCurrentQuantity() < quantity) {
            throw new RuntimeException("库存不足");
        }
        
        // 更新资产数量
        int beforeQuantity = asset.getCurrentQuantity();
        int afterQuantity = beforeQuantity - quantity;
        asset.setCurrentQuantity(afterQuantity);
        updateById(asset);
        
        // 记录操作
        AssetOperation operation = new AssetOperation();
        operation.setAssetId(assetId);
        operation.setOperationType(5); // 报废
        operation.setOperationQuantity(quantity);
        operation.setBeforeQuantity(beforeQuantity);
        operation.setAfterQuantity(afterQuantity);
        operation.setOperator(operator);
        operation.setOperatorId(operatorId);
        operation.setOperationTime(LocalDateTime.now());
        operation.setReason(reason);
        operation.setApprovalStatus(1); // 已通过
        assetOperationService.save(operation);
        
        return true;
    }
    
    @Override
    public Map<String, Object> getAssetStatistics() {
        Map<String, Object> result = new HashMap<>();
        
        // 资产总数
        LambdaQueryWrapper<AssetInfo> countQuery = new LambdaQueryWrapper<>();
        countQuery.eq(AssetInfo::getStatus, 1);
        long totalAssets = count(countQuery);
        result.put("totalAssets", totalAssets);
        
        // 资产总价值
        // 这里需要查询数据库计算总价值，简化处理
        result.put("totalValue", 0);
        
        // 预警资产数
        List<AssetInfo> warningAssets = getWarningAssets();
        result.put("warningCount", warningAssets.size());
        
        // 分类统计
        List<Map<String, Object>> categoryStats = baseMapper.groupByCategory();
        result.put("categoryStats", categoryStats);
        
        return result;
    }
}