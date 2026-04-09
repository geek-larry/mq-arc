package com.license.client.handler.impl;

import com.license.client.handler.AbstractMessageHandler;
import com.license.common.message.LicenseMessage;
import com.license.common.payload.kickout.*;
import com.license.common.payload.renew.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * 用户操作处理器
 * 处理浮动许可用户操作请求（踢出、续期等）
 */
@Slf4j
@Component
public class UserOperationHandler extends AbstractMessageHandler<Object> {

    @Autowired
    private String clientHostname;

    @Override
    protected void doHandle(LicenseMessage<Object> message) throws Exception {
        Object payloadObj = message.getPayload();
        String softwareType = message.getSoftwareType();
        String operationType = message.getOperationType();
        
        log.info("Processing user operation: operationType={}, softwareType={}", 
                operationType, softwareType);
        
        routeToHandler(softwareType, operationType, payloadObj);
    }

    /**
     * 根据软件类型和操作类型路由到对应的处理方法
     * 方法命名规范: handle{SoftwareType}{Operation}
     * 例如: handleFlexnetKickout, handleFlexnetRenew
     */
    private void routeToHandler(String softwareType, String operationType, Object payload) throws Exception {
        String softwareTypeLower = softwareType.toLowerCase();
        String operationTypeLower = operationType.toLowerCase();
        
        switch (softwareTypeLower) {
            case "flexnet":
                handleFlexnetOperation(operationTypeLower, payload);
                break;
            case "sentinel":
                handleSentinelOperation(operationTypeLower, payload);
                break;
            case "lmx":
                handleLmxOperation(operationTypeLower, payload);
                break;
            default:
                throw new IllegalArgumentException("Unsupported software type: " + softwareType);
        }
    }

    /**
     * 处理Flexnet操作
     */
    private void handleFlexnetOperation(String operationType, Object payload) throws Exception {
        switch (operationType) {
            case "user_kickout":
                handleFlexnetKickout((FlexnetUserKickoutPayload) payload);
                break;
            case "user_renew":
                handleFlexnetRenew((FlexnetUserRenewPayload) payload);
                break;
            default:
                throw new IllegalArgumentException("Unsupported Flexnet operation: " + operationType);
        }
    }

    /**
     * 处理Sentinel操作
     */
    private void handleSentinelOperation(String operationType, Object payload) throws Exception {
        switch (operationType) {
            case "user_kickout":
                handleSentinelKickout((SentinelUserKickoutPayload) payload);
                break;
            case "user_renew":
                handleSentinelRenew((SentinelUserRenewPayload) payload);
                break;
            default:
                throw new IllegalArgumentException("Unsupported Sentinel operation: " + operationType);
        }
    }

    /**
     * 处理LMX操作
     */
    private void handleLmxOperation(String operationType, Object payload) throws Exception {
        switch (operationType) {
            case "user_kickout":
                handleLmxKickout((LmxUserKickoutPayload) payload);
                break;
            case "user_renew":
                handleLmxRenew((LmxUserRenewPayload) payload);
                break;
            default:
                throw new IllegalArgumentException("Unsupported LMX operation: " + operationType);
        }
    }

    // ==================== Flexnet 操作 ====================

    private void handleFlexnetKickout(FlexnetUserKickoutPayload payload) throws Exception {
        log.info("Kicking out Flexnet user [{}] feature: {}, reason: {}",
                payload.getUsername(), payload.getFeatureName(), payload.getReason());
        
        simulateDelay(100, 500);
        
        boolean success = simulateFlexnetKickout(payload);
        
        if (!success) {
            throw new RuntimeException("Failed to kickout Flexnet user: " + payload.getUsername());
        }
        
        log.info("Flexnet user [{}] kicked out successfully", payload.getUsername());
    }

    private void handleFlexnetRenew(FlexnetUserRenewPayload payload) throws Exception {
        log.info("Renewing Flexnet user [{}] feature: {}, extendDays: {}, reason: {}",
                payload.getUsername(), payload.getFeatureName(), payload.getExtendDays(), payload.getReason());
        
        simulateDelay(100, 500);
        
        boolean success = simulateFlexnetRenew(payload);
        
        if (!success) {
            throw new RuntimeException("Failed to renew Flexnet user: " + payload.getUsername());
        }
        
        log.info("Flexnet user [{}] renewed successfully, extended {} days", 
                payload.getUsername(), payload.getExtendDays());
    }

    private boolean simulateFlexnetKickout(FlexnetUserKickoutPayload payload) {
        boolean success = simulateRandomSuccess(0.9);
        
        if (success) {
            log.debug("Flexnet server: User {} kicked out, hostId: {}, feature: {}",
                    payload.getUsername(), payload.getHostId(), payload.getFeatureName());
        } else {
            log.warn("Flexnet server: Failed to kickout user {}", payload.getUsername());
        }
        
        return success;
    }

