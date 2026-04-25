package com.aioa.asset.mapper;

import com.aioa.asset.entity.AssetLabel;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;
import java.util.Map;

/**
 * 资产标签Mapper接口
 */
@Mapper
public interface AssetLabelMapper extends BaseMapper<AssetLabel> {
    
    /**
     * 根据资产ID查询标签
     */
    @Select("SELECT * FROM asset_label WHERE asset_id = #{assetId} AND label_status = 1 ORDER BY create_time DESC")
    List<AssetLabel> selectByAssetId(@Param("assetId") Long assetId);
    
    /**
     * 根据标签编码查询标签
     */
    @Select("SELECT * FROM asset_label WHERE label_code = #{labelCode} AND label_status = 1")
    AssetLabel selectByLabelCode(@Param("labelCode") String labelCode);
    
    /**
     * 查询打印历史
     */
    @Select("SELECT * FROM asset_label WHERE print_status = 1 ORDER BY last_print_time DESC LIMIT #{limit}")
    List<AssetLabel> selectPrintHistory(@Param("limit") Integer limit);
    
    /**
     * 统计标签打印数量
     */
    @Select("SELECT " +
            "SUM(CASE WHEN print_status = 1 THEN 1 ELSE 0 END) as printed_count, " +
            "SUM(CASE WHEN print_status = 0 THEN 1 ELSE 0 END) as unprinted_count, " +
            "SUM(CASE WHEN print_status = 2 THEN 1 ELSE 0 END) as failed_count " +
            "FROM asset_label WHERE label_status = 1")
    Map<String, Object> countPrintStatus();
}