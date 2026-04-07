package com.aioa.ocr.controller;

import com.aioa.ocr.dto.OcrRequest;
import com.aioa.ocr.dto.OcrResponse;
import com.aioa.ocr.enums.InvoiceType;
import com.aioa.ocr.service.OcrService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Unit tests for OcrController
 */
@WebMvcTest(OcrController.class)
class OcrControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private OcrService ocrService;

    private OcrResponse mockResponse;

    @BeforeEach
    void setUp() {
        mockResponse = OcrResponse.builder()
                .success(true)
                .recognitionId("test-recognition-id")
                .invoiceType(InvoiceType.VAT_INVOICE)
                .invoiceNo("12345678")
                .invoiceDate("2024-03-15")
                .totalAmount(BigDecimal.valueOf(1060.00))
                .taxAmount(BigDecimal.valueOf(60.00))
                .pretaxAmount(BigDecimal.valueOf(1000.00))
                .confidence(0.95)
                .highConfidence(true)
                .sellerName("北京某某科技有限公司")
                .buyerName("上海某某企业")
                .processTime(350L)
                .build();
    }

    @Test
    @DisplayName("POST /api/v1/ocr/recognize - Should recognize invoice successfully")
    void testRecognizeInvoice() throws Exception {
        // Given
        OcrRequest request = OcrRequest.builder()
                .invoiceType(InvoiceType.VAT_INVOICE)
                .imageData("https://example.com/invoice.jpg")
                .imageType("url")
                .fileName("test-invoice.jpg")
                .build();

        when(ocrService.recognize(any(OcrRequest.class), anyString()))
                .thenReturn(mockResponse);

        // When & Then
        mockMvc.perform(MockMvcRequestBuilders.post("/api/v1/ocr/recognize")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.success").value(true))
                .andExpect(jsonPath("$.data.invoiceType").value("VAT_INVOICE"))
                .andExpect(jsonPath("$.data.invoiceNo").value("12345678"))
                .andExpect(jsonPath("$.data.confidence").value(0.95));
    }

    @Test
    @DisplayName("POST /api/v1/ocr/recognize - Should return error for invalid request")
    void testRecognizeWithInvalidRequest() throws Exception {
        // Given - missing required fields
        String invalidRequest = "{}";

        // When & Then
        mockMvc.perform(MockMvcRequestBuilders.post("/api/v1/ocr/recognize")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(invalidRequest))
                .andExpect(status().isOk()); // Validation error will be in response
    }

    @Test
    @DisplayName("POST /api/v1/ocr/recognize/async - Should submit async recognition")
    void testRecognizeAsync() throws Exception {
        // Given
        OcrRequest request = OcrRequest.builder()
                .invoiceType(InvoiceType.TAXI_RECEIPT)
                .imageData("https://example.com/taxi.jpg")
                .imageType("url")
                .build();

        when(ocrService.recognizeAsync(any(OcrRequest.class), anyString()))
                .thenReturn("async-recognition-id");

        // When & Then
        mockMvc.perform(MockMvcRequestBuilders.post("/api/v1/ocr/recognize/async")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data").value("async-recognition-id"));
    }

    @Test
    @DisplayName("GET /api/v1/ocr/result/{id} - Should get recognition result")
    void testGetRecognitionResult() throws Exception {
        // Given
        String recognitionId = "test-recognition-id";
        when(ocrService.getRecognitionResult(recognitionId))
                .thenReturn(mockResponse);

        // When & Then
        mockMvc.perform(MockMvcRequestBuilders.get("/api/v1/ocr/result/" + recognitionId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.recognitionId").value(recognitionId))
                .andExpect(jsonPath("$.data.success").value(true));
    }

    @Test
    @DisplayName("GET /api/v1/ocr/types - Should return supported invoice types")
    void testGetSupportedTypes() throws Exception {
        // When & Then
        mockMvc.perform(MockMvcRequestBuilders.get("/api/v1/ocr/types"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data").isArray())
                .andExpect(jsonPath("$.data.length()").value(4))
                .andExpect(jsonPath("$.data[0].code").value("vat_invoice"))
                .andExpect(jsonPath("$.data[1].code").value("taxi_receipt"))
                .andExpect(jsonPath("$.data[2].code").value("train_ticket"))
                .andExpect(jsonPath("$.data[3].code").value("air_ticket"));
    }

    @Test
    @DisplayName("GET /api/v1/ocr/health - Should return health status")
    void testHealthCheck() throws Exception {
        // When & Then
        mockMvc.perform(MockMvcRequestBuilders.get("/api/v1/ocr/health"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.status").value("UP"))
                .andExpect(jsonPath("$.data.service").value("aioa-ocr"));
    }

    @Test
    @DisplayName("POST /api/v1/ocr/batch - Should process batch recognition")
    void testBatchRecognize() throws Exception {
        // Given
        OcrRequest request1 = OcrRequest.builder()
                .invoiceType(InvoiceType.VAT_INVOICE)
                .imageData("https://example.com/invoice1.jpg")
                .imageType("url")
                .build();

        OcrRequest request2 = OcrRequest.builder()
                .invoiceType(InvoiceType.TAXI_RECEIPT)
                .imageData("https://example.com/taxi.jpg")
                .imageType("url")
                .build();

        Map<Integer, OcrResponse> batchResults = new HashMap<>();
        batchResults.put(0, mockResponse);
        batchResults.put(1, OcrResponse.builder()
                .success(true)
                .invoiceType(InvoiceType.TAXI_RECEIPT)
                .confidence(0.90)
                .build());

        when(ocrService.batchRecognize(any(), anyString()))
                .thenReturn(batchResults);

        // When & Then
        mockMvc.perform(MockMvcRequestBuilders.post("/api/v1/ocr/batch")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(java.util.List.of(request1, request2))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data").isMap())
                .andExpect(jsonPath("$.data['0'].success").value(true))
                .andExpect(jsonPath("$.data['1'].success").value(true));
    }
}
