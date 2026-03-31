package com.license.server.config;

import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.client.exception.MQClientException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class RocketMQStarter implements CommandLineRunner {

    @Autowired
    private RocketMQProducerConfig producerConfig;

    @Autowired
    private RocketMQConsumerConfig consumerConfig;

    @Override
    public void run(String... args) throws Exception {
        try {
            log.info("Starting RocketMQ producer...");
            producerConfig.start();
            
            log.info("Starting RocketMQ consumer...");
            consumerConfig.start();
            
            log.info("RocketMQ components started successfully");
        } catch (MQClientException e) {
            log.error("Failed to start RocketMQ components", e);
            throw e;
        }
    }
}
