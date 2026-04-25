package com.aioa.license.mapper;

import com.aioa.license.entity.LicenseCategory;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

/**
 * 证照分类Mapper接口
 */
@Mapper
public interface LicenseCategoryMapper extends BaseMapper<LicenseCategory> {
}