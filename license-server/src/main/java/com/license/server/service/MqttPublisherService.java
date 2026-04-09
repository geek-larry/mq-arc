package com.license.server.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.license.common.message.LicenseMessage;
import com.license.common.mqtt.MqttClientService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.UUID;

/**
 * MQTT消息发布服务
 */
@Slf4j
@Service
public class MqttPublisherService {

    @Autowired
    private MqttClientService mqttClientService;

    @Autowired
    private ObjectMapper objectMapper;

    /**
     * 发送消息到指定客户端
     *
     * @param targetClientId 目标客户端ID（hostname）
     * @param operationType 操作类型
     * @param softwareType 软件类型
     * @param messageType 消息类型
     * @param payload 消息体
     */
    public void send(String targetClientId, String operationType, 
                     String softwareType, String messageType, Object payload) {
        try {
            String messageId = UUID.randomUUID().toString();
            
            LicenseMessage<Object> message = new LicenseMessage<>();
            message.setMessageId(messageId);
            message.setOperationType(operationType);
            message.setSoftwareType(softwareType);
            message.setHostname(targetClientId);
            message.setMessageType(messageType);
            message.setPayload(payload);
            
            log.info("Sending message to client: {}, operationType: {}, messageId: {}", 
                targetClientId, operationType, messageId);
            
            mqttClientService.sendToClient(targetClientId, message);
        } catch (Exception e) {
            log.error("Failed to send message to client: {}, operationType: {}", targetClientId, operationType, e);
            throw new RuntimeException("Failed to send message", e);
        }
    }
}
