package com.aioa.asset.controller;

import com.aioa.asset.dto.StockInDto;
import com.aioa.asset.dto.StockOutDto;
import com.aioa.asset.dto.StockCheckDto;
import com.aioa.asset.dto.StockTransferDto;
import com.aioa.asset.entity.StockRecord;
import com.aioa.asset.service.StockService;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * StockController 单元测试
 */
@WebMvcTest(StockController.class)
@DisplayName("StockControllerTest 库存出入库控制器测试")
class StockControllerTest {

    @Autowired
    private MockMvc mockMvc;

    private ObjectMapper objectMapper;

    @MockBean
    private StockService stockService;

    private StockRecord testRecord;
    private StockInDto stockInDto;
    private StockOutDto stockOutDto;

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());

        testRecord = new StockRecord();
        testRecord.setId(1L);
        testRecord.setRecordNo("STK-20250425-001");
        testRecord.setAssetId(100L);
        testRecord.setAssetCode("AST-001");
        testRecord.setAssetName("测试资产");
        testRecord.setOperationType(1);
        testRecord.setSubType(101);
        testRecord.setQuantity(100);
        testRecord.setBeforeQuantity(0);
        testRecord.setAfterQuantity(100);
        testRecord.setUnitPrice(BigDecimal.valueOf(10.00));
        testRecord.setTotalAmount(BigDecimal.valueOf(1000.00));
        testRecord.setWarehouse("A-01-01");
        testRecord.setOperator("张三");
        testRecord.setOperatorId("user001");
        testRecord.setStatus(1);
        testRecord.setCreateTime(LocalDateTime.now());

        stockInDto = new StockInDto();
        stockInDto.setAssetId(100L);
        stockInDto.setQuantity(100);
        stockInDto.setWarehouse("A-01-01");
        stockInDto.setOperator("张三");
        stockInDto.setOperatorId("user001");
        stockInDto.setBatchNo("BATCH-001");
        stockInDto.setPartner("供应商A");
        stockInDto.setUnitPrice(BigDecimal.valueOf(10.00));
        stockInDto.setRelatedOrderNo("PO-20250425-001");

        stockOutDto = new StockOutDto();
        stockOutDto.setAssetId(100L);
        stockOutDto.setQuantity(50);
        stockOutDto.setWarehouse("A-01-01");
        stockOutDto.setOperator("李四");
        stockOutDto.setOperatorId("user002");
    }

    // ==================== 扫码入库 ====================

    @Test
    @DisplayName("扫码入库成功")
    void scanIn_success() throws Exception {
        stockInDto.setLabelCode("LBL-001");
        when(stockService.scanIn(
                eq("LBL-001"), eq(100), eq("A-01-01"), eq("张三"), eq("user001"),
                eq("BATCH-001"), eq("供应商A"), eq(BigDecimal.valueOf(10.00)), eq("PO-20250425-001")
        )).thenReturn(testRecord);

        mockMvc.perform(post("/asset/stock/scan-in")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(stockInDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.recordNo").value("STK-20250425-001"))
                .andExpect(jsonPath("$.quantity").value(100));

        verify(stockService, times(1)).scanIn(any(), any(), any(), any(), any(), any(), any(), any(), any());
    }

    @Test
    @DisplayName("扫码入库参数校验 - 数量为空")
    void scanIn_validationError() throws Exception {
        stockInDto.setQuantity(null);

        mockMvc.perform(post("/asset/stock/scan-in")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(stockInDto)))
                .andExpect(status().isBadRequest());

        verify(stockService, never()).scanIn(any(), any(), any(), any(), any(), any(), any(), any(), any());
    }

    // ==================== 扫码出库 ====================

    @Test
    @DisplayName("扫码出库成功")
    void scanOut_success() throws Exception {
        stockOutDto.setLabelCode("LBL-001");
        StockRecord outRecord = new StockRecord();
        outRecord.setId(2L);
        outRecord.setRecordNo("STK-20250425-002");
        outRecord.setOperationType(2);
        outRecord.setQuantity(50);

        when(stockService.scanOut(
                eq("LBL-001"), eq(50), eq("A-01-01"), eq("李四"), eq("user002"),
                isNull(), isNull(), isNull(), isNull()
        )).thenReturn(outRecord);

        mockMvc.perform(post("/asset/stock/scan-out")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(stockOutDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.recordNo").value("STK-20250425-002"))
                .andExpect(jsonPath("$.quantity").value(50));
    }

    // ==================== 手动入库 ====================

    @Test
    @DisplayName("手动入库成功")
    void manualIn_success() throws Exception {
        when(stockService.manualIn(
                eq(100L), eq(100), eq("A-01-01"), eq("张三"), eq("user001"),
                eq("BATCH-001"), eq("供应商A"), eq(BigDecimal.valueOf(10.00)), eq("PO-20250425-001")
        )).thenReturn(testRecord);

        mockMvc.perform(post("/asset/stock/manual-in")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(stockInDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.recordNo").value("STK-20250425-001"));

        verify(stockService, times(1)).manualIn(any(), any(), any(), any(), any(), any(), any(), any(), any());
    }

    @Test
    @DisplayName("手动入库参数校验 - 仓库为空")
    void manualIn_validationError() throws Exception {
        stockInDto.setWarehouse(null);

        mockMvc.perform(post("/asset/stock/manual-in")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(stockInDto)))
                .andExpect(status().isBadRequest());

        verify(stockService, never()).manualIn(any(), any(), any(), any(), any(), any(), any(), any(), any());
    }

    // ==================== 手动出库 ====================

    @Test
    @DisplayName("手动出库成功")
    void manualOut_success() throws Exception {
        StockRecord outRecord = new StockRecord();
        outRecord.setId(2L);
        outRecord.setRecordNo("STK-20250425-002");
        outRecord.setQuantity(50);

        when(stockService.manualOut(
                eq(100L), eq(50), eq("A-01-01"), eq("李四"), eq("user002"),
                isNull(), isNull(), isNull(), isNull()
        )).thenReturn(outRecord);

        mockMvc.perform(post("/asset/stock/manual-out")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(stockOutDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.quantity").value(50));
    }

    // ==================== 库存盘点 ====================

    @Test
    @DisplayName("库存盘点成功")
    void inventoryCheck_success() throws Exception {
        StockCheckDto checkDto = new StockCheckDto();
        checkDto.setAssetId(100L);
        checkDto.setActualQuantity(95);
        checkDto.setWarehouse("A-01-01");
        checkDto.setOperator("王五");
        checkDto.setOperatorId("user003");

        StockRecord checkRecord = new StockRecord();
        checkRecord.setId(3L);
        checkRecord.setRecordNo("STK-20250425-003");
        checkRecord.setOperationType(3);
        checkRecord.setQuantity(-5);

        when(stockService.inventoryCheck(eq(100L), eq(95), eq("A-01-01"), eq("王五"), eq("user003")))
                .thenReturn(checkRecord);

        mockMvc.perform(post("/asset/stock/inventory-check")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(checkDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.operationType").value(3))
                .andExpect(jsonPath("$.quantity").value(-5));
    }

    // ==================== 库存调拨 ====================

    @Test
    @DisplayName("库存调拨成功")
    void stockTransfer_success() throws Exception {
        StockTransferDto transferDto = new StockTransferDto();
        transferDto.setAssetId(100L);
        transferDto.setQuantity(30);
        transferDto.setFromWarehouse("A-01-01");
        transferDto.setToWarehouse("B-02-02");
        transferDto.setOperator("赵六");
        transferDto.setOperatorId("user004");
        transferDto.setReason("部门调拨");

        StockRecord transferRecord = new StockRecord();
        transferRecord.setId(4L);
        transferRecord.setRecordNo("STK-20250425-004");
        transferRecord.setOperationType(4);
        transferRecord.setQuantity(30);

        when(stockService.stockTransfer(
                eq(100L), eq(30), eq("A-01-01"), eq("B-02-02"), eq("赵六"), eq("user004"), eq("部门调拨")
        )).thenReturn(transferRecord);

        mockMvc.perform(post("/asset/stock/transfer")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(transferDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.operationType").value(4))
                .andExpect(jsonPath("$.quantity").value(30));
    }

    // ==================== 分页查询库存流水 ====================

    @Test
    @DisplayName("分页查询库存流水成功")
    void pageRecords_success() throws Exception {
        Page<StockRecord> page = new Page<>(1, 10);
        page.setRecords(List.of(testRecord));
        page.setTotal(1);

        when(stockService.pageRecords(any(Page.class), any(StockRecord.class)))
                .thenReturn(page);

        mockMvc.perform(get("/asset/stock/page")
                        .param("page", "1")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.records[0].recordNo").value("STK-20250425-001"))
                .andExpect(jsonPath("$.total").value(1));
    }

    @Test
    @DisplayName("分页查询库存流水为空")
    void pageRecords_empty() throws Exception {
        Page<StockRecord> page = new Page<>(1, 10);
        page.setRecords(List.of());
        page.setTotal(0);

        when(stockService.pageRecords(any(Page.class), any(StockRecord.class)))
                .thenReturn(page);

        mockMvc.perform(get("/asset/stock/page")
                        .param("page", "1")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.records.length()").value(0));
    }

    // ==================== 根据资产ID查询库存流水 ====================

    @Test
    @DisplayName("根据资产ID查询库存流水成功")
    void getRecordsByAssetId_success() throws Exception {
        when(stockService.getRecordsByAssetId(100L))
                .thenReturn(List.of(testRecord));

        mockMvc.perform(get("/asset/stock/records/asset/100"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].recordNo").value("STK-20250425-001"));

        verify(stockService, times(1)).getRecordsByAssetId(100L);
    }

    @Test
    @DisplayName("根据资产ID查询库存流水为空")
    void getRecordsByAssetId_empty() throws Exception {
        when(stockService.getRecordsByAssetId(999L))
                .thenReturn(List.of());

        mockMvc.perform(get("/asset/stock/records/asset/999"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(0));
    }

    // ==================== 查询实时库存 ====================

    @Test
    @DisplayName("查询实时库存成功")
    void getRealTimeStock_success() throws Exception {
        Map<String, Object> realtimeStock = new HashMap<>();
        realtimeStock.put("assetId", 100L);
        realtimeStock.put("assetName", "测试资产");
        realtimeStock.put("totalQuantity", 500);
        realtimeStock.put("availableQuantity", 450);

        when(stockService.getRealTimeStock(100L))
                .thenReturn(realtimeStock);

        mockMvc.perform(get("/asset/stock/realtime/100"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalQuantity").value(500))
                .andExpect(jsonPath("$.availableQuantity").value(450));
    }

    // ==================== 查询所有资产实时库存 ====================

    @Test
    @DisplayName("查询所有资产实时库存成功")
    void getAllRealTimeStock_success() throws Exception {
        Map<String, Object> stock1 = new HashMap<>();
        stock1.put("assetId", 100L);
        stock1.put("totalQuantity", 500);

        Map<String, Object> stock2 = new HashMap<>();
        stock2.put("assetId", 101L);
        stock2.put("totalQuantity", 300);

        when(stockService.getAllRealTimeStock())
                .thenReturn(Arrays.asList(stock1, stock2));

        mockMvc.perform(get("/asset/stock/realtime/all"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2));
    }

    // ==================== 查询库存统计 ====================

    @Test
    @DisplayName("查询库存统计成功")
    void getStockStatistics_success() throws Exception {
        Map<String, Object> stats = new HashMap<>();
        stats.put("totalInCount", 1000);
        stats.put("totalOutCount", 800);
        stats.put("netQuantity", 200);

        LocalDateTime startTime = LocalDateTime.now().minusDays(7);
        LocalDateTime endTime = LocalDateTime.now();

        when(stockService.getStockStatistics(any(LocalDateTime.class), any(LocalDateTime.class)))
                .thenReturn(stats);

        mockMvc.perform(get("/asset/stock/statistics")
                        .param("startTime", startTime.toString())
                        .param("endTime", endTime.toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalInCount").value(1000))
                .andExpect(jsonPath("$.totalOutCount").value(800));
    }

    // ==================== 查询库存台账 ====================

    @Test
    @DisplayName("查询库存台账成功")
    void getStockLedger_success() throws Exception {
        Map<String, Object> ledger1 = new HashMap<>();
        ledger1.put("assetId", 100L);
        ledger1.put("assetName", "测试资产");
        ledger1.put("openingStock", 400);
        ledger1.put("inQuantity", 200);
        ledger1.put("outQuantity", 100);
        ledger1.put("closingStock", 500);

        LocalDateTime startTime = LocalDateTime.now().minusDays(7);
        LocalDateTime endTime = LocalDateTime.now();

        when(stockService.getStockLedger(any(LocalDateTime.class), any(LocalDateTime.class)))
                .thenReturn(List.of(ledger1));

        mockMvc.perform(get("/asset/stock/ledger")
                        .param("startTime", startTime.toString())
                        .param("endTime", endTime.toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].closingStock").value(500));
    }

    // ==================== 根据标签查询操作历史 ====================

    @Test
    @DisplayName("根据标签查询操作历史成功")
    void getRecordsByLabelCode_success() throws Exception {
        StockRecord labelRecord = new StockRecord();
        labelRecord.setScannedLabelCode("LBL-001");
        when(stockService.getRecordsByLabelCode("LBL-001"))
                .thenReturn(List.of(labelRecord));

        mockMvc.perform(get("/asset/stock/records/label/LBL-001"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].scannedLabelCode").value("LBL-001"));
    }

    // ==================== 确认库存操作 ====================

    @Test
    @DisplayName("确认库存操作成功")
    void confirmRecord_success() throws Exception {
        when(stockService.confirmRecord(1L, "审批人", "同意"))
                .thenReturn(true);

        mockMvc.perform(put("/asset/stock/confirm/1")
                        .param("approver", "审批人")
                        .param("approvalComment", "同意"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").value(true));
    }

    @Test
    @DisplayName("确认库存操作失败")
    void confirmRecord_failure() throws Exception {
        when(stockService.confirmRecord(999L, "审批人", "同意"))
                .thenReturn(false);

        mockMvc.perform(put("/asset/stock/confirm/999")
                        .param("approver", "审批人")
                        .param("approvalComment", "同意"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").value(false));
    }

    // ==================== 作废库存操作 ====================

    @Test
    @DisplayName("作废库存操作成功")
    void cancelRecord_success() throws Exception {
        when(stockService.cancelRecord(1L, "操作失误"))
                .thenReturn(true);

        mockMvc.perform(put("/asset/stock/cancel/1")
                        .param("reason", "操作失误"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").value(true));
    }

    // ==================== 获取记录详情 ====================

    @Test
    @DisplayName("获取记录详情成功")
    void getById_success() throws Exception {
        when(stockService.getById(1L)).thenReturn(testRecord);

        mockMvc.perform(get("/asset/stock/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.recordNo").value("STK-20250425-001"));

        verify(stockService, times(1)).getById(1L);
    }

    @Test
    @DisplayName("获取记录详情不存在")
    void getById_notFound() throws Exception {
        when(stockService.getById(999L)).thenReturn(null);

        mockMvc.perform(get("/asset/stock/999"))
                .andExpect(status().isOk());

        verify(stockService, times(1)).getById(999L);
    }

    // ==================== 删除记录 ====================

    @Test
    @DisplayName("删除记录成功")
    void deleteRecord_success() throws Exception {
        when(stockService.removeById(1L)).thenReturn(true);

        mockMvc.perform(delete("/asset/stock/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").value(true));
    }

    @Test
    @DisplayName("删除记录失败")
    void deleteRecord_failure() throws Exception {
        when(stockService.removeById(999L)).thenReturn(false);

        mockMvc.perform(delete("/asset/stock/999"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").value(false));
    }

    @Test
    @DisplayName("无效JSON格式")
    void invalidJson() throws Exception {
        mockMvc.perform(post("/asset/stock/manual-in")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{invalid json}"))
                .andExpect(status().isBadRequest());

        verify(stockService, never()).manualIn(any(), any(), any(), any(), any(), any(), any(), any(), any());
    }
}