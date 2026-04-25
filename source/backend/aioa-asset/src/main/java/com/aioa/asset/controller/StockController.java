package com.aioa.asset.controller;

import com.aioa.asset.dto.StockInDto;
import com.aioa.asset.dto.StockOutDto;
import com.aioa.asset.dto.StockCheckDto;
import com.aioa.asset.dto.StockTransferDto;
import com.aioa.asset.entity.StockRecord;
import com.aioa.asset.service.StockService;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * 库存出入库Controller
 */
@Tag(name = "物料出入库登记")
@RestController
@RequestMapping("/asset/stock")
public class StockController {
    
    @Autowired
    private StockService stockService;
    
    @Operation(summary = "扫码入库")
    @PostMapping("/scan-in")
    public StockRecord scanIn(@Validated @RequestBody StockInDto dto) {
        return stockService.scanIn(
            dto.getLabelCode(), dto.getQuantity(), dto.getWarehouse(),
            dto.getOperator(), dto.getOperatorId(), dto.getBatchNo(),
            dto.getPartner(), dto.getUnitPrice(), dto.getRelatedOrderNo()
        );
    }
    
    @Operation(summary = "扫码出库")
    @PostMapping("/scan-out")
    public StockRecord scanOut(@Validated @RequestBody StockOutDto dto) {
        return stockService.scanOut(
            dto.getLabelCode(), dto.getQuantity(), dto.getWarehouse(),
            dto.getOperator(), dto.getOperatorId(), dto.getBatchNo(),
            dto.getPartner(), dto.getUnitPrice(), dto.getRelatedOrderNo()
        );
    }
    
    @Operation(summary = "手动入库")
    @PostMapping("/manual-in")
    public StockRecord manualIn(@Validated @RequestBody StockInDto dto) {
        return stockService.manualIn(
            dto.getAssetId(), dto.getQuantity(), dto.getWarehouse(),
            dto.getOperator(), dto.getOperatorId(), dto.getBatchNo(),
            dto.getPartner(), dto.getUnitPrice(), dto.getRelatedOrderNo()
        );
    }
    
    @Operation(summary = "手动出库")
    @PostMapping("/manual-out")
    public StockRecord manualOut(@Validated @RequestBody StockOutDto dto) {
        return stockService.manualOut(
            dto.getAssetId(), dto.getQuantity(), dto.getWarehouse(),
            dto.getOperator(), dto.getOperatorId(), dto.getBatchNo(),
            dto.getPartner(), dto.getUnitPrice(), dto.getRelatedOrderNo()
        );
    }
    
    @Operation(summary = "库存盘点")
    @PostMapping("/inventory-check")
    public StockRecord inventoryCheck(@Validated @RequestBody StockCheckDto dto) {
        return stockService.inventoryCheck(
            dto.getAssetId(), dto.getActualQuantity(), dto.getWarehouse(),
            dto.getOperator(), dto.getOperatorId()
        );
    }
    
    @Operation(summary = "库存调拨")
    @PostMapping("/transfer")
    public StockRecord stockTransfer(@Validated @RequestBody StockTransferDto dto) {
        return stockService.stockTransfer(
            dto.getAssetId(), dto.getQuantity(), dto.getFromWarehouse(),
            dto.getToWarehouse(), dto.getOperator(), dto.getOperatorId(),
            dto.getReason()
        );
    }
    
    @Operation(summary = "分页查询库存流水")
    @GetMapping("/page")
    public Page<StockRecord> pageRecords(Page<StockRecord> page, StockRecord query) {
        return stockService.pageRecords(page, query);
    }
    
    @Operation(summary = "根据资产ID查询库存流水")
    @GetMapping("/records/asset/{assetId}")
    public List<StockRecord> getRecordsByAssetId(@PathVariable Long assetId) {
        return stockService.getRecordsByAssetId(assetId);
    }
    
    @Operation(summary = "查询实时库存")
    @GetMapping("/realtime/{assetId}")
    public Map<String, Object> getRealTimeStock(@PathVariable Long assetId) {
        return stockService.getRealTimeStock(assetId);
    }
    
    @Operation(summary = "查询所有资产实时库存")
    @GetMapping("/realtime/all")
    public List<Map<String, Object>> getAllRealTimeStock() {
        return stockService.getAllRealTimeStock();
    }
    
    @Operation(summary = "查询库存统计")
    @GetMapping("/statistics")
    public Map<String, Object> getStockStatistics(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startTime,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endTime) {
        return stockService.getStockStatistics(startTime, endTime);
    }
    
    @Operation(summary = "查询库存台账")
    @GetMapping("/ledger")
    public List<Map<String, Object>> getStockLedger(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startTime,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endTime) {
        return stockService.getStockLedger(startTime, endTime);
    }
    
    @Operation(summary = "根据标签查询操作历史")
    @GetMapping("/records/label/{labelCode}")
    public List<StockRecord> getRecordsByLabelCode(@PathVariable String labelCode) {
        return stockService.getRecordsByLabelCode(labelCode);
    }
    
    @Operation(summary = "确认库存操作")
    @PutMapping("/confirm/{recordId}")
    public boolean confirmRecord(@PathVariable Long recordId,
                                @RequestParam String approver,
                                @RequestParam String approvalComment) {
        return stockService.confirmRecord(recordId, approver, approvalComment);
    }
    
    @Operation(summary = "作废库存操作")
    @PutMapping("/cancel/{recordId}")
    public boolean cancelRecord(@PathVariable Long recordId,
                               @RequestParam String reason) {
        return stockService.cancelRecord(recordId, reason);
    }
    
    @Operation(summary = "获取记录详情")
    @GetMapping("/{id}")
    public StockRecord getById(@PathVariable Long id) {
        return stockService.getById(id);
    }
    
    @Operation(summary = "删除记录")
    @DeleteMapping("/{id}")
    public boolean deleteRecord(@PathVariable Long id) {
        return stockService.removeById(id);
    }
}