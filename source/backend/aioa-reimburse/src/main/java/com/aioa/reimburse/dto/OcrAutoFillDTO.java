package com.aioa.reimburse.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.util.List;

/**
 * OCR Auto-Fill Request DTO
 * Used when user wants to auto-fill reimbursement form from OCR-recognized invoice data
 */
@Data
@Schema(name = "OcrAutoFillDTO", description = "OCR auto-fill request DTO")
public class OcrAutoFillDTO {

    @Schema(description = "OCR recognition record ID(s)")
    @NotBlank(message = "OCR记录ID不能为空")
    private String ocrRecordId;

    @Schema(description = "Reimbursement title (auto-generated if not provided)")
    private String title;

    @Schema(description = "Reimbursement type")
    private String type;

    @Schema(description = "OCR recognized invoice data (if passed directly instead of by ID)")
    private Object ocrData;

    @Schema(description = "Whether to create draft reimburse (false = just preview)")
    private Boolean createDraft = true;

    @Schema(description = "Expense type to use for auto-created item")
    private String expenseType;

    @Schema(description = "Additional description prefix")
    private String descriptionPrefix;
}
