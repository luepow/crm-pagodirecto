package com.pagodirecto.core.exception;

import lombok.Getter;

/**
 * Base exception for business rule violations.
 * Used throughout the application to signal domain-specific errors.
 */
@Getter
public class BusinessException extends RuntimeException {

    private final String errorCode;
    private final Object[] args;

    public BusinessException(String message) {
        super(message);
        this.errorCode = "BUSINESS_ERROR";
        this.args = null;
    }

    public BusinessException(String errorCode, String message) {
        super(message);
        this.errorCode = errorCode;
        this.args = null;
    }

    public BusinessException(String errorCode, String message, Object... args) {
        super(message);
        this.errorCode = errorCode;
        this.args = args;
    }

    public BusinessException(String message, Throwable cause) {
        super(message, cause);
        this.errorCode = "BUSINESS_ERROR";
        this.args = null;
    }
}
