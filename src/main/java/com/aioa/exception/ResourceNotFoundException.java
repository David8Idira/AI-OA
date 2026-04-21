package com.aioa.exception;

/**
 * 资源未找到异常
 */
public class ResourceNotFoundException extends BusinessException {
    
    public ResourceNotFoundException(String resource, Object id) {
        super("NOT_FOUND", String.format("%s not found: %s", resource, id));
    }

    public ResourceNotFoundException(String resource, String field, String value) {
        super("NOT_FOUND", String.format("%s not found with %s: %s", resource, field, value));
    }
    
    public ResourceNotFoundException(String message) {
        super("NOT_FOUND", message);
    }
}
