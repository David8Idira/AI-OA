package com.aioa.asset.service;

import com.aioa.asset.entity.AssetInfo;
import com.aioa.asset.entity.AssetLabel;
import com.aioa.asset.entity.StockRecord;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Transactional
class StockServiceTest {
    
    @Autowired
    private StockService stockService;
    
    @Autowired
    private AssetInfoService assetInfoService;
    
    @Autowired
    private LabelService labelService;
    
    @Test
    void testManualInAndOut() {
        // 创建测试资产
        AssetInfo asset = createTestAsset("ASSET-STOCK-001", "测试资产-手动出入库", 100);
        
        // 手动入库
        StockRecord inRecord = stockService.manualIn(
            asset.getId(), 50, "仓库A", "操作员1", "operator1",
            "BATCH-001", "供应商A", new BigDecimal("100.00"), "PO-001");
        
        assertNotNull(inRecord);
        assertEquals(asset.getId(), inRecord.getAssetId());
        assertEquals(1, inRecord.getOperationType()); // 入库
        assertEquals(50, inRecord.getQuantity());
        assertEquals(100, inRecord.getBeforeQuantity());
        assertEquals(150, inRecord.getAfterQuantity());
        assertEquals("仓库A", inRecord.getWarehouse());
        
        // 验证库存更新
        AssetInfo updatedAsset = assetInfoService.getById(asset.getId());
        assertEquals(150, updatedAsset.getCurrentQuantity());
        
        // 手动出库
        StockRecord outRecord = stockService.manualOut(
            asset.getId(), 30, "仓库A", "操作员2", "operator2",
            "BATCH-001", "客户A", new BigDecimal("120.00"), "SO-001");
        
        assertNotNull(outRecord);
        assertEquals(2, outRecord.getOperationType()); // 出库
        assertEquals(30, outRecord.getQuantity());
        assertEquals(150, outRecord.getBeforeQuantity());
        assertEquals(120, outRecord.getAfterQuantity());
        
        // 验证库存更新
        updatedAsset = assetInfoService.getById(asset.getId());
        assertEquals(120, updatedAsset.getCurrentQuantity());
    }
    
    @Test
    void testScanInAndOut() {
        // 创建测试资产和标签
        AssetInfo asset = createTestAsset("ASSET-STOCK-002", "测试资产-扫码出入库", 200);
        AssetLabel label = labelService.generateLabel(asset.getId(), 1L, "testUser");
        
        // 扫码入库
        StockRecord scanInRecord = stockService.scanIn(
            label.getLabelCode(), 100, "仓库B", "操作员3", "operator3",
            "BATCH-002", "供应商B", new BigDecimal("200.00"), "PO-002");
        
        assertNotNull(scanInRecord);
        assertEquals(1, scanInRecord.getOperationType()); // 入库
        assertEquals(label.getLabelCode(), scanInRecord.getScannedLabelCode());
        assertEquals(1, scanInRecord.getScanMethod()); // 扫码枪
        assertEquals(100, scanInRecord.getQuantity());
        assertEquals(200, scanInRecord.getBeforeQuantity());
        assertEquals(300, scanInRecord.getAfterQuantity());
        
        // 扫码出库
        StockRecord scanOutRecord = stockService.scanOut(
            label.getLabelCode(), 50, "仓库B", "操作员4", "operator4",
            "BATCH-002", "客户B", new BigDecimal("220.00"), "SO-002");
        
        assertNotNull(scanOutRecord);
        assertEquals(2, scanOutRecord.getOperationType()); // 出库
        assertEquals(label.getLabelCode(), scanOutRecord.getScannedLabelCode());
        assertEquals(50, scanOutRecord.getQuantity());
        assertEquals(300, scanOutRecord.getBeforeQuantity());
        assertEquals(250, scanOutRecord.getAfterQuantity());
        
        // 验证库存更新
        AssetInfo updatedAsset = assetInfoService.getById(asset.getId());
        assertEquals(250, updatedAsset.getCurrentQuantity());
    }
    
