package com.aioa.common.result;

import lombok.Getter;
import lombok.Setter;
import java.io.Serializable;

/**
 * Result Code - represents API result codes
 * Uses a class instead of enum to allow dynamic error codes
 */
@Getter
@Setter
public class ResultCode implements Serializable {
    private static final long serialVersionUID = 1L;

    private Integer code;
    private String message;

    private ResultCode() {}

    public ResultCode(Integer code, String message) {
        this.code = code;
        this.message = message;
    }

    // ==================== Static Predefined Codes ====================

    public static final ResultCode SUCCESS = new ResultCode(200, "操作成功");
    
    // 4xx - Client errors
    public static final ResultCode BAD_REQUEST = new ResultCode(400, "请求参数错误");
    public static final ResultCode UNAUTHORIZED = new ResultCode(401, "未授权，请登录");
    public static final ResultCode FORBIDDEN = new ResultCode(403, "无权限访问");
    public static final ResultCode NOT_FOUND = new ResultCode(404, "资源不存在");
    public static final ResultCode METHOD_NOT_ALLOWED = new ResultCode(405, "请求方法不支持");
    
    // 5xx - Server errors
    public static final ResultCode INTERNAL_SERVER_ERROR = new ResultCode(500, "服务器内部错误");
    public static final ResultCode SERVICE_UNAVAILABLE = new ResultCode(503, "服务不可用");
    
    // Business errors (1xxxx)
    public static final ResultCode USERNAME_PASSWORD_ERROR = new ResultCode(10001, "用户名或密码错误");
    public static final ResultCode TOKEN_EXPIRED = new ResultCode(10002, "Token已过期");
    public static final ResultCode TOKEN_INVALID = new ResultCode(10003, "Token无效");
    public static final ResultCode ACCOUNT_DISABLED = new ResultCode(10004, "账户已被禁用");
    
    // User errors (2xxxx)
    public static final ResultCode USER_NOT_FOUND = new ResultCode(20001, "用户不存在");
    public static final ResultCode USER_ALREADY_EXISTS = new ResultCode(20002, "用户已存在");
    
    // Approval errors (3xxxx)
    public static final ResultCode APPROVAL_NOT_FOUND = new ResultCode(30001, "审批不存在");
    public static final ResultCode APPROVAL_PERMISSION_DENIED = new ResultCode(30002, "无权审批此单");
    public static final ResultCode APPROVAL_STATUS_ERROR = new ResultCode(30003, "审批状态不允许操作");
    
    // AI errors (4xxxx)
    public static final ResultCode AI_SERVICE_UNAVAILABLE = new ResultCode(40001, "AI服务不可用");
    public static final ResultCode AI_MODEL_QUOTA_EXCEEDED = new ResultCode(40002, "AI模型配额不足");
    public static final ResultCode AI_REQUEST_TIMEOUT = new ResultCode(40003, "AI请求超时");
    
    // File errors (5xxxx)
    public static final ResultCode FILE_UPLOAD_ERROR = new ResultCode(50001, "文件上传失败");
    public static final ResultCode FILE_SIZE_EXCEEDED = new ResultCode(50002, "文件大小超限");
    public static final ResultCode FILE_TYPE_NOT_SUPPORTED = new ResultCode(50003, "文件类型不支持");

    /**
     * Create a dynamic error code with custom code and message.
     */
    public static ResultCode error(Integer code, String message) {
        return new ResultCode(code, message);
    }

    /**
     * Create a dynamic error code with custom code and message (alias for error).
     */
    public static ResultCode create(int code, String message) {
        return new ResultCode(code, message);
    }
}
