package com.aioa.report.enums;

import lombok.Getter;

/**
 * Report type enumeration
 */
@Getter
public enum ReportTypeEnum {

    DAILY("DAILY", "日报"),
    WEEKLY("WEEKLY", "周报"),
    MONTHLY("MONTHLY", "月报"),
    QUARTERLY("QUARTERLY", "季报"),
    ANNUAL("ANNUAL", "年报"),
    CUSTOM("CUSTOM", "自定义报表");

    private final String code;
    private final String description;

    ReportTypeEnum(String code, String description) {
        this.code = code;
        this.description = description;
    }

    public static ReportTypeEnum fromCode(String code) {
        if (code == null) {
            return null;
        }
        for (ReportTypeEnum type : values()) {
            if (type.code.equals(code)) {
                return type;
            }
        }
        return null;
    }
}