    @Test
    void testInventoryCheck() {
        // 创建测试资产
        AssetInfo asset = createTestAsset("ASSET-STOCK-003", "测试资产-盘点", 300);
        
        // 库存盘点（实际数量为280，盘点差异-20）
        StockRecord checkRecord = stockService.inventoryCheck(
            asset.getId(), 280, "仓库C", "操作员5", "operator5");
        
        assertNotNull(checkRecord);
        assertEquals(3, checkRecord.getOperationType()); // 盘点
        assertEquals(-20, checkRecord.getQuantity()); // 盘亏20
        assertEquals(300, checkRecord.getBeforeQuantity());
        assertEquals(280, checkRecord.getAfterQuantity());
        
        // 验证库存更新
        AssetInfo updatedAsset = assetInfoService.getById(asset.getId());
        assertEquals(280, updatedAsset.getCurrentQuantity());
    }
    
    @Test
    void testStockTransfer() {
        // 创建测试资产
        AssetInfo asset = createTestAsset("ASSET-STOCK-004", "测试资产-调拨", 400);
        
        // 库存调拨
        StockRecord transferRecord = stockService.stockTransfer(
            asset.getId(), 100, "仓库D", "仓库E", "操作员6", "operator6", "部门调整");
        
        assertNotNull(transferRecord);
        assertEquals(4, transferRecord.getOperationType()); // 调拨
        assertEquals(100, transferRecord.getQuantity());
        assertEquals(400, transferRecord.getBeforeQuantity());
        assertEquals(300, transferRecord.getAfterQuantity());
        assertEquals("仓库D", transferRecord.getWarehouse());
        assertEquals("仓库E", transferRecord.getTargetWarehouse());
        
        // 验证库存更新
        AssetInfo updatedAsset = assetInfoService.getById(asset.getId());
        assertEquals(300, updatedAsset.getCurrentQuantity());
    }
    
    @Test
    void testGetRealTimeStock() {
        // 创建测试资产
        AssetInfo asset = createTestAsset("ASSET-STOCK-005", "测试资产-实时库存", 500);
        
        // 获取实时库存
        Map<String, Object> stockInfo = stockService.getRealTimeStock(asset.getId());
        
        assertNotNull(stockInfo);
        assertEquals(asset.getId(), stockInfo.get("assetId"));
        assertEquals("ASSET-STOCK-005", stockInfo.get("assetCode"));
        assertEquals("测试资产-实时库存", stockInfo.get("assetName"));
        assertEquals(500, stockInfo.get("currentQuantity"));
        assertEquals(10, stockInfo.get("warningQuantity")); // 默认预警数量
    }
    
    @Test
    void testGetAllRealTimeStock() {
        // 创建多个测试资产
        createTestAsset("ASSET-STOCK-006", "测试资产6", 100);
        createTestAsset("ASSET-STOCK-007", "测试资产7", 200);
        
        // 获取所有实时库存
        List<Map<String, Object>> allStock = stockService.getAllRealTimeStock();
        
        assertFalse(allStock.isEmpty());
        assertTrue(allStock.size() >= 2);
        assertTrue(allStock.stream().anyMatch(stock -> "ASSET-STOCK-006".equals(stock.get("assetCode"))));
        assertTrue(allStock.stream().anyMatch(stock -> "ASSET-STOCK-007".equals(stock.get("assetCode"))));
    }
    
