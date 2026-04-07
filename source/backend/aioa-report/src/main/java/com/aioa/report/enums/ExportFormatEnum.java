package com.aioa.report.enums;

import lombok.Getter;

/**
 * Export format enumeration
 */
@Getter
public enum ExportFormatEnum {

    PDF("PDF", "PDF文件"),
    EXCEL("EXCEL", "Excel文件"),
    HTML("HTML", "HTML文件");

    private final String code;
    private final String description;

    ExportFormatEnum(String code, String description) {
        this.code = code;
        this.description = description;
    }

    public static ExportFormatEnum fromCode(String code) {
        if (code == null) {
            return null;
        }
        for (ExportFormatEnum format : values()) {
            if (format.code.equals(code)) {
                return format;
            }
        }
        return null;
    }
}
