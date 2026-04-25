package com.aioa.asset.service;

import com.aioa.asset.entity.StockRecord;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * 库存出入库Service接口
 */
public interface StockService extends IService<StockRecord> {
    
    /**
     * 扫码入库
     */
    StockRecord scanIn(String labelCode, Integer quantity, String warehouse, String operator, String operatorId, 
                      String batchNo, String partner, BigDecimal unitPrice, String relatedOrderNo);
    
    /**
     * 扫码出库
     */
    StockRecord scanOut(String labelCode, Integer quantity, String warehouse, String operator, String operatorId,
                       String batchNo, String partner, BigDecimal unitPrice, String relatedOrderNo);
    
    /**
     * 手动入库
     */
    StockRecord manualIn(Long assetId, Integer quantity, String warehouse, String operator, String operatorId,
                        String batchNo, String partner, BigDecimal unitPrice, String relatedOrderNo);
    
    /**
     * 手动出库
     */
    StockRecord manualOut(Long assetId, Integer quantity, String warehouse, String operator, String operatorId,
                         String batchNo, String partner, BigDecimal unitPrice, String relatedOrderNo);
    
    /**
     * 库存盘点
     */
    StockRecord inventoryCheck(Long assetId, Integer actualQuantity, String warehouse, String operator, String operatorId);
    
    /**
     * 库存调拨
     */
    StockRecord stockTransfer(Long assetId, Integer quantity, String fromWarehouse, String toWarehouse, 
                             String operator, String operatorId, String reason);
    
    /**
     * 分页查询库存流水
     */
    Page<StockRecord> pageRecords(Page<StockRecord> page, StockRecord query);
    
    /**
     * 根据资产ID查询库存流水
     */
    List<StockRecord> getRecordsByAssetId(Long assetId);
    
    /**
     * 查询实时库存
     */
    Map<String, Object> getRealTimeStock(Long assetId);
    
    /**
     * 查询所有资产实时库存
     */
    List<Map<String, Object>> getAllRealTimeStock();
    
    /**
     * 查询出入库统计
     */
    Map<String, Object> getStockStatistics(LocalDateTime startTime, LocalDateTime endTime);
    
    /**
     * 查询库存台账
     */
    List<Map<String, Object>> getStockLedger(LocalDateTime startTime, LocalDateTime endTime);
    
    /**
     * 确认库存操作
     */
    boolean confirmRecord(Long recordId, String approver, String approvalComment);
    
    /**
     * 作废库存操作
     */
    boolean cancelRecord(Long recordId, String reason);
    
    /**
     * 根据扫描标签查询历史
     */
    List<StockRecord> getRecordsByLabelCode(String labelCode);
}