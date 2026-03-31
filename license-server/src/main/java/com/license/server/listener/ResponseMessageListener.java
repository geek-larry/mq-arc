package com.license.server.listener;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.license.common.message.LicenseResponse;
import com.license.server.service.MessagePublishService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class ResponseMessageListener {

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private MessagePublishService messagePublishService;

    public void onMessage(String message) {
        try {
            log.debug("Received response message: {}", message);
            
            LicenseResponse<?> response = objectMapper.readValue(message, LicenseResponse.class);
            
            log.info("Processing response for businessKey: {}, status: {}", 
                    response.getBusinessKey(), response.getStatus());
            
            messagePublishService.handleResponse(response);
            
        } catch (Exception e) {
            log.error("Failed to process response message: {}", message, e);
        }
    }
}
