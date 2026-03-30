package com.license.client.handler.impl;

import com.license.client.handler.AbstractMessageHandler;
import com.license.common.enums.OperationType;
import com.license.common.enums.SoftwareType;
import com.license.common.message.LicenseMessage;
import com.license.common.payload.UserKickoutPayload;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * 用户踢出处理器
 * 处理浮动许可用户踢出请求
 */
@Slf4j
@Component
public class UserKickoutHandler extends AbstractMessageHandler<UserKickoutPayload, Void> {

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
    protected Void doHandle(LicenseMessage<UserKickoutPayload> message) throws Exception {
        UserKickoutPayload payload = message.getPayload();
        SoftwareType softwareType = message.getSoftwareType();
        
        log.info("Kicking out user [{}] from {} license, feature: {}, reason: {}",
                payload.getUsername(), softwareType, payload.getFeatureCode(), payload.getReason());
        
        // 模拟处理延迟
        simulateDelay(100, 500);
        
        // 模拟调用实际的许可服务器接口
        boolean success = simulateKickoutUser(softwareType, payload);
        
        if (!success) {
            throw new RuntimeException("Failed to kickout user: " + payload.getUsername());
        }
        
        log.info("User [{}] kicked out successfully from {}", payload.getUsername(), softwareType);
        return null;
    }

    /**
     * 模拟调用许可服务器的踢出用户接口
     */
    private boolean simulateKickoutUser(SoftwareType softwareType, UserKickoutPayload payload) {
        // 模拟90%成功率
        boolean success = simulateRandomSuccess(0.9);
        
        if (success) {
            log.debug("License server {}: User {} kicked out, session: {}",
                    softwareType, payload.getUsername(), payload.getSessionId());
        } else {
            log.warn("License server {}: Failed to kickout user {}",
                    softwareType, payload.getUsername());
        }
        
        return success;
    }

    @Override
    protected String getClientHostname() {
        return clientHostname;
    }
}
