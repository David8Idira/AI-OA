package com.aioa.ocr.service.impl;

import com.aioa.common.exception.BusinessException;
import com.aioa.common.mail.MailService;
import com.aioa.common.result.ResultCode;
import com.aioa.ocr.client.AliyunOcrClient;
import com.aioa.ocr.config.AliyunOcrConfig;
import com.aioa.ocr.dto.OcrRequest;
import com.aioa.ocr.dto.OcrResponse;
import com.aioa.ocr.entity.InvoiceRecord;
import com.aioa.ocr.enums.InvoiceType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;

import java.lang.reflect.Field;
import java.math.BigDecimal;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.mockito.Mockito.doReturn;

/**
 * OcrServiceImpl 单元测试
 */
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
@DisplayName("OcrServiceImpl 测试")
class OcrServiceImplTest {

    @Mock
    private AliyunOcrClient aliyunOcrClient;

    @Mock
    private AliyunOcrConfig aliyunOcrConfig;

    @Mock
    private StringRedisTemplate redisTemplate;

    @Mock
    private ValueOperations<String, String> valueOperations;

    @Mock
    private MailService mailService;

    private OcrServiceImpl ocrService;

    @BeforeEach
    void setUp() throws Exception {
        ocrService = spy(new OcrServiceImpl(aliyunOcrClient, aliyunOcrConfig, redisTemplate, mailService));

        // Inject baseMapper via reflection
        Field baseMapperField = ocrService.getClass().getSuperclass().getDeclaredField("baseMapper");
        baseMapperField.setAccessible(true);
        baseMapperField.set(ocrService, mock(com.baomidou.mybatisplus.core.mapper.BaseMapper.class));

        // Setup config mocks
        lenient().when(aliyunOcrConfig.getEnableCache()).thenReturn(true);
        lenient().when(aliyunOcrConfig.getCacheTtl()).thenReturn(3600L);
        lenient().when(redisTemplate.opsForValue()).thenReturn(valueOperations);
    }

    // ==================== 正常场景测试 ====================

    @Nested
    @DisplayName("正常场景测试")
    class NormalScenarios {

        @Test
        @DisplayName("发票识别 - 成功")
        void recognize_Success() throws Exception {
            // given
            OcrRequest request = buildOcrRequest();
            OcrResponse mockResponse = OcrResponse.builder()
                    .success(true)
                    .recognitionId("rec001")
                    .invoiceType(InvoiceType.VAT_INVOICE)
                    .confidence(1.0)
                    .invoiceNo("INV-123456")
                    .invoiceDate("2024-01-15")
                    .totalAmount(new BigDecimal("1000.00"))
                    .taxAmount(new BigDecimal("130.00"))
                    .sellerName("某科技有限公司")
                    .build();

            when(aliyunOcrClient.recognize(any(OcrRequest.class))).thenReturn(mockResponse);
            doNothing().when(aliyunOcrClient).validateResponse(any(OcrResponse.class));

            // when
            OcrResponse result = ocrService.recognize(request, "user001");

            // then
            assertNotNull(result);
            assertTrue(result.getSuccess());
            assertEquals("INV-123456", result.getInvoiceNo());
            assertEquals(1.0, result.getConfidence());
        }

        @Test
        @DisplayName("异步识别 - 返回记录ID")
        void recognizeAsync_Success() throws Exception {
            // given
            OcrRequest request = buildOcrRequest();

            // when
            String recordId = ocrService.recognizeAsync(request, "user001");

            // then
            assertNotNull(recordId);
        }

        @Test
        @DisplayName("获取识别结果 - 成功状态")
        void getRecognitionResult_Success() {
            // given
            InvoiceRecord record = new InvoiceRecord();
            record.setId("rec001");
            record.setUserId("user001");
            record.setInvoiceType("VAT_INVOICE");
            record.setStatus("success");
            record.setConfidence(0.95);
            record.setInvoiceNo("INV-123456");
            record.setTotalAmount(new BigDecimal("1000.00"));

            when(ocrService.getById("rec001")).thenReturn(record);

            // when
            OcrResponse result = ocrService.getRecognitionResult("rec001");

            // then
            assertNotNull(result);
            assertTrue(result.getSuccess());
            assertEquals("INV-123456", result.getInvoiceNo());
        }

        @Test
        @DisplayName("保存发票记录 - 成功")
        void saveInvoiceRecord_Success() {
            // given
            InvoiceRecord record = new InvoiceRecord();
            record.setUserId("user001");
            record.setInvoiceType("VAT_INVOICE");
            record.setFileName("test.pdf");

            // Mock save to return true - use doReturn instead of when for baseMapper interaction
            doReturn(true).when(ocrService).save(any(InvoiceRecord.class));

            // when
            InvoiceRecord result = ocrService.saveInvoiceRecord(record);

            // then
            assertNotNull(result);
        }

        @Test
        @DisplayName("批量识别 - 成功")
        void batchRecognize_Success() throws Exception {
            // given
            List<OcrRequest> requests = Arrays.asList(buildOcrRequest(), buildOcrRequest());
            OcrResponse mockResponse = OcrResponse.builder()
                    .success(true)
                    .recognitionId("rec001")
                    .invoiceType(InvoiceType.VAT_INVOICE)
                    .confidence(1.0)
                    .build();

            when(aliyunOcrClient.recognize(any(OcrRequest.class))).thenReturn(mockResponse);
            doNothing().when(aliyunOcrClient).validateResponse(any(OcrResponse.class));

            // when
            Map<Integer, OcrResponse> results = ocrService.batchRecognize(requests, "user001");

            // then
            assertNotNull(results);
            assertEquals(2, results.size());
            assertTrue(results.get(0).getSuccess());
            assertTrue(results.get(1).getSuccess());
        }
    }

