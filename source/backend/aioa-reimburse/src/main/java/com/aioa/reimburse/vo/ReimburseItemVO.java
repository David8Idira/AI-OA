package com.aioa.reimburse.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Reimburse Item VO
 */
@Data
@Schema(name = "ReimburseItemVO", description = "Reimbursement item view object")
public class ReimburseItemVO {

    @Schema(description = "Item ID")
    private String id;

    @Schema(description = "Reimburse ID")
    private String reimburseId;

    @Schema(description = "Item sequence number")
    private Integer itemNo;

    @Schema(description = "Expense type")
    private String expenseType;

    @Schema(description = "Expense type name")
    private String expenseTypeName;

    @Schema(description = "Description")
    private String description;

    @Schema(description = "Expense date")
    private LocalDate expenseDate;

    @Schema(description = "Quantity")
    private BigDecimal quantity;

    @Schema(description = "Unit price")
    private BigDecimal unitPrice;

    @Schema(description = "Total amount")
    private BigDecimal amount;

    @Schema(description = "Currency code")
    private String currency;

    @Schema(description = "Tax included")
    private Integer taxIncluded;

    @Schema(description = "Tax rate")
    private BigDecimal taxRate;

    @Schema(description = "Tax amount")
    private BigDecimal taxAmount;

    @Schema(description = "Invoice ID")
    private String invoiceId;

    @Schema(description = "Invoice number")
    private String invoiceNo;

    @Schema(description = "Origin place")
    private String originPlace;

    @Schema(description = "Destination")
    private String destination;

    @Schema(description = "Receipt attached")
    private Integer receiptAttached;

    @Schema(description = "Attachments")
    private String attachments;

    @Schema(description = "Remark")
    private String remark;

    @Schema(description = "Create time")
    private LocalDateTime createTime;
}
