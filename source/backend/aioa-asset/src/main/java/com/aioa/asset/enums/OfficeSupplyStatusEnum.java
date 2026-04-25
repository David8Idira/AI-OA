package com.aioa.asset.enums;

import lombok.Getter;

/**
 * 办公用品状态枚举
 */
@Getter
public enum OfficeSupplyStatusEnum {
    
    // 申请单状态
    REQUEST_DRAFT(0, "草稿"),
    REQUEST_PENDING_APPROVAL(1, "待审批"),
    REQUEST_APPROVED(2, "审批通过"),
    REQUEST_REJECTED(3, "审批拒绝"),
    REQUEST_PARTIAL_CLAIMED(4, "部分领取"),
    REQUEST_FULLY_CLAIMED(5, "已全部领取"),
    REQUEST_CANCELLED(6, "已取消"),
    
    // 库存检查状态
    INVENTORY_UNCHECKED(0, "未检查"),
    INVENTORY_SUFFICIENT(1, "库存充足"),
    INVENTORY_INSUFFICIENT(2, "库存不足"),
    INVENTORY_QUEUED(3, "自动排队"),
    
    // 领用方式
    CLAIM_METHOD_QR_CODE(1, "扫码领用"),
    CLAIM_METHOD_MANUAL(2, "手动领用"),
    
    // 签收状态
    SIGN_PENDING(0, "待签收"),
    SIGN_COMPLETED(1, "已签收"),
    SIGN_CANCELLED(2, "已取消"),
    
    // 领用类型
    CLAIM_TYPE_ONCE(1, "一次性领取"),
    CLAIM_TYPE_BATCH(2, "分批领取"),
    
    // 紧急程度
    URGENCY_NORMAL(1, "普通"),
    URGENCY_URGENT(2, "加急"),
    URGENCY_EMERGENCY(3, "紧急");
    
    private final Integer code;
    private final String description;
    
    OfficeSupplyStatusEnum(Integer code, String description) {
        this.code = code;
        this.description = description;
    }
    
    /**
     * 根据code获取枚举
     */
    public static OfficeSupplyStatusEnum getByCode(Integer code) {
        if (code == null) {
            return null;
        }
        for (OfficeSupplyStatusEnum status : values()) {
            if (status.code.equals(code)) {
                return status;
            }
        }
        return null;
    }
    
    /**
     * 根据code获取描述
     */
    public static String getDescriptionByCode(Integer code) {
        OfficeSupplyStatusEnum status = getByCode(code);
        return status != null ? status.description : "未知";
    }
    
    /**
     * 判断是否为有效的申请单状态
     */
    public static boolean isValidRequestStatus(Integer code) {
        if (code == null) {
            return false;
        }
        return code >= REQUEST_DRAFT.code && code <= REQUEST_CANCELLED.code;
    }
    
    /**
     * 判断申请单是否可审批
     */
    public static boolean isApprovable(Integer statusCode) {
        return REQUEST_PENDING_APPROVAL.code.equals(statusCode);
    }
    
    /**
     * 判断申请单是否可领用
     */
    public static boolean isClaimable(Integer statusCode) {
        return REQUEST_APPROVED.code.equals(statusCode) || 
               REQUEST_PARTIAL_CLAIMED.code.equals(statusCode);
    }
    
    /**
     * 判断申请单是否可取消
     */
    public static boolean isCancellable(Integer statusCode) {
        return REQUEST_DRAFT.code.equals(statusCode) || 
               REQUEST_PENDING_APPROVAL.code.equals(statusCode);
    }
}