package com.aioa.ocr.client;

import com.aioa.ocr.config.AliyunOcrConfig;
import com.aioa.ocr.dto.OcrRequest;
import com.aioa.ocr.dto.OcrResponse;
import com.aioa.ocr.enums.InvoiceType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for AliyunOcrClient
 */
@ExtendWith(MockitoExtension.class)
class AliyunOcrClientTest {

    private AliyunOcrClient client;
    private AliyunOcrConfig config;

    @BeforeEach
    void setUp() {
        config = new AliyunOcrConfig();
        config.setEnabled(false); // Use mock
        config.setAccessKeyId(null);
        config.setAccessKeySecret(null);
        config.setConfidenceThreshold(0.8);
        config.setEnableCache(false);

        client = new AliyunOcrClient(config);
    }

    @Test
    @DisplayName("Should recognize VAT invoice successfully")
    void testRecognizeVatInvoice() {
        // Given
        OcrRequest request = OcrRequest.builder()
                .invoiceType(InvoiceType.VAT_INVOICE)
                .imageData("https://example.com/vat-invoice.jpg")
                .imageType("url")
                .fileName("test-vat.jpg")
                .build();

        // When
        OcrResponse response = client.recognize(request);

        // Then
        assertNotNull(response);
        assertTrue(response.getSuccess());
        assertEquals(InvoiceType.VAT_INVOICE, response.getInvoiceType());
        assertNotNull(response.getConfidence());
        assertTrue(response.getConfidence() >= 0.0 && response.getConfidence() <= 1.0);
        assertTrue(response.getHighConfidence());
        assertNotNull(response.getTotalAmount());
        assertNotNull(response.getSellerName());
        assertNotNull(response.getBuyerName());
    }

    @Test
    @DisplayName("Should recognize taxi receipt successfully")
    void testRecognizeTaxiReceipt() {
        // Given
        OcrRequest request = OcrRequest.builder()
                .invoiceType(InvoiceType.TAXI_RECEIPT)
                .imageData("https://example.com/taxi.jpg")
                .imageType("url")
                .fileName("test-taxi.jpg")
                .build();

        // When
        OcrResponse response = client.recognize(request);

        // Then
        assertNotNull(response);
        assertTrue(response.getSuccess());
        assertEquals(InvoiceType.TAXI_RECEIPT, response.getInvoiceType());
        assertNotNull(response.getInvoiceNo());
        assertNotNull(response.getTotalAmount());
    }

    @Test
    @DisplayName("Should recognize train ticket successfully")
    void testRecognizeTrainTicket() {
        // Given
        OcrRequest request = OcrRequest.builder()
                .invoiceType(InvoiceType.TRAIN_TICKET)
                .imageData("https://example.com/train.jpg")
                .imageType("url")
                .fileName("test-train.jpg")
                .build();

        // When
        OcrResponse response = client.recognize(request);

        // Then
        assertNotNull(response);
        assertTrue(response.getSuccess());
        assertEquals(InvoiceType.TRAIN_TICKET, response.getInvoiceType());
        assertNotNull(response.getTransportationInfo());
        assertNotNull(response.getTransportationInfo().getDepartureStation());
        assertNotNull(response.getTransportationInfo().getArrivalStation());
    }

    @Test
    @DisplayName("Should recognize air ticket successfully")
    void testRecognizeAirTicket() {
        // Given
        OcrRequest request = OcrRequest.builder()
                .invoiceType(InvoiceType.AIR_TICKET)
                .imageData("https://example.com/airline.jpg")
                .imageType("url")
                .fileName("test-airline.jpg")
                .build();

        // When
        OcrResponse response = client.recognize(request);

        // Then
        assertNotNull(response);
        assertTrue(response.getSuccess());
        assertEquals(InvoiceType.AIR_TICKET, response.getInvoiceType());
        assertNotNull(response.getTransportationInfo());
        assertNotNull(response.getTransportationInfo().getFlightNo());
        assertNotNull(response.getTransportationInfo().getCarrier());
    }

    @Test
    @DisplayName("Should handle Base64 image data")
    void testRecognizeWithBase64Image() {
        // Given
        String base64Image = "data:image/jpeg;base64,/9j/4AAQSkZJRg...";
        OcrRequest request = OcrRequest.builder()
                .invoiceType(InvoiceType.TAXI_RECEIPT)
                .imageData(base64Image)
                .imageType("base64")
                .fileName("test-taxi-base64.jpg")
                .build();

        // When
        OcrResponse response = client.recognize(request);

        // Then
        assertNotNull(response);
        assertTrue(response.getSuccess());
    }

    @Test
    @DisplayName("Should validate response correctly")
    void testValidateResponse() {
        // Given
        OcrResponse successResponse = OcrResponse.builder()
                .success(true)
                .confidence(0.95)
                .build();

        // Then - should not throw exception
        assertDoesNotThrow(() -> client.validateResponse(successResponse));

        // Given
        OcrResponse failedResponse = OcrResponse.builder()
                .success(false)
                .errorMessage("Recognition failed")
                .build();

        // Then - should not throw exception
        assertDoesNotThrow(() -> client.validateResponse(failedResponse));

        // Given
        OcrResponse failedWithoutMessage = OcrResponse.builder()
                .success(false)
                .build();

        // Then - should throw exception
        assertThrows(Exception.class, () -> client.validateResponse(failedWithoutMessage));
    }

    @Test
    @DisplayName("Should handle null response gracefully")
    void testValidateNullResponse() {
        assertThrows(Exception.class, () -> client.validateResponse(null));
    }

    @Test
    @DisplayName("Should generate unique recognition IDs")
    void testUniqueRecognitionIds() {
        // Given
        OcrRequest request = OcrRequest.builder()
                .invoiceType(InvoiceType.VAT_INVOICE)
                .imageData("https://example.com/test.jpg")
                .imageType("url")
                .build();

        // When
        OcrResponse response1 = client.recognize(request);
        OcrResponse response2 = client.recognize(request);

        // Then
        assertNotNull(response1.getRecognitionId());
        assertNotNull(response2.getRecognitionId());
        assertNotEquals(response1.getRecognitionId(), response2.getRecognitionId());
    }

    @Test
    @DisplayName("Should set high confidence flag correctly")
    void testHighConfidenceFlag() {
        // Given
        OcrRequest request = OcrRequest.builder()
                .invoiceType(InvoiceType.VAT_INVOICE)
                .imageData("https://example.com/test.jpg")
                .imageType("url")
                .build();

        // When
        OcrResponse response = client.recognize(request);

        // Then
        assertNotNull(response.getHighConfidence());
        // Mock implementation should always have confidence >= 0.8
        assertEquals(response.getConfidence() >= config.getConfidenceThreshold(),
                response.getHighConfidence());
    }

    @Test
    @DisplayName("Should include fields in response for VAT invoice")
    void testVatInvoiceFields() {
        // Given
        OcrRequest request = OcrRequest.builder()
                .invoiceType(InvoiceType.VAT_INVOICE)
                .imageData("https://example.com/vat.jpg")
                .imageType("url")
                .build();

        // When
        OcrResponse response = client.recognize(request);

        // Then
        assertNotNull(response.getFields());
        assertTrue(response.getFields().containsKey("invoiceNo"));
        assertTrue(response.getFields().containsKey("totalAmount"));
        assertTrue(response.getFields().containsKey("sellerName"));
    }
}
