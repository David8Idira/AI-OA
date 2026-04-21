package com.aioa.report.enums;

import lombok.Getter;

/**
 * Report status enumeration
 */
@Getter
public enum ReportStatusEnum {

    DRAFT(0, "草稿"),
    GENERATING(1, "生成中"),
    GENERATED(2, "已生成"),
    FAILED(3, "生成失败"),
    ARCHIVED(4, "已归档");

    private final Integer code;
    private final String description;

    ReportStatusEnum(Integer code, String description) {
        this.code = code;
        this.description = description;
    }

    public static ReportStatusEnum fromCode(Integer code) {
        if (code == null) {
            return null;
        }
        for (ReportStatusEnum status : values()) {
            if (status.code.equals(code)) {
                return status;
            }
        }
        return null;
    }
}
