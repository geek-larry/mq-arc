package com.license.common.exception;

/**
 * 消息处理异常
 */
public class MessageProcessException extends RuntimeException {

    public MessageProcessException(String message) {
        super(message);
    }

    public MessageProcessException(String message, Throwable cause) {
        super(message, cause);
    }
}
