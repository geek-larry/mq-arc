package com.license.common.exception;

/**
 * 许可管理基础异常
 */
public class LicenseException extends RuntimeException {

    private final String errorCode;

    public LicenseException(String errorCode, String message) {
        super(message);
        this.errorCode = errorCode;
    }

    public LicenseException(String errorCode, String message, Throwable cause) {
        super(message, cause);
        this.errorCode = errorCode;
    }

    public String getErrorCode() {
        return errorCode;
    }
}
