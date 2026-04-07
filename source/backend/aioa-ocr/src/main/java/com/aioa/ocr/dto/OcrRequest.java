package com.aioa.ocr.dto;

import com.aioa.ocr.enums.InvoiceType;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.validator.constraints.URL;

import java.io.Serial;
import java.io.Serializable;

/**
 * OCR Recognition Request DTO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(name = "OCR识别请求")
public class OcrRequest implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * Invoice type - required
     */
    @NotNull(message = "发票类型不能为空")
    @Schema(description = "发票类型", example = "vat_invoice")
    private InvoiceType invoiceType;

    /**
     * Image URL or Base64 encoded image data
     */
    @NotBlank(message = "图片不能为空")
    @Schema(description = "图片URL或Base64编码的图片数据")
    private String imageData;

    /**
     * Image type: url or base64
     */
    @Schema(description = "图片类型: url或base64", example = "url")
    private String imageType = "url";

    /**
     * File name (optional, for record keeping)
     */
    @Schema(description = "文件名", example = "invoice.jpg")
    private String fileName;

    /**
     * User ID (optional, will be set from context if not provided)
     */
    @Schema(description = "用户ID")
    private String userId;

    /**
     * Whether to save the recognition result to database
     */
    @Schema(description = "是否保存识别结果", example = "true")
    private Boolean saveResult = true;

    /**
     * Additional options as JSON string
     */
    @Schema(description = "额外选项(JSON格式)")
    private String options;

    /**
     * Check if image data is URL
     */
    public boolean isUrlImage() {
        return "url".equalsIgnoreCase(imageType) || imageData.startsWith("http");
    }

    /**
     * Check if image data is Base64
     */
    public boolean isBase64Image() {
        return "base64".equalsIgnoreCase(imageType) || imageData.startsWith("data:image");
    }
}
