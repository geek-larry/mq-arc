package com.license.common.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * MQTT配置属性
 */
@Data
@Component
@ConfigurationProperties(prefix = "mqtt")
public class MqttProperties {

    /**
     * MQTT Broker地址
     */
    private String brokerUrl = "tcp://localhost:1883";

    /**
     * 用户名
     */
    private String username;

    /**
     * 密码
     */
    private String password;

    /**
     * 连接超时时间（秒）
     */
    private int connectionTimeout = 30;

    /**
     * 心跳间隔（秒）
     */
    private int keepAliveInterval = 60;

    /**
     * 是否自动重连
     */
    private boolean autoReconnect = true;

    /**
     * 清除会话
     */
    private boolean cleanSession = true;

    /**
     * QoS级别（0、1、2）
     */
    private int qos = 1;

    /**
     * 消息超时时间（毫秒）
     */
    private long timeout = 30000;
}