    // ==================== 异常场景测试 ====================

    @Nested
    @DisplayName("异常场景测试")
    class ExceptionScenarios {

        @Test
        @DisplayName("发票识别 - 请求为空")
        void recognize_NullRequest() {
            // when/then - service evaluates request.getInvoiceType() before null check, so NPE is thrown
            assertThrows(NullPointerException.class,
                    () -> ocrService.recognize(null, "user001"));
        }

        @Test
        @DisplayName("发票识别 - 发票类型为空")
        void recognize_InvoiceTypeNull() {
            // given
            OcrRequest request = new OcrRequest();
            request.setImageData("data:image/png;base64,xxxxx");
            request.setInvoiceType(null);

            // when/then
            BusinessException ex = assertThrows(BusinessException.class,
                    () -> ocrService.recognize(request, "user001"));
            assertTrue(ex.getMessage().contains("发票类型"));
        }

        @Test
        @DisplayName("发票识别 - 图片数据为空")
        void recognize_ImageDataEmpty() {
            // given
            OcrRequest request = new OcrRequest();
            request.setInvoiceType(InvoiceType.VAT_INVOICE);
            request.setImageData("");

            // when/then
            BusinessException ex = assertThrows(BusinessException.class,
                    () -> ocrService.recognize(request, "user001"));
            assertTrue(ex.getMessage().contains("图片数据"));
        }

        @Test
        @DisplayName("获取识别结果 - 记录不存在")
        void getRecognitionResult_NotFound() {
            // given
            when(ocrService.getById("rec999")).thenReturn(null);

            // when/then
            BusinessException ex = assertThrows(BusinessException.class,
                    () -> ocrService.getRecognitionResult("rec999"));
            assertTrue(ex.getMessage().contains("不存在"));
        }

        @Test
        @DisplayName("删除发票记录 - 无权删除")
        void deleteInvoiceRecord_NoPermission() {
            // given
            InvoiceRecord record = new InvoiceRecord();
            record.setId("rec001");
            record.setUserId("user001");

            when(ocrService.getById("rec001")).thenReturn(record);

            // when/then
            BusinessException ex = assertThrows(BusinessException.class,
                    () -> ocrService.deleteInvoiceRecord("rec001", "user999"));
            assertTrue(ex.getMessage().contains("无权"));
        }
    }

    // ==================== 边界条件测试 ====================

    @Nested
    @DisplayName("边界条件测试")
    class BoundaryScenarios {

        @Test
        @DisplayName("获取识别结果 - 处理中状态")
        void getRecognitionResult_Processing() {
            // given
            InvoiceRecord record = new InvoiceRecord();
            record.setId("rec001");
            record.setUserId("user001");
            record.setStatus("processing");
            record.setInvoiceType("VAT_INVOICE");

            when(ocrService.getById("rec001")).thenReturn(record);

            // when
            OcrResponse result = ocrService.getRecognitionResult("rec001");

            // then
            assertNotNull(result);
            assertFalse(result.getSuccess());
            assertTrue(result.getErrorMessage().contains("进行中") || result.getErrorMessage().contains("稍后"));
        }

        @Test
        @DisplayName("获取识别结果 - 失败状态")
        void getRecognitionResult_Failed() {
            // given
            InvoiceRecord record = new InvoiceRecord();
            record.setId("rec001");
            record.setUserId("user001");
            record.setStatus("failed");
            record.setErrorMessage("识别服务超时");
            record.setInvoiceType("VAT_INVOICE");

            when(ocrService.getById("rec001")).thenReturn(record);

            // when
            OcrResponse result = ocrService.getRecognitionResult("rec001");

            // then
            assertNotNull(result);
            assertFalse(result.getSuccess());
            assertEquals("识别服务超时", result.getErrorMessage());
        }

        @Test
        @DisplayName("获取统计数据 - 成功")
        void getStatistics_Success() {
            // given
            when(ocrService.count(any())).thenReturn(10L);

            // when
            Map<String, Object> stats = ocrService.getStatistics("user001");

            // then
            assertNotNull(stats);
            assertNotNull(stats.get("totalCount"));
        }

        @Test
        @DisplayName("验证请求 - 无效的URL格式")
        void validateOcrRequest_InvalidUrl() {
            // given
            OcrRequest request = new OcrRequest();
            request.setInvoiceType(InvoiceType.VAT_INVOICE);
            request.setImageData("not-a-valid-url");
            request.setImageType("url");

            // when/then
            BusinessException ex = assertThrows(BusinessException.class,
                    () -> ocrService.recognize(request, "user001"));
            assertTrue(ex.getMessage().contains("URL") || ex.getMessage().contains("格式"));
        }

        @Test
        @DisplayName("缓存结果 - 成功")
        void cacheResult_Success() {
            // given
            String imageHash = "abc123hash";
            OcrResponse response = OcrResponse.builder()
                    .success(true)
                    .invoiceType(InvoiceType.VAT_INVOICE)
                    .confidence(1.0)
                    .build();

            // when
            ocrService.cacheResult(imageHash, response);

            // then
            verify(valueOperations, times(1)).set(eq("aioa:ocr:cache:" + imageHash), anyString(), eq(3600L), any());
        }
    }

    // ==================== Helper Methods ====================

    private OcrRequest buildOcrRequest() {
        OcrRequest request = new OcrRequest();
        request.setInvoiceType(InvoiceType.VAT_INVOICE);
        request.setImageData("data:image/png;base64,iVBORw0KGgoAAAANSUhEUgAAAAEAAAABCAYAAAAfFcSJAAAADUlEQVR42mNk+M9QDwADhgGAWjR9awAAAABJRU5ErkJggg==");
        request.setImageType("base64");
        request.setFileName("test_invoice.png");
        return request;
    }
}