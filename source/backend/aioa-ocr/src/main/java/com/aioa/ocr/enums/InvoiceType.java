package com.aioa.ocr.enums;

import lombok.Getter;

/**
 * Invoice Type Enum
 * Defines the supported invoice types for OCR recognition
 */
@Getter
public enum InvoiceType {

    /**
     * VAT Invoice (增值税发票)
     */
    VAT_INVOICE("vat_invoice", "增值税发票", "支持增值税专用发票和普通发票的识别"),

    /**
     * Taxi Receipt (出租车票)
     */
    TAXI_RECEIPT("taxi_receipt", "出租车票", "支持出租车发票的识别"),

    /**
     * Train Ticket (火车票)
     */
    TRAIN_TICKET("train_ticket", "火车票", "支持火车票的识别"),

    /**
     * Air Ticket (机票)
     */
    AIR_TICKET("air_ticket", "机票", "支持机票的识别");

    private final String code;
    private final String name;
    private final String description;

    InvoiceType(String code, String name, String description) {
        this.code = code;
        this.name = name;
        this.description = description;
    }

    /**
     * Get InvoiceType by code
     *
     * @param code invoice type code
     * @return InvoiceType or null if not found
     */
    public static InvoiceType getByCode(String code) {
        if (code == null) {
            return null;
        }
        for (InvoiceType type : values()) {
            if (type.getCode().equals(code)) {
                return type;
            }
        }
        return null;
    }

    /**
     * Check if the code is valid
     *
     * @param code invoice type code
     * @return true if valid, false otherwise
     */
    public static boolean isValidCode(String code) {
        return getByCode(code) != null;
    }
}
