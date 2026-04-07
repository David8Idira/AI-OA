package com.aioa.reimburse.entity;

import com.aioa.common.entity.BaseEntity;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.annotation.TableLogic;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Invoice Entity - Invoice records linked to reimbursement items
 * Stores OCR-recognized or manually entered invoice data
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("reimburse_invoice")
public class Invoice extends BaseEntity {

    private static final long serialVersionUID = 1L;

    /**
     * Reimburse ID (FK to reimburse.id, nullable for unused invoices)
     */
    private String reimburseId;

    /**
     * Reimburse item ID (FK to reimburse_item.id)
     */
    private String reimburseItemId;

    /**
     * OCR record ID (FK to ocr_invoice_record.id, if OCR-generated)
     */
    private String ocrRecordId;

    /**
     * Invoice type: VAT_INVOICE, TAXI_RECEIPT, TRAIN_TICKET, AIR_TICKET, HANDWRITTEN, OTHER
     */
    private String invoiceType;

    /**
     * Invoice type name (denormalized)
     */
    private String invoiceTypeName;

    /**
     * Invoice number
     */
    private String invoiceNo;

    /**
     * Invoice code (for VAT invoice)
     */
    private String invoiceCode;

    /**
     * Invoice date
     */
    private LocalDate invoiceDate;

    /**
     * Invoice issue date (display format)
     */
    private String invoiceDateStr;

    /**
     * Total amount (含税金额)
     */
    private BigDecimal totalAmount;

    /**
     * Pre-tax amount (不含税金额)
     */
    private BigDecimal pretaxAmount;

    /**
     * Tax amount (税额)
     */
    private BigDecimal taxAmount;

    /**
     * Tax rate (percentage)
     */
    private BigDecimal taxRate;

    /**
     * Currency code: CNY, USD, EUR
     */
    private String currency;

    /**
     * Seller name (销售方名称)
     */
    private String sellerName;

    /**
     * Seller taxpayer ID (销售方纳税人识别号)
     */
    private String sellerTaxId;

    /**
     * Buyer name (购买方名称)
     */
    private String buyerName;

    /**
     * Buyer taxpayer ID (购买方纳税人识别号)
     */
    private String buyerTaxId;

    /**
     * OCR confidence score (0.0 - 1.0)
     */
    private Double ocrConfidence;

    /**
     * Source: OCR, MANUAL
     */
    private String source;

    /**
     * File URL of the invoice image
     */
    private String fileUrl;

    /**
     * Original file name
     */
    private String fileName;

    /**
     * Whether verified: 0-No, 1-Yes
     */
    private Integer verified;

    /**
     * Verification remark
     */
    private String verifyRemark;

    /**
     * Verified by user ID
     */
    private String verifiedBy;

    /**
     * Verification time
     */
    private LocalDateTime verifiedTime;

    /**
     * Status: active, invalidated
     */
    private String status;

    /**
     * Invalidation reason
     */
    private String invalidateReason;

    /**
     * Raw OCR result JSON (stored for audit)
     */
    private String rawOcrResult;

    /**
     * Remark
     */
    private String remark;

    /**
     * Check if this is a VAT invoice
     */
    public boolean isVatInvoice() {
        return "VAT_INVOICE".equals(invoiceType);
    }

    /**
     * Check if OCR-generated
     */
    public boolean isFromOcr() {
        return "OCR".equals(source);
    }
}
