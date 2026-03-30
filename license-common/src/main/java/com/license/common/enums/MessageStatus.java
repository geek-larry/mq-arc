package com.license.common.enums;

import lombok.Getter;

/**
 * 消息状态枚举
 */
@Getter
public enum MessageStatus {
    PENDING("PENDING", "待处理"),
    PROCESSING("PROCESSING", "处理中"),
    SUCCESS("SUCCESS", "成功"),
    FAILED("FAILED", "失败"),
    TIMEOUT("TIMEOUT", "超时"),
    RETRYING("RETRYING", "重试中"),
    CANCELLED("CANCELLED", "已取消");

    private final String code;
    private final String description;

    MessageStatus(String code, String description) {
        this.code = code;
        this.description = description;
    }

    public boolean isFinal() {
        return this == SUCCESS || this == FAILED || this == TIMEOUT || this == CANCELLED;
    }
}
