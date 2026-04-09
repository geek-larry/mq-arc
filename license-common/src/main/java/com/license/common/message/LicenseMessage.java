package com.license.common.message;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.UUID;

/**
 * 许可管理消息基类
 * 所有消息类型的统一封装
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LicenseMessage<T> implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 消息唯一标识
     */
    private String messageId;

    /**
     * 目标服务器主机名
     */
    private String hostname;

    /**
     * 软件类型
     */
    private String softwareType;

    /**
     * 操作类型
     * 用于区分不同的操作场景
     */
    private String operationType;

    /**
     * 消息类型
     * 用于路由到对应的handler
     */
    private String messageType;

    /**
     * 消息体（具体业务数据）
     */
    private T payload;

    /**
     * 创建消息构建器
     */
    public static <T> LicenseMessageBuilder<T> builder() {
        return new LicenseMessageBuilder<T>()
                .messageId(UUID.randomUUID().toString());
    }
}
