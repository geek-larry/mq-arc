package com.license.server.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.license.common.enums.OperationType;
import com.license.common.enums.SoftwareType;
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
     * @param operation 操作类型
     * @param softwareType 软件类型
     * @param payload 消息体
     */
    public void send(String targetClientId, OperationType operation, 
                     SoftwareType softwareType, Object payload) {
        try {
            String messageId = UUID.randomUUID().toString();
            String businessKey = buildBusinessKey(softwareType, targetClientId, operation);
            
            LicenseMessage<Object> message = new LicenseMessage<>();
            message.setMessageId(messageId);
            message.setBusinessKey(businessKey);
            message.setOperationType(operation);
            message.setSoftwareType(softwareType);
            message.setHostname(targetClientId);
            message.setPayload(payload);
            
            log.info("Sending message to client: {}, operation: {}, messageId: {}", 
                targetClientId, operation, messageId);
            
            mqttClientService.sendToClient(targetClientId, operation.getCode(), message);
        } catch (Exception e) {
            log.error("Failed to send message to client: {}, operation: {}", targetClientId, operation, e);
            throw new RuntimeException("Failed to send message", e);
        }
    }

    /**
     * 构建业务Key
     */
    private String buildBusinessKey(SoftwareType softwareType, String hostname, OperationType operation) {
        return String.format("%s:%s:%s:%d:%s",
            softwareType.getCode(),
            hostname,
            operation.getCode(),
            System.currentTimeMillis(),
            UUID.randomUUID().toString().substring(0, 8)
        );
    }
}
