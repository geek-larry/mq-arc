package com.license.client.config;

import com.license.client.listener.LicenseMessageListener;
import com.license.common.constant.LicenseConstants;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.client.consumer.DefaultMQPushConsumer;
import org.apache.rocketmq.client.consumer.listener.ConsumeConcurrentlyContext;
import org.apache.rocketmq.client.consumer.listener.ConsumeConcurrentlyStatus;
import org.apache.rocketmq.client.consumer.listener.MessageListenerConcurrently;
import org.apache.rocketmq.client.exception.MQClientException;
import org.apache.rocketmq.common.consumer.ConsumeFromWhere;
import org.apache.rocketmq.common.message.MessageExt;
import org.apache.rocketmq.common.protocol.heartbeat.MessageModel;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.annotation.PreDestroy;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@Component
public class RocketMQConsumerConfig {

    @Value("${rocketmq.name-server:localhost:9876}")
    private String nameServer;

    @Value("${license.client.hostname:unknown}")
    private String clientHostname;

    @Value("${rocketmq.consumer.consume-thread-min:1}")
    private int defaultConsumeThreadMin;

    @Value("${rocketmq.consumer.monitor-thread-min:20}")
    private int monitorConsumeThreadMin;

    @Autowired
    private LicenseMessageListener licenseMessageListener;

    @Getter
    private List<DefaultMQPushConsumer> consumers = new ArrayList<>();

    private volatile boolean started = false;

    public synchronized void start() throws MQClientException {
        if (started) {
            log.warn("Consumers already started");
            return;
        }

        startConsumer(
                clientHostname + "-user-mgmt-group",
                LicenseConstants.TOPIC_USER_MGMT,
                "*",
                defaultConsumeThreadMin,
                "user-mgmt"
        );

        startConsumer(
                clientHostname + "-service-mgmt-group",
                LicenseConstants.TOPIC_SERVICE_MGMT,
                "*",
                defaultConsumeThreadMin,
                "service-mgmt"
        );

        startConsumer(
                clientHostname + "-whitelist-mgmt-group",
                LicenseConstants.TOPIC_WHITELIST_MGMT,
                "*",
                defaultConsumeThreadMin,
                "whitelist-mgmt"
        );

        startConsumer(
                clientHostname + "-file-mgmt-group",
                LicenseConstants.TOPIC_FILE_MGMT,
                "*",
                defaultConsumeThreadMin,
                "file-mgmt"
        );

        startConsumer(
                clientHostname + "-monitor-group",
                LicenseConstants.TOPIC_MONITOR,
                "*",
                monitorConsumeThreadMin,
                "monitor"
        );

        started = true;
        log.info("All consumers started successfully, total: {}", consumers.size());
    }

    private void startConsumer(String consumerGroup, String topic, String tag, 
                              int consumeThreadMin, String consumerName) throws MQClientException {
        DefaultMQPushConsumer consumer = new DefaultMQPushConsumer(consumerGroup);
        consumer.setNamesrvAddr(nameServer);
        consumer.setConsumeFromWhere(ConsumeFromWhere.CONSUME_FROM_LAST_OFFSET);
        consumer.setMessageModel(MessageModel.CLUSTERING);
        consumer.setConsumeThreadMin(consumeThreadMin);
        consumer.setConsumeThreadMax(consumeThreadMin * 2);
        consumer.subscribe(topic, tag);
        
        final String name = consumerName;
        consumer.registerMessageListener(new MessageListenerConcurrently() {
            @Override
            public ConsumeConcurrentlyStatus consumeMessage(List<MessageExt> msgs, ConsumeConcurrentlyContext context) {
                for (MessageExt messageExt : msgs) {
                    try {
                        log.debug("[{}] Processing message: msgId={}", name, messageExt.getMsgId());
                        licenseMessageListener.onMessage(messageExt);
                    } catch (Exception e) {
                        log.error("[{}] Failed to process message: msgId={}", name, messageExt.getMsgId(), e);
                        return ConsumeConcurrentlyStatus.RECONSUME_LATER;
                    }
                }
                return ConsumeConcurrentlyStatus.CONSUME_SUCCESS;
            }
        });
        
        consumer.start();
        consumers.add(consumer);
        
        log.info("Consumer [{}] started: group={}, topic={}, threads={}", 
                name, consumerGroup, topic, consumeThreadMin);
    }

    public synchronized void shutdown() {
        if (!started || consumers.isEmpty()) {
            log.warn("Consumers not started or already shutdown");
            return;
        }

        for (DefaultMQPushConsumer consumer : consumers) {
            try {
                consumer.shutdown();
                log.info("Consumer shutdown successfully");
            } catch (Exception e) {
                log.error("Failed to shutdown consumer", e);
            }
        }
        
        consumers.clear();
        started = false;
        log.info("All consumers shutdown successfully");
    }

    public boolean isStarted() {
        return started;
    }

    @PreDestroy
    public void destroy() {
        shutdown();
    }
}
