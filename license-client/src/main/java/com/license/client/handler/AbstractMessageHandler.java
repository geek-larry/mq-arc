package com.license.client.handler;

import com.license.common.exception.MessageProcessException;
import com.license.common.message.LicenseMessage;
import com.license.common.message.LicenseResponse;
import com.license.common.mqtt.MqttClientService;
import lombok.extern.slf4j.Slf4j;

/**
 * 消息处理器抽象基类
 * 提供通用的处理逻辑和异常处理
 */
@Slf4j
public abstract class AbstractMessageHandler<T> implements MessageHandler<T> {

    @Override
    public void handle(LicenseMessage<T> message, MqttClientService mqttClientService) {
        long startTime = System.currentTimeMillis();
        
        try {
            log.info("[{}] Processing message: messageId={}, messageType={}, operationType={}",
                    getHandlerName(), message.getMessageId(), message.getMessageType(), 
                    message.getOperationType());
            
            doHandle(message);
            
            long processTime = System.currentTimeMillis() - startTime;
            log.info("[{}] Message processed successfully: messageId={}, time={}ms",
                    getHandlerName(), message.getMessageId(), processTime);
            
            sendSuccessResponse(message, mqttClientService, processTime);
            
        } catch (Exception e) {
            long processTime = System.currentTimeMillis() - startTime;
            log.error("[{}] Failed to process message: messageId={}, time={}ms",
                    getHandlerName(), message.getMessageId(), processTime, e);
            
            sendErrorResponse(message, mqttClientService, processTime, e);
        }
    }

    /**
     * 发送成功响应
     */
    protected void sendSuccessResponse(LicenseMessage<T> message, MqttClientService mqttClientService, long processTime) {
        try {
            LicenseResponse response = LicenseResponse.builder()
                    .requestId(message.getMessageId())
                    .status("SUCCESS")
                    .message("Operation completed successfully")
                    .timestamp(System.currentTimeMillis())
                    .data(buildResponseData(message, processTime))
                    .build();
            
            String targetClientId = message.getHostname();
            mqttClientService.sendResponse(targetClientId, response);
            
            log.debug("[{}] Success response sent, messageId: {}", getHandlerName(), message.getMessageId());
        } catch (Exception e) {
            log.error("[{}] Failed to send success response, messageId: {}", 
                    getHandlerName(), message.getMessageId(), e);
        }
    }

    /**
     * 发送错误响应
     */
    protected void sendErrorResponse(LicenseMessage<T> message, MqttClientService mqttClientService, 
                                     long processTime, Exception error) {
        try {
            LicenseResponse response = LicenseResponse.builder()
                    .requestId(message.getMessageId())
                    .status("FAILED")
                    .message(error.getMessage())
                    .timestamp(System.currentTimeMillis())
                    .data(buildErrorData(error, processTime))
                    .build();
            
            String targetClientId = message.getHostname();
            mqttClientService.sendResponse(targetClientId, response);
            
            log.debug("[{}] Error response sent, messageId: {}", getHandlerName(), message.getMessageId());
        } catch (Exception e) {
            log.error("[{}] Failed to send error response, messageId: {}", 
                    getHandlerName(), message.getMessageId(), e);
        }
    }

    /**
     * 构建响应数据（子类可重写）
     */
    protected Object buildResponseData(LicenseMessage<T> message, long processTime) {
        return null;
    }

    /**
     * 构建错误数据
     */
    protected Object buildErrorData(Exception error, long processTime) {
        return java.util.Collections.singletonMap("processTime", processTime);
    }

    /**
     * 执行具体的业务处理逻辑
     * 由子类实现
     */
    protected abstract void doHandle(LicenseMessage<T> message) throws Exception;

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
