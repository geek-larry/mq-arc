package com.license.client.handler;

import com.license.common.message.LicenseMessage;
import com.license.common.mqtt.MqttClientService;

/**
 * 消息处理器接口
 * 定义各类消息的处理逻辑
 */
public interface MessageHandler<T> {

    /**
     * 处理消息
     * handler内部负责发送响应
     *
     * @param message 接收到的消息
     * @param mqttClientService MQTT客户端服务，用于发送响应
     */
    void handle(LicenseMessage<T> message, MqttClientService mqttClientService);
}
