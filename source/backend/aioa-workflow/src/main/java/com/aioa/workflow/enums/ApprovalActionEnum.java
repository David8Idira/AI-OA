package com.aioa.workflow.enums;

import lombok.Getter;

/**
 * Approval Action Type Enum
 */
@Getter
public enum ApprovalActionEnum {

    /**
     * Approve (agree)
     */
    APPROVE(1, "同意"),

    /**
     * Reject
     */
    REJECT(2, "驳回"),

    /**
     * Transfer to another approver
     */
    TRANSFER(3, "转交"),

    /**
     * Cancel (withdraw) by applicant
     */
    CANCEL(4, "撤回");

    private final Integer code;
    private final String description;

    ApprovalActionEnum(Integer code, String description) {
        this.code = code;
        this.description = description;
    }

    /**
     * Get enum by code
     */
    public static ApprovalActionEnum getByCode(Integer code) {
        if (code == null) {
            return null;
        }
        for (ApprovalActionEnum action : values()) {
            if (action.code.equals(code)) {
                return action;
            }
        }
        return null;
    }
}
