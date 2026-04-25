package com.aioa.asset.mapper;

import com.aioa.asset.entity.AssetInfo;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;
import java.util.Map;

/**
 * 资产信息Mapper接口
 */
@Mapper
public interface AssetInfoMapper extends BaseMapper<AssetInfo> {
    
    /**
     * 查询资产预警列表
     */
    @Select("SELECT * FROM asset_info WHERE current_quantity <= warning_quantity AND status = 1 AND asset_status = 1")
    List<AssetInfo> selectWarningAssets();
    
    /**
     * 统计各类资产数量
     */
    @Select("SELECT category_id, COUNT(*) as count, SUM(current_quantity) as total_quantity FROM asset_info WHERE status = 1 GROUP BY category_id")
    List<Map<String, Object>> groupByCategory();
}