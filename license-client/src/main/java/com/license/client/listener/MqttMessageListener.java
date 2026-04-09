package com.license.client.listener;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.license.client.handler.MessageHandler;
import com.license.common.enums.MessageType;
import com.license.common.message.LicenseMessage;
import com.license.common.mqtt.MqttClientService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
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
    private List<MessageHandler<?>> handlers;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private String clientHostname;

    @PostConstruct
    public void init() {
        mqttClientService.setMessageListener(this::onMessage);
        log.info("MQTT message listener initialized for client: {}", clientHostname);
    }

    /**
     * 处理接收到的消息
     */
    private void onMessage(LicenseMessage<?> message) {
        log.info("Received message from server, messageType: {}, messageId: {}",
            message.getMessageType(), message.getMessageId());

        try {
            MessageHandler<?> handler = findHandler(message);
            if (handler == null) {
                log.warn("No handler found for messageType: {}, operationType: {}",
                    message.getMessageType(), message.getOperationType());
                return;
            }

            processMessage(handler, message);

        } catch (Exception e) {
            log.error("Failed to process message, messageId: {}", message.getMessageId(), e);
        }
    }

    /**
     * 查找对应的handler
     * 基于messageType找到对应的handler
     */
    @SuppressWarnings("unchecked")
    private MessageHandler<Object> findHandler(LicenseMessage<?> message) {
        String messageType = message.getMessageType();
        
        if (messageType == null) {
            log.warn("Message type is null, cannot find handler");
            return null;
        }

        MessageType msgType = MessageType.fromCode(messageType);
        if (msgType == null) {
            log.warn("Unknown message type: {}", messageType);
            return null;
        }

        String handlerClassName = msgType.getHandlerClassName();
        
        for (MessageHandler<?> handler : handlers) {
            if (handler.getClass().getName().equals(handlerClassName)) {
                return (MessageHandler<Object>) handler;
            }
        }
        
        log.warn("Handler not found for class: {}", handlerClassName);
        return null;
    }

    /**
     * 处理消息
     */
    @SuppressWarnings({"unchecked", "rawtypes"})
    private void processMessage(MessageHandler handler, LicenseMessage message) {
        try {
            handler.handle(message, mqttClientService);
        } catch (Exception e) {
            log.error("Handler execution failed for message: {}", message.getMessageId(), e);
            throw new RuntimeException("Handler execution failed", e);
        }
    }
}
