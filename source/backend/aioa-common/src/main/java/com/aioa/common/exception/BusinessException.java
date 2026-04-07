package com.aioa.common.exception;

import com.aioa.common.result.ResultCode;
import lombok.Getter;

/**
 * Business Exception
 */
@Getter
public class BusinessException extends RuntimeException {
    
    private final ResultCode resultCode;
    
    public BusinessException(ResultCode resultCode) {
        super(resultCode.getMessage());
        this.resultCode = resultCode;
    }
    
    public BusinessException(ResultCode resultCode, String message) {
        super(message);
        this.resultCode = resultCode;
    }
    
    public BusinessException(Integer code, String message) {
        super(message);
        this.resultCode = ResultCode.error(code, message);
    }
}
