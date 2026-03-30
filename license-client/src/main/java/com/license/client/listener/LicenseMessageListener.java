package com.license.client.listener;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.license.client.handler.MessageHandler;
import com.license.common.constant.LicenseConstants;
import com.license.common.message.LicenseMessage;
import com.license.common.message.LicenseResponse;
import com.license.common.util.BusinessKeyGenerator;
import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.common.message.MessageExt;
import org.apache.rocketmq.spring.annotation.RocketMQMessageListener;
import org.apache.rocketmq.spring.core.RocketMQListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * 许可管理消息监听器
 * 监听各类Topic的消息并进行处理
 */
@Slf4j
@Component
@RocketMQMessageListener(
        topic = "${license.client.consume.topic:license-user-mgmt}",
        consumerGroup = "${license.client.consumer.group:license-client-consumer-group}",
        selectorExpression = "*"
)
public class LicenseMessageListener implements RocketMQListener<MessageExt> {

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private List<MessageHandler<?, ?>> handlers;

    @Autowired
    private ResponseSendService responseSendService;

    @Autowired
    private String clientHostname;

    @Override
    public void onMessage(MessageExt messageExt) {
        String topic = messageExt.getTopic();
        String tags = messageExt.getTags();
        String body = new String(messageExt.getBody());

        log.info("Received message from topic: {}, tags: {}, msgId: {}",
                topic, tags, messageExt.getMsgId());

        try {
            // 解析消息
            LicenseMessage<?> message = parseMessage(body);

            // 验证消息是否针对当前主机
            if (!isMessageForThisHost(message)) {
                log.debug("Message not for this host, skipping. target: {}, current: {}",
                        message.getHostname(), clientHostname);
                return;
            }

            // 查找并执行处理器
            MessageHandler<?, ?> handler = findHandler(message);
            if (handler == null) {
                log.warn("No handler found for message: topic={}, operation={}, software={}",
                        topic, message.getOperationType(), message.getSoftwareType());
                sendErrorResponse(message, "NO_HANDLER", "No handler found for this message type");
                return;
            }

            // 执行处理
            processMessage(handler, message);

        } catch (Exception e) {
            log.error("Failed to process message: {}", body, e);
        }
    }

    /**
     * 解析消息
     */
    private LicenseMessage<?> parseMessage(String body) throws JsonProcessingException {
        return objectMapper.readValue(body, LicenseMessage.class);
    }

    /**
     * 验证消息是否针对当前主机
     */
    private boolean isMessageForThisHost(LicenseMessage<?> message) {
        // 严格的主机名匹配，只处理明确指定当前主机的消息
        String targetHostname = message.getHostname();
        return targetHostname != null && targetHostname.equals(clientHostname);
    }

    /**
     * 查找合适的处理器
     */
    @SuppressWarnings("unchecked")
    private <T, R> MessageHandler<T, R> findHandler(LicenseMessage<T> message) {
        for (MessageHandler<?, ?> handler : handlers) {
            if (handler.supports(message)) {
                return (MessageHandler<T, R>) handler;
            }
        }
        return null;
    }

    /**
     * 处理消息
     */
    private void processMessage(MessageHandler<?, ?> handler, LicenseMessage<?> message) {
        try {
            @SuppressWarnings({"unchecked", "rawtypes"})
            LicenseResponse<?> response = ((MessageHandler) handler).handle(message);

            // 发送响应
            if (response != null) {
                responseSendService.sendResponse(response);
            }

        } catch (Exception e) {
            log.error("Handler execution failed for message: {}", message.getBusinessKey(), e);
            sendErrorResponse(message, "HANDLER_ERROR", e.getMessage());
        }
    }

    /**
     * 发送错误响应
     */
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
