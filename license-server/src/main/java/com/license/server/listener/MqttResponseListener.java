package com.license.server.listener;

import com.license.common.message.LicenseResponse;
import com.license.common.mqtt.MqttClientService;
import com.license.server.rrpc.ResponseWaitManager;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;

/**
 * MQTT响应监听器
 * 接收来自客户端的响应消息
 */
@Slf4j
@Component
public class MqttResponseListener {

    @Autowired
    private MqttClientService mqttClientService;

    @Autowired
    private ResponseWaitManager responseWaitManager;

    @PostConstruct
    public void init() {
        mqttClientService.setResponseListener(this::onResponse);
        mqttClientService.subscribeResponse();
        log.info("MQTT response listener initialized");
    }

    /**
     * 处理接收到的响应
     */
    private void onResponse(LicenseResponse response) {
        log.info("Received response, requestId: {}, status: {}", 
            response.getRequestId(), response.getStatus());
        
        boolean completed = responseWaitManager.completeRequest(response);
        
        if (completed) {
            log.debug("Response processed successfully, requestId: {}", response.getRequestId());
        } else {
            log.warn("Failed to process response, requestId: {} (may already timeout)", response.getRequestId());
        }
    }
}
