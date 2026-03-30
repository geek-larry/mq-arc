package com.license.common.exception;

/**
 * 许可服务异常
 */
public class LicenseServiceException extends LicenseException {

    public LicenseServiceException(String message) {
        super("LICENSE_SERVICE_ERROR", message);
    }

    public LicenseServiceException(String message, Throwable cause) {
        super("LICENSE_SERVICE_ERROR", message, cause);
    }

    public LicenseServiceException(String errorCode, String message) {
        super(errorCode, message);
    }

    public LicenseServiceException(String errorCode, String message, Throwable cause) {
        super(errorCode, message, cause);
    }
}
