package com.license.server.listener;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.license.common.constant.LicenseConstants;
import com.license.common.message.LicenseResponse;
import com.license.server.service.MessagePublishService;
import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.spring.annotation.RocketMQMessageListener;
import org.apache.rocketmq.spring.core.RocketMQListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * 响应消息监听器
 * 监听客户端返回的响应消息
 */
@Slf4j
@Component
@RocketMQMessageListener(
        topic = "license-response",
        consumerGroup = "${license.server.consumer.group:license-server-response-group}",
        selectorExpression = "*"
)
public class ResponseMessageListener implements RocketMQListener<String> {

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private MessagePublishService messagePublishService;

    @Override
    public void onMessage(String message) {
        try {
            log.debug("Received response message: {}", message);
            
            LicenseResponse<?> response = objectMapper.readValue(message, LicenseResponse.class);
            
            log.info("Processing response for businessKey: {}, status: {}", 
                    response.getBusinessKey(), response.getStatus());
            
            // 将响应传递给等待的请求
            messagePublishService.handleResponse(response);
            
        } catch (Exception e) {
            log.error("Failed to process response message: {}", message, e);
        }
    }
}
