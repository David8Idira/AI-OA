package com.aioa.ocr.enums;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.junit.jupiter.params.provider.ValueSource;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for InvoiceType enum
 */
class InvoiceTypeTest {

    @Test
    @DisplayName("Should have correct number of invoice types")
    void testInvoiceTypeCount() {
        assertEquals(4, InvoiceType.values().length);
    }

    @ParameterizedTest
    @EnumSource(InvoiceType.class)
    @DisplayName("Each invoice type should have non-null code, name, and description")
    void testInvoiceTypeFields(InvoiceType type) {
        assertNotNull(type.getCode());
        assertFalse(type.getCode().isEmpty());
        assertNotNull(type.getName());
        assertFalse(type.getName().isEmpty());
        assertNotNull(type.getDescription());
        assertFalse(type.getDescription().isEmpty());
    }

    @Test
    @DisplayName("VAT_INVOICE should have correct code")
    void testVatInvoiceCode() {
        assertEquals("vat_invoice", InvoiceType.VAT_INVOICE.getCode());
        assertEquals("增值税发票", InvoiceType.VAT_INVOICE.getName());
    }

    @Test
    @DisplayName("TAXI_RECEIPT should have correct code")
    void testTaxiReceiptCode() {
        assertEquals("taxi_receipt", InvoiceType.TAXI_RECEIPT.getCode());
        assertEquals("出租车票", InvoiceType.TAXI_RECEIPT.getName());
    }

    @Test
    @DisplayName("TRAIN_TICKET should have correct code")
    void testTrainTicketCode() {
        assertEquals("train_ticket", InvoiceType.TRAIN_TICKET.getCode());
        assertEquals("火车票", InvoiceType.TRAIN_TICKET.getName());
    }

    @Test
    @DisplayName("AIR_TICKET should have correct code")
    void testAirTicketCode() {
        assertEquals("air_ticket", InvoiceType.AIR_TICKET.getCode());
        assertEquals("机票", InvoiceType.AIR_TICKET.getName());
    }

    @ParameterizedTest
    @ValueSource(strings = {"vat_invoice", "taxi_receipt", "train_ticket", "air_ticket"})
    @DisplayName("getByCode should return correct enum for valid codes")
    void testGetByCodeValid(String code) {
        InvoiceType type = InvoiceType.getByCode(code);
        assertNotNull(type);
        assertEquals(code, type.getCode());
    }

    @ParameterizedTest
    @ValueSource(strings = {"invalid", "VAT", "receipt", ""})
    @DisplayName("getByCode should return null for invalid codes")
    void testGetByCodeInvalid(String code) {
        InvoiceType type = InvoiceType.getByCode(code);
        assertNull(type);
    }

    @Test
    @DisplayName("getByCode should return null for null input")
    void testGetByCodeNull() {
        assertNull(InvoiceType.getByCode(null));
    }

    @ParameterizedTest
    @ValueSource(strings = {"vat_invoice", "taxi_receipt", "train_ticket", "air_ticket"})
    @DisplayName("isValidCode should return true for valid codes")
    void testIsValidCodeTrue(String code) {
        assertTrue(InvoiceType.isValidCode(code));
    }

    @ParameterizedTest
    @ValueSource(strings = {"invalid", "VAT", "receipt", "", "   "})
    @DisplayName("isValidCode should return false for invalid codes")
    void testIsValidCodeFalse(String code) {
        assertFalse(InvoiceType.isValidCode(code));
    }

    @Test
    @DisplayName("isValidCode should return false for null")
    void testIsValidCodeNull() {
        assertFalse(InvoiceType.isValidCode(null));
    }
}
