package com.aioa.workflow.enums;

import lombok.Getter;

/**
 * Approval Priority Enum
 */
@Getter
public enum ApprovalPriorityEnum {

    /**
     * Low priority
     */
    LOW(0, "低"),

    /**
     * Normal priority
     */
    NORMAL(1, "普通"),

    /**
     * High priority
     */
    HIGH(2, "高"),

    /**
     * Urgent priority
     */
    URGENT(3, "紧急");

    private final Integer code;
    private final String description;

    ApprovalPriorityEnum(Integer code, String description) {
        this.code = code;
        this.description = description;
    }

    /**
     * Get enum by code
     */
    public static ApprovalPriorityEnum getByCode(Integer code) {
        if (code == null) {
            return null;
        }
        for (ApprovalPriorityEnum priority : values()) {
            if (priority.code.equals(code)) {
                return priority;
            }
        }
        return null;
    }
}