    @Test
    void testGetStockStatistics() {
        // 创建测试资产并执行出入库操作
        AssetInfo asset = createTestAsset("ASSET-STOCK-008", "测试资产-统计", 100);
        
        stockService.manualIn(asset.getId(), 50, "仓库F", "op1", "op1", 
                             "BATCH-001", "供应商", new BigDecimal("100.00"), null);
        stockService.manualOut(asset.getId(), 30, "仓库F", "op2", "op2",
                             "BATCH-001", "客户", new BigDecimal("120.00"), null);
        
        // 获取统计信息
        LocalDateTime endTime = LocalDateTime.now();
        LocalDateTime startTime = endTime.minusDays(7);
        Map<String, Object> stats = stockService.getStockStatistics(startTime, endTime);
        
        assertNotNull(stats);
        assertTrue(stats.containsKey("totalInQuantity"));
        assertTrue(stats.containsKey("totalOutQuantity"));
        assertTrue(stats.containsKey("operationStats"));
        assertTrue(stats.containsKey("stockBalance"));
    }
    
    @Test
    void testGetStockLedger() {
        // 创建测试资产并执行操作
        AssetInfo asset = createTestAsset("ASSET-STOCK-009", "测试资产-台账", 200);
        
        stockService.manualIn(asset.getId(), 100, "仓库G", "op1", "op1", 
                             "BATCH-002", "供应商", new BigDecimal("150.00"), null);
        
        // 获取台账
        LocalDateTime endTime = LocalDateTime.now();
        LocalDateTime startTime = endTime.minusDays(1);
        List<Map<String, Object>> ledger = stockService.getStockLedger(startTime, endTime);
        
        assertFalse(ledger.isEmpty());
        Map<String, Object> firstRecord = ledger.get(0);
        assertEquals("ASSET-STOCK-009", firstRecord.get("assetCode"));
        assertEquals(100, firstRecord.get("quantity"));
    }
    
    @Test
    void testGetRecordsByLabelCode() {
        // 创建测试资产和标签
        AssetInfo asset = createTestAsset("ASSET-STOCK-010", "测试资产-标签历史", 300);
        AssetLabel label = labelService.generateLabel(asset.getId(), 1L, "testUser");
        
        // 执行扫码操作
        stockService.scanIn(label.getLabelCode(), 50, "仓库H", "op1", "op1",
                           "BATCH-003", "供应商", null, null);
        
        // 根据标签查询历史
        List<StockRecord> records = stockService.getRecordsByLabelCode(label.getLabelCode());
        
        assertFalse(records.isEmpty());
        assertEquals(label.getLabelCode(), records.get(0).getScannedLabelCode());
        assertEquals(1, records.get(0).getOperationType()); // 入库
    }
    
    @Test
    void testConfirmAndCancelRecord() {
        // 创建测试资产并入库
        AssetInfo asset = createTestAsset("ASSET-STOCK-011", "测试资产-确认作废", 400);
        StockRecord record = stockService.manualIn(
            asset.getId(), 100, "仓库I", "op1", "op1",
            "BATCH-004", "供应商", null, null);
        
        // 确认记录
        boolean confirmed = stockService.confirmRecord(record.getId(), "审批人", "同意");
        assertTrue(confirmed);
        
        StockRecord confirmedRecord = stockService.getById(record.getId());
        assertEquals(1, confirmedRecord.getApprovalStatus()); // 已通过
        
        // 作废记录
        boolean canceled = stockService.cancelRecord(record.getId(), "测试作废");
        assertTrue(canceled);
        
        StockRecord canceledRecord = stockService.getById(record.getId());
        assertEquals(2, canceledRecord.getStatus()); // 已作废
        
        // 验证库存还原
        AssetInfo finalAsset = assetInfoService.getById(asset.getId());
        assertEquals(400, finalAsset.getCurrentQuantity()); // 还原到原始库存
    }
    
    private AssetInfo createTestAsset(String assetCode, String assetName, Integer initialQuantity) {
        AssetInfo asset = new AssetInfo();
        asset.setAssetCode(assetCode);
        asset.setAssetName(assetName);
        asset.setCategoryId(1L);
        asset.setModel("测试型号");
        asset.setCurrentQuantity(initialQuantity);
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