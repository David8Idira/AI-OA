package com.aioa.ocr.config;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import java.util.HashMap;
import java.util.Map;

/**
 * Aliyun OCR Configuration
 */
@Data
@Configuration
@ConfigurationProperties(prefix = "aioa.ocr.aliyun")
@Schema(name = "阿里云OCR配置")
public class AliyunOcrConfig {

    /**
     * Whether to enable Aliyun OCR (if false, use mock implementation)
     */
    @Schema(description = "是否启用阿里云OCR")
    private Boolean enabled = true;

    /**
     * Aliyun Access Key ID
     */
    @Schema(description = "阿里云AccessKey ID")
    private String accessKeyId;

    /**
     * Aliyun Access Key Secret
     */
    @Schema(description = "阿里云AccessKey Secret")
    private String accessKeySecret;

    /**
     * Aliyun OCR endpoint
     */
    @Schema(description = "阿里云OCR服务地址")
    private String endpoint = "ocr-api.cn-hangzhou.aliyuncs.com";

    /**
     * Region ID
     */
    @Schema(description = "区域ID")
    private String regionId = "cn-hangzhou";

    /**
     * Request timeout in milliseconds
     */
    @Schema(description = "请求超时时间(毫秒)")
    private Integer timeout = 30000;

    /**
     * Retry count on failure
     */
    @Schema(description = "失败重试次数")
    private Integer retryCount = 3;

    /**
     * Confidence threshold for high confidence flag
     */
    @Schema(description = "高置信度阈值")
    private Double confidenceThreshold = 0.8;

    /**
     * Whether to enable caching of recognition results
     */
    @Schema(description = "是否启用识别结果缓存")
    private Boolean enableCache = true;

    /**
     * Cache TTL in seconds
     */
    @Schema(description = "缓存有效期(秒)")
    private Long cacheTtl = 3600L;

    /**
     * API mapping for different invoice types
     */
    @Schema(description = "发票类型API映射")
    private Map<String, String> invoiceApiMapping = new HashMap<>();

    public AliyunOcrConfig() {
        // Default API mappings
        invoiceApiMapping.put("vat_invoice", "RecognizeVatInvoice");
        invoiceApiMapping.put("taxi_receipt", "RecognizeTaxiReceipt");
        invoiceApiMapping.put("train_ticket", "RecognizeTrainTicket");
        invoiceApiMapping.put("air_ticket", "RecognizeAirlineItinerary");
    }

    /**
     * Get API name for invoice type
     */
    public String getApiName(String invoiceType) {
        return invoiceApiMapping.getOrDefault(invoiceType, "RecognizeVatInvoice");
    }

    /**
     * Check if OCR is properly configured
     */
    public boolean isConfigured() {
        return enabled && accessKeyId != null && accessKeySecret != null
                && !accessKeyId.isEmpty() && !accessKeySecret.isEmpty();
    }
}
