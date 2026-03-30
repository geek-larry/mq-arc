package com.license.client.listener;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.license.common.constant.LicenseConstants;
import com.license.common.message.LicenseResponse;
import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.spring.core.RocketMQTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.stereotype.Service;

/**
 * 响应发送服务
 * 负责将处理结果发送回服务端
 */
@Slf4j
@Service
public class ResponseSendService {

    @Autowired
    private RocketMQTemplate rocketMQTemplate;

    @Autowired
    private ObjectMapper objectMapper;

    @Value("${license.response.topic:license-response}")
    private String responseTopic;

    @Value("${license.response.tag:default}")
    private String responseTag;

    /**
     * 发送响应消息
     */
    public <T> void sendResponse(LicenseResponse<T> response) {
        try {
            String jsonResponse = objectMapper.writeValueAsString(response);

            String destination = responseTopic + ":" + responseTag;

            org.springframework.messaging.Message<String> message = MessageBuilder.withPayload(jsonResponse)
                    .setHeader(LicenseConstants.MSG_KEY_BUSINESS_KEY, response.getBusinessKey())
                    .setHeader(LicenseConstants.MSG_KEY_CORRELATION_ID, response.getCorrelationId())
                    .setHeader(LicenseConstants.MSG_KEY_TIMESTAMP, System.currentTimeMillis())
                    .build();

            rocketMQTemplate.asyncSend(destination, message, new org.apache.rocketmq.client.producer.SendCallback() {
                @Override
                public void onSuccess(org.apache.rocketmq.client.producer.SendResult sendResult) {
                    log.debug("Response sent successfully, msgId: {}, businessKey: {}",
                            sendResult.getMsgId(), response.getBusinessKey());
                }

                @Override
                public void onException(Throwable e) {
                    log.error("Failed to send response for businessKey: {}",
                            response.getBusinessKey(), e);
                }
            });

            log.info("Response prepared for businessKey: {}, status: {}",
                    response.getBusinessKey(), response.getStatus());

        } catch (JsonProcessingException e) {
            log.error("Failed to serialize response: {}", response, e);
        }
    }
}
