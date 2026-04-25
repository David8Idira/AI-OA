package com.aioa.asset.mapper;

import com.aioa.asset.entity.AssetOperation;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

/**
 * 资产操作记录Mapper接口
 */
@Mapper
public interface AssetOperationMapper extends BaseMapper<AssetOperation> {
    
}