package com.license.client.handler;

import com.license.common.exception.MessageProcessException;
import com.license.common.message.LicenseMessage;
import com.license.common.mqtt.MqttClientService;
import lombok.extern.slf4j.Slf4j;

/**
 * 消息处理器抽象基类
 * 提供通用的处理逻辑和异常处理
 */
@Slf4j
public abstract class AbstractMessageHandler<T, R> implements MessageHandler<T, R> {

    @Override
    public void handle(LicenseMessage<T> message, MqttClientService mqttClientService, 
                       String sourceClientId, String operation) {
        long startTime = System.currentTimeMillis();
        
        try {
            log.info("[{}] Processing message: businessKey={}, operation={}",
                    getHandlerName(), message.getBusinessKey(), message.getOperationType());
            
            // 执行具体的业务逻辑
            R result = doHandle(message);
            
            long processTime = System.currentTimeMillis() - startTime;
            log.info("[{}] Message processed successfully: businessKey={}, time={}ms",
                    getHandlerName(), message.getBusinessKey(), processTime);
            
        } catch (Exception e) {
            long processTime = System.currentTimeMillis() - startTime;
            log.error("[{}] Failed to process message: businessKey={}, time={}ms",
                    getHandlerName(), message.getBusinessKey(), processTime, e);
        }
    }

    /**
     * 执行具体的业务处理逻辑
     * 由子类实现
     */
    protected abstract R doHandle(LicenseMessage<T> message) throws Exception;

    /**
     * 获取客户端主机名
     */
    protected abstract String getClientHostname();

    /**
     * 获取处理器名称（用于日志）
     */
    protected String getHandlerName() {
        return this.getClass().getSimpleName();
    }

    /**
     * 模拟随机成功/失败（用于测试）
     */
    protected boolean simulateRandomSuccess(double successRate) {
        return Math.random() < successRate;
    }

    /**
     * 模拟处理延迟
     */
    protected void simulateDelay(long minMillis, long maxMillis) {
        try {
            long delay = minMillis + (long) (Math.random() * (maxMillis - minMillis));
            Thread.sleep(delay);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new MessageProcessException("Processing interrupted", e);
        }
    }
}
