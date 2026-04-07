package com.aioa.reimburse.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

/**
 * Reimburse Item DTO - Individual expense line item
 */
@Data
@Schema(name = "ReimburseItemDTO", description = "Reimbursement item DTO")
public class ReimburseItemDTO {

    @Schema(description = "Item sequence number")
    private Integer itemNo;

    @Schema(description = "Expense type: TRANSPORT, ACCOMMODATION, MEAL, COMMUNICATION, ENTERTAINMENT, MATERIAL, OTHER")
    @NotBlank(message = "费用类型不能为空")
    @Size(max = 50, message = "费用类型长度不能超过50")
    private String expenseType;

    @Schema(description = "Item description")
    @NotBlank(message = "费用描述不能为空")
    @Size(max = 500, message = "描述长度不能超过500")
    private String description;

    @Schema(description = "Expense date")
    @NotNull(message = "费用日期不能为空")
    private LocalDate expenseDate;

    @Schema(description = "Quantity")
    @NotNull(message = "数量不能为空")
    @DecimalMin(value = "0.01", message = "数量必须大于0")
    private BigDecimal quantity = BigDecimal.ONE;

    @Schema(description = "Unit price")
    @NotNull(message = "单价不能为空")
    @DecimalMin(value = "0.01", message = "单价必须大于0")
    private BigDecimal unitPrice;

    @Schema(description = "Total amount (auto-calculated if not provided)")
    private BigDecimal amount;

    @Schema(description = "Currency code: CNY, USD, EUR")
    private String currency = "CNY";

    @Schema(description = "Tax included: 0-No, 1-Yes")
    private Integer taxIncluded = 0;

    @Schema(description = "Tax rate (percentage, e.g., 6 for 6%)")
    private BigDecimal taxRate;

    @Schema(description = "Tax amount")
    private BigDecimal taxAmount;

    @Schema(description = "OCR record ID (if auto-filled from OCR)")
    private String ocrRecordId;

    @Schema(description = "Invoice number")
    private String invoiceNo;

    @Schema(description = "Origin place (for transport)")
    private String originPlace;

    @Schema(description = "Destination (for transport)")
    private String destination;

    @Schema(description = "Whether receipt attached: 0-No, 1-Yes")
    private Integer receiptAttached = 0;

    @Schema(description = "Attachment URLs (comma-separated)")
    private String attachments;

    @Schema(description = "Remark for this item")
    private String remark;
}
