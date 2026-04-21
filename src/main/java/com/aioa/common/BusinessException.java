package com.aioa.common;

import org.springframework.http.HttpStatus;

public class BusinessException extends RuntimeException {
    private final HttpStatus status;
    private final int code;

    public BusinessException(String message) {
        super(message);
        this.status = HttpStatus.INTERNAL_SERVER_ERROR;
        this.code = 500;
    }

    public BusinessException(int code, String message) {
        super(message);
        this.status = HttpStatus.INTERNAL_SERVER_ERROR;
        this.code = code;
    }

    public BusinessException(HttpStatus status, String message) {
        super(message);
        this.status = status;
        this.code = status.value();
    }

    public BusinessException(int code, String message, Throwable cause) {
        super(message, cause);
        this.status = HttpStatus.INTERNAL_SERVER_ERROR;
        this.code = code;
    }

    public int getCode() { return code; }
    public HttpStatus getStatus() { return status; }
}
