package com.license.common.enums;

import lombok.Getter;

/**
 * 消息类型枚举
 * 用于根据messageType路由到对应的handler
 */
@Getter
public enum MessageType {

    USER_OPERATION("user_operation", "用户操作", "com.license.client.handler.impl.UserOperationHandler");

    private final String code;
    private final String description;
    private final String handlerClassName;

    MessageType(String code, String description, String handlerClassName) {
        this.code = code;
        this.description = description;
        this.handlerClassName = handlerClassName;
    }

    /**
     * 根据code查找MessageType
     */
    public static MessageType fromCode(String code) {
        if (code == null) {
            return null;
        }
        
        for (MessageType type : values()) {
            if (type.code.equalsIgnoreCase(code)) {
                return type;
            }
        }
        return null;
    }

    /**
     * 获取handler类名
     */
    public String getHandlerClassName() {
        return handlerClassName;
    }

    /**
     * 检查是否匹配
     */
    public boolean matches(String code) {
        return this.code.equalsIgnoreCase(code);
    }
}
