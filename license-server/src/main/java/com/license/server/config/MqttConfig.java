package com.license.server.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.license.common.config.MqttProperties;
import com.license.common.mqtt.MqttClientService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * MQTT配置类（Server端）
 */
@Slf4j
@Configuration
@EnableConfigurationProperties(MqttProperties.class)
public class MqttConfig {

    @Value("${license.server.id:license-server}")
    private String serverId;

    @Bean
    public MqttClientService mqttClientService(MqttProperties properties, ObjectMapper objectMapper) {
        try {
            MqttClientService service = new MqttClientService(properties, objectMapper, serverId);
            service.start();
            log.info("MQTT client service started for server: {}", serverId);
            return service;
        } catch (Exception e) {
            log.error("Failed to create MQTT client service", e);
            throw new RuntimeException("Failed to create MQTT client service", e);
        }
    }
}
