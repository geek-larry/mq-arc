package com.license.client.handler;

import com.license.common.message.LicenseMessage;
import com.license.common.message.LicenseResponse;

/**
 * 消息处理器接口
 * 定义各类消息的处理逻辑
 */
public interface MessageHandler<T, R> {

    /**
     * 获取处理器支持的操作类型
     */
    String getOperationType();

    /**
     * 获取处理器支持的软件类型
     */
    String getSoftwareType();

    /**
     * 处理消息
     *
     * @param message 接收到的消息
     * @return 处理结果
     */
    LicenseResponse<R> handle(LicenseMessage<T> message);

    /**
     * 是否支持该消息
     *
     * @param message 消息对象
     * @return 是否支持
     */
    default boolean supports(LicenseMessage<?> message) {
        if (message.getOperationType() == null || message.getSoftwareType() == null) {
            return false;
        }
        boolean operationMatch = "*".equals(getOperationType()) || 
                               message.getOperationType().getCode().equals(getOperationType());
        boolean softwareMatch = "*".equals(getSoftwareType()) || 
                               message.getSoftwareType().getCode().equals(getSoftwareType());
        return operationMatch && softwareMatch;
    }
}
