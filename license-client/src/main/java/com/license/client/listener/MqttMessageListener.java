package com.license.client.listener;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.license.client.handler.MessageHandler;
import com.license.common.message.LicenseMessage;
import com.license.common.mqtt.MqttClientService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.util.List;

/**
 * MQTT消息监听器
 * 接收并处理来自server的消息
 */
@Slf4j
@Component
public class MqttMessageListener {

    @Autowired
    private MqttClientService mqttClientService;

    @Autowired
    private List<MessageHandler<?, ?>> handlers;

    @Autowired
    private ObjectMapper objectMapper;

    @Value("${license.client.hostname:unknown}")
    private String hostname;

    @PostConstruct
    public void init() {
        // 设置消息监听器
        mqttClientService.setMessageListener(this::onMessage);
        log.info("MQTT message listener initialized for client: {}", hostname);
    }

    /**
     * 处理接收到的消息
     */
    private void onMessage(String sourceClientId, String operation, LicenseMessage<?> message) {
        log.info("Received message from server: {}, operation: {}, messageId: {}",
            sourceClientId, operation, message.getMessageId());

        try {
            // 查找对应的handler
            MessageHandler<?, ?> handler = findHandler(message);
            if (handler == null) {
                log.warn("No handler found for operation: {}, softwareType: {}",
                    message.getOperationType(), message.getSoftwareType());
                return;
            }

            // 处理消息
            processMessage(handler, message, sourceClientId, operation);

        } catch (Exception e) {
            log.error("Failed to process message, messageId: {}", message.getMessageId(), e);
        }
    }

    /**
     * 查找对应的handler
     */
    @SuppressWarnings("unchecked")
    private MessageHandler<Object, Object> findHandler(LicenseMessage<?> message) {
        for (MessageHandler<?, ?> handler : handlers) {
            if (handler.supports(message)) {
                return (MessageHandler<Object, Object>) handler;
            }
        }
        return null;
    }

    /**
     * 处理消息
     */
    @SuppressWarnings({"unchecked", "rawtypes"})
    private void processMessage(MessageHandler handler, LicenseMessage message, 
                                String sourceClientId, String operation) {
        try {
            handler.handle(message, mqttClientService, sourceClientId, operation);
        } catch (Exception e) {
            log.error("Handler execution failed for message: {}", message.getMessageId(), e);
            throw new RuntimeException("Handler execution failed", e);
        }
    }
}
