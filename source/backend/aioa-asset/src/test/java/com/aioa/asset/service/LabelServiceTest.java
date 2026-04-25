package com.aioa.asset.service;

import com.aioa.asset.entity.AssetInfo;
import com.aioa.asset.entity.AssetLabel;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Transactional
class LabelServiceTest {
    
    @Autowired
    private LabelService labelService;
    
    @Autowired
    private AssetInfoService assetInfoService;
    
    @Test
    void testGenerateLabel() {
        // 创建测试资产
        AssetInfo asset = createTestAsset("ASSET-LABEL-001", "测试资产-标签");
        
        // 生成标签
        AssetLabel label = labelService.generateLabel(asset.getId(), 1L, "testUser");
        
        assertNotNull(label);
        assertNotNull(label.getId());
        assertEquals(asset.getId(), label.getAssetId());
        assertEquals("ASSET-LABEL-001", label.getAssetCode());
        assertEquals("测试资产-标签", label.getAssetName());
        assertNotNull(label.getLabelCode());
        assertNotNull(label.getQrContent());
        assertNotNull(label.getBarcodeContent());
        assertEquals(0, label.getPrintStatus()); // 未打印
        assertEquals(1, label.getLabelStatus()); // 启用
    }
    
    @Test
    void testBatchGenerateLabels() {
        // 创建多个测试资产
        AssetInfo asset1 = createTestAsset("ASSET-LABEL-002", "测试资产1");
        AssetInfo asset2 = createTestAsset("ASSET-LABEL-003", "测试资产2");
        
        // 批量生成标签
        List<AssetLabel> labels = labelService.batchGenerateLabels(
            Arrays.asList(asset1.getId(), asset2.getId()), 1L, "testUser");
        
        assertEquals(2, labels.size());
        assertTrue(labels.stream().allMatch(label -> label.getLabelStatus() == 1));
    }
    
    @Test
    void testPrintLabel() {
        // 创建测试资产和标签
        AssetInfo asset = createTestAsset("ASSET-LABEL-004", "测试资产-打印");
        AssetLabel label = labelService.generateLabel(asset.getId(), 1L, "testUser");
        
        // 打印标签
        boolean printed = labelService.printLabel(label.getId(), "打印机1", "printer1");
        assertTrue(printed);
        
        // 验证打印状态
        AssetLabel printedLabel = labelService.getById(label.getId());
        assertEquals(1, printedLabel.getPrintStatus()); // 已打印
        assertEquals(1, printedLabel.getPrintCount());
        assertNotNull(printedLabel.getPrintTime());
        assertNotNull(printedLabel.getLastPrintTime());
        assertEquals("打印机1", printedLabel.getPrinter());
    }
    
    @Test
    void testGetByLabelCode() {
        // 创建测试资产和标签
        AssetInfo asset = createTestAsset("ASSET-LABEL-005", "测试资产-编码查询");
        AssetLabel label = labelService.generateLabel(asset.getId(), 1L, "testUser");
        
        // 根据标签编码查询
        AssetLabel queried = labelService.getByLabelCode(label.getLabelCode());
        assertNotNull(queried);
        assertEquals(label.getId(), queried.getId());
        assertEquals(label.getLabelCode(), queried.getLabelCode());
    }
    
    @Test
    void testGetPrintHistory() {
        // 创建测试资产和标签
        AssetInfo asset = createTestAsset("ASSET-LABEL-006", "测试资产-打印历史");
        AssetLabel label = labelService.generateLabel(asset.getId(), 1L, "testUser");
        labelService.printLabel(label.getId(), "打印机1", "printer1");
        
        // 获取打印历史
        List<AssetLabel> history = labelService.getPrintHistory(10);
        assertFalse(history.isEmpty());
        assertTrue(history.stream().anyMatch(l -> l.getId().equals(label.getId())));
    }
    
    @Test
    void testGetPrintStatistics() {
        // 创建测试资产和标签
        AssetInfo asset1 = createTestAsset("ASSET-LABEL-007", "测试资产-统计1");
        AssetInfo asset2 = createTestAsset("ASSET-LABEL-008", "测试资产-统计2");
        
        AssetLabel label1 = labelService.generateLabel(asset1.getId(), 1L, "testUser");
        AssetLabel label2 = labelService.generateLabel(asset2.getId(), 1L, "testUser");
        
        // 打印一个标签
        labelService.printLabel(label1.getId(), "打印机1", "printer1");
        
        // 获取统计信息
        Map<String, Object> stats = labelService.getPrintStatistics();
        assertNotNull(stats);
        assertTrue(stats.containsKey("printed_count"));
        assertTrue(stats.containsKey("unprinted_count"));
        assertTrue(stats.containsKey("failed_count"));
    }
    
    @Test
    void testInvalidateLabel() {
        // 创建测试资产和标签
        AssetInfo asset = createTestAsset("ASSET-LABEL-009", "测试资产-作废");
        AssetLabel label = labelService.generateLabel(asset.getId(), 1L, "testUser");
        
        // 作废标签
        boolean invalidated = labelService.invalidateLabel(label.getId(), "测试作废");
        assertTrue(invalidated);
        
        // 验证标签状态
        AssetLabel invalidatedLabel = labelService.getById(label.getId());
        assertEquals(2, invalidatedLabel.getLabelStatus()); // 已作废
    }
    
    @Test
    void testRegenerateCode() {
        // 创建测试资产和标签
        AssetInfo asset = createTestAsset("ASSET-LABEL-010", "测试资产-重新生成");
        AssetLabel label = labelService.generateLabel(asset.getId(), 1L, "testUser");
        String originalQrContent = label.getQrContent();
        
        // 重新生成二维码
        AssetLabel regenerated = labelService.regenerateCode(label.getId());
        assertNotNull(regenerated);
        assertNotEquals(originalQrContent, regenerated.getQrContent());
        assertTrue(regenerated.getQrContent().contains(label.getLabelCode()));
        assertTrue(regenerated.getQrContent().contains(asset.getAssetCode()));
    }
    
    private AssetInfo createTestAsset(String assetCode, String assetName) {
        AssetInfo asset = new AssetInfo();
        asset.setAssetCode(assetCode);
        asset.setAssetName(assetName);
        asset.setCategoryId(1L);
        asset.setModel("测试型号");
        asset.setCurrentQuantity(100);
        asset.setWarningQuantity(10);
        asset.setPurchasePrice(new BigDecimal("1000.00"));
        asset.setPurchaseDate(LocalDate.now());
        asset.setAssetStatus(1);
        asset.setStatus(1);
        asset.setCreateBy("testUser");
        
        assetInfoService.save(asset);
        return asset;
    }
}