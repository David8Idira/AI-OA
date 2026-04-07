package com.aioa.reimburse.enums;

import lombok.Getter;

/**
 * Reimburse Type Enum
 */
@Getter
public enum ReimburseTypeEnum {

    BUSINESS_TRIP("BUSINESS_TRIP", "差旅报销"),
    DAILY("DAILY", "日常报销"),
    COMMUNICATION("COMMUNICATION", "通讯报销"),
    ENTERTAINMENT("ENTERTAINMENT", "招待报销"),
    PURCHASE("PURCHASE", "采购报销"),
    OTHER("OTHER", "其他报销");

    private final String code;
    private final String desc;

    ReimburseTypeEnum(String code, String desc) {
        this.code = code;
        this.desc = desc;
    }

    public static ReimburseTypeEnum getByCode(String code) {
        if (code == null) {
            return null;
        }
        for (ReimburseTypeEnum e : values()) {
            if (e.getCode().equals(code)) {
                return e;
            }
        }
        return null;
    }

    public static boolean isValidCode(String code) {
        return getByCode(code) != null;
    }
}
