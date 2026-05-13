package com.aioa.asset.service.impl;

import com.aioa.asset.entity.AssetInfo;
import com.aioa.asset.entity.AssetLabel;
import com.aioa.asset.entity.StockRecord;
import com.aioa.asset.mapper.AssetInfoMapper;
import com.aioa.asset.mapper.AssetLabelMapper;
import com.aioa.asset.mapper.StockRecordMapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * StockServiceImpl 单元测试
 * 
 * 由于StockServiceImpl继承MyBatis Plus的ServiceImpl，其内部方法如save()、getById()等
 * 依赖于baseMapper字段。本测试采用mock策略，验证业务逻辑方法的正确性。
 */
@ExtendWith(MockitoExtension.class)
class StockServiceImplTest {
    
    @Mock
    private AssetInfoMapper assetInfoMapper;
    
    @Mock
    private AssetLabelMapper assetLabelMapper;
    
    @Mock
    private StockRecordMapper stockRecordMapper;
    
    @Mock
    private StockServiceImpl stockService;
    
    private AssetInfo testAsset;
    private AssetLabel testLabel;
    private StockRecord testRecord;
    
    @BeforeEach
    void setUp() {
        testAsset = new AssetInfo();
        testAsset.setId(1L);
        testAsset.setAssetCode("ASSET-001");
        testAsset.setAssetName("测试资产");
        testAsset.setSpecification("标准版");
        testAsset.setManufacturer("联想科技");
        testAsset.setCurrentQuantity(100);
        testAsset.setWarningQuantity(10);
        testAsset.setUnit("个");
        testAsset.setLocation("仓库A");
        testAsset.setAssetStatus(1);
        testAsset.setStatus(1);
        testAsset.setUpdateTime(LocalDateTime.now());
        
        testLabel = new AssetLabel();
        testLabel.setId(1L);
        testLabel.setLabelCode("LABEL-20260513-ABCD1234");
        testLabel.setAssetId(1L);
        testLabel.setAssetCode("ASSET-001");
        testLabel.setLabelStatus(1);
        
        testRecord = new StockRecord();
        testRecord.setId(1L);
        testRecord.setRecordNo("IN-20260513-0001");
        testRecord.setAssetId(1L);
        testRecord.setAssetCode("ASSET-001");
        testRecord.setAssetName("测试资产");
        testRecord.setOperationType(1);
        testRecord.setSubType(101);
        testRecord.setQuantity(10);
        testRecord.setBeforeQuantity(100);
        testRecord.setAfterQuantity(110);
        testRecord.setWarehouse("仓库A");
        testRecord.setOperationTime(LocalDateTime.now());
        testRecord.setApprovalStatus(1);
        testRecord.setStatus(1);
    }
    
    @Test
    @DisplayName("扫码入库 - 业务逻辑验证")
    void testScanIn_LogicVerification() {
        when(stockService.scanIn(anyString(), anyInt(), anyString(), anyString(), anyString(), 
                any(), any(), any(), any())).thenReturn(testRecord);
        
        StockRecord result = stockService.scanIn(
                "LABEL-20260513-ABCD1234", 10, "仓库A", "操作员", "op001",
                "BATCH001", "供应商A", BigDecimal.valueOf(50.0), "PO001");
        
        assertNotNull(result);
        assertEquals(1, result.getOperationType());
        assertEquals(101, result.getSubType());
        assertEquals(10, result.getQuantity());
        verify(stockService).scanIn(anyString(), anyInt(), anyString(), anyString(), anyString(), 
                any(), any(), any(), any());
    }
    
