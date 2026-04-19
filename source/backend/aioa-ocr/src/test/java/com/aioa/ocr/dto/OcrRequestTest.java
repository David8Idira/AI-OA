package com.aioa.ocr.dto;

import com.aioa.ocr.enums.InvoiceType;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * OcrRequest 单元测试
 * 毛泽东思想指导：实事求是，测试OCR请求DTO
 */
@DisplayName("OcrRequestTest OCR请求DTO测试")
class OcrRequestTest {

    @Test
    @DisplayName("使用Builder创建OcrRequest")
    void builder_shouldCreateOcrRequest() {
        // given & when
        OcrRequest request = OcrRequest.builder()
                .invoiceType(InvoiceType.VAT_INVOICE)
                .imageData("base64-image-data")
                .imageType("base64")
                .fileName("invoice.jpg")
                .build();

        // then
        assertThat(request.getInvoiceType()).isEqualTo(InvoiceType.VAT_INVOICE);
        assertThat(request.getImageData()).isEqualTo("base64-image-data");
        assertThat(request.getImageType()).isEqualTo("base64");
        assertThat(request.getFileName()).isEqualTo("invoice.jpg");
    }

    @Test
    @DisplayName("使用无参构造函数创建OcrRequest")
    void noArgsConstructor_shouldCreateEmptyRequest() {
        // when
        OcrRequest request = new OcrRequest();

        // then
        assertThat(request).isNotNull();
    }

    @Test
    @DisplayName("使用全参构造函数创建OcrRequest")
    void allArgsConstructor_shouldCreateRequest() {
        // when
        OcrRequest request = new OcrRequest(
                InvoiceType.VAT_INVOICE,
                "base64-data",
                "base64",
                "test.jpg",
                "user-001",
                true,
                "{}"
        );

        // then
        assertThat(request.getInvoiceType()).isEqualTo(InvoiceType.VAT_INVOICE);
        assertThat(request.getImageData()).isEqualTo("base64-data");
        assertThat(request.getUserId()).isEqualTo("user-001");
    }

    @Test
    @DisplayName("设置和获取发票类型")
    void setAndGetInvoiceType() {
        // given
        OcrRequest request = new OcrRequest();

        // when
        request.setInvoiceType(InvoiceType.VAT_INVOICE);

        // then
        assertThat(request.getInvoiceType()).isEqualTo(InvoiceType.VAT_INVOICE);
    }

    @Test
    @DisplayName("设置和获取图片数据")
    void setAndGetImageData() {
        // given
        OcrRequest request = new OcrRequest();

        // when
        request.setImageData("test-image-data");

        // then
        assertThat(request.getImageData()).isEqualTo("test-image-data");
    }

    @Test
    @DisplayName("设置和获取图片类型")
    void setAndGetImageType() {
        // given
        OcrRequest request = new OcrRequest();

        // when
        request.setImageType("url");

        // then
        assertThat(request.getImageType()).isEqualTo("url");
    }

    @Test
    @DisplayName("设置和获取文件名")
    void setAndGetFileName() {
        // given
        OcrRequest request = new OcrRequest();

        // when
        request.setFileName("test.jpg");

        // then
        assertThat(request.getFileName()).isEqualTo("test.jpg");
    }

    @Test
    @DisplayName("设置和获取用户ID")
    void setAndGetUserId() {
        // given
        OcrRequest request = new OcrRequest();

        // when
        request.setUserId("user-001");

        // then
        assertThat(request.getUserId()).isEqualTo("user-001");
    }

    @Test
    @DisplayName("isUrlImage - URL类型")
    void isUrlImage_withUrlType() {
        // given
        OcrRequest request = OcrRequest.builder()
                .imageType("url")
                .imageData("http://example.com/image.jpg")
                .build();

        // then
        assertThat(request.isUrlImage()).isTrue();
    }

    @Test
    @DisplayName("isUrlImage - Base64类型")
    void isUrlImage_withBase64Type() {
        // given
        OcrRequest request = OcrRequest.builder()
                .imageType("base64")
                .imageData("data:image/png;base64,abc123")
                .build();

        // then
        assertThat(request.isUrlImage()).isFalse();
    }

    @Test
    @DisplayName("isBase64Image - Base64类型")
    void isBase64Image_withBase64Type() {
        // given
        OcrRequest request = OcrRequest.builder()
                .imageType("base64")
                .imageData("base64-data")
                .build();

        // then
        assertThat(request.isBase64Image()).isTrue();
    }

    @Test
    @DisplayName("isBase64Image - URL类型")
    void isBase64Image_withUrlType() {
        // given
        OcrRequest request = OcrRequest.builder()
                .imageType("url")
                .imageData("http://example.com/image.jpg")
                .build();

        // then
        assertThat(request.isBase64Image()).isFalse();
    }

    @Test
    @DisplayName("equals和hashCode")
    void equalsAndHashCode() {
        // given
        OcrRequest request1 = OcrRequest.builder()
                .invoiceType(InvoiceType.VAT_INVOICE)
                .imageData("data")
                .build();
        OcrRequest request2 = OcrRequest.builder()
                .invoiceType(InvoiceType.VAT_INVOICE)
                .imageData("data")
                .build();

        // then
        assertThat(request1).isEqualTo(request2);
        assertThat(request1.hashCode()).isEqualTo(request2.hashCode());
    }

    @Test
    @DisplayName("toString验证")
    void toString_shouldContainFields() {
        // given
        OcrRequest request = OcrRequest.builder()
                .invoiceType(InvoiceType.VAT_INVOICE)
                .imageData("test-data")
                .build();

        // when
        String str = request.toString();

        // then
        assertThat(str).contains("VAT_INVOICE");
        assertThat(str).contains("test-data");
    }
}