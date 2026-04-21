package com.aioa.ocr.dto;

import com.aioa.ocr.enums.InvoiceType;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serial;
import java.io.Serializable;
import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

/**
 * OCR Recognition Response DTO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(name = "OCR识别响应")
public class OcrResponse implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * Whether recognition was successful
     */
    @Schema(description = "是否成功")
    private Boolean success;

    /**
     * Recognition ID for tracking
     */
    @Schema(description = "识别记录ID")
    private String recognitionId;

    /**
     * Invoice type
     */
    @Schema(description = "发票类型")
    private InvoiceType invoiceType;

    /**
     * Invoice number
     */
    @Schema(description = "发票号码")
    private String invoiceNo;

    /**
     * Invoice code (for VAT invoice)
     */
    @Schema(description = "发票代码")
    private String invoiceCode;

    /**
     * Invoice date
     */
    @Schema(description = "开票日期")
    private String invoiceDate;

    /**
     * Total amount (含税金额)
     */
    @Schema(description = "价税合计金额")
    private BigDecimal totalAmount;

    /**
     * Pre-tax amount (不含税金额)
     */
    @Schema(description = "不含税金额")
    private BigDecimal pretaxAmount;

    /**
     * Tax amount (税额)
     */
    @Schema(description = "税额")
    private BigDecimal taxAmount;

    /**
     * Seller name (销售方名称)
     */
    @Schema(description = "销售方名称")
    private String sellerName;

    /**
     * Seller taxpayer ID (销售方纳税人识别号)
     */
    @Schema(description = "销售方纳税人识别号")
    private String sellerTaxId;

    /**
     * Buyer name (购买方名称)
     */
    @Schema(description = "购买方名称")
    private String buyerName;

    /**
     * Buyer taxpayer ID (购买方纳税人识别号)
     */
    @Schema(description = "购买方纳税人识别号")
    private String buyerTaxId;

    /**
     * Recognition confidence score (0.0 - 1.0)
     */
    @Schema(description = "置信度", example = "0.95")
    private Double confidence;

    /**
     * Whether the result is high confidence (>= 0.8)
     */
    @Schema(description = "是否高置信度")
    private Boolean highConfidence;

    /**
     * Recognition processing time in milliseconds
     */
    @Schema(description = "处理时间(毫秒)")
    private Long processTime;

    /**
     * Recognized text fields with confidence
     */
    @Schema(description = "识别字段详情")
    private Map<String, FieldConfidence> fields;

    /**
     * Raw OCR result from provider
     */
    @Schema(description = "原始识别结果")
    private Map<String, Object> rawResult;

    /**
     * Error message if recognition failed
     */
    @Schema(description = "错误信息")
    private String errorMessage;

    /**
     * Invoice items (for VAT invoice)
     */
    @Schema(description = "发票明细")
    private List<InvoiceItem> items;

    /**
     * Transportation details (for train/air tickets)
     */
    @Schema(description = "行程信息")
    private TransportationInfo transportationInfo;

    /**
     * Check if result is reliable
     */
    public boolean isReliable() {
        return Boolean.TRUE.equals(highConfidence) && Boolean.TRUE.equals(success);
    }

    /**
     * Field confidence detail
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class FieldConfidence implements Serializable {
        @Serial
        private static final long serialVersionUID = 1L;

        private String fieldName;
        private String fieldValue;
        private Double confidence;
        private Boolean recognized;
    }

    /**
     * Invoice item (明细行)
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class InvoiceItem implements Serializable {
        @Serial
        private static final long serialVersionUID = 1L;

        private String name;
        private String spec;
        private String unit;
        private BigDecimal quantity;
        private BigDecimal unitPrice;
        private BigDecimal amount;
        private BigDecimal taxRate;
        private BigDecimal taxAmount;
    }

    /**
     * Transportation info (行程信息)
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class TransportationInfo implements Serializable {
        @Serial
        private static final long serialVersionUID = 1L;

        private String departureStation;
        private String arrivalStation;
        private String departureTime;
        private String flightNo;
        private String seatClass;
        private String carrier;
    }
}
