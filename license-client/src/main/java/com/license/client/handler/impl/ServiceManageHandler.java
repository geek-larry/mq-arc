package com.license.client.handler.impl;

import com.license.client.handler.AbstractMessageHandler;
import com.license.common.enums.OperationType;
import com.license.common.enums.SoftwareType;
import com.license.common.message.LicenseMessage;
import com.license.common.payload.ServiceManagePayload;
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
public class ServiceManageHandler extends AbstractMessageHandler<ServiceManagePayload, Void> {

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
    protected Void doHandle(LicenseMessage<ServiceManagePayload> message) throws Exception {
        ServiceManagePayload payload = message.getPayload();
        SoftwareType softwareType = message.getSoftwareType();
        OperationType operationType = message.getOperationType();
        
        String serviceKey = buildServiceKey(softwareType, payload.getServiceName());
        
        log.info("Managing service [{}] for {}: operation={}, port={}",
                payload.getServiceName(), softwareType, operationType, payload.getPort());
        
        // 模拟处理延迟
        simulateDelay(500, 2000);
        
        boolean success;
        switch (operationType) {
            case START:
                success = simulateStartService(softwareType, payload);
                if (success) {
                    serviceStatus.put(serviceKey, true);
                }
                break;
            case STOP:
                success = simulateStopService(softwareType, payload);
                if (success) {
                    serviceStatus.put(serviceKey, false);
                }
                break;
            case RESTART:
                success = simulateRestartService(softwareType, payload);
                if (success) {
                    serviceStatus.put(serviceKey, true);
                }
                break;
            default:
                throw new UnsupportedOperationException("Unsupported operation: " + operationType);
        }
        
        if (!success) {
            throw new RuntimeException("Failed to " + operationType.getCode() + " service: " + payload.getServiceName());
        }
        
        log.info("Service [{}] {} successfully for {}", 
                payload.getServiceName(), operationType.getCode(), softwareType);
        return null;
    }

    /**
     * 模拟启动服务
     */
    private boolean simulateStartService(SoftwareType softwareType, ServiceManagePayload payload) {
        log.debug("Starting {} service: {}, port: {}, params: {}",
                softwareType, payload.getServiceName(), payload.getPort(), payload.getStartupParams());
        
        // 模拟85%成功率
        boolean success = simulateRandomSuccess(0.85);
        
        if (success) {
            log.debug("{} service {} started on port {}",
                    softwareType, payload.getServiceName(), payload.getPort());
        }
        
        return success;
    }

    /**
     * 模拟停止服务
     */
    private boolean simulateStopService(SoftwareType softwareType, ServiceManagePayload payload) {
        log.debug("Stopping {} service: {}", softwareType, payload.getServiceName());
        
        // 模拟95%成功率
        boolean success = simulateRandomSuccess(0.95);
        
        if (success) {
            log.debug("{} service {} stopped", softwareType, payload.getServiceName());
        }
        
        return success;
    }

    /**
     * 模拟重启服务
     */
    private boolean simulateRestartService(SoftwareType softwareType, ServiceManagePayload payload) {
        log.debug("Restarting {} service: {}", softwareType, payload.getServiceName());
        
        // 模拟80%成功率
        boolean success = simulateRandomSuccess(0.8);
        
        if (success) {
            log.debug("{} service {} restarted", softwareType, payload.getServiceName());
        }
        
        return success;
    }

    private String buildServiceKey(SoftwareType softwareType, String serviceName) {
        return softwareType.getCode() + ":" + serviceName;
    }

    @Override
    protected String getClientHostname() {
        return clientHostname;
    }
}
