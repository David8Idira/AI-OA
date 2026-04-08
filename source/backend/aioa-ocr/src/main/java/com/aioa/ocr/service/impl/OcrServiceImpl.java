package com.aioa.ocr.service.impl;

import cn.hutool.core.util.IdUtil;
import cn.hutool.crypto.digest.DigestUtil;
import com.alibaba.fastjson2.JSON;
import com.aioa.common.mail.MailService;
import com.aioa.common.exception.BusinessException;
import com.aioa.common.result.ResultCode;
import com.aioa.ocr.client.AliyunOcrClient;
import com.aioa.ocr.config.AliyunOcrConfig;
import com.aioa.ocr.dto.OcrRequest;
import com.aioa.ocr.dto.OcrResponse;
import com.aioa.ocr.entity.InvoiceRecord;
import com.aioa.ocr.enums.InvoiceType;
import com.aioa.ocr.service.OcrService;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * OCR Service Implementation
 * Implements invoice OCR recognition using Aliyun OCR or mock fallback
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class OcrServiceImpl extends ServiceImpl<OcrServiceImpl.InvoiceRecordMapper, InvoiceRecord>
        implements OcrService {

    private final AliyunOcrClient aliyunOcrClient;
    private final AliyunOcrConfig aliyunOcrConfig;
    private final StringRedisTemplate redisTemplate;
    private final MailService mailService;

    private static final String CACHE_PREFIX = "aioa:ocr:cache:";
    private static final String RECORD_STATUS_PENDING = "pending";
    private static final String RECORD_STATUS_PROCESSING = "processing";
    private static final String RECORD_STATUS_SUCCESS = "success";
    private static final String RECORD_STATUS_FAILED = "failed";

    @Override
    public OcrResponse recognize(OcrRequest request, String userId) {
        log.info("Starting OCR recognition for user: {}, invoice type: {}", userId, request.getInvoiceType());

        // Validate request
        validateOcrRequest(request);

        // Check cache if enabled
        String imageHash = calculateImageHash(request.getImageData());
        if (aliyunOcrConfig.getEnableCache()) {
            OcrResponse cachedResponse = getCachedResult(imageHash);
            if (cachedResponse != null) {
                log.info("Returning cached OCR result for hash: {}", imageHash);
                return cachedResponse;
            }
        }

        // Create invoice record for tracking
        InvoiceRecord record = createInvoiceRecord(request, userId);

        try {
            // Perform OCR recognition
            OcrResponse response = aliyunOcrClient.recognize(request);

            // Validate response
            aliyunOcrClient.validateResponse(response);

            // Update record with results
            updateRecordWithResponse(record, response);

            // Set recognition ID for tracking
            response.setRecognitionId(record.getId());

            // Cache result if enabled
            if (aliyunOcrConfig.getEnableCache() && response.getSuccess()) {
                cacheResult(imageHash, response);
            }

            log.info("OCR recognition completed successfully, record ID: {}, confidence: {}",
                    record.getId(), response.getConfidence());

            // 低置信度邮件通知 (置信度<85%)
            if (response.getConfidence() != null && response.getConfidence() < 0.85) {
                try {
                    mailService.sendOcrNotice(userId, request.getFileName(), 
                        response.getConfidence() * 100, true);
                    log.info("Low confidence OCR notice sent to user: {}", userId);
                } catch (Exception e) {
                    log.error("Failed to send OCR notice email", e);
                }
            }

            return response;

        } catch (Exception e) {
            log.error("OCR recognition failed for record: {}, error: {}", record.getId(), e.getMessage(), e);

            // Update record with error
            updateRecordStatus(record.getId(), RECORD_STATUS_FAILED, e.getMessage());

            return OcrResponse.builder()
                    .success(false)
                    .recognitionId(record.getId())
                    .invoiceType(request.getInvoiceType())
                    .errorMessage(e.getMessage())
                    .build();
        }
    }

    @Override
    public String recognizeAsync(OcrRequest request, String userId) {
        log.info("Starting async OCR recognition for user: {}", userId);

        // Validate request
        validateOcrRequest(request);

        // Create invoice record
        InvoiceRecord record = createInvoiceRecord(request, userId);

        // Start async processing
        processOcrAsync(record.getId(), request);

        return record.getId();
    }

    @Async
    protected void processOcrAsync(String recordId, OcrRequest request) {
        log.info("Async OCR processing started for record: {}", recordId);

        try {
            // Update status to processing
            updateRecordStatus(recordId, RECORD_STATUS_PROCESSING, null);

            // Get record
            InvoiceRecord record = getById(recordId);
            if (record == null) {
                log.error("Record not found for async processing: {}", recordId);
                return;
            }

            // Perform OCR
            OcrResponse response = aliyunOcrClient.recognize(request);

            // Update record with results
            updateRecordWithResponse(record, response);

            log.info("Async OCR processing completed for record: {}", recordId);

        } catch (Exception e) {
            log.error("Async OCR processing failed for record: {}, error: {}", recordId, e.getMessage(), e);
            updateRecordStatus(recordId, RECORD_STATUS_FAILED, e.getMessage());
        }
    }

    @Override
    public OcrResponse getRecognitionResult(String recognitionId) {
        InvoiceRecord record = getById(recognitionId);
        if (record == null) {
            throw new BusinessException(ResultCode.NOT_FOUND, "识别记录不存在");
        }

        if (RECORD_STATUS_PROCESSING.equals(record.getStatus())) {
            return OcrResponse.builder()
                    .success(false)
                    .recognitionId(recognitionId)
                    .errorMessage("识别进行中，请稍后查询")
                    .build();
        }

        if (RECORD_STATUS_FAILED.equals(record.getStatus())) {
            return OcrResponse.builder()
                    .success(false)
                    .recognitionId(recognitionId)
                    .errorMessage(record.getErrorMessage())
                    .build();
        }

        return buildResponseFromRecord(record);
    }

    @Override
    public InvoiceRecord saveInvoiceRecord(InvoiceRecord record) {
        if (record.getId() == null) {
            record.setId(IdUtil.fastSimpleUUID());
        }
        this.save(record);
        return record;
    }

    @Override
    public InvoiceRecord getInvoiceRecordById(String id) {
        return getById(id);
    }

    @Override
    public List<InvoiceRecord> getInvoiceRecordsByUserId(String userId, int page, int size) {
        LambdaQueryWrapper<InvoiceRecord> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(InvoiceRecord::getUserId, userId)
                .orderByDesc(InvoiceRecord::getCreateTime)
                .last("LIMIT " + (page - 1) * size + "," + size);

        return list(wrapper);
    }

    @Override
    public List<InvoiceRecord> getInvoiceRecordsByType(String invoiceType, int page, int size) {
        LambdaQueryWrapper<InvoiceRecord> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(InvoiceRecord::getInvoiceType, invoiceType)
                .orderByDesc(InvoiceRecord::getCreateTime)
                .last("LIMIT " + (page - 1) * size + "," + size);

        return list(wrapper);
    }

    @Override
    public boolean deleteInvoiceRecord(String id, String userId) {
        InvoiceRecord record = getById(id);
        if (record == null) {
            throw new BusinessException(ResultCode.NOT_FOUND, "记录不存在");
        }

        // Check authorization
        if (!userId.equals(record.getUserId())) {
            throw new BusinessException(ResultCode.FORBIDDEN, "无权删除此记录");
        }

        return removeById(id);
    }

    @Override
    public Map<Integer, OcrResponse> batchRecognize(List<OcrRequest> requests, String userId) {
        log.info("Starting batch OCR recognition for {} items, user: {}", requests.size(), userId);

        Map<Integer, OcrResponse> results = new HashMap<>();

        for (int i = 0; i < requests.size(); i++) {
            try {
                OcrResponse response = recognize(requests.get(i), userId);
                results.put(i, response);
            } catch (Exception e) {
                log.error("Batch OCR failed for index {}: {}", i, e.getMessage());
                results.put(i, OcrResponse.builder()
                        .success(false)
                        .errorMessage(e.getMessage())
                        .build());
            }
        }

        log.info("Batch OCR completed, success: {}", results.values().stream()
                .filter(OcrResponse::getSuccess).count());

        return results;
    }

    @Override
    public Map<String, Object> getStatistics(String userId) {
        Map<String, Object> stats = new HashMap<>();

        LambdaQueryWrapper<InvoiceRecord> wrapper = new LambdaQueryWrapper<>();

        if (userId != null) {
            wrapper.eq(InvoiceRecord::getUserId, userId);
        }

        // Total count
        stats.put("totalCount", count(wrapper));

        // Count by status
        stats.put("successCount", count(wrapper.clone().eq(InvoiceRecord::getStatus, RECORD_STATUS_SUCCESS)));
        stats.put("failedCount", count(wrapper.clone().eq(InvoiceRecord::getStatus, RECORD_STATUS_FAILED)));

        // Count by invoice type
        Map<String, Long> typeCountMap = new HashMap<>();
        for (InvoiceType type : InvoiceType.values()) {
            long count = count(wrapper.clone().eq(InvoiceRecord::getInvoiceType, type.getCode()));
            typeCountMap.put(type.getCode(), count);
        }
        stats.put("countByType", typeCountMap);

        // Average confidence
        stats.put("averageConfidence", calculateAverageConfidence(wrapper));

        return stats;
    }

    @Override
    public InvoiceRecord updateRecordStatus(String id, String status, String errorMessage) {
        InvoiceRecord record = getById(id);
        if (record != null) {
            record.setStatus(status);
            if (errorMessage != null) {
                record.setErrorMessage(errorMessage);
            }
            updateById(record);
        }
        return record;
    }

    @Override
    public OcrResponse getCachedResult(String imageHash) {
        if (!aliyunOcrConfig.getEnableCache()) {
            return null;
        }

        String key = CACHE_PREFIX + imageHash;
        String cached = redisTemplate.opsForValue().get(key);

        if (cached != null) {
            try {
                return JSON.parseObject(cached, OcrResponse.class);
            } catch (Exception e) {
                log.warn("Failed to parse cached OCR result: {}", e.getMessage());
            }
        }

        return null;
    }

    @Override
    public void cacheResult(String imageHash, OcrResponse response) {
        if (!aliyunOcrConfig.getEnableCache() || !response.getSuccess()) {
            return;
        }

        String key = CACHE_PREFIX + imageHash;
        String value = JSON.toJSONString(response);

        redisTemplate.opsForValue().set(key, value,
                aliyunOcrConfig.getCacheTtl(), TimeUnit.SECONDS);
    }

    // ==================== Private Helper Methods ====================

    private void validateOcrRequest(OcrRequest request) {
        if (request == null) {
            throw new BusinessException(ResultCode.BAD_REQUEST, "OCR请求不能为空");
        }

        if (request.getInvoiceType() == null) {
            throw new BusinessException(ResultCode.BAD_REQUEST, "发票类型不能为空");
        }

        if (request.getImageData() == null || request.getImageData().isEmpty()) {
            throw new BusinessException(ResultCode.BAD_REQUEST, "图片数据不能为空");
        }

        // Validate image data format
        if (request.isUrlImage() && !isValidUrl(request.getImageData())) {
            throw new BusinessException(ResultCode.BAD_REQUEST, "图片URL格式不正确");
        }

        if (request.isBase64Image() && !isValidBase64(request.getImageData())) {
            throw new BusinessException(ResultCode.BAD_REQUEST, "Base64图片数据格式不正确");
        }
    }

    private boolean isValidUrl(String url) {
        return url.startsWith("http://") || url.startsWith("https://");
    }

    private boolean isValidBase64(String data) {
        if (data.startsWith("data:image")) {
            return data.contains(",") && data.split(",").length == 2;
        }
        // Basic Base64 validation
        return data.matches("^[A-Za-z0-9+/=]+$") && data.length() % 4 == 0;
    }

    private InvoiceRecord createInvoiceRecord(OcrRequest request, String userId) {
        InvoiceRecord record = new InvoiceRecord();
        record.setId(IdUtil.fastSimpleUUID());
        record.setUserId(userId);
        record.setInvoiceType(request.getInvoiceType().getCode());
        record.setFileName(request.getFileName());
        record.setFileUrl(request.isUrlImage() ? request.getImageData() : null);
        record.setStatus(RECORD_STATUS_PENDING);
        record.setConfidence(0.0);

        this.save(record);
        return record;
    }

    private void updateRecordWithResponse(InvoiceRecord record, OcrResponse response) {
        record.setStatus(response.getSuccess() ? RECORD_STATUS_SUCCESS : RECORD_STATUS_FAILED);
        record.setConfidence(response.getConfidence());
        record.setProcessTime(response.getProcessTime());

        if (response.getSuccess()) {
            record.setInvoiceNo(response.getInvoiceNo());
            record.setInvoiceDate(response.getInvoiceDate());
            record.setTotalAmount(response.getTotalAmount());
            record.setTaxAmount(response.getTaxAmount());
            record.setInvoiceCode(response.getInvoiceCode());
            record.setSellerName(response.getSellerName());
            record.setSellerTaxId(response.getSellerTaxId());
            record.setBuyerName(response.getBuyerName());
            record.setBuyerTaxId(response.getBuyerTaxId());

            if (response.getRawResult() != null) {
                record.setRawResult(JSON.toJSONString(response.getRawResult()));
            }
        }

        if (!response.getSuccess() && response.getErrorMessage() != null) {
            record.setErrorMessage(response.getErrorMessage());
        }

        this.updateById(record);
    }

    private OcrResponse buildResponseFromRecord(InvoiceRecord record) {
        InvoiceType invoiceType = record.getInvoiceTypeEnum();

        OcrResponse.OcrResponseBuilder builder = OcrResponse.builder()
                .success(record.isSuccess())
                .recognitionId(record.getId())
                .invoiceType(invoiceType)
                .confidence(record.getConfidence())
                .highConfidence(record.isHighConfidence())
                .invoiceNo(record.getInvoiceNo())
                .invoiceDate(record.getInvoiceDate())
                .totalAmount(record.getTotalAmount())
                .taxAmount(record.getTaxAmount())
                .invoiceCode(record.getInvoiceCode())
                .sellerName(record.getSellerName())
                .sellerTaxId(record.getSellerTaxId())
                .buyerName(record.getBuyerName())
                .buyerTaxId(record.getBuyerTaxId())
                .processTime(record.getProcessTime())
                .errorMessage(record.getErrorMessage());

        // Parse raw result if available
        if (record.getRawResult() != null && !record.getRawResult().isEmpty()) {
            try {
                builder.rawResult(JSON.parseObject(record.getRawResult()));
            } catch (Exception e) {
                log.warn("Failed to parse raw result for record: {}", record.getId());
            }
        }

        return builder.build();
    }

    private String calculateImageHash(String imageData) {
        // For URL, use URL as hash key
        if (imageData.startsWith("http")) {
            return DigestUtil.md5Hex(imageData);
        }

        // For Base64, use the data part
        if (imageData.contains(",")) {
            imageData = imageData.split(",")[1];
        }

        // Use first 1000 chars + length for faster hashing
        String toHash = imageData.substring(0, Math.min(1000, imageData.length()))
                + imageData.length();

        return DigestUtil.md5Hex(toHash);
    }

    private Double calculateAverageConfidence(LambdaQueryWrapper<InvoiceRecord> wrapper) {
        List<InvoiceRecord> records = list(wrapper.clone().eq(InvoiceRecord::getStatus, RECORD_STATUS_SUCCESS));

        if (records.isEmpty()) {
            return 0.0;
        }

        return records.stream()
                .filter(r -> r.getConfidence() != null)
                .mapToDouble(InvoiceRecord::getConfidence)
                .average()
                .orElse(0.0);
    }

    // ==================== Mapper Interface ====================

    public interface InvoiceRecordMapper extends com.baomidou.mybatisplus.core.mapper.BaseMapper<InvoiceRecord> {
    }
}
