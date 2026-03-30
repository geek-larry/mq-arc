package com.license.client.handler.impl;

import com.license.client.handler.AbstractMessageHandler;
import com.license.common.enums.OperationType;
import com.license.common.enums.SoftwareType;
import com.license.common.message.LicenseMessage;
import com.license.common.payload.WhitelistManagePayload;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * 白名单管理处理器
 * 处理白名单的初始化、添加、删除、查询请求
 */
@Slf4j
@Component
public class WhitelistManageHandler extends AbstractMessageHandler<WhitelistManagePayload, Object> {

    @Autowired
    private String clientHostname;

    // 模拟白名单数据存储
    private final Map<String, List<WhitelistManagePayload.WhitelistUser>> whitelistStore = new ConcurrentHashMap<>();

    @Override
    public String getOperationType() {
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
        return message.getOperationType() == OperationType.INIT ||
               message.getOperationType() == OperationType.ADD ||
               message.getOperationType() == OperationType.REMOVE ||
               message.getOperationType() == OperationType.QUERY;
    }

    @Override
    protected Object doHandle(LicenseMessage<WhitelistManagePayload> message) throws Exception {
        WhitelistManagePayload payload = message.getPayload();
        SoftwareType softwareType = message.getSoftwareType();
        OperationType operationType = message.getOperationType();
        
        String storeKey = buildStoreKey(softwareType);
        
        log.info("Managing whitelist for {}: operation={}", softwareType, operationType);
        
        // 模拟处理延迟
        simulateDelay(100, 300);
        
        switch (operationType) {
            case INIT:
                return handleInit(storeKey, payload, softwareType);
            case ADD:
                return handleAdd(storeKey, payload, softwareType);
            case REMOVE:
                return handleRemove(storeKey, payload, softwareType);
            case QUERY:
                return handleQuery(storeKey, payload, softwareType);
            default:
                throw new UnsupportedOperationException("Unsupported operation: " + operationType);
        }
    }

    /**
     * 处理初始化
     */
    private Void handleInit(String storeKey, WhitelistManagePayload payload, SoftwareType softwareType) {
        log.debug("Initializing {} whitelist with {} users", 
                softwareType, payload.getUsers() != null ? payload.getUsers().size() : 0);
        
        if (Boolean.TRUE.equals(payload.getOverwrite()) || !whitelistStore.containsKey(storeKey)) {
            whitelistStore.put(storeKey, new ArrayList<>());
        }
        
        if (payload.getUsers() != null) {
            whitelistStore.get(storeKey).addAll(payload.getUsers());
        }
        
        log.info("{} whitelist initialized with {} users", 
                softwareType, whitelistStore.get(storeKey).size());
        return null;
    }

    /**
     * 处理添加用户
     */
    private Void handleAdd(String storeKey, WhitelistManagePayload payload, SoftwareType softwareType) {
        if (payload.getUser() == null) {
            throw new IllegalArgumentException("User is required for add operation");
        }
        
        List<WhitelistManagePayload.WhitelistUser> whitelist = 
                whitelistStore.computeIfAbsent(storeKey, k -> new ArrayList<>());
        
        // 检查是否已存在
        boolean exists = whitelist.stream()
                .anyMatch(u -> u.getUserId().equals(payload.getUser().getUserId()));
        
        if (!exists) {
            whitelist.add(payload.getUser());
            log.info("Added user [{}] to {} whitelist", payload.getUser().getUsername(), softwareType);
        } else {
            log.warn("User [{}] already exists in {} whitelist", payload.getUser().getUsername(), softwareType);
        }
        
        return null;
    }

    /**
     * 处理删除用户
     */
    private Void handleRemove(String storeKey, WhitelistManagePayload payload, SoftwareType softwareType) {
        if (payload.getUser() == null) {
            throw new IllegalArgumentException("User is required for remove operation");
        }
        
        List<WhitelistManagePayload.WhitelistUser> whitelist = whitelistStore.get(storeKey);
        if (whitelist == null) {
            log.warn("No whitelist found for {}", softwareType);
            return null;
        }
        
        boolean removed = whitelist.removeIf(u -> u.getUserId().equals(payload.getUser().getUserId()));
        
        if (removed) {
            log.info("Removed user [{}] from {} whitelist", payload.getUser().getUsername(), softwareType);
        } else {
            log.warn("User [{}] not found in {} whitelist", payload.getUser().getUsername(), softwareType);
        }
        
        return null;
    }

    /**
     * 处理查询
     */
    private List<WhitelistManagePayload.WhitelistUser> handleQuery(
            String storeKey, WhitelistManagePayload payload, SoftwareType softwareType) {
        
        List<WhitelistManagePayload.WhitelistUser> whitelist = whitelistStore.getOrDefault(storeKey, new ArrayList<>());
        
        String queryCondition = payload.getQueryCondition();
        if (queryCondition == null || queryCondition.isEmpty()) {
            log.info("Query {} whitelist, returning {} users", softwareType, whitelist.size());
            return whitelist;
        }
        
        // 简单过滤逻辑
        List<WhitelistManagePayload.WhitelistUser> filtered = whitelist.stream()
                .filter(u -> u.getUsername().contains(queryCondition) ||
                            u.getUserId().contains(queryCondition) ||
                            u.getDepartment().contains(queryCondition))
                .collect(Collectors.toList());
        
        log.info("Query {} whitelist with condition [{}], returning {} users", 
                softwareType, queryCondition, filtered.size());
        return filtered;
    }

    private String buildStoreKey(SoftwareType softwareType) {
        return clientHostname + ":" + softwareType.getCode() + ":whitelist";
    }

    @Override
    protected String getClientHostname() {
        return clientHostname;
    }
}
