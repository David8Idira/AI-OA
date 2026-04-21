package com.aioa.reimburse.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Invoice VO
 */
@Data
@Schema(name = "InvoiceVO", description = "Invoice view object")
public class InvoiceVO {

    @Schema(description = "Invoice ID")
    private String id;

    @Schema(description = "Reimburse ID")
    private String reimburseId;

    @Schema(description = "Reimburse item ID")
    private String reimburseItemId;

    @Schema(description = "OCR record ID")
    private String ocrRecordId;

    @Schema(description = "Invoice type")
    private String invoiceType;

    @Schema(description = "Invoice type name")
    private String invoiceTypeName;

    @Schema(description = "Invoice number")
    private String invoiceNo;

    @Schema(description = "Invoice code")
    private String invoiceCode;

    @Schema(description = "Invoice date")
    private LocalDate invoiceDate;

    @Schema(description = "Total amount")
    private BigDecimal totalAmount;

    @Schema(description = "Pre-tax amount")
    private BigDecimal pretaxAmount;

    @Schema(description = "Tax amount")
    private BigDecimal taxAmount;

    @Schema(description = "Tax rate")
    private BigDecimal taxRate;

    @Schema(description = "Currency code")
    private String currency;

    @Schema(description = "Seller name")
    private String sellerName;

    @Schema(description = "Seller taxpayer ID")
    private String sellerTaxId;

    @Schema(description = "Buyer name")
    private String buyerName;

    @Schema(description = "OCR confidence")
    private Double ocrConfidence;

    @Schema(description = "Source: OCR, MANUAL")
    private String source;

    @Schema(description = "File URL")
    private String fileUrl;

    @Schema(description = "Original file name")
    private String fileName;

    @Schema(description = "Verified: 0-No, 1-Yes")
    private Integer verified;

    @Schema(description = "Verified by")
    private String verifiedBy;

    @Schema(description = "Verification time")
    private LocalDateTime verifiedTime;

    @Schema(description = "Status")
    private String status;

    @Schema(description = "Create time")
    private LocalDateTime createTime;
}
