package com.aioa.asset.service;

import com.aioa.asset.entity.AssetInventory;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;

/**
 * 资产盘点Service接口
 */
public interface AssetInventoryService extends IService<AssetInventory> {
    
    /**
     * 创建盘点任务
     */
    boolean createInventory(AssetInventory inventory);
    
    /**
     * 开始盘点
     */
    boolean startInventory(Long inventoryId);
    
    /**
     * 完成盘点
     */
    boolean completeInventory(Long inventoryId);
    
    /**
     * 分页查询盘点任务
     */
    Page<AssetInventory> pageInventories(Page<AssetInventory> page, AssetInventory query);
    
    /**
     * 生成盘点报告
     */
    String generateInventoryReport(Long inventoryId);
}