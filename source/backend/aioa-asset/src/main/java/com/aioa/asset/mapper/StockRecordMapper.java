package com.aioa.asset.mapper;

import com.aioa.asset.entity.StockRecord;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * 库存流水Mapper接口
 */
@Mapper
public interface StockRecordMapper extends BaseMapper<StockRecord> {
    
    /**
     * 根据资产ID查询库存流水
     */
    @Select("SELECT * FROM stock_record WHERE asset_id = #{assetId} AND status = 1 ORDER BY operation_time DESC")
    List<StockRecord> selectByAssetId(@Param("assetId") Long assetId);
    
    /**
     * 根据流水号查询库存流水
     */
    @Select("SELECT * FROM stock_record WHERE record_no = #{recordNo} AND status = 1")
    StockRecord selectByRecordNo(@Param("recordNo") String recordNo);
    
    /**
     * 查询时间段内的库存流水
     */
    @Select("SELECT * FROM stock_record WHERE operation_time BETWEEN #{startTime} AND #{endTime} AND status = 1 ORDER BY operation_time DESC")
    List<StockRecord> selectByTimeRange(@Param("startTime") LocalDateTime startTime, @Param("endTime") LocalDateTime endTime);
    
    /**
     * 查询出入库统计
     */
    @Select("SELECT " +
            "operation_type, " +
            "COUNT(*) as count, " +
            "SUM(quantity) as total_quantity, " +
            "SUM(total_amount) as total_amount " +
            "FROM stock_record " +
            "WHERE status = 1 AND operation_time BETWEEN #{startTime} AND #{endTime} " +
            "GROUP BY operation_type")
    List<Map<String, Object>> groupByOperationType(@Param("startTime") LocalDateTime startTime, @Param("endTime") LocalDateTime endTime);
    
    /**
     * 查询库存余额
     */
    @Select("SELECT " +
            "asset_id, " +
            "asset_code, " +
            "asset_name, " +
            "SUM(CASE WHEN operation_type = 1 THEN quantity ELSE 0 END) - " +
            "SUM(CASE WHEN operation_type = 2 THEN quantity ELSE 0 END) as stock_balance " +
            "FROM stock_record " +
            "WHERE status = 1 " +
            "GROUP BY asset_id, asset_code, asset_name " +
            "HAVING stock_balance > 0")
    List<Map<String, Object>> selectStockBalance();
    
    /**
     * 根据扫描标签查询相关流水
     */
    @Select("SELECT * FROM stock_record WHERE scanned_label_code = #{labelCode} AND status = 1 ORDER BY operation_time DESC")
    List<StockRecord> selectByLabelCode(@Param("labelCode") String labelCode);
}