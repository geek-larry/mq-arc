package com.license.client.listener;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.license.client.handler.MessageHandler;
import com.license.common.message.LicenseMessage;
import com.license.common.message.LicenseResponse;
import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.common.message.MessageExt;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.List;

@Slf4j
@Component
public class LicenseMessageListener {

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private List<MessageHandler<?, ?>> handlers;

    @Autowired
    private ResponseSendService responseSendService;

    @Value("${license.client.hostname:unknown}")
    private String clientHostname;

    public void onMessage(MessageExt messageExt) {
        String topic = messageExt.getTopic();
        String tags = messageExt.getTags();
        String body = new String(messageExt.getBody());

        log.info("Received message from topic: {}, tags: {}, msgId: {}",
                topic, tags, messageExt.getMsgId());

        try {
            LicenseMessage<?> message = parseMessage(body);

            if (!isMessageForThisHost(message)) {
                log.debug("Message not for this host, skipping. target: {}, current: {}",
                        message.getHostname(), clientHostname);
                return;
            }

            MessageHandler<?, ?> handler = findHandler(message);
            if (handler == null) {
                log.warn("No handler found for message: topic={}, operation={}, software={}",
                        topic, message.getOperationType(), message.getSoftwareType());
                sendErrorResponse(message, "NO_HANDLER", "No handler found for this message type");
                return;
            }

            processMessage(handler, message);

        } catch (Exception e) {
            log.error("Failed to process message: {}", body, e);
        }
    }

    private LicenseMessage<?> parseMessage(String body) throws JsonProcessingException {
        return objectMapper.readValue(body, LicenseMessage.class);
    }

    private boolean isMessageForThisHost(LicenseMessage<?> message) {
        String targetHostname = message.getHostname();
        return targetHostname != null && targetHostname.equals(clientHostname);
    }

    @SuppressWarnings("unchecked")
    private <T, R> MessageHandler<T, R> findHandler(LicenseMessage<T> message) {
        for (MessageHandler<?, ?> handler : handlers) {
            if (handler.supports(message)) {
                return (MessageHandler<T, R>) handler;
            }
        }
        return null;
    }

    private void processMessage(MessageHandler<?, ?> handler, LicenseMessage<?> message) {
        try {
            @SuppressWarnings({"unchecked", "rawtypes"})
            LicenseResponse<?> response = ((MessageHandler) handler).handle(message);

            if (response != null) {
                responseSendService.sendResponse(response);
            }

        } catch (Exception e) {
            log.error("Handler execution failed for message: {}", message.getBusinessKey(), e);
            sendErrorResponse(message, "HANDLER_ERROR", e.getMessage());
        }
    }

    private void sendErrorResponse(LicenseMessage<?> message, String errorCode, String errorMessage) {
        LicenseResponse<Void> response = LicenseResponse.failure(
                message.getMessageId(),
                message.getCorrelationId(),
                message.getBusinessKey(),
                errorCode,
                errorMessage,
                clientHostname
        );
        responseSendService.sendResponse(response);
    }
}
