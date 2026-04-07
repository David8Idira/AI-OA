package com.aioa.reimburse.enums;

import lombok.Getter;

/**
 * Expense Item Type Enum
 */
@Getter
public enum ExpenseTypeEnum {

    TRANSPORT("TRANSPORT", "交通费", "机票、火车票、汽车票、出租车费等"),
    ACCOMMODATION("ACCOMMODATION", "住宿费", "酒店住宿费用"),
    MEAL("MEAL", "餐饮费", "因公用餐费用"),
    COMMUNICATION("COMMUNICATION", "通讯费", "手机费、网络费等"),
    ENTERTAINMENT("ENTERTAINMENT", "招待费", "客户招待费用"),
    MATERIAL("MATERIAL", "办公耗材", "办公用品、文具等"),
    TRAINING("TRAINING", "培训费", "培训、会议费用"),
    MARKETING("MARKETING", "市场推广费", "市场活动费用"),
    POSTAGE("POSTAGE", "邮寄费", "快递、邮寄费用"),
    OTHER("OTHER", "其他费用", "其他杂项费用");

    private final String code;
    private final String desc;
    private final String remark;

    ExpenseTypeEnum(String code, String desc, String remark) {
        this.code = code;
        this.desc = desc;
        this.remark = remark;
    }

    public static ExpenseTypeEnum getByCode(String code) {
        if (code == null) {
            return null;
        }
        for (ExpenseTypeEnum e : values()) {
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
