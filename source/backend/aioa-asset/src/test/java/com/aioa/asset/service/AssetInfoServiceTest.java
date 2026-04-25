package com.aioa.asset.service;

import com.aioa.asset.entity.AssetInfo;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class AssetInfoServiceTest {
    
    @Autowired
    private AssetInfoService assetInfoService;
    
    @Test
    void testCreateAndQueryAsset() {
        // 创建测试资产
        AssetInfo asset = new AssetInfo();
        asset.setAssetCode("ASSET-001");
        asset.setAssetName("笔记本电脑");
        asset.setCategoryId(1L);
        asset.setModel("ThinkPad X1 Carbon");
        asset.setCurrentQuantity(10);
        asset.setWarningQuantity(3);
        asset.setPurchasePrice(new BigDecimal("12999.00"));
        asset.setPurchaseDate(LocalDate.now());
        asset.setAssetStatus(1);
        asset.setStatus(1);
        
        boolean saved = assetInfoService.save(asset);
        assertTrue(saved);
        assertNotNull(asset.getId());
        
        // 查询资产
        AssetInfo queried = assetInfoService.getById(asset.getId());
        assertNotNull(queried);
        assertEquals("笔记本电脑", queried.getAssetName());
        assertEquals(10, queried.getCurrentQuantity());
    }
    
    @Test
    void testGetWarningAssets() {
        // 创建低库存资产
        AssetInfo lowStockAsset = new AssetInfo();
        lowStockAsset.setAssetCode("ASSET-002");
        lowStockAsset.setAssetName("打印纸");
        lowStockAsset.setCurrentQuantity(2);
        lowStockAsset.setWarningQuantity(5);
        lowStockAsset.setAssetStatus(1);
        lowStockAsset.setStatus(1);
        assetInfoService.save(lowStockAsset);
        
        // 测试获取预警资产
        var warningAssets = assetInfoService.getWarningAssets();
        assertNotNull(warningAssets);
    }
}