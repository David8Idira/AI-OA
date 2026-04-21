package com.aioa.ocr.service;

import com.aioa.ocr.dto.OcrRequest;
import com.aioa.ocr.dto.OcrResponse;
import com.aioa.ocr.entity.InvoiceRecord;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.List;
import java.util.Map;

/**
 * OCR Service Interface
 * Defines the contract for invoice OCR recognition operations
 */
public interface OcrService {

    /**
     * Recognize invoice from image
     *
     * @param request OCR request containing image data and invoice type
     * @param userId user ID (optional, will be set from security context)
     * @return OCR response with recognition results and confidence
     */
    OcrResponse recognize(OcrRequest request, String userId);

    /**
     * Recognize invoice asynchronously
     *
     * @param request OCR request
     * @param userId user ID
     * @return recognition ID for tracking
     */
    String recognizeAsync(OcrRequest request, String userId);

    /**
     * Get recognition result by ID
     *
     * @param recognitionId recognition ID
     * @return OCR response
     */
    OcrResponse getRecognitionResult(String recognitionId);

    /**
     * Save invoice record
     *
     * @param record invoice record to save
     * @return saved record
     */
    InvoiceRecord saveInvoiceRecord(InvoiceRecord record);

    /**
     * Get invoice record by ID
     *
     * @param id record ID
     * @return invoice record or null
     */
    InvoiceRecord getInvoiceRecordById(String id);

    /**
     * Get invoice records by user ID
     *
     * @param userId user ID
     * @param page page number
     * @param size page size
     * @return list of invoice records
     */
    List<InvoiceRecord> getInvoiceRecordsByUserId(String userId, int page, int size);

    /**
     * Get invoice records by type
     *
     * @param invoiceType invoice type code
     * @param page page number
     * @param size page size
     * @return list of invoice records
     */
    List<InvoiceRecord> getInvoiceRecordsByType(String invoiceType, int page, int size);

    /**
     * Delete invoice record (soft delete)
     *
     * @param id record ID
     * @param userId user ID (for authorization)
     * @return true if deleted successfully
     */
    boolean deleteInvoiceRecord(String id, String userId);

    /**
     * Batch recognize invoices
     *
     * @param requests list of OCR requests
     * @param userId user ID
     * @return map of request index to OCR response
     */
    Map<Integer, OcrResponse> batchRecognize(List<OcrRequest> requests, String userId);

    /**
     * Get OCR statistics
     *
     * @param userId user ID (optional, if null returns global stats)
     * @return statistics map
     */
    Map<String, Object> getStatistics(String userId);

    /**
     * Update invoice record status
     *
     * @param id record ID
     * @param status new status
     * @param errorMessage error message (if failed)
     * @return updated record
     */
    InvoiceRecord updateRecordStatus(String id, String status, String errorMessage);

    /**
     * Check if a result is cached
     *
     * @param imageHash hash of the image data
     * @return cached response or null
     */
    OcrResponse getCachedResult(String imageHash);

    /**
     * Cache OCR result
     *
     * @param imageHash hash of the image data
     * @param response OCR response to cache
     */
    void cacheResult(String imageHash, OcrResponse response);
}
