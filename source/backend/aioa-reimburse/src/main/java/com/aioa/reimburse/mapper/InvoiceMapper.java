package com.aioa.reimburse.mapper;

import com.aioa.reimburse.entity.Invoice;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * Invoice Mapper
 */
@Mapper
public interface InvoiceMapper extends BaseMapper<Invoice> {

    /**
     * Find invoices by reimburse ID
     */
    List<Invoice> selectByReimburseId(@Param("reimburseId") String reimburseId);

    /**
     * Find invoices by reimburse item ID
     */
    List<Invoice> selectByReimburseItemId(@Param("reimburseItemId") String reimburseItemId);

    /**
     * Find invoice by OCR record ID
     */
    Invoice selectByOcrRecordId(@Param("ocrRecordId") String ocrRecordId);

    /**
     * Count by reimburse ID
     */
    Long countByReimburseId(@Param("reimburseId") String reimburseId);

    /**
     * Batch insert invoices
     */
    int batchInsert(@Param("list") List<Invoice> invoices);

    /**
     * Update verify status
     */
    int updateVerifyStatus(@Param("id") String id, @Param("verified") Integer verified,
                           @Param("verifiedBy") String verifiedBy, @Param("verifyRemark") String verifyRemark);
}
