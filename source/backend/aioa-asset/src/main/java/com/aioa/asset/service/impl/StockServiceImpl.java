package com.aioa.asset.service.impl;

import com.aioa.asset.entity.AssetInfo;
import com.aioa.asset.entity.AssetLabel;
import com.aioa.asset.entity.StockRecord;
import com.aioa.asset.mapper.AssetInfoMapper;
import com.aioa.asset.mapper.AssetLabelMapper;
import com.aioa.asset.mapper.StockRecordMapper;
import com.aioa.asset.service.StockService;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 库存出入库Service实现
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class StockServiceImpl extends ServiceImpl<StockRecordMapper, StockRecord> implements StockService {
    
    private final AssetInfoMapper assetInfoMapper;
    private final AssetLabelMapper assetLabelMapper;
    private final StockRecordMapper stockRecordMapper;
    
    @Override
    @Transactional(rollbackFor = Exception.class)
    public StockRecord scanIn(String labelCode, Integer quantity, String warehouse, String operator, String operatorId,
                             String batchNo, String partner, BigDecimal unitPrice, String relatedOrderNo) {
        // 根据标签编码查找资产
        AssetLabel label = assetLabelMapper.selectByLabelCode(labelCode);
        if (label == null) {
            throw new RuntimeException("标签不存在");
        }
        
        AssetInfo assetInfo = assetInfoMapper.selectById(label.getAssetId());
        if (assetInfo == null) {
            throw new RuntimeException("资产不存在");
        }
        
        // 查询当前库存
        Integer currentStock = assetInfo.getCurrentQuantity();
        
        // 创建入库记录
        StockRecord record = createStockRecord(
            assetInfo.getId(),
            assetInfo.getAssetCode(),
            assetInfo.getAssetName(),
            1, // 入库
            101, // 采购入库
            quantity,
            currentStock,
            currentStock + quantity,
            warehouse,
            null,
            operator,
            operatorId,
            labelCode,
            1, // 扫码枪
            unitPrice,
            unitPrice != null ? unitPrice.multiply(BigDecimal.valueOf(quantity)) : null,
            batchNo,
            partner,
            relatedOrderNo
        );
        
        // 更新资产库存
        updateAssetStock(assetInfo.getId(), currentStock + quantity);
        
        log.info("扫码入库成功，资产ID：{}，数量：{}，标签：{}", assetInfo.getId(), quantity, labelCode);
        return record;
    }
    
    @Override
    @Transactional(rollbackFor = Exception.class)
    public StockRecord scanOut(String labelCode, Integer quantity, String warehouse, String operator, String operatorId,
                              String batchNo, String partner, BigDecimal unitPrice, String relatedOrderNo) {
        // 根据标签编码查找资产
        AssetLabel label = assetLabelMapper.selectByLabelCode(labelCode);
        if (label == null) {
            throw new RuntimeException("标签不存在");
        }
        
        AssetInfo assetInfo = assetInfoMapper.selectById(label.getAssetId());
        if (assetInfo == null) {
            throw new RuntimeException("资产不存在");
        }
        
        // 查询当前库存
        Integer currentStock = assetInfo.getCurrentQuantity();
        if (currentStock < quantity) {
            throw new RuntimeException("库存不足，当前库存：" + currentStock + "，出库数量：" + quantity);
        }
        
        // 创建出库记录
        StockRecord record = createStockRecord(
            assetInfo.getId(),
            assetInfo.getAssetCode(),
            assetInfo.getAssetName(),
            2, // 出库
            202, // 领用出库
            quantity,
            currentStock,
            currentStock - quantity,
            warehouse,
            null,
            operator,
            operatorId,
            labelCode,
            1, // 扫码枪
            unitPrice,
            unitPrice != null ? unitPrice.multiply(BigDecimal.valueOf(quantity)) : null,
            batchNo,
            partner,
            relatedOrderNo
        );
        
        // 更新资产库存
        updateAssetStock(assetInfo.getId(), currentStock - quantity);
        
        log.info("扫码出库成功，资产ID：{}，数量：{}，标签：{}", assetInfo.getId(), quantity, labelCode);
        return record;
    }
    
    @Override
    @Transactional(rollbackFor = Exception.class)
    public StockRecord manualIn(Long assetId, Integer quantity, String warehouse, String operator, String operatorId,
                               String batchNo, String partner, BigDecimal unitPrice, String relatedOrderNo) {
        AssetInfo assetInfo = assetInfoMapper.selectById(assetId);
        if (assetInfo == null) {
            throw new RuntimeException("资产不存在");
        }
        
        // 查询当前库存
        Integer currentStock = assetInfo.getCurrentQuantity();
        
        // 创建入库记录
        StockRecord record = createStockRecord(
            assetId,
            assetInfo.getAssetCode(),
            assetInfo.getAssetName(),
            1, // 入库
            101, // 采购入库
            quantity,
            currentStock,
            currentStock + quantity,
            warehouse,
            null,
            operator,
            operatorId,
            null,
            3, // 手动输入
            unitPrice,
            unitPrice != null ? unitPrice.multiply(BigDecimal.valueOf(quantity)) : null,
            batchNo,
            partner,
            relatedOrderNo
        );
        
        // 更新资产库存
        updateAssetStock(assetId, currentStock + quantity);
        
        log.info("手动入库成功，资产ID：{}，数量：{}", assetId, quantity);
        return record;
    }
    
    @Override
    @Transactional(rollbackFor = Exception.class)
    public StockRecord manualOut(Long assetId, Integer quantity, String warehouse, String operator, String operatorId,
                                String batchNo, String partner, BigDecimal unitPrice, String relatedOrderNo) {
        AssetInfo assetInfo = assetInfoMapper.selectById(assetId);
        if (assetInfo == null) {
            throw new RuntimeException("资产不存在");
        }
        
        // 查询当前库存
        Integer currentStock = assetInfo.getCurrentQuantity();
        if (currentStock < quantity) {
            throw new RuntimeException("库存不足，当前库存：" + currentStock + "，出库数量：" + quantity);
        }
        
        // 创建出库记录
        StockRecord record = createStockRecord(
            assetId,
            assetInfo.getAssetCode(),
            assetInfo.getAssetName(),
            2, // 出库
            202, // 领用出库
            quantity,
            currentStock,
            currentStock - quantity,
            warehouse,
            null,
            operator,
            operatorId,
            null,
            3, // 手动输入
            unitPrice,
            unitPrice != null ? unitPrice.multiply(BigDecimal.valueOf(quantity)) : null,
            batchNo,
            partner,
            relatedOrderNo
        );
        
        // 更新资产库存
        updateAssetStock(assetId, currentStock - quantity);
        
        log.info("手动出库成功，资产ID：{}，数量：{}", assetId, quantity);
        return record;
    }
    
    @Override
    @Transactional(rollbackFor = Exception.class)
    public StockRecord inventoryCheck(Long assetId, Integer actualQuantity, String warehouse, String operator, String operatorId) {
        AssetInfo assetInfo = assetInfoMapper.selectById(assetId);
        if (assetInfo == null) {
            throw new RuntimeException("资产不存在");
        }
        
        // 查询当前库存
        Integer currentStock = assetInfo.getCurrentQuantity();
        Integer difference = actualQuantity - currentStock;
        
        // 创建盘点记录
        StockRecord record = createStockRecord(
            assetId,
            assetInfo.getAssetCode(),
            assetInfo.getAssetName(),
            3, // 盘点
            0, // 无子类型
            difference,
            currentStock,
            actualQuantity,
            warehouse,
            null,
            operator,
            operatorId,
            null,
            3, // 手动输入
            null,
            null,
            null,
            null,
            null
        );
        
        // 更新资产库存
        updateAssetStock(assetId, actualQuantity);
        
        log.info("库存盘点完成，资产ID：{}，原库存：{}，实际库存：{}，差异：{}", 
                assetId, currentStock, actualQuantity, difference);
        return record;
    }
    
    @Override
    @Transactional(rollbackFor = Exception.class)
    public StockRecord stockTransfer(Long assetId, Integer quantity, String fromWarehouse, String toWarehouse,
                                    String operator, String operatorId, String reason) {
        AssetInfo assetInfo = assetInfoMapper.selectById(assetId);
        if (assetInfo == null) {
            throw new RuntimeException("资产不存在");
        }
        
        // 查询当前库存
        Integer currentStock = assetInfo.getCurrentQuantity();
        if (currentStock < quantity) {
            throw new RuntimeException("库存不足，当前库存：" + currentStock + "，调拨数量：" + quantity);
        }
        
        // 创建调拨记录
        StockRecord record = createStockRecord(
            assetId,
            assetInfo.getAssetCode(),
            assetInfo.getAssetName(),
            4, // 调拨
            0, // 无子类型
            quantity,
            currentStock,
            currentStock - quantity,
            fromWarehouse,
            toWarehouse,
            operator,
            operatorId,
            null,
            3, // 手动输入
            null,
            null,
            null,
            null,
            null
        );
        
        // 更新资产库存（调拨出库）
        updateAssetStock(assetId, currentStock - quantity);
        
        log.info("库存调拨成功，资产ID：{}，数量：{}，从：{} 到：{}", 
                assetId, quantity, fromWarehouse, toWarehouse);
        return record;
    }
    
    @Override
    public Page<StockRecord> pageRecords(Page<StockRecord> page, StockRecord query) {
        LambdaQueryWrapper<StockRecord> queryWrapper = new LambdaQueryWrapper<>();
        
        if (query != null) {
            if (StringUtils.hasText(query.getRecordNo())) {
                queryWrapper.like(StockRecord::getRecordNo, query.getRecordNo());
            }
            if (StringUtils.hasText(query.getAssetCode())) {
                queryWrapper.like(StockRecord::getAssetCode, query.getAssetCode());
            }
            if (StringUtils.hasText(query.getAssetName())) {
                queryWrapper.like(StockRecord::getAssetName, query.getAssetName());
            }
            if (query.getOperationType() != null) {
                queryWrapper.eq(StockRecord::getOperationType, query.getOperationType());
            }
            if (query.getSubType() != null) {
                queryWrapper.eq(StockRecord::getSubType, query.getSubType());
            }
            if (StringUtils.hasText(query.getWarehouse())) {
                queryWrapper.like(StockRecord::getWarehouse, query.getWarehouse());
            }
            if (StringUtils.hasText(query.getOperator())) {
                queryWrapper.like(StockRecord::getOperator, query.getOperator());
            }
            if (query.getStatus() != null) {
                queryWrapper.eq(StockRecord::getStatus, query.getStatus());
            }
            if (query.getApprovalStatus() != null) {
                queryWrapper.eq(StockRecord::getApprovalStatus, query.getApprovalStatus());
            }
            if (query.getOperationTime() != null) {
                queryWrapper.ge(StockRecord::getOperationTime, query.getOperationTime().withHour(0).withMinute(0).withSecond(0));
                queryWrapper.le(StockRecord::getOperationTime, query.getOperationTime().withHour(23).withMinute(59).withSecond(59));
            }
        }
        
        queryWrapper.orderByDesc(StockRecord::getOperationTime);
        return page(page, queryWrapper);
    }
    
    @Override
    public List<StockRecord> getRecordsByAssetId(Long assetId) {
        return stockRecordMapper.selectByAssetId(assetId);
    }
    
    @Override
    public Map<String, Object> getRealTimeStock(Long assetId) {
        AssetInfo assetInfo = assetInfoMapper.selectById(assetId);
        if (assetInfo == null) {
            throw new RuntimeException("资产不存在");
        }
        
        Map<String, Object> result = new HashMap<>();
        result.put("assetId", assetId);
        result.put("assetCode", assetInfo.getAssetCode());
        result.put("assetName", assetInfo.getAssetName());
        result.put("currentQuantity", assetInfo.getCurrentQuantity());
        result.put("warningQuantity", assetInfo.getWarningQuantity());
        result.put("unit", assetInfo.getUnit());
        result.put("location", assetInfo.getLocation());
        
        // 计算最近一次操作时间
        List<StockRecord> recentRecords = getRecordsByAssetId(assetId);
        if (!recentRecords.isEmpty()) {
            result.put("lastOperationTime", recentRecords.get(0).getOperationTime());
            result.put("lastOperationType", recentRecords.get(0).getOperationType());
        }
        
        return result;
    }
    
    @Override
    public List<Map<String, Object>> getAllRealTimeStock() {
        // 查询所有资产
        List<AssetInfo> allAssets = assetInfoMapper.selectList(null);
        
        return allAssets.stream()
            .map(asset -> {
                Map<String, Object> stockInfo = new HashMap<>();
                stockInfo.put("assetId", asset.getId());
                stockInfo.put("assetCode", asset.getAssetCode());
                stockInfo.put("assetName", asset.getAssetName());
                stockInfo.put("currentQuantity", asset.getCurrentQuantity());
                stockInfo.put("warningQuantity", asset.getWarningQuantity());
                stockInfo.put("unit", asset.getUnit());
                stockInfo.put("location", asset.getLocation());
                stockInfo.put("assetStatus", asset.getAssetStatus());
                return stockInfo;
            })
            .collect(Collectors.toList());
    }
    
    @Override
    public Map<String, Object> getStockStatistics(LocalDateTime startTime, LocalDateTime endTime) {
        Map<String, Object> result = new HashMap<>();
        
        // 统计出入库数量
        List<Map<String, Object>> operationStats = stockRecordMapper.groupByOperationType(startTime, endTime);
        result.put("operationStats", operationStats);
        
        // 统计总出入库数量
        int totalIn = 0;
        int totalOut = 0;
        BigDecimal totalInAmount = BigDecimal.ZERO;
        BigDecimal totalOutAmount = BigDecimal.ZERO;
        
        for (Map<String, Object> stat : operationStats) {
            Integer operationType = (Integer) stat.get("operation_type");
            Long count = (Long) stat.get("count");
            Long totalQuantity = (Long) stat.get("total_quantity");
            BigDecimal totalAmount = (BigDecimal) stat.get("total_amount");
            
            if (operationType == 1) { // 入库
                totalIn += totalQuantity != null ? totalQuantity.intValue() : 0;
                if (totalAmount != null) {
                    totalInAmount = totalInAmount.add(totalAmount);
                }
            } else if (operationType == 2) { // 出库
                totalOut += totalQuantity != null ? totalQuantity.intValue() : 0;
                if (totalAmount != null) {
                    totalOutAmount = totalOutAmount.add(totalAmount);
                }
            }
        }
        
        result.put("totalInQuantity", totalIn);
        result.put("totalOutQuantity", totalOut);
        result.put("totalInAmount", totalInAmount);
        result.put("totalOutAmount", totalOutAmount);
        
        // 查询库存余额
        List<Map<String, Object>> stockBalance = stockRecordMapper.selectStockBalance();
        result.put("stockBalance", stockBalance);
        
        return result;
    }
    
    @Override
    public List<Map<String, Object>> getStockLedger(LocalDateTime startTime, LocalDateTime endTime) {
        List<StockRecord> records = stockRecordMapper.selectByTimeRange(startTime, endTime);
        
        return records.stream()
            .map(record -> {
                Map<String, Object> ledger = new HashMap<>();
                ledger.put("recordNo", record.getRecordNo());
                ledger.put("operationTime", record.getOperationTime());
                ledger.put("assetCode", record.getAssetCode());
                ledger.put("assetName", record.getAssetName());
                ledger.put("operationType", record.getOperationType());
                ledger.put("quantity", record.getQuantity());
                ledger.put("beforeQuantity", record.getBeforeQuantity());
                ledger.put("afterQuantity", record.getAfterQuantity());
                ledger.put("warehouse", record.getWarehouse());
                ledger.put("operator", record.getOperator());
                ledger.put("totalAmount", record.getTotalAmount());
                ledger.put("batchNo", record.getBatchNo());
                ledger.put("partner", record.getPartner());
                return ledger;
            })
            .collect(Collectors.toList());
    }
    
    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean confirmRecord(Long recordId, String approver, String approvalComment) {
        StockRecord record = getById(recordId);
        if (record == null) {
            throw new RuntimeException("记录不存在");
        }
        
        record.setApprovalStatus(1);
        record.setApprovalComment(approvalComment);
        record.setUpdateBy(approver);
        record.setUpdateTime(LocalDateTime.now());
        
        return updateById(record);
    }
    
    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean cancelRecord(Long recordId, String reason) {
        StockRecord record = getById(recordId);
        if (record == null) {
            throw new RuntimeException("记录不存在");
        }
        
        // 如果是已确认的记录，需要还原库存
        if (record.getStatus() == 1) {
            AssetInfo assetInfo = assetInfoMapper.selectById(record.getAssetId());
            if (assetInfo != null) {
                if (record.getOperationType() == 1) { // 入库
                    updateAssetStock(record.getAssetId(), assetInfo.getCurrentQuantity() - record.getQuantity());
                } else if (record.getOperationType() == 2) { // 出库
                    updateAssetStock(record.getAssetId(), assetInfo.getCurrentQuantity() + record.getQuantity());
                }
            }
        }
        
        record.setStatus(2); // 已作废
        record.setRemark(reason);
        record.setUpdateTime(LocalDateTime.now());
        
        return updateById(record);
    }
    
    @Override
    public List<StockRecord> getRecordsByLabelCode(String labelCode) {
        return stockRecordMapper.selectByLabelCode(labelCode);
    }
    
    /**
     * 创建库存记录
     */
    private StockRecord createStockRecord(Long assetId, String assetCode, String assetName,
                                         Integer operationType, Integer subType, Integer quantity,
                                         Integer beforeQuantity, Integer afterQuantity,
                                         String warehouse, String targetWarehouse,
                                         String operator, String operatorId, String scannedLabelCode,
                                         Integer scanMethod, BigDecimal unitPrice, BigDecimal totalAmount,
                                         String batchNo, String partner, String relatedOrderNo) {
        StockRecord record = new StockRecord();
        record.setRecordNo(generateRecordNo(operationType));
        record.setAssetId(assetId);
        record.setAssetCode(assetCode);
        record.setAssetName(assetName);
        record.setOperationType(operationType);
        record.setSubType(subType);
        record.setQuantity(quantity);
        record.setBeforeQuantity(beforeQuantity);
        record.setAfterQuantity(afterQuantity);
        record.setWarehouse(warehouse);
        record.setTargetWarehouse(targetWarehouse);
        record.setOperator(operator);
        record.setOperatorId(operatorId);
        record.setScannedLabelCode(scannedLabelCode);
        record.setScanMethod(scanMethod);
        record.setUnitPrice(unitPrice);
        record.setTotalAmount(totalAmount);
        record.setBatchNo(batchNo);
        record.setPartner(partner);
        record.setRelatedOrderNo(relatedOrderNo);
        record.setOperationTime(LocalDateTime.now());
        record.setApprovalStatus(1); // 自动通过
        record.setStatus(1); // 已确认
        record.setCreateBy(operator);
        record.setCreateTime(LocalDateTime.now());
        
        save(record);
        return record;
    }
    
    /**
     * 生成流水号
     */
    private String generateRecordNo(Integer operationType) {
        String prefix;
        switch (operationType) {
            case 1: prefix = "IN"; break;  // 入库
            case 2: prefix = "OUT"; break; // 出库
            case 3: prefix = "CHK"; break; // 盘点
            case 4: prefix = "TRF"; break; // 调拨
            default: prefix = "STK"; break;
        }
        
        String datePart = LocalDateTime.now().format(java.time.format.DateTimeFormatter.ofPattern("yyyyMMddHHmmss"));
        String randomPart = UUID.randomUUID().toString().substring(0, 4).toUpperCase();
        return prefix + "-" + datePart + "-" + randomPart;
    }
    
    /**
     * 更新资产库存
     */
    private void updateAssetStock(Long assetId, Integer newQuantity) {
        AssetInfo assetInfo = new AssetInfo();
        assetInfo.setId(assetId);
        assetInfo.setCurrentQuantity(newQuantity);
        assetInfo.setUpdateTime(LocalDateTime.now());
        assetInfoMapper.updateById(assetInfo);
    }
}