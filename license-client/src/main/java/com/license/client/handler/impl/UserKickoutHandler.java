package com.license.client.handler.impl;

import com.license.client.handler.AbstractMessageHandler;
import com.license.common.enums.OperationType;
import com.license.common.enums.SoftwareType;
import com.license.common.message.LicenseMessage;
import com.license.common.payload.kickout.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * 用户踢出处理器
 * 处理浮动许可用户踢出请求
 */
@Slf4j
@Component
public class UserKickoutHandler extends AbstractMessageHandler<Object, Void> {

    @Autowired
    private String clientHostname;

    @Override
    public String getOperationType() {
        return OperationType.KICKOUT.getCode();
    }

    @Override
    public String getSoftwareType() {
        // 支持所有软件类型
        return "*";
    }

    @Override
    public boolean supports(LicenseMessage<?> message) {
        if (message.getOperationType() == null) {
            return false;
        }
        return message.getOperationType() == OperationType.KICKOUT;
    }

    @Override
    protected Void doHandle(LicenseMessage<Object> message) throws Exception {
        Object payloadObj = message.getPayload();
        SoftwareType softwareType = message.getSoftwareType();
        
        // 根据软件类型处理不同的Payload
        switch (softwareType) {
            case FLEXNET:
                handleFlexnetKickout((FlexnetUserKickoutPayload) payloadObj);
                break;
            case SENTINEL:
                handleSentinelKickout((SentinelUserKickoutPayload) payloadObj);
                break;
            case LMX:
                handleLmxKickout((LmxUserKickoutPayload) payloadObj);
                break;
            default:
                throw new IllegalArgumentException("Unsupported software type: " + softwareType);
        }
        
        return null;
    }

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

    @Override
    protected String getClientHostname() {
        return clientHostname;
    }
}
