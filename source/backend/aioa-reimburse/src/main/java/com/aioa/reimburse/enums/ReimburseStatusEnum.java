package com.aioa.reimburse.enums;

import lombok.Getter;

/**
 * Reimburse Status Enum
 */
@Getter
public enum ReimburseStatusEnum {

    DRAFT(0, "草稿"),
    PENDING(1, "待审批"),
    APPROVED(2, "已审批"),
    REJECTED(3, "已驳回"),
    CANCELLED(4, "已撤回"),
    PAID(5, "已打款");

    private final Integer code;
    private final String desc;

    ReimburseStatusEnum(Integer code, String desc) {
        this.code = code;
        this.desc = desc;
    }

    public static ReimburseStatusEnum getByCode(Integer code) {
        if (code == null) {
            return null;
        }
        for (ReimburseStatusEnum e : values()) {
            if (e.getCode().equals(code)) {
                return e;
            }
        }
        return null;
    }

    public static boolean isValidCode(Integer code) {
        return getByCode(code) != null;
    }
}
