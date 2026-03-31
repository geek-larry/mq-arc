package com.license.server.config;

import com.license.server.listener.ResponseMessageListener;
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
import java.util.List;

@Slf4j
@Component
public class RocketMQConsumerConfig {

    @Value("${rocketmq.name-server:localhost:9876}")
    private String nameServer;

    @Value("${rocketmq.consumer.group:license-server-response-group}")
    private String consumerGroup;

    @Value("${rocketmq.consumer.topic:license-response}")
    private String topic;

    @Value("${rocketmq.consumer.tag:*}")
    private String tag;

    @Value("${rocketmq.consumer.consume-thread-min:5}")
    private int consumeThreadMin;

    @Value("${rocketmq.consumer.consume-thread-max:10}")
    private int consumeThreadMax;

    @Autowired
    private ResponseMessageListener responseMessageListener;

    @Getter
    private DefaultMQPushConsumer consumer;

    private volatile boolean started = false;

    public synchronized void start() throws MQClientException {
        if (started) {
            log.warn("Consumer already started");
            return;
        }

        consumer = new DefaultMQPushConsumer(consumerGroup);
        consumer.setNamesrvAddr(nameServer);
        consumer.setConsumeFromWhere(ConsumeFromWhere.CONSUME_FROM_LAST_OFFSET);
        consumer.setMessageModel(MessageModel.CLUSTERING);
        consumer.setConsumeThreadMin(consumeThreadMin);
        consumer.setConsumeThreadMax(consumeThreadMax);
        consumer.subscribe(topic, tag);
        
        consumer.registerMessageListener(new MessageListenerConcurrently() {
            @Override
            public ConsumeConcurrentlyStatus consumeMessage(List<MessageExt> msgs, ConsumeConcurrentlyContext context) {
                for (MessageExt messageExt : msgs) {
                    try {
                        String body = new String(messageExt.getBody());
                        log.debug("[response-consumer] Processing message: msgId={}", messageExt.getMsgId());
                        responseMessageListener.onMessage(body);
                    } catch (Exception e) {
                        log.error("[response-consumer] Failed to process message: msgId={}", messageExt.getMsgId(), e);
                        return ConsumeConcurrentlyStatus.RECONSUME_LATER;
                    }
                }
                return ConsumeConcurrentlyStatus.CONSUME_SUCCESS;
            }
        });
        
        consumer.start();
        started = true;
        
        log.info("Consumer started: group={}, topic={}, tag={}, threads={}", 
                consumerGroup, topic, tag, consumeThreadMin);
    }

    public synchronized void shutdown() {
        if (!started || consumer == null) {
            log.warn("Consumer not started or already shutdown");
            return;
        }

        try {
            consumer.shutdown();
            started = false;
            log.info("Consumer shutdown successfully");
        } catch (Exception e) {
            log.error("Failed to shutdown consumer", e);
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
