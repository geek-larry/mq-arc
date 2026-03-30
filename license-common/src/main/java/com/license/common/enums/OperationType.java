package com.license.common.enums;

import lombok.Getter;

/**
 * 操作类型枚举
 */
@Getter
public enum OperationType {
    // 用户管理
    KICKOUT("kickout", "踢出用户"),

    // 服务管理
    START("start", "启动服务"),
    STOP("stop", "停止服务"),
    RESTART("restart", "重启服务"),

    // 白名单管理
    INIT("init", "初始化白名单"),
    ADD("add", "添加用户"),
    REMOVE("remove", "删除用户"),
    QUERY("query", "查询用户"),

    // 文件管理
    LICENSE_QUERY("license:query", "查询许可文件"),
    LICENSE_UPDATE("license:update", "更新许可文件"),
    LICENSE_DOWNLOAD("license:download", "下载许可文件"),
    LOG_QUERY("log:query", "查询日志文件"),
    LOG_DOWNLOAD("log:download", "下载日志文件"),

    // 监控
    USAGE("usage", "使用情况监控");

    private final String code;
    private final String description;

    OperationType(String code, String description) {
        this.code = code;
        this.description = description;
    }

    public static OperationType fromCode(String code) {
        for (OperationType type : values()) {
            if (type.code.equalsIgnoreCase(code)) {
                return type;
            }
        }
        throw new IllegalArgumentException("Unknown operation type: " + code);
    }
}