    private boolean simulateFlexnetRenew(FlexnetUserRenewPayload payload) {
        boolean success = simulateRandomSuccess(0.9);
        
        if (success) {
            log.debug("Flexnet server: User {} renewed, hostId: {}, feature: {}, extendDays: {}",
                    payload.getUsername(), payload.getHostId(), payload.getFeatureName(), payload.getExtendDays());
        } else {
            log.warn("Flexnet server: Failed to renew user {}", payload.getUsername());
        }
        
        return success;
    }

    // ==================== Sentinel 操作 ====================

    private void handleSentinelKickout(SentinelUserKickoutPayload payload) throws Exception {
        log.info("Kicking out Sentinel user [{}] feature: {}, reason: {}",
                payload.getUsername(), payload.getFeatureName(), payload.getReason());
        
        simulateDelay(100, 500);
        
        boolean success = simulateSentinelKickout(payload);
        
        if (!success) {
            throw new RuntimeException("Failed to kickout Sentinel user: " + payload.getUsername());
        }
        
        log.info("Sentinel user [{}] kicked out successfully", payload.getUsername());
    }

    private void handleSentinelRenew(SentinelUserRenewPayload payload) throws Exception {
        log.info("Renewing Sentinel user [{}] feature: {}, extendDays: {}, reason: {}",
                payload.getUsername(), payload.getFeatureName(), payload.getExtendDays(), payload.getReason());
        
        simulateDelay(100, 500);
        
        boolean success = simulateSentinelRenew(payload);
        
        if (!success) {
            throw new RuntimeException("Failed to renew Sentinel user: " + payload.getUsername());
        }
        
        log.info("Sentinel user [{}] renewed successfully, extended {} days", 
                payload.getUsername(), payload.getExtendDays());
    }

    private boolean simulateSentinelKickout(SentinelUserKickoutPayload payload) {
        boolean success = simulateRandomSuccess(0.9);
        
        if (success) {
            log.debug("Sentinel server: User {} kicked out, sessionId: {}, feature: {}",
                    payload.getUsername(), payload.getSessionId(), payload.getFeatureName());
        } else {
            log.warn("Sentinel server: Failed to kickout user {}", payload.getUsername());
        }
        
        return success;
    }

    private boolean simulateSentinelRenew(SentinelUserRenewPayload payload) {
        boolean success = simulateRandomSuccess(0.9);
        
        if (success) {
            log.debug("Sentinel server: User {} renewed, sessionId: {}, feature: {}, extendDays: {}",
                    payload.getUsername(), payload.getSessionId(), payload.getFeatureName(), payload.getExtendDays());
        } else {
            log.warn("Sentinel server: Failed to renew user {}", payload.getUsername());
        }
        
        return success;
    }

    // ==================== LMX 操作 ====================

    private void handleLmxKickout(LmxUserKickoutPayload payload) throws Exception {
        log.info("Kicking out LMX user [{}] product: {}, reason: {}",
                payload.getUsername(), payload.getProductName(), payload.getReason());
        
        simulateDelay(100, 500);
        
        boolean success = simulateLmxKickout(payload);
        
        if (!success) {
            throw new RuntimeException("Failed to kickout LMX user: " + payload.getUsername());
        }
        
        log.info("LMX user [{}] kicked out successfully", payload.getUsername());
    }

    private void handleLmxRenew(LmxUserRenewPayload payload) throws Exception {
        log.info("Renewing LMX user [{}] product: {}, extendDays: {}, reason: {}",
                payload.getUsername(), payload.getProductName(), payload.getExtendDays(), payload.getReason());
        
        simulateDelay(100, 500);
        
        boolean success = simulateLmxRenew(payload);
        
        if (!success) {
            throw new RuntimeException("Failed to renew LMX user: " + payload.getUsername());
        }
        
        log.info("LMX user [{}] renewed successfully, extended {} days", 
                payload.getUsername(), payload.getExtendDays());
    }

    private boolean simulateLmxKickout(LmxUserKickoutPayload payload) {
        boolean success = simulateRandomSuccess(0.9);
        
        if (success) {
            log.debug("LMX server: User {} kicked out, computerName: {}, product: {}",
                    payload.getUsername(), payload.getComputerName(), payload.getProductName());
        } else {
            log.warn("LMX server: Failed to kickout user {}", payload.getUsername());
        }
        
        return success;
    }

    private boolean simulateLmxRenew(LmxUserRenewPayload payload) {
        boolean success = simulateRandomSuccess(0.9);
        
        if (success) {
            log.debug("LMX server: User {} renewed, computerName: {}, product: {}, extendDays: {}",
                    payload.getUsername(), payload.getComputerName(), payload.getProductName(), payload.getExtendDays());
        } else {
            log.warn("LMX server: Failed to renew user {}", payload.getUsername());
        }
        
        return success;
    }

    @Override
    protected String getClientHostname() {
        return clientHostname;
    }
}
