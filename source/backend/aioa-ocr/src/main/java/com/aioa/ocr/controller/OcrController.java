package com.aioa.ocr.controller;

import com.aioa.common.annotation.Login;
import com.aioa.common.result.Result;
import com.aioa.common.result.ResultCode;
import com.aioa.ocr.dto.OcrRequest;
import com.aioa.ocr.dto.OcrResponse;
import com.aioa.ocr.entity.InvoiceRecord;
import com.aioa.ocr.enums.InvoiceType;
import com.aioa.ocr.service.OcrService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * OCR Controller
 * Provides REST API endpoints for invoice OCR recognition
 */
@Slf4j
@RestController
@RequestMapping("/api/v1/ocr")
@RequiredArgsConstructor
@Tag(name = "OCR发票识别", description = "发票OCR识别API")
public class OcrController {

    private final OcrService ocrService;

    /**
     * Recognize invoice from image
     * POST /api/v1/ocr/recognize
     */
    @PostMapping("/recognize")
    @Operation(
            summary = "识别发票",
            description = "对发票图片进行OCR识别，支持增值税发票、出租车票、火车票、机票",
            responses = {
                    @ApiResponse(responseCode = "200", description = "识别成功",
                            content = @Content(mediaType = "application/json",
                                    schema = @Schema(implementation = OcrResponse.class))),
                    @ApiResponse(responseCode = "400", description = "请求参数错误"),
                    @ApiResponse(responseCode = "500", description = "服务器内部错误")
            }
    )
    public Result<OcrResponse> recognize(
            @Valid @RequestBody OcrRequest request,
            @Parameter(description = "用户ID (可选，自动从上下文获取)")
            @RequestAttribute(value = "userId", required = false) String userId) {

        log.info("Received OCR recognition request, invoice type: {}, userId: {}",
                request.getInvoiceType(), userId);

        // Set userId if not provided
        if (userId == null && request.getUserId() != null) {
            userId = request.getUserId();
        }

        // Perform OCR recognition
        OcrResponse response = ocrService.recognize(request, userId);

        log.info("OCR recognition completed, success: {}, confidence: {}",
                response.getSuccess(), response.getConfidence());

        return Result.success(response);
    }

    /**
     * Recognize invoice asynchronously
     * POST /api/v1/ocr/recognize/async
     */
    @PostMapping("/recognize/async")
    @Operation(
            summary = "异步识别发票",
            description = "异步对发票图片进行OCR识别，立即返回识别ID用于后续查询结果",
            responses = {
                    @ApiResponse(responseCode = "200", description = "提交成功"),
                    @ApiResponse(responseCode = "400", description = "请求参数错误")
            }
    )
    public Result<String> recognizeAsync(
            @Valid @RequestBody OcrRequest request,
            @RequestAttribute(value = "userId", required = false) String userId) {

        log.info("Received async OCR recognition request, invoice type: {}", request.getInvoiceType());

        if (userId == null && request.getUserId() != null) {
            userId = request.getUserId();
        }

        String recognitionId = ocrService.recognizeAsync(request, userId);

        return Result.success("识别任务已提交", recognitionId);
    }

    /**
     * Get recognition result by ID
     * GET /api/v1/ocr/result/{recognitionId}
     */
    @GetMapping("/result/{recognitionId}")
    @Operation(
            summary = "查询识别结果",
            description = "通过识别ID查询OCR识别结果",
            responses = {
                    @ApiResponse(responseCode = "200", description = "查询成功"),
                    @ApiResponse(responseCode = "404", description = "识别记录不存在")
            }
    )
    public Result<OcrResponse> getRecognitionResult(
            @Parameter(description = "识别ID")
            @PathVariable String recognitionId) {

        log.info("Querying recognition result for ID: {}", recognitionId);

        OcrResponse response = ocrService.getRecognitionResult(recognitionId);

        return Result.success(response);
    }

    /**
     * Get invoice record by ID
     * GET /api/v1/ocr/records/{id}
     */
    @GetMapping("/records/{id}")
    @Operation(summary = "获取发票记录")
    @Login
    public Result<InvoiceRecord> getInvoiceRecord(
            @Parameter(description = "记录ID")
            @PathVariable String id,
            @RequestAttribute("userId") String userId) {

        InvoiceRecord record = ocrService.getInvoiceRecordById(id);

        if (record == null) {
            return Result.error(ResultCode.NOT_FOUND.getCode(), "记录不存在");
        }

        return Result.success(record);
    }

