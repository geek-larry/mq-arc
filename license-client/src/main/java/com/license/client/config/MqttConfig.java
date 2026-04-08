package com.license.client.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.license.common.config.MqttProperties;
import com.license.common.mqtt.MqttClientService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * MQTT配置类（Client端）
 */
@Slf4j
@Configuration
@EnableConfigurationProperties(MqttProperties.class)
public class MqttConfig {

    @Value("${license.client.hostname:unknown}")
    private String hostname;

    @Bean
    public MqttClientService mqttClientService(MqttProperties properties, ObjectMapper objectMapper) {
        try {
            // 使用hostname作为clientId，实现P2P通信
            MqttClientService service = new MqttClientService(properties, objectMapper, hostname);
            service.start();
            
            // 订阅接收消息
            service.subscribeIncomingMessages();
            
            log.info("MQTT client service started for client: {}", hostname);
            return service;
        } catch (Exception e) {
            log.error("Failed to create MQTT client service", e);
            throw new RuntimeException("Failed to create MQTT client service", e);
        }
    }
}
