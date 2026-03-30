package com.license.server.config;

import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.client.producer.DefaultMQProducer;
import org.apache.rocketmq.spring.core.RocketMQTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * RocketMQ 配置类
 */
@Slf4j
@Configuration
public class RocketMQConfig {

    @Value("${rocketmq.name-server:localhost:9876}")
    private String nameServer;

    @Value("${rocketmq.producer.group:license-server-producer-group}")
    private String producerGroup;

    @Value("${rocketmq.producer.send-message-timeout:30000}")
    private Integer sendMessageTimeout;

    @Value("${rocketmq.producer.retry-times-when-send-failed:3}")
    private Integer retryTimesWhenSendFailed;

    /**
     * 配置RocketMQ Producer
     */
    @Bean
    public DefaultMQProducer defaultMQProducer() {
        DefaultMQProducer producer = new DefaultMQProducer(producerGroup);
        producer.setNamesrvAddr(nameServer);
        producer.setSendMsgTimeout(sendMessageTimeout);
        producer.setRetryTimesWhenSendFailed(retryTimesWhenSendFailed);
        producer.setRetryTimesWhenSendAsyncFailed(retryTimesWhenSendFailed);
        
        try {
            producer.start();
            log.info("RocketMQ Producer started successfully. NameServer: {}", nameServer);
        } catch (Exception e) {
            log.error("Failed to start RocketMQ Producer", e);
            throw new RuntimeException("Failed to start RocketMQ Producer", e);
        }
        
        return producer;
    }

    /**
     * 配置RocketMQ Template
     */
    @Bean
    public RocketMQTemplate rocketMQTemplate(DefaultMQProducer producer) {
        RocketMQTemplate template = new RocketMQTemplate();
        template.setProducer(producer);
        return template;
    }
}
