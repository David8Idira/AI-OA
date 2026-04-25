package com.aioa.asset.mapper;

import com.aioa.asset.entity.AssetInventory;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

/**
 * 资产盘点Mapper接口
 */
@Mapper
public interface AssetInventoryMapper extends BaseMapper<AssetInventory> {
    
}