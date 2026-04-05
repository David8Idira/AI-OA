package com.aioa.common.result;

import lombok.Getter;
import lombok.Setter;

/**
 * Result Code Enum
 */
@Getter
public enum ResultCode {
    SUCCESS(200, "操作成功"),
    
    // 4xx - Client errors
    BAD_REQUEST(400, "请求参数错误"),
    UNAUTHORIZED(401, "未授权，请登录"),
    FORBIDDEN(403, "无权限访问"),
    NOT_FOUND(404, "资源不存在"),
    METHOD_NOT_ALLOWED(405, "请求方法不支持"),
    
    // 5xx - Server errors
    INTERNAL_SERVER_ERROR(500, "服务器内部错误"),
    SERVICE_UNAVAILABLE(503, "服务不可用"),
    
    // Business errors (1xxxx)
    USERNAME_PASSWORD_ERROR(10001, "用户名或密码错误"),
    TOKEN_EXPIRED(10002, "Token已过期"),
    TOKEN_INVALID(10003, "Token无效"),
    ACCOUNT_DISABLED(10004, "账户已被禁用"),
    
    // User errors (2xxxx)
    USER_NOT_FOUND(20001, "用户不存在"),
    USER_ALREADY_EXISTS(20002, "用户已存在"),
    
    // Approval errors (3xxxx)
    APPROVAL_NOT_FOUND(30001, "审批不存在"),
    APPROVAL_PERMISSION_DENIED(30002, "无权审批此单"),
    APPROVAL_STATUS_ERROR(30003, "审批状态不允许操作"),
    
    // AI errors (4xxxx)
    AI_SERVICE_UNAVAILABLE(40001, "AI服务不可用"),
    AI_MODEL_QUOTA_EXCEEDED(40002, "AI模型配额不足"),
    AI_REQUEST_TIMEOUT(40003, "AI请求超时"),
    
    // File errors (5xxxx)
    FILE_UPLOAD_ERROR(50001, "文件上传失败"),
    FILE_SIZE_EXCEEDED(50002, "文件大小超限"),
    FILE_TYPE_NOT_SUPPORTED(50003, "文件类型不支持");

    @Setter
    private Integer code;
    @Setter
    private String message;

    ResultCode(Integer code, String message) {
        this.code = code;
        this.message = message;
    }
}
