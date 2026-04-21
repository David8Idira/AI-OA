package com.aioa.exception;

/**
 * 资源冲突异常
 */
public class ResourceConflictException extends BusinessException {
    
    public ResourceConflictException(String message) {
        super("CONFLICT", message);
    }
    
    public ResourceConflictException(String resource, String field, String value) {
        super("CONFLICT", String.format("%s with %s '%s' already exists", resource, field, value));
    }
}