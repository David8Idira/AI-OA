package com.aioa.ocr.entity;

import com.aioa.common.entity.BaseEntity;
import com.aioa.ocr.enums.InvoiceType;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.annotation.TableLogic;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * Invoice Record Entity
 * Stores OCR recognition results for invoices
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("ocr_invoice_record")
public class InvoiceRecord extends BaseEntity {

    /**
     * User ID who submitted the OCR request
     */
    private String userId;

    /**
     * Invoice type code
     */
    private String invoiceType;

    /**
     * Original file name
     */
    private String fileName;

    /**
     * File URL or path
     */
    private String fileUrl;

    /**
     * OCR confidence score (0.0 - 1.0)
     */
    private Double confidence;

    /**
     * Recognition status: pending, processing, success, failed
     */
    private String status;

    /**
     * Recognized invoice number
     */
    private String invoiceNo;

    /**
     * Invoice date
     */
    private String invoiceDate;

    /**
     * Total amount
     */
    private java.math.BigDecimal totalAmount;

    /**
     * Tax amount
     */
    private java.math.BigDecimal taxAmount;

    /**
     * Invoice code (for VAT invoice)
     */
    private String invoiceCode;

    /**
     * Seller name
     */
    private String sellerName;

    /**
     * Seller taxpayer ID
     */
    private String sellerTaxId;

    /**
     * Buyer name
     */
    private String buyerName;

    /**
     * Buyer taxpayer ID
     */
    private String buyerTaxId;

    /**
     * Raw OCR result JSON
     */
    private String rawResult;

    /**
     * Error message if recognition failed
     */
    private String errorMessage;

    /**
     * Processing time in milliseconds
     */
    private Long processTime;

    /**
     * Get InvoiceType enum
     */
    public InvoiceType getInvoiceTypeEnum() {
        return InvoiceType.getByCode(this.invoiceType);
    }

    /**
     * Set InvoiceType enum
     */
    public void setInvoiceTypeEnum(InvoiceType type) {
        if (type != null) {
            this.invoiceType = type.getCode();
        }
    }

    /**
     * Check if recognition was successful
     */
    public boolean isSuccess() {
        return "success".equals(this.status);
    }

    /**
     * Check if confidence meets threshold (>= 0.8)
     */
    public boolean isHighConfidence() {
        return this.confidence != null && this.confidence >= 0.8;
    }
}
