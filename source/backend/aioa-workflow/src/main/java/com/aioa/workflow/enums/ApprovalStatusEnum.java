package com.aioa.workflow.enums;

import lombok.Getter;

/**
 * Approval Status Enum
 */
@Getter
public enum ApprovalStatusEnum {

    /**
     * Pending approval
     */
    PENDING(0, "待审批"),

    /**
     * Approved
     */
    APPROVED(1, "已同意"),

    /**
     * Rejected
     */
    REJECTED(2, "已驳回"),

    /**
     * Cancelled
     */
    CANCELLED(3, "已撤回"),

    /**
     * Transferred
     */
    TRANSFERRED(4, "已转交");

    private final Integer code;
    private final String description;

    ApprovalStatusEnum(Integer code, String description) {
        this.code = code;
        this.description = description;
    }

    /**
     * Get enum by code
     */
    public static ApprovalStatusEnum getByCode(Integer code) {
        if (code == null) {
            return null;
        }
        for (ApprovalStatusEnum status : values()) {
            if (status.code.equals(code)) {
                return status;
            }
        }
        return null;
    }
}