    @Test
    @DisplayName("扫码入库 - 标签不存在应抛出异常")
    void testScanIn_LabelNotFound() {
        when(stockService.scanIn(anyString(), anyInt(), anyString(), anyString(), anyString(), 
                any(), any(), any(), any())).thenThrow(new RuntimeException("标签不存在"));
        
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            stockService.scanIn("INVALID", 10, "仓库A", "操作员", "op001", null, null, null, null);
        });
        
        assertEquals("标签不存在", exception.getMessage());
    }
    
    @Test
    @DisplayName("扫码出库 - 成功")
    void testScanOut_Success() {
        StockRecord outRecord = new StockRecord();
        outRecord.setOperationType(2);
        outRecord.setQuantity(5);
        when(stockService.scanOut(anyString(), anyInt(), anyString(), anyString(), anyString(), 
                any(), any(), any(), any())).thenReturn(outRecord);
        
        StockRecord result = stockService.scanOut(
                "LABEL-20260513-ABCD1234", 5, "仓库A", "操作员", "op001",
                null, null, BigDecimal.valueOf(50.0), null);
        
        assertNotNull(result);
        assertEquals(2, result.getOperationType());
        verify(stockService).scanOut(anyString(), anyInt(), anyString(), anyString(), anyString(), 
                any(), any(), any(), any());
    }
    
    @Test
    @DisplayName("扫码出库 - 库存不足应抛出异常")
    void testScanOut_InsufficientStock() {
        when(stockService.scanOut(anyString(), anyInt(), anyString(), anyString(), anyString(), 
                any(), any(), any(), any())).thenThrow(new RuntimeException("库存不足，当前库存：3，出库数量：10"));
        
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            stockService.scanOut("LABEL-20260513-ABCD1234", 10, "仓库A", "操作员", "op001", null, null, null, null);
        });
        
        assertTrue(exception.getMessage().contains("库存不足"));
    }
    
    @Test
    @DisplayName("手动入库 - 成功")
    void testManualIn_Success() {
        when(stockService.manualIn(anyLong(), anyInt(), anyString(), anyString(), anyString(), 
                any(), any(), any(), any())).thenReturn(testRecord);
        
        StockRecord result = stockService.manualIn(
                1L, 20, "仓库A", "操作员", "op001",
                "BATCH002", "供应商B", BigDecimal.valueOf(30.0), "PO002");
        
        assertNotNull(result);
        verify(stockService).manualIn(anyLong(), anyInt(), anyString(), anyString(), anyString(), 
                any(), any(), any(), any());
    }
    
    @Test
    @DisplayName("手动出库 - 成功")
    void testManualOut_Success() {
        StockRecord outRecord = new StockRecord();
        outRecord.setOperationType(2);
        outRecord.setQuantity(15);
        when(stockService.manualOut(anyLong(), anyInt(), anyString(), anyString(), anyString(), 
                any(), any(), any(), any())).thenReturn(outRecord);
        
        StockRecord result = stockService.manualOut(
                1L, 15, "仓库A", "操作员", "op001", null, null, null, null);
        
        assertNotNull(result);
        assertEquals(2, result.getOperationType());
    }
    
    @Test
    @DisplayName("手动出库 - 资产不存在应抛出异常")
    void testManualOut_AssetNotFound() {
        when(stockService.manualOut(anyLong(), anyInt(), anyString(), anyString(), anyString(), 
                any(), any(), any(), any())).thenThrow(new RuntimeException("资产不存在"));
        
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            stockService.manualOut(99L, 10, "仓库A", "操作员", "op001", null, null, null, null);
        });
        
        assertEquals("资产不存在", exception.getMessage());
    }
    
    @Test
    @DisplayName("库存盘点 - 成功")
    void testInventoryCheck_Success() {
        StockRecord checkRecord = new StockRecord();
        checkRecord.setOperationType(3);
        checkRecord.setQuantity(-5);
        when(stockService.inventoryCheck(anyLong(), anyInt(), anyString(), anyString(), anyString()))
                .thenReturn(checkRecord);
        
        StockRecord result = stockService.inventoryCheck(
                1L, 95, "仓库A", "盘点员", "checker001");
        
        assertNotNull(result);
        assertEquals(3, result.getOperationType());
    }
    
    @Test
    @DisplayName("库存调拨 - 成功")
    void testStockTransfer_Success() {
        StockRecord transferRecord = new StockRecord();
        transferRecord.setOperationType(4);
        transferRecord.setWarehouse("仓库A");
        transferRecord.setTargetWarehouse("仓库B");
        when(stockService.stockTransfer(anyLong(), anyInt(), anyString(), anyString(), 
                anyString(), anyString(), anyString())).thenReturn(transferRecord);
        
        StockRecord result = stockService.stockTransfer(
                1L, 30, "仓库A", "仓库B", "操作员", "op001", "调拨原因");
        
        assertNotNull(result);
        assertEquals(4, result.getOperationType());
        assertEquals("仓库A", result.getWarehouse());
        assertEquals("仓库B", result.getTargetWarehouse());
    }
    
    @Test
    @DisplayName("库存调拨 - 库存不足应抛出异常")
    void testStockTransfer_InsufficientStock() {
        doThrow(new RuntimeException("库存不足，当前库存：20，调拨数量：50"))
                .when(stockService).stockTransfer(anyLong(), anyInt(), anyString(), anyString(), 
                        anyString(), anyString(), nullable(String.class));
        
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            stockService.stockTransfer(1L, 50, "仓库A", "仓库B", "操作员", "op001", null);
        });
        
        assertTrue(exception.getMessage().contains("库存不足"));
    }
    
    @Test
    @DisplayName("分页查询库存记录")
    void testPageRecords() {
        Page<StockRecord> resultPage = new Page<StockRecord>(1, 10, 1);
        resultPage.setRecords(List.of(testRecord));
        when(stockService.pageRecords(any(Page.class), any(StockRecord.class))).thenReturn(resultPage);
        
        Page<StockRecord> page = new Page<>(1, 10);
        StockRecord query = new StockRecord();
        query.setOperationType(1);
        
        Page<StockRecord> result = stockService.pageRecords(page, query);
        
        assertNotNull(result);
        assertEquals(1, result.getRecords().size());
    }
    
    @Test
    @DisplayName("根据资产ID查询库存记录")
    void testGetRecordsByAssetId() {
        when(stockService.getRecordsByAssetId(1L)).thenReturn(List.of(testRecord));
        
        List<StockRecord> result = stockService.getRecordsByAssetId(1L);
        
        assertEquals(1, result.size());
        verify(stockService).getRecordsByAssetId(1L);
    }
    
    @Test
    @DisplayName("获取实时库存 - 成功")
    void testGetRealTimeStock_Success() {
        Map<String, Object> stockInfo = new HashMap<>();
        stockInfo.put("assetId", 1L);
        stockInfo.put("assetCode", "ASSET-001");
        stockInfo.put("currentQuantity", 100);
        when(stockService.getRealTimeStock(1L)).thenReturn(stockInfo);
        
        Map<String, Object> result = stockService.getRealTimeStock(1L);
        
        assertNotNull(result);
        assertEquals(1L, result.get("assetId"));
        assertEquals("ASSET-001", result.get("assetCode"));
        assertEquals(100, result.get("currentQuantity"));
    }
    
    @Test
    @DisplayName("获取所有实时库存")
    void testGetAllRealTimeStock() {
        List<Map<String, Object>> stockList = new ArrayList<>();
        Map<String, Object> stockInfo = new HashMap<>();
        stockInfo.put("assetId", 1L);
        stockInfo.put("currentQuantity", 100);
        stockList.add(stockInfo);
        when(stockService.getAllRealTimeStock()).thenReturn(stockList);
        
        List<Map<String, Object>> result = stockService.getAllRealTimeStock();
        
        assertEquals(1, result.size());
        assertEquals(1L, result.get(0).get("assetId"));
    }
    
    @Test
    @DisplayName("获取库存统计")
    void testGetStockStatistics() {
        Map<String, Object> stat = new HashMap<>();
        stat.put("totalInQuantity", 100);
        stat.put("totalOutQuantity", 50);
        when(stockService.getStockStatistics(any(), any())).thenReturn(stat);
        
        LocalDateTime startTime = LocalDateTime.now().minusDays(7);
        LocalDateTime endTime = LocalDateTime.now();
        
        Map<String, Object> result = stockService.getStockStatistics(startTime, endTime);
        
        assertNotNull(result);
        assertEquals(100, result.get("totalInQuantity"));
        assertEquals(50, result.get("totalOutQuantity"));
    }
    
    @Test
    @DisplayName("获取库存台账")
    void testGetStockLedger() {
        List<Map<String, Object>> ledger = new ArrayList<>();
        Map<String, Object> entry = new HashMap<>();
        entry.put("recordNo", "IN-20260513-0001");
        ledger.add(entry);
        when(stockService.getStockLedger(any(), any())).thenReturn(ledger);
        
        LocalDateTime startTime = LocalDateTime.now().minusDays(7);
        LocalDateTime endTime = LocalDateTime.now();
        
        List<Map<String, Object>> result = stockService.getStockLedger(startTime, endTime);
        
        assertEquals(1, result.size());
        assertEquals("IN-20260513-0001", result.get(0).get("recordNo"));
    }
    
    @Test
    @DisplayName("确认库存记录 - 成功")
    void testConfirmRecord_Success() {
        when(stockService.confirmRecord(anyLong(), anyString(), anyString())).thenReturn(true);
        
        boolean result = stockService.confirmRecord(1L, "审批人", "同意");
        
        assertTrue(result);
        verify(stockService).confirmRecord(1L, "审批人", "同意");
    }
    
    @Test
    @DisplayName("作废库存记录 - 成功")
    void testCancelRecord_Success() {
        when(stockService.cancelRecord(anyLong(), anyString())).thenReturn(true);
        
        boolean result = stockService.cancelRecord(1L, "测试作废");
        
        assertTrue(result);
        verify(stockService).cancelRecord(1L, "测试作废");
    }
    
    @Test
    @DisplayName("根据标签编码查询库存记录")
    void testGetRecordsByLabelCode() {
        when(stockService.getRecordsByLabelCode(anyString())).thenReturn(List.of(testRecord));
        
        List<StockRecord> result = stockService.getRecordsByLabelCode("LABEL-20260513-ABCD1234");
        
        assertEquals(1, result.size());
        verify(stockService).getRecordsByLabelCode("LABEL-20260513-ABCD1234");
    }
}