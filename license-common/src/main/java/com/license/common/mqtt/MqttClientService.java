package com.license.common.mqtt;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.license.common.config.MqttProperties;
import com.license.common.message.LicenseMessage;
import com.license.common.message.LicenseResponse;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.paho.client.mqttv3.*;

/**
 * MQTT客户端服务
 * 支持P2P通信模式，基于clientId路由
 */
@Slf4j
public class MqttClientService implements MqttCallback {

    private final MqttProperties properties;
    private final ObjectMapper objectMapper;
    private final MqttAsyncClient client;
    private final String clientId;
    
    private MqttMessageListener messageListener;
    private MqttResponseListener responseListener;
    private volatile boolean isConnected = false;

    public MqttClientService(MqttProperties properties, ObjectMapper objectMapper, String hostname) throws MqttException {
        this.properties = properties;
        this.objectMapper = objectMapper;
        this.clientId = hostname; // 使用hostname作为clientId
        
        this.client = new MqttAsyncClient(properties.getBrokerUrl(), clientId);
        this.client.setCallback(this);
    }

    public void start() {
        try {
            MqttConnectOptions options = new MqttConnectOptions();
            options.setCleanSession(properties.isCleanSession());
            options.setConnectionTimeout(properties.getConnectionTimeout());
            options.setKeepAliveInterval(properties.getKeepAliveInterval());
            options.setAutomaticReconnect(properties.isAutoReconnect());
            
            if (properties.getUsername() != null && !properties.getUsername().isEmpty()) {
                options.setUserName(properties.getUsername());
            }
            if (properties.getPassword() != null && !properties.getPassword().isEmpty()) {
                options.setPassword(properties.getPassword().toCharArray());
            }
            
            log.info("Connecting to MQTT broker: {}, clientId: {}", properties.getBrokerUrl(), clientId);
            client.connect(options).waitForCompletion();
            isConnected = true;
            
            log.info("MQTT client connected successfully, clientId: {}", clientId);
        } catch (Exception e) {
            log.error("Failed to start MQTT client, clientId: {}", clientId, e);
            throw new RuntimeException("Failed to start MQTT client", e);
        }
    }

    public void stop() {
        try {
            isConnected = false;
            
            if (client.isConnected()) {
                client.disconnect().waitForCompletion();
                log.info("MQTT client disconnected, clientId: {}", clientId);
            }
            client.close();
            log.info("MQTT client closed, clientId: {}", clientId);
        } catch (Exception e) {
            log.error("Failed to stop MQTT client, clientId: {}", clientId, e);
        }
    }

    /**
     * 发送消息到指定客户端（P2P模式）
     * Topic格式: monitor/{targetClientId}
     */
    public void sendToClient(String targetClientId, LicenseMessage<?> message) {
        if (!isConnected || !client.isConnected()) {
            throw new IllegalStateException("MQTT client is not connected");
        }
        
        String topic = buildP2PTopic(targetClientId);
        
        try {
            String json = objectMapper.writeValueAsString(message);
            MqttMessage mqttMessage = new MqttMessage(json.getBytes());
            mqttMessage.setQos(properties.getQos());
            
            log.info("Sending P2P message to client: {}, messageId: {}, size: {} bytes",
                targetClientId, message.getMessageId(), json.length());
            
            client.publish(topic, mqttMessage).waitForCompletion();
        } catch (Exception e) {
            log.error("Failed to send P2P message, targetClientId: {}", targetClientId, e);
            throw new RuntimeException("Failed to send P2P message", e);
        }
    }

    /**
     * 订阅接收消息（P2P模式）
     * 订阅Topic: monitor/{clientId}
     */
    public void subscribeIncomingMessages() {
        String topic = "monitor/" + clientId;
        subscribe(topic);
        log.info("Subscribed to incoming messages, topic: {}", topic);
    }

