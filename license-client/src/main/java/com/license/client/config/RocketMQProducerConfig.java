package com.license.client.config;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.client.exception.MQClientException;
import org.apache.rocketmq.client.producer.DefaultMQProducer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.annotation.PreDestroy;

@Slf4j
@Component
public class RocketMQProducerConfig {

    @Value("${rocketmq.name-server:localhost:9876}")
    private String nameServer;

    @Value("${rocketmq.producer.group:license-client-producer-group}")
    private String producerGroup;

    @Value("${rocketmq.producer.send-message-timeout:3000}")
    private int sendMessageTimeout;

    @Value("${rocketmq.producer.retry-times-when-send-failed:2}")
    private int retryTimesWhenSendFailed;

    @Getter
    private DefaultMQProducer producer;

    private volatile boolean started = false;

    public synchronized void start() throws MQClientException {
        if (started) {
            log.warn("Producer already started");
            return;
        }

        producer = new DefaultMQProducer(producerGroup);
        producer.setNamesrvAddr(nameServer);
        producer.setSendMsgTimeout(sendMessageTimeout);
        producer.setRetryTimesWhenSendFailed(retryTimesWhenSendFailed);
        producer.setCompressMsgBodyOverHowmuch(4096);
        
        producer.start();
        started = true;
        
        log.info("Client producer started: group={}, nameServer={}", producerGroup, nameServer);
    }

    public synchronized void shutdown() {
        if (!started || producer == null) {
            log.warn("Producer not started or already shutdown");
            return;
        }

        try {
            producer.shutdown();
            started = false;
            log.info("Client producer shutdown successfully");
        } catch (Exception e) {
            log.error("Failed to shutdown client producer", e);
        }
    }

    public boolean isStarted() {
        return started;
    }

    @PreDestroy
    public void destroy() {
        shutdown();
    }
}
