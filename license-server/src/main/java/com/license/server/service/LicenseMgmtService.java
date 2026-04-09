package com.license.server.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * 许可管理服务
 * 提供各类许可管理操作的业务接口
 * 
 * 功能：
 * - 用户踢出管理
 * - 用户续期管理
 * 
 * 设计模式：服务模式
 * 
 * @author License System Team
 * @version 1.0.0
 */
@Slf4j
@Service
public class LicenseMgmtService {

    @Autowired
    private MqttPublisherService mqttPublisherService;

    /**
     * 踢出浮动许可用户
     * 
     * @param softwareType 软件类型
     * @param hostname 目标主机名
     * @param payload 踢出参数（根据软件类型不同而不同）
     */
    public void kickoutUser(String softwareType, String hostname, Object payload) {
        log.info("Request to kickout user from {} on {}", softwareType, hostname);
        
        mqttPublisherService.send(hostname, "user_kickout", softwareType, "user_operation", payload);
    }

    /**
     * 续期浮动许可用户
     * 
     * @param softwareType 软件类型
     * @param hostname 目标主机名
     * @param payload 续期参数（根据软件类型不同而不同）
     */
    public void renewUser(String softwareType, String hostname, Object payload) {
        log.info("Request to renew user from {} on {}", softwareType, hostname);
        
        mqttPublisherService.send(hostname, "user_renew", softwareType, "user_operation", payload);
    }
}
