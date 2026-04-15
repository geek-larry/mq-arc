package com.license.server.service;

import com.license.common.message.LicenseResponse;
import com.license.server.rrpc.ResponseWaitManager;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.concurrent.CompletableFuture;

/**
 * 许可管理服务
 * 提供各类许可管理操作的业务接口
 * 
 * 功能：
 * - 用户踢出管理（支持RRPC同步等待响应）
 * - 用户续期管理（支持RRPC同步等待响应）
 * 
 * 设计模式：服务模式 + RRPC模式
 * 
 * @author License System Team
 * @version 1.0.0
 */
@Slf4j
@Service
public class LicenseMgmtService {

    @Autowired
    private MqttPublisherService mqttPublisherService;

    @Autowired
    private ResponseWaitManager responseWaitManager;

    /**
     * 踢出浮动许可用户（异步，不等待响应）
     * 
     * @param softwareType 软件类型
     * @param hostname 目标主机名
     * @param payload 踢出参数（根据软件类型不同而不同）
     */
    public void kickoutUser(String softwareType, String hostname, Object payload) {
        log.info("Request to kickout user from {} on {}", softwareType, hostname);
        
        String messageId = mqttPublisherService.sendAsync(hostname, "user_kickout", softwareType, "user_operation", payload);
        log.info("Kickout request sent, messageId: {}", messageId);
    }

    /**
     * 踢出浮动许可用户（同步，等待响应）
     * 
     * @param softwareType 软件类型
     * @param hostname 目标主机名
     * @param payload 踢出参数（根据软件类型不同而不同）
     * @return CompletableFuture<LicenseResponse> 响应结果
     */
    public CompletableFuture<LicenseResponse> kickoutUserSync(String softwareType, String hostname, Object payload) {
        log.info("Request to kickout user from {} on {} (sync)", softwareType, hostname);
        
        String messageId = mqttPublisherService.sendAsync(hostname, "user_kickout", softwareType, "user_operation", payload);
        
        return responseWaitManager.registerRequest(messageId);
    }

    /**
     * 续期浮动许可用户（异步，不等待响应）
     * 
     * @param softwareType 软件类型
     * @param hostname 目标主机名
     * @param payload 续期参数（根据软件类型不同而不同）
     */
    public void renewUser(String softwareType, String hostname, Object payload) {
        log.info("Request to renew user from {} on {}", softwareType, hostname);
        
        String messageId = mqttPublisherService.sendAsync(hostname, "user_renew", softwareType, "user_operation", payload);
        log.info("Renew request sent, messageId: {}", messageId);
    }

    /**
     * 续期浮动许可用户（同步，等待响应）
     * 
     * @param softwareType 软件类型
     * @param hostname 目标主机名
     * @param payload 续期参数（根据软件类型不同而不同）
     * @return CompletableFuture<LicenseResponse> 响应结果
     */
    public CompletableFuture<LicenseResponse> renewUserSync(String softwareType, String hostname, Object payload) {
        log.info("Request to renew user from {} on {} (sync)", softwareType, hostname);
        
        String messageId = mqttPublisherService.sendAsync(hostname, "user_renew", softwareType, "user_operation", payload);
        
        return responseWaitManager.registerRequest(messageId);
    }
}
