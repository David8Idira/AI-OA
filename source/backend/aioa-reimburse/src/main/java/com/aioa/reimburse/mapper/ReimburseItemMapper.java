package com.aioa.reimburse.mapper;

import com.aioa.reimburse.entity.ReimburseItem;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * ReimburseItem Mapper
 */
@Mapper
public interface ReimburseItemMapper extends BaseMapper<ReimburseItem> {

    /**
     * Find items by reimburse ID
     */
    List<ReimburseItem> selectByReimburseId(@Param("reimburseId") String reimburseId);

    /**
     * Find item by invoice ID
     */
    ReimburseItem selectByInvoiceId(@Param("invoiceId") String invoiceId);

    /**
     * Delete items by reimburse ID (batch delete)
     */
    int deleteByReimburseId(@Param("reimburseId") String reimburseId);

    /**
     * Count items by reimburse ID
     */
    Long countByReimburseId(@Param("reimburseId") String reimburseId);
}
