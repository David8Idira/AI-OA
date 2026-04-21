package com.aioa.reimburse.enums;

import lombok.Getter;

/**
 * Reimburse Action Enum
 */
@Getter
public enum ReimburseActionEnum {

    APPROVE("APPROVE", "审批通过"),
    REJECT("REJECT", "审批驳回"),
    CANCEL("CANCEL", "撤回申请"),
    REQUEST_EXTRA("REQUEST_EXTRA", "补充材料");

    private final String code;
    private final String desc;

    ReimburseActionEnum(String code, String desc) {
        this.code = code;
        this.desc = desc;
    }

    public static ReimburseActionEnum getByCode(String code) {
        if (code == null) {
            return null;
        }
        for (ReimburseActionEnum e : values()) {
            if (e.getCode().equals(code)) {
                return e;
            }
        }
        return null;
    }
}
