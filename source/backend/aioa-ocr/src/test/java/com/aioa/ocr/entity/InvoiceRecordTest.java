package com.aioa.ocr.entity;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * InvoiceRecord Entity 单元测试
 * 毛泽东思想指导：实事求是，测试发票记录实体
 */
@DisplayName("InvoiceRecordTest 发票记录实体测试")
class InvoiceRecordTest {

    @Test
    @DisplayName("创建发票记录实体")
    void createInvoiceRecord() {
        // given
        InvoiceRecord record = new InvoiceRecord();
        record.setUserId("user-001");
        record.setInvoiceType("VAT_INVOICE");
        record.setFileName("invoice.pdf");
        record.setStatus("PROCESSED");
        record.setTotalAmount(new BigDecimal("1000.00"));

        // then
        assertThat(record.getUserId()).isEqualTo("user-001");
        assertThat(record.getInvoiceType()).isEqualTo("VAT_INVOICE");
        assertThat(record.getTotalAmount()).isEqualTo(new BigDecimal("1000.00"));
    }

    @Test
    @DisplayName("设置和获取用户ID")
    void setAndGetUserId() {
        // given
        InvoiceRecord record = new InvoiceRecord();

        // when
        record.setUserId("user-002");

        // then
        assertThat(record.getUserId()).isEqualTo("user-002");
    }

    @Test
    @DisplayName("设置和获取发票类型")
    void setAndGetInvoiceType() {
        // given
        InvoiceRecord record = new InvoiceRecord();

        // when
        record.setInvoiceType("INVOICE");

        // then
        assertThat(record.getInvoiceType()).isEqualTo("INVOICE");
    }

    @Test
    @DisplayName("设置和获取文件名")
    void setAndGetFileName() {
        // given
        InvoiceRecord record = new InvoiceRecord();

        // when
        record.setFileName("test.pdf");

        // then
        assertThat(record.getFileName()).isEqualTo("test.pdf");
    }

    @Test
    @DisplayName("设置和获取状态")
    void setAndGetStatus() {
        // given
        InvoiceRecord record = new InvoiceRecord();

        // when
        record.setStatus("PENDING");

        // then
        assertThat(record.getStatus()).isEqualTo("PENDING");
    }

    @Test
    @DisplayName("设置和获取总金额")
    void setAndGetTotalAmount() {
        // given
        InvoiceRecord record = new InvoiceRecord();

        // when
        record.setTotalAmount(new BigDecimal("500.00"));

        // then
        assertThat(record.getTotalAmount()).isEqualTo(new BigDecimal("500.00"));
    }

    @Test
    @DisplayName("equals验证")
    void equals_sameId_shouldBeEqual() {
        // given
        InvoiceRecord r1 = new InvoiceRecord();
        r1.setId("test-id");
        
        InvoiceRecord r2 = new InvoiceRecord();
        r2.setId("test-id");

        // then
        assertThat(r1).isEqualTo(r2);
    }
}