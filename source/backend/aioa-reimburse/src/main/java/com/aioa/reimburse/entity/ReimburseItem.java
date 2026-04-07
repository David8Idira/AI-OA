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
 * ReimburseItem Entity - Reimbursement line items (expense details)
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("reimburse_item")
public class ReimburseItem extends BaseEntity {

    private static final long serialVersionUID = 1L;

    /**
     * Reimburse ID (FK to reimburse.id)
     */
    private String reimburseId;

    /**
     * Item sequence number within the reimbursement
     */
    private Integer itemNo;

    /**
     * Expense type: TRANSPORT, ACCOMMODATION, MEAL, COMMUNICATION, ENTERTAINMENT, MATERIAL, OTHER
     */
    private String expenseType;

    /**
     * Expense type name (denormalized for display)
     */
    private String expenseTypeName;

    /**
     * Item description
     */
    private String description;

    /**
     * Expense date
     */
    private LocalDate expenseDate;

    /**
     * Quantity
     */
    private BigDecimal quantity;

    /**
     * Unit price
     */
    private BigDecimal unitPrice;

    /**
     * Total amount for this item
     */
    private BigDecimal amount;

    /**
     * Currency code: CNY, USD, EUR
     */
    private String currency;

    /**
     * Whether tax included: 0-No, 1-Yes
     */
    private Integer taxIncluded;

    /**
     * Tax rate (percentage, e.g., 6 for 6%)
     */
    private BigDecimal taxRate;

    /**
     * Tax amount
     */
    private BigDecimal taxAmount;

    /**
     * Invoice ID (FK to invoice.id, if OCR-generated)
     */
    private String invoiceId;

    /**
     * Invoice number (denormalized)
     */
    private String invoiceNo;

    /**
     * Origin place (for transport: departure)
     */
    private String originPlace;

    /**
     * Destination (for transport: arrival)
     */
    private String destination;

    /**
     * Whether receipt attached: 0-No, 1-Yes
     */
    private Integer receiptAttached;

    /**
     * Attachment URLs (comma-separated or JSON)
     */
    private String attachments;

    /**
     * Remark for this item
     */
    private String remark;
}
