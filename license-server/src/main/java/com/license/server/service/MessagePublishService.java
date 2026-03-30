package com.license.server.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.license.common.constant.LicenseConstants;
import com.license.common.enums.MessageStatus;
import com.license.common.enums.OperationType;
import com.license.common.enums.SoftwareType;
import com.license.common.exception.MessagePublishException;
import com.license.common.message.LicenseMessage;
import com.license.common.message.LicenseResponse;
import com.license.common.util.BusinessKeyGenerator;
import com.license.common.util.TagBuilder;
import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.client.producer.SendCallback;
import org.apache.rocketmq.client.producer.SendResult;
import org.apache.rocketmq.spring.core.RocketMQTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

/**
 * 消息发布服务
 * 负责向消息队列发送各类许可管理消息
 */
@Slf4j
@Service
public class MessagePublishService {

    @Autowired
    private RocketMQTemplate rocketMQTemplate;

    @Autowired
    private ObjectMapper objectMapper;

    @Value("${license.message.timeout:30000}")
    private long messageTimeout;

    // 用于存储等待响应的请求
    private final Map<String, CompletableFuture<LicenseResponse<?>>> pendingRequests = new ConcurrentHashMap<>();

    /**
     * 同步发送消息并等待响应
     */
    public <T, R> LicenseResponse<R> sendAndWait(String topic, SoftwareType softwareType, 
                                                  OperationType operationType, String hostname,
                                                  T payload, Class<R> responseType) {
        String correlationId = UUID.randomUUID().toString();
        return sendAndWaitWithCorrelationId(topic, softwareType, operationType, hostname, payload, correlationId, responseType);
    }

    /**
     * 同步发送消息并等待响应（指定correlationId）
     */
    public <T, R> LicenseResponse<R> sendAndWaitWithCorrelationId(String topic, SoftwareType softwareType,
                                                                   OperationType operationType, String hostname,
                                                                   T payload, String correlationId,
                                                                   Class<R> responseType) {
        LicenseMessage<T> message = buildMessage(topic, softwareType, operationType, hostname, payload, correlationId);
        
        CompletableFuture<LicenseResponse<?>> future = new CompletableFuture<>();
        pendingRequests.put(correlationId, future);

        try {
            log.info("Sending sync message to topic: {}, tag: {}, businessKey: {}", 
                    topic, message.getTag(), message.getBusinessKey());

            String destination = topic + ":" + message.getTag();
            org.springframework.messaging.Message<String> rocketMsg = buildRocketMessage(message);
            
            SendResult sendResult = rocketMQTemplate.syncSend(destination, rocketMsg, messageTimeout);
            
            if (sendResult == null || !sendResult.getSendStatus().name().equals("SEND_OK")) {
                throw new MessagePublishException("Failed to send message: " + (sendResult != null ? sendResult.getSendStatus() : "null result"));
            }

            message.markAsSent();
            log.info("Message sent successfully, msgId: {}", sendResult.getMsgId());

            // 等待响应
            LicenseResponse<?> response = future.get(messageTimeout, TimeUnit.MILLISECONDS);
            
            // 类型安全检查
            if (response == null) {
                throw new MessagePublishException("Received null response");
            }
            
            // 类型转换（由于泛型擦除，无法进行运行时类型检查）
            @SuppressWarnings("unchecked")
            LicenseResponse<R> typedResponse = (LicenseResponse<R>) response;
            return typedResponse;

        } catch (Exception e) {
            log.error("Error sending message or waiting for response", e);
            message.markAsFailed("SEND_ERROR", e.getMessage());
            throw new MessagePublishException("Failed to send message or receive response", e);
        } finally {
            pendingRequests.remove(correlationId);
        }
    }

    /**
     * 异步发送消息
     */
    public <T> void sendAsync(String topic, SoftwareType softwareType, OperationType operationType,
                              String hostname, T payload, SendCallback callback) {
        LicenseMessage<T> message = buildMessage(topic, softwareType, operationType, hostname, payload, null);
        
        try {
            log.info("Sending async message to topic: {}, tag: {}, businessKey: {}",
                    topic, message.getTag(), message.getBusinessKey());

            String destination = topic + ":" + message.getTag();
            org.springframework.messaging.Message<String> rocketMsg = buildRocketMessage(message);

            rocketMQTemplate.asyncSend(destination, rocketMsg, new SendCallback() {
                @Override
                public void onSuccess(SendResult sendResult) {
                    log.info("Async message sent successfully, msgId: {}", sendResult.getMsgId());
                    message.markAsSent();
                    if (callback != null) {
                        callback.onSuccess(sendResult);
                    }
                }

                @Override
                public void onException(Throwable e) {
                    log.error("Failed to send async message", e);
                    message.markAsFailed("ASYNC_SEND_ERROR", e.getMessage());
                    if (callback != null) {
                        callback.onException(e);
                    }
                }
            }, messageTimeout);

        } catch (Exception e) {
            log.error("Error preparing async message", e);
            throw new MessagePublishException("Failed to prepare async message", e);
        }
    }

