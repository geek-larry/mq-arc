package com.license.client.handler.impl;

import com.license.client.handler.AbstractMessageHandler;
import com.license.common.enums.OperationType;
import com.license.common.enums.SoftwareType;
import com.license.common.message.LicenseMessage;
import com.license.common.payload.service.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 服务管理处理器
 * 处理许可服务的启动、停止、重启请求
 */
@Slf4j
@Component
public class ServiceManageHandler extends AbstractMessageHandler<Object, Void> {

    @Autowired
    private String clientHostname;

    // 模拟服务状态存储
    private final Map<String, Boolean> serviceStatus = new ConcurrentHashMap<>();

    @Override
    public String getOperationType() {
        // 支持多种操作类型
        return "*";
    }

    @Override
    public String getSoftwareType() {
        return "*";
    }

    @Override
    public boolean supports(LicenseMessage<?> message) {
        if (message.getOperationType() == null) {
            return false;
        }
        return message.getOperationType() == OperationType.START ||
               message.getOperationType() == OperationType.STOP ||
               message.getOperationType() == OperationType.RESTART;
    }

    @Override
    protected Void doHandle(LicenseMessage<Object> message) throws Exception {
        Object payloadObj = message.getPayload();
        SoftwareType softwareType = message.getSoftwareType();
        OperationType operationType = message.getOperationType();
        
        // 根据软件类型处理不同的Payload
        switch (softwareType) {
            case FLEXNET:
                handleFlexnetService((FlexnetServiceManagePayload) payloadObj, operationType);
                break;
            case SENTINEL:
                handleSentinelService((SentinelServiceManagePayload) payloadObj, operationType);
                break;
            case LMX:
                handleLmxService((LmxServiceManagePayload) payloadObj, operationType);
                break;
            default:
                throw new IllegalArgumentException("Unsupported software type: " + softwareType);
        }
        
        return null;
    }

    private void handleFlexnetService(FlexnetServiceManagePayload payload, OperationType operationType) throws Exception {
        String serviceKey = buildServiceKey(SoftwareType.FLEXNET, payload.getVendorDaemonName());
        
        log.info("Managing Flexnet service [{}] operation={}, port={}",
                payload.getVendorDaemonName(), operationType, payload.getPort());
        
        simulateDelay(500, 2000);
        
        boolean success;
        switch (operationType) {
            case START:
                success = simulateFlexnetStart(payload);
                if (success) {
                    serviceStatus.put(serviceKey, true);
                }
                break;
            case STOP:
                success = simulateFlexnetStop(payload);
                if (success) {
                    serviceStatus.put(serviceKey, false);
                }
                break;
            case RESTART:
                success = simulateFlexnetRestart(payload);
                if (success) {
                    serviceStatus.put(serviceKey, true);
                }
                break;
            default:
                throw new UnsupportedOperationException("Unsupported operation: " + operationType);
        }
        
        if (!success) {
            throw new RuntimeException("Failed to " + operationType.getCode() + " Flexnet service: " + payload.getVendorDaemonName());
        }
        
        log.info("Flexnet service [{}] {} successfully", payload.getVendorDaemonName(), operationType.getCode());
    }

    private void handleSentinelService(SentinelServiceManagePayload payload, OperationType operationType) throws Exception {
        String serviceKey = buildServiceKey(SoftwareType.SENTINEL, payload.getServerName());
        
        log.info("Managing Sentinel service [{}] operation={}, port={}",
                payload.getServerName(), operationType, payload.getPort());
        
        simulateDelay(500, 2000);
        
        boolean success;
        switch (operationType) {
            case START:
                success = simulateSentinelStart(payload);
                if (success) {
                    serviceStatus.put(serviceKey, true);
                }
                break;
            case STOP:
                success = simulateSentinelStop(payload);
                if (success) {
                    serviceStatus.put(serviceKey, false);
                }
                break;
            case RESTART:
                success = simulateSentinelRestart(payload);
                if (success) {
                    serviceStatus.put(serviceKey, true);
                }
                break;
            default:
                throw new UnsupportedOperationException("Unsupported operation: " + operationType);
        }
        
        if (!success) {
            throw new RuntimeException("Failed to " + operationType.getCode() + " Sentinel service: " + payload.getServerName());
        }
        
        log.info("Sentinel service [{}] {} successfully", payload.getServerName(), operationType.getCode());
    }

