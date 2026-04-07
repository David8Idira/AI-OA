package com.aioa.reimburse.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

/**
 * OCR Auto-Fill Preview VO
 * Shows the preview of auto-filled reimbursement data from OCR
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(name = "OcrAutoFillVO", description = "OCR auto-fill preview result")
public class OcrAutoFillVO {

    @Schema(description = "OCR record ID")
    private String ocrRecordId;

    @Schema(description = "Suggested reimbursement title")
    private String suggestedTitle;

    @Schema(description = "Suggested reimbursement type")
    private String suggestedType;

    @Schema(description = "Suggested expense type for the item")
    private String suggestedExpenseType;

    @Schema(description = "Suggested expense date")
    private String suggestedExpenseDate;

    @Schema(description = "Suggested amount")
    private BigDecimal suggestedAmount;

    @Schema(description = "Suggested description")
    private String suggestedDescription;

    @Schema(description = "Invoice number from OCR")
    private String invoiceNo;

    @Schema(description = "Invoice date from OCR")
    private String invoiceDate;

    @Schema(description = "Seller name from OCR")
    private String sellerName;

    @Schema(description = "Total amount from OCR")
    private BigDecimal ocrTotalAmount;

    @Schema(description = "OCR confidence score")
    private Double ocrConfidence;

    @Schema(description = "Invoice type from OCR")
    private String invoiceType;

    @Schema(description = "Raw OCR data")
    private Object rawData;

    @Schema(description = "Available expense type options derived from invoice type")
    private List<String> suggestedExpenseTypes;

    @Schema(description = "Remarks or warnings from OCR processing")
    private String remark;

    @Schema(description = "Whether data is high confidence and reliable")
    private Boolean reliable;
}