    /**
     * 订阅Topic
     */
    private void subscribe(String topic) {
        try {
            client.subscribe(topic, properties.getQos()).waitForCompletion();
            log.debug("Subscribed to topic: {}", topic);
        } catch (Exception e) {
            log.error("Failed to subscribe to topic: {}", topic, e);
            throw new RuntimeException("Failed to subscribe to topic", e);
        }
    }

    /**
     * 构建P2P Topic
     * Topic格式: monitor/{targetClientId}
     */
    private String buildP2PTopic(String targetClientId) {
        return "monitor/" + targetClientId;
    }

    /**
     * 设置消息监听器
     */
    public void setMessageListener(MqttMessageListener messageListener) {
        this.messageListener = messageListener;
    }

    @Override
    public void connectionLost(Throwable cause) {
        isConnected = false;
        log.error("MQTT connection lost, clientId: {}, will auto-reconnect if enabled", clientId, cause);
    }

    @Override
    public void messageArrived(String topic, MqttMessage message) throws Exception {
        try {
            String body = new String(message.getPayload());
            
            log.info("Received message, topic: {}, size: {} bytes", topic, body.length());
            
            if (topic.endsWith("/response")) {
                handleResponse(body);
            } else {
                handleRequest(body);
            }
        } catch (Exception e) {
            log.error("Failed to process arrived message, topic: {}, clientId: {}", topic, clientId, e);
        }
    }

    /**
     * 处理请求消息
     */
    private void handleRequest(String body) throws Exception {
        LicenseMessage<?> licenseMessage = objectMapper.readValue(body, LicenseMessage.class);
        
        if (messageListener != null) {
            messageListener.onMessage(licenseMessage);
        } else {
            log.warn("No message listener registered, message ignored");
        }
    }

    /**
     * 处理响应消息
     */
    private void handleResponse(String body) throws Exception {
        LicenseResponse response = objectMapper.readValue(body, LicenseResponse.class);
        
        if (responseListener != null) {
            responseListener.onResponse(response);
        } else {
            log.warn("No response listener registered, response ignored, requestId: {}", response.getRequestId());
        }
    }

    /**
     * 发送响应消息
     * 
     * @param targetClientId 目标客户端ID
     * @param response 响应消息
     */
    public void sendResponse(String targetClientId, LicenseResponse response) {
        if (!isConnected || !client.isConnected()) {
            throw new IllegalStateException("MQTT client is not connected");
        }
        
        String topic = buildResponseTopic(targetClientId);
        
        try {
            String json = objectMapper.writeValueAsString(response);
            MqttMessage mqttMessage = new MqttMessage(json.getBytes());
            mqttMessage.setQos(properties.getQos());
            
            log.info("Sending response to client: {}, requestId: {}, status: {}",
                targetClientId, response.getRequestId(), response.getStatus());
            
            client.publish(topic, mqttMessage).waitForCompletion();
        } catch (Exception e) {
            log.error("Failed to send response, targetClientId: {}", targetClientId, e);
            throw new RuntimeException("Failed to send response", e);
        }
    }

    /**
     * 构建响应Topic
     * 格式: monitor/{targetClientId}/response
     */
    private String buildResponseTopic(String targetClientId) {
        return "monitor/" + targetClientId + "/response";
    }

    /**
     * 订阅响应Topic
     */
    public void subscribeResponse() {
        String topic = "monitor/" + clientId + "/response";
        subscribe(topic);
        log.info("Subscribed to response topic: {}", topic);
    }

    public void setResponseListener(MqttResponseListener responseListener) {
        this.responseListener = responseListener;
    }

    @Override
    public void deliveryComplete(IMqttDeliveryToken token) {
        log.debug("Message delivery complete, messageId: {}", token.getMessageId());
    }
    
    /**
     * 消息监听器接口
     */
    public interface MqttMessageListener {
        void onMessage(LicenseMessage<?> message);
    }

    /**
     * 响应监听器接口
     */
    public interface MqttResponseListener {
        void onResponse(LicenseResponse response);
    }
}