    private void handleLmxService(LmxServiceManagePayload payload, OperationType operationType) throws Exception {
        String serviceKey = buildServiceKey(SoftwareType.LMX, payload.getServerName());
        
        log.info("Managing LMX service [{}] operation={}, port={}",
                payload.getServerName(), operationType, payload.getPort());
        
        simulateDelay(500, 2000);
        
        boolean success;
        switch (operationType) {
            case START:
                success = simulateLmxStart(payload);
                if (success) {
                    serviceStatus.put(serviceKey, true);
                }
                break;
            case STOP:
                success = simulateLmxStop(payload);
                if (success) {
                    serviceStatus.put(serviceKey, false);
                }
                break;
            case RESTART:
                success = simulateLmxRestart(payload);
                if (success) {
                    serviceStatus.put(serviceKey, true);
                }
                break;
            default:
                throw new UnsupportedOperationException("Unsupported operation: " + operationType);
        }
        
        if (!success) {
            throw new RuntimeException("Failed to " + operationType.getCode() + " LMX service: " + payload.getServerName());
        }
        
        log.info("LMX service [{}] {} successfully", payload.getServerName(), operationType.getCode());
    }

    private boolean simulateFlexnetStart(FlexnetServiceManagePayload payload) {
        log.debug("Starting Flexnet service: {}, port: {}, params: {}",
                payload.getVendorDaemonName(), payload.getPort(), payload.getVendorOptions());
        return simulateRandomSuccess(0.85);
    }

    private boolean simulateFlexnetStop(FlexnetServiceManagePayload payload) {
        log.debug("Stopping Flexnet service: {}", payload.getVendorDaemonName());
        return simulateRandomSuccess(0.95);
    }

    private boolean simulateFlexnetRestart(FlexnetServiceManagePayload payload) {
        log.debug("Restarting Flexnet service: {}", payload.getVendorDaemonName());
        return simulateRandomSuccess(0.8);
    }

    private boolean simulateSentinelStart(SentinelServiceManagePayload payload) {
        log.debug("Starting Sentinel service: {}, port: {}, params: {}",
                payload.getServerName(), payload.getPort(), payload.getServerOptions());
        return simulateRandomSuccess(0.85);
    }

    private boolean simulateSentinelStop(SentinelServiceManagePayload payload) {
        log.debug("Stopping Sentinel service: {}", payload.getServerName());
        return simulateRandomSuccess(0.95);
    }

    private boolean simulateSentinelRestart(SentinelServiceManagePayload payload) {
        log.debug("Restarting Sentinel service: {}", payload.getServerName());
        return simulateRandomSuccess(0.8);
    }

    private boolean simulateLmxStart(LmxServiceManagePayload payload) {
        log.debug("Starting LMX service: {}, port: {}, params: {}",
                payload.getServerName(), payload.getPort(), payload.getServerOptions());
        return simulateRandomSuccess(0.85);
    }

    private boolean simulateLmxStop(LmxServiceManagePayload payload) {
        log.debug("Stopping LMX service: {}", payload.getServerName());
        return simulateRandomSuccess(0.95);
    }

    private boolean simulateLmxRestart(LmxServiceManagePayload payload) {
        log.debug("Restarting LMX service: {}", payload.getServerName());
        return simulateRandomSuccess(0.8);
    }

    private String buildServiceKey(SoftwareType softwareType, String serviceName) {
        return softwareType.getCode() + ":" + serviceName;
    }

    @Override
    protected String getClientHostname() {
        return clientHostname;
    }
}
