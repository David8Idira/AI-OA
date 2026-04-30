package com.aioa.asset.service;

import com.aioa.asset.entity.AssetInfo;
import com.aioa.asset.entity.AssetLabel;
import com.aioa.asset.entity.StockRecord;
import com.aioa.asset.mapper.AssetInfoMapper;
import com.aioa.asset.mapper.AssetLabelMapper;
import com.aioa.asset.mapper.StockRecordMapper;
import com.aioa.asset.service.impl.StockServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import java.lang.reflect.Field;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * 库存服务单元测试 - Mockito版本
 */
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class StockServiceTest {
    
    @Mock
    private AssetInfoMapper assetInfoMapper;
    
    @Mock
    private AssetLabelMapper assetLabelMapper;
    
    @Mock
    private StockRecordMapper stockRecordMapper;
    
    private StockServiceImpl stockService;
    
    private AssetInfo testAsset;
    private AssetLabel testLabel;
    
    @BeforeEach
    void setUp() throws Exception {
        // 使用@RequiredArgsConstructor构造service
        stockService = new StockServiceImpl(assetInfoMapper, assetLabelMapper, stockRecordMapper);
        
        // 通过反射设置baseMapper（来自ServiceImpl）
        Field baseMapperField = stockService.getClass().getSuperclass().getDeclaredField("baseMapper");
        baseMapperField.setAccessible(true);
        baseMapperField.set(stockService, stockRecordMapper);
        
        testAsset = new AssetInfo();
        testAsset.setId(1L);
        testAsset.setAssetCode("ASSET-STOCK-001");
        testAsset.setAssetName("测试资产-库存");
        testAsset.setCategoryId(1L);
        testAsset.setModel("测试型号");
        testAsset.setCurrentQuantity(100);
        testAsset.setWarningQuantity(10);
        testAsset.setPurchasePrice(new BigDecimal("1000.00"));
        testAsset.setPurchaseDate(LocalDate.now());
        testAsset.setAssetStatus(1);
        testAsset.setStatus(1);
        
        testLabel = new AssetLabel();
        testLabel.setId(1L);
        testLabel.setAssetId(1L);
        testLabel.setAssetCode("ASSET-STOCK-001");
        testLabel.setAssetName("测试资产-库存");
        testLabel.setLabelCode("LABEL-STOCK-001");
        testLabel.setLabelStatus(1);
        testLabel.setPrintStatus(0);
    }
    
    @Test
    void testManualIn() {
        when(assetInfoMapper.selectById(1L)).thenReturn(testAsset);
        when(assetInfoMapper.updateById(any(AssetInfo.class))).thenReturn(1);
        when(stockRecordMapper.insert(any(StockRecord.class))).thenReturn(1);
        
        StockRecord result = stockService.manualIn(
                1L, 50, "仓库A", "操作员1", "operator1",
                "BATCH-001", "供应商A", new BigDecimal("100.00"), "PO-001");
        
        assertNotNull(result);
        assertEquals(testAsset.getId(), result.getAssetId());
        assertEquals(1, result.getOperationType()); // 入库
        assertEquals(50, result.getQuantity());
        assertEquals(100, result.getBeforeQuantity());
        assertEquals(150, result.getAfterQuantity());
        assertEquals("仓库A", result.getWarehouse());
        
        verify(assetInfoMapper, times(1)).selectById(1L);
        verify(assetInfoMapper, times(1)).updateById(any(AssetInfo.class));
        verify(stockRecordMapper, times(1)).insert(any(StockRecord.class));
    }
    
    @Test
    void testManualOut() {
        when(assetInfoMapper.selectById(1L)).thenReturn(testAsset);
        when(assetInfoMapper.updateById(any(AssetInfo.class))).thenReturn(1);
        when(stockRecordMapper.insert(any(StockRecord.class))).thenReturn(1);
        
        StockRecord result = stockService.manualOut(
                1L, 30, "仓库A", "操作员2", "operator2",
                "BATCH-001", "客户A", new BigDecimal("120.00"), "SO-001");
        
        assertNotNull(result);
        assertEquals(2, result.getOperationType()); // 出库
        assertEquals(30, result.getQuantity());
        assertEquals(100, result.getBeforeQuantity());
        assertEquals(70, result.getAfterQuantity());
        
        verify(assetInfoMapper, times(1)).selectById(1L);
        verify(assetInfoMapper, times(1)).updateById(any(AssetInfo.class));
    }
    
    @Test
    void testManualOut_InsufficientStock() {
        when(assetInfoMapper.selectById(1L)).thenReturn(testAsset);
        
        RuntimeException exception = assertThrows(RuntimeException.class,
                () -> stockService.manualOut(1L, 150, "仓库A", "op", "op", null, null, null, null));
        
        assertTrue(exception.getMessage().contains("库存不足"));
    }
    
    @Test
    void testManualOut_AssetNotFound() {
        when(assetInfoMapper.selectById(99L)).thenReturn(null);
        
        RuntimeException exception = assertThrows(RuntimeException.class,
                () -> stockService.manualOut(99L, 10, "仓库A", "op", "op", null, null, null, null));
        
        assertTrue(exception.getMessage().contains("资产不存在"));
    }
    
    @Test
    void testScanIn() {
        when(assetLabelMapper.selectByLabelCode("LABEL-STOCK-001")).thenReturn(testLabel);
        when(assetInfoMapper.selectById(1L)).thenReturn(testAsset);
        when(assetInfoMapper.updateById(any(AssetInfo.class))).thenReturn(1);
        when(stockRecordMapper.insert(any(StockRecord.class))).thenReturn(1);
        
        StockRecord result = stockService.scanIn(
                "LABEL-STOCK-001", 100, "仓库B", "操作员3", "operator3",
                "BATCH-002", "供应商B", new BigDecimal("200.00"), "PO-002");
        
        assertNotNull(result);
        assertEquals(1, result.getOperationType());
        assertEquals("LABEL-STOCK-001", result.getScannedLabelCode());
        assertEquals(1, result.getScanMethod()); // 扫码枪
        assertEquals(100, result.getQuantity());
        assertEquals(100, result.getBeforeQuantity());
        assertEquals(200, result.getAfterQuantity());
    }
    
    @Test
    void testScanIn_LabelNotFound() {
        when(assetLabelMapper.selectByLabelCode("INVALID")).thenReturn(null);
        
        RuntimeException exception = assertThrows(RuntimeException.class,
                () -> stockService.scanIn("INVALID", 50, "仓库", "op", "op", null, null, null, null));
        
        assertTrue(exception.getMessage().contains("标签不存在"));
    }
    
    @Test
    void testScanIn_AssetNotFound() {
        when(assetLabelMapper.selectByLabelCode("LABEL-STOCK-001")).thenReturn(testLabel);
        when(assetInfoMapper.selectById(1L)).thenReturn(null);
        
        RuntimeException exception = assertThrows(RuntimeException.class,
                () -> stockService.scanIn("LABEL-STOCK-001", 50, "仓库", "op", "op", null, null, null, null));
        
        assertTrue(exception.getMessage().contains("资产不存在"));
    }
    
    @Test
    void testScanOut() {
        when(assetLabelMapper.selectByLabelCode("LABEL-STOCK-001")).thenReturn(testLabel);
        when(assetInfoMapper.selectById(1L)).thenReturn(testAsset);
        when(assetInfoMapper.updateById(any(AssetInfo.class))).thenReturn(1);
        when(stockRecordMapper.insert(any(StockRecord.class))).thenReturn(1);
        
        StockRecord result = stockService.scanOut(
                "LABEL-STOCK-001", 50, "仓库B", "操作员4", "operator4",
                "BATCH-002", "客户B", new BigDecimal("220.00"), "SO-002");
        
        assertNotNull(result);
        assertEquals(2, result.getOperationType());
        assertEquals(50, result.getQuantity());
        assertEquals("LABEL-STOCK-001", result.getScannedLabelCode());
    }
    
    @Test
    void testScanOut_InsufficientStock() {
        when(assetLabelMapper.selectByLabelCode("LABEL-STOCK-001")).thenReturn(testLabel);
        when(assetInfoMapper.selectById(1L)).thenReturn(testAsset);
        
        RuntimeException exception = assertThrows(RuntimeException.class,
                () -> stockService.scanOut("LABEL-STOCK-001", 150, "仓库", "op", "op", null, null, null, null));
        
        assertTrue(exception.getMessage().contains("库存不足"));
    }
    
    @Test
    void testInventoryCheck() {
        when(assetInfoMapper.selectById(1L)).thenReturn(testAsset);
        when(assetInfoMapper.updateById(any(AssetInfo.class))).thenReturn(1);
        when(stockRecordMapper.insert(any(StockRecord.class))).thenReturn(1);
        
        StockRecord result = stockService.inventoryCheck(1L, 80, "仓库C", "操作员5", "operator5");
        
        assertNotNull(result);
        assertEquals(3, result.getOperationType()); // 盘点
        assertEquals(-20, result.getQuantity()); // 盘亏20
        assertEquals(100, result.getBeforeQuantity());
        assertEquals(80, result.getAfterQuantity());
    }
    
    @Test
    void testStockTransfer() {
        when(assetInfoMapper.selectById(1L)).thenReturn(testAsset);
        when(assetInfoMapper.updateById(any(AssetInfo.class))).thenReturn(1);
        when(stockRecordMapper.insert(any(StockRecord.class))).thenReturn(1);
        
        StockRecord result = stockService.stockTransfer(
                1L, 30, "仓库D", "仓库E", "操作员6", "operator6", "部门调整");
        
        assertNotNull(result);
        assertEquals(4, result.getOperationType()); // 调拨
        assertEquals(30, result.getQuantity());
        assertEquals(100, result.getBeforeQuantity());
        assertEquals(70, result.getAfterQuantity());
        assertEquals("仓库D", result.getWarehouse());
        assertEquals("仓库E", result.getTargetWarehouse());
    }
    
    @Test
    void testStockTransfer_InsufficientStock() {
        when(assetInfoMapper.selectById(1L)).thenReturn(testAsset);
        
        RuntimeException exception = assertThrows(RuntimeException.class,
                () -> stockService.stockTransfer(1L, 150, "仓库D", "仓库E", "op", "op", "调拨"));
        
        assertTrue(exception.getMessage().contains("库存不足"));
    }
    
    @Test
    void testGetRealTimeStock() {
        when(assetInfoMapper.selectById(1L)).thenReturn(testAsset);
        when(stockRecordMapper.selectByAssetId(1L)).thenReturn(Collections.emptyList());
        
        Map<String, Object> result = stockService.getRealTimeStock(1L);
        
        assertNotNull(result);
        assertEquals(1L, result.get("assetId"));
        assertEquals("ASSET-STOCK-001", result.get("assetCode"));
        assertEquals("测试资产-库存", result.get("assetName"));
        assertEquals(100, result.get("currentQuantity"));
    }
    
    @Test
    void testGetRealTimeStock_AssetNotFound() {
        when(assetInfoMapper.selectById(99L)).thenReturn(null);
        
        RuntimeException exception = assertThrows(RuntimeException.class,
                () -> stockService.getRealTimeStock(99L));
        
        assertTrue(exception.getMessage().contains("资产不存在"));
    }
    
    @Test
    void testGetAllRealTimeStock() {
        AssetInfo asset2 = new AssetInfo();
        asset2.setId(2L);
        asset2.setAssetCode("ASSET-STOCK-002");
        asset2.setAssetName("测试资产2");
        asset2.setCurrentQuantity(200);
        asset2.setWarningQuantity(20);
        
        when(assetInfoMapper.selectList(any())).thenReturn(Arrays.asList(testAsset, asset2));
        
        List<Map<String, Object>> result = stockService.getAllRealTimeStock();
        
        assertNotNull(result);
        assertEquals(2, result.size());
    }
    
    @Test
    void testGetRecordsByLabelCode() {
        StockRecord record = new StockRecord();
        record.setId(1L);
        record.setAssetId(1L);
        record.setScannedLabelCode("LABEL-STOCK-001");
        record.setOperationType(1);
        
        when(stockRecordMapper.selectByLabelCode("LABEL-STOCK-001"))
                .thenReturn(Collections.singletonList(record));
        
        List<StockRecord> result = stockService.getRecordsByLabelCode("LABEL-STOCK-001");
        
        assertFalse(result.isEmpty());
        assertEquals("LABEL-STOCK-001", result.get(0).getScannedLabelCode());
    }
    
    @Test
    void testConfirmRecord() {
        StockRecord record = new StockRecord();
        record.setId(1L);
        record.setApprovalStatus(0);
        
        when(stockRecordMapper.selectById(1L)).thenReturn(record);
        when(stockRecordMapper.updateById(any(StockRecord.class))).thenReturn(1);
        
        boolean result = stockService.confirmRecord(1L, "审批人", "同意");
        
        assertTrue(result);
        verify(stockRecordMapper, times(1)).updateById(any(StockRecord.class));
    }
    
    @Test
    void testConfirmRecord_NotFound() {
        when(stockRecordMapper.selectById(99L)).thenReturn(null);
        
        RuntimeException exception = assertThrows(RuntimeException.class,
                () -> stockService.confirmRecord(99L, "审批人", "同意"));
        
        assertTrue(exception.getMessage().contains("记录不存在"));
    }
    
    @Test
    void testCancelRecord() {
        StockRecord record = new StockRecord();
        record.setId(1L);
        record.setAssetId(1L);
        record.setOperationType(1); // 入库
        record.setQuantity(50);
        record.setStatus(1);
        
        when(stockRecordMapper.selectById(1L)).thenReturn(record);
        when(assetInfoMapper.selectById(1L)).thenReturn(testAsset);
        when(assetInfoMapper.updateById(any(AssetInfo.class))).thenReturn(1);
        when(stockRecordMapper.updateById(any(StockRecord.class))).thenReturn(1);
        
        boolean result = stockService.cancelRecord(1L, "测试作废");
        
        assertTrue(result);
        verify(assetInfoMapper, times(1)).updateById(any(AssetInfo.class));
        verify(stockRecordMapper, times(1)).updateById(any(StockRecord.class));
    }
    
    @Test
    void testGetStockStatistics() {
        List<Map<String, Object>> operationStats = new ArrayList<>();
        Map<String, Object> stat1 = new HashMap<>();
        stat1.put("operation_type", 1);
        stat1.put("count", 10L);
        stat1.put("total_quantity", 100L);
        stat1.put("total_amount", new BigDecimal("1000.00"));
        operationStats.add(stat1);
        
        when(stockRecordMapper.groupByOperationType(any(), any())).thenReturn(operationStats);
        when(stockRecordMapper.selectStockBalance()).thenReturn(Collections.emptyList());
        
        LocalDateTime endTime = LocalDateTime.now();
        LocalDateTime startTime = endTime.minusDays(7);
        Map<String, Object> result = stockService.getStockStatistics(startTime, endTime);
        
        assertNotNull(result);
        assertTrue(result.containsKey("totalInQuantity"));
        assertTrue(result.containsKey("totalOutQuantity"));
    }
    
    @Test
    void testGetStockLedger() {
        StockRecord record = new StockRecord();
        record.setId(1L);
        record.setAssetCode("ASSET-STOCK-001");
        record.setAssetName("测试资产");
        record.setQuantity(100);
        record.setOperationTime(LocalDateTime.now());
        
        when(stockRecordMapper.selectByTimeRange(any(), any()))
                .thenReturn(Collections.singletonList(record));
        
        LocalDateTime endTime = LocalDateTime.now();
        LocalDateTime startTime = endTime.minusDays(1);
        List<Map<String, Object>> result = stockService.getStockLedger(startTime, endTime);
        
        assertFalse(result.isEmpty());
        assertEquals("ASSET-STOCK-001", result.get(0).get("assetCode"));
    }
}
