package com.aioa.report.enums;

import lombok.Getter;

/**
 * Chart type enumeration
 */
@Getter
public enum ChartTypeEnum {

    BAR("BAR", "柱状图"),
    LINE("LINE", "折线图"),
    PIE("PIE", "饼图"),
    AREA("AREA", "面积图"),
    SCATTER("SCATTER", "散点图"),
    TABLE("TABLE", "数据表格"),
    TEXT("TEXT", "文本描述");

    private final String code;
    private final String description;

    ChartTypeEnum(String code, String description) {
        this.code = code;
        this.description = description;
    }

    public static ChartTypeEnum fromCode(String code) {
        if (code == null) {
            return null;
        }
        for (ChartTypeEnum type : values()) {
            if (type.code.equals(code)) {
                return type;
            }
        }
        return null;
    }
}
