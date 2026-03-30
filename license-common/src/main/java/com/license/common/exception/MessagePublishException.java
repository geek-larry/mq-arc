package com.license.common.exception;

/**
 * 消息发布异常
 */
public class MessagePublishException extends LicenseException {

    public MessagePublishException(String message) {
        super("MSG_PUBLISH_ERROR", message);
    }

    public MessagePublishException(String message, Throwable cause) {
        super("MSG_PUBLISH_ERROR", message, cause);
    }
}
