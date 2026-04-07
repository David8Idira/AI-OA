package com.aioa.reimburse.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * Create Reimburse Request DTO
 */
@Data
@Schema(name = "CreateReimburseDTO", description = "Create reimbursement request DTO")
public class CreateReimburseDTO {

    @Schema(description = "Reimbursement title")
    @NotBlank(message = "标题不能为空")
    @Size(max = 200, message = "标题长度不能超过200")
    private String title;

    @Schema(description = "Reimbursement type")
    @NotBlank(message = "报销类型不能为空")
    @Size(max = 50, message = "报销类型长度不能超过50")
    private String type;

    @Schema(description = "Currency code: CNY, USD, EUR")
    @NotBlank(message = "货币不能为空")
    @Size(max = 10, message = "货币代码长度不能超过10")
    private String currency = "CNY";

    @Schema(description = "Priority: 0-Low, 1-Normal, 2-High, 3-Urgent")
    @NotNull(message = "优先级不能为空")
    private Integer priority;

    @Schema(description = "Approver user ID")
    @NotBlank(message = "审批人不能为空")
    private String approverId;

    @Schema(description = "Reimbursement date (month being reimbursed)")
    @NotNull(message = "报销日期不能为空")
    private LocalDateTime reimburseDate;

    @Schema(description = "Expected payment date")
    private LocalDateTime expectPayDate;

    @Schema(description = "Payment method: BANK_TRANSFER, ALIPAY, WECHAT, CASH")
    private String payMethod;

    @Schema(description = "Bank account number (encrypted)")
    private String bankAccount;

    @Schema(description = "Bank name")
    private String bankName;

    @Schema(description = "CC user IDs (comma-separated)")
    private String ccUsers;

    @Schema(description = "Description/reason")
    @Size(max = 2000, message = "描述长度不能超过2000")
    private String description;

    @Schema(description = "Attachment URLs (comma-separated)")
    private String attachments;

    @Schema(description = "OCR auto-fill flag: 0-manual, 1-auto-filled")
    private Integer ocrAutoFill = 0;

    @Schema(description = "Reimbursement items")
    @NotNull(message = "报销明细不能为空")
    @Size(min = 1, message = "至少需要一个报销明细")
    @Valid
    private List<ReimburseItemDTO> items;
}