    /**
     * 单向发送消息（不关心结果）
     */
    public <T> void sendOneWay(String topic, SoftwareType softwareType, OperationType operationType,
                               String hostname, T payload) {
        LicenseMessage<T> message = buildMessage(topic, softwareType, operationType, hostname, payload, null);
        
        try {
            log.info("Sending one-way message to topic: {}, tag: {}", topic, message.getTag());

            String destination = topic + ":" + message.getTag();
            org.springframework.messaging.Message<String> rocketMsg = buildRocketMessage(message);

            rocketMQTemplate.sendOneWay(destination, rocketMsg);
            message.markAsSent();
            
            log.info("One-way message sent to destination: {}", destination);
        } catch (Exception e) {
            log.error("Error sending one-way message", e);
            throw new MessagePublishException("Failed to send one-way message", e);
        }
    }

    /**
     * 批量发送监控数据（优化大数据量场景）
     */
    public <T> void sendBatch(String topic, SoftwareType softwareType, OperationType operationType,
                              String hostname, java.util.List<T> payloads) {
        if (payloads == null || payloads.isEmpty()) {
            log.warn("Empty payload list for batch send");
            return;
        }

        try {
            log.info("Sending batch messages to topic: {}, count: {}", topic, payloads.size());

            String destination = topic + ":" + TagBuilder.build(softwareType, operationType);
            int successCount = 0;
            
            for (T payload : payloads) {
                try {
                    LicenseMessage<T> message = buildMessage(topic, softwareType, operationType, hostname, payload, null);
                    org.springframework.messaging.Message<String> rocketMsg = buildRocketMessage(message);
                    rocketMQTemplate.syncSend(destination, rocketMsg, messageTimeout);
                    successCount++;
                } catch (Exception e) {
                    log.error("Failed to send batch message: {}", e.getMessage());
                }
            }

            log.info("Batch messages sent successfully, success: {}/{} ", successCount, payloads.size());
        } catch (Exception e) {
            log.error("Error sending batch messages", e);
            throw new MessagePublishException("Failed to send batch messages", e);
        }
    }

    /**
     * 处理收到的响应
     */
    public void handleResponse(LicenseResponse<?> response) {
        String correlationId = response.getCorrelationId();
        CompletableFuture<LicenseResponse<?>> future = pendingRequests.get(correlationId);
        
        if (future != null) {
            future.complete(response);
            log.debug("Response handled for correlationId: {}", correlationId);
        } else {
            log.warn("No pending request found for correlationId: {}", correlationId);
        }
    }

    /**
     * 构建消息对象
     */
    private <T> LicenseMessage<T> buildMessage(String topic, SoftwareType softwareType,
                                                OperationType operationType, String hostname,
                                                T payload, String correlationId) {
        String messageId = UUID.randomUUID().toString();
        String businessKey = BusinessKeyGenerator.generate(softwareType, hostname, operationType);
        String tag = TagBuilder.build(softwareType, operationType);

        return LicenseMessage.<T>builder()
                .messageId(messageId)
                .businessKey(businessKey)
                .correlationId(correlationId != null ? correlationId : UUID.randomUUID().toString())
                .hostname(hostname)
                .softwareType(softwareType)
                .operationType(operationType)
                .topic(topic)
                .tag(tag)
                .status(MessageStatus.PENDING)
                .createTime(LocalDateTime.now())
                .payload(payload)
                .build();
    }

    /**
     * 构建RocketMQ消息
     */
    private <T> org.springframework.messaging.Message<String> buildRocketMessage(LicenseMessage<T> message) 
            throws JsonProcessingException {
        String jsonPayload = objectMapper.writeValueAsString(message);
        
        return MessageBuilder.withPayload(jsonPayload)
                .setHeader(LicenseConstants.MSG_KEY_BUSINESS_KEY, message.getBusinessKey())
                .setHeader(LicenseConstants.MSG_KEY_CORRELATION_ID, message.getCorrelationId())
                .setHeader(LicenseConstants.MSG_KEY_HOSTNAME, message.getHostname())
                .setHeader(LicenseConstants.MSG_KEY_SOFTWARE_TYPE, 
                        message.getSoftwareType() != null ? message.getSoftwareType().getCode() : null)
                .setHeader(LicenseConstants.MSG_KEY_OPERATION, 
                        message.getOperationType() != null ? message.getOperationType().getCode() : null)
                .setHeader(LicenseConstants.MSG_KEY_TIMESTAMP, System.currentTimeMillis())
                .build();
    }
}
