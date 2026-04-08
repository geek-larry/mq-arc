package com.license.client.handler;

import com.license.common.message.LicenseMessage;
import com.license.common.mqtt.MqttClientService;

/**
 * 消息处理器接口
 * 定义各类消息的处理逻辑
 * 
 * 匹配规则：
 * 按照topic匹配找到对应的handler
 */
public interface MessageHandler<T, R> {

    /**
     * 获取处理器支持的Topic
     * 
     * @return Topic名称，返回"*"表示支持所有Topic
     */
    String getSupportedTopic();

    /**
     * 处理消息
     * handler内部负责发送响应
     *
     * @param message 接收到的消息
     * @param mqttClientService MQTT客户端服务，用于发送响应
     * @param sourceClientId 消息来源客户端ID
     * @param operation 操作类型
     */
    void handle(LicenseMessage<T> message, MqttClientService mqttClientService, 
                String sourceClientId, String operation);

    /**
     * 是否支持该消息
     * 
     * 检查topic是否匹配
     *
     * @param message 消息对象
     * @return 是否支持
     */
    default boolean supports(LicenseMessage<?> message) {
        if (message == null) {
            return false;
        }

        if (message.getTopic() == null) {
            return false;
        }

        return "*".equals(getSupportedTopic()) || 
               message.getTopic().equals(getSupportedTopic());
    }
}