    /**
     * Get user's invoice records
     * GET /api/v1/ocr/records
     */
    @GetMapping("/records")
    @Operation(summary = "获取用户发票记录列表")
    @Login
    public Result<List<InvoiceRecord>> getInvoiceRecords(
            @Parameter(description = "用户ID")
            @RequestAttribute("userId") String userId,
            @Parameter(description = "发票类型筛选")
            @RequestParam(required = false) String invoiceType,
            @Parameter(description = "页码")
            @RequestParam(defaultValue = "1") int page,
            @Parameter(description = "每页数量")
            @RequestParam(defaultValue = "10") int size) {

        log.info("Querying invoice records for user: {}, type: {}, page: {}", userId, invoiceType, page);

        List<InvoiceRecord> records;

        if (invoiceType != null && InvoiceType.isValidCode(invoiceType)) {
            records = ocrService.getInvoiceRecordsByType(invoiceType, page, size);
        } else {
            records = ocrService.getInvoiceRecordsByUserId(userId, page, size);
        }

        return Result.success(records);
    }

    /**
     * Delete invoice record
     * DELETE /api/v1/ocr/records/{id}
     */
    @DeleteMapping("/records/{id}")
    @Operation(summary = "删除发票记录")
    @Login
    public Result<Void> deleteInvoiceRecord(
            @Parameter(description = "记录ID")
            @PathVariable String id,
            @RequestAttribute("userId") String userId) {

        log.info("Deleting invoice record: {}, user: {}", id, userId);

        boolean success = ocrService.deleteInvoiceRecord(id, userId);

        return success ? Result.success() : Result.error("删除失败");
    }

    /**
     * Batch recognize invoices
     * POST /api/v1/ocr/batch
     */
    @PostMapping("/batch")
    @Operation(
            summary = "批量识别发票",
            description = "批量对多张发票图片进行OCR识别",
            responses = {
                    @ApiResponse(responseCode = "200", description = "批量识别完成")
            }
    )
    public Result<Map<Integer, OcrResponse>> batchRecognize(
            @Valid @RequestBody List<OcrRequest> requests,
            @RequestAttribute(value = "userId", required = false) String userId) {

        log.info("Received batch OCR request for {} items, user: {}", requests.size(), userId);

        if (userId == null && !requests.isEmpty() && requests.get(0).getUserId() != null) {
            userId = requests.get(0).getUserId();
        }

        Map<Integer, OcrResponse> results = ocrService.batchRecognize(requests, userId);

        return Result.success(results);
    }

    /**
     * Get OCR statistics
     * GET /api/v1/ocr/statistics
     */
    @GetMapping("/statistics")
    @Operation(summary = "获取OCR统计信息")
    @Login
    public Result<Map<String, Object>> getStatistics(
            @RequestAttribute("userId") String userId) {

        log.info("Getting OCR statistics for user: {}", userId);

        Map<String, Object> statistics = ocrService.getStatistics(userId);

        return Result.success(statistics);
    }

    /**
     * Get supported invoice types
     * GET /api/v1/ocr/types
     */
    @GetMapping("/types")
    @Operation(summary = "获取支持的发票类型")
    public Result<List<InvoiceTypeVO>> getSupportedTypes() {

        List<InvoiceTypeVO> types = List.of(InvoiceType.values()).stream()
                .map(type -> new InvoiceTypeVO(
                        type.getCode(),
                        type.getName(),
                        type.getDescription()
                ))
                .toList();

        return Result.success(types);
    }

    /**
     * Health check endpoint
     * GET /api/v1/ocr/health
     */
    @GetMapping("/health")
    @Operation(summary = "健康检查")
    public Result<Map<String, Object>> healthCheck() {

        Map<String, Object> health = Map.of(
                "status", "UP",
                "timestamp", System.currentTimeMillis(),
                "service", "aioa-ocr"
        );

        return Result.success(health);
    }

    /**
     * Invoice Type VO for API response
     */
    @Schema(name = "InvoiceTypeVO", description = "发票类型信息")
    public record InvoiceTypeVO(
            @Schema(description = "类型编码")
            String code,

            @Schema(description = "类型名称")
            String name,

            @Schema(description = "类型描述")
            String description
    ) {
    }
}
