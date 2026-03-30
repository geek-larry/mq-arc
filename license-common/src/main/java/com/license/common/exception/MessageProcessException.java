package com.license.common.exception;

/**
 * 消息处理异常
 */
public class MessageProcessException extends LicenseException {

    public MessageProcessException(String message) {
        super("MSG_PROCESS_ERROR", message);
    }

    public MessageProcessException(String message, Throwable cause) {
        super("MSG_PROCESS_ERROR", message, cause);
    }

    public MessageProcessException(String errorCode, String message) {
        super(errorCode, message);
    }

    public MessageProcessException(String errorCode, String message, Throwable cause) {
        super(errorCode, message, cause);
    }
}
