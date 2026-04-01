package com.license.client.handler.impl;

import com.license.client.handler.AbstractMessageHandler;
import com.license.common.constant.LicenseConstants;
import com.license.common.enums.OperationType;
import com.license.common.enums.SoftwareType;
import com.license.common.message.LicenseMessage;
import com.license.common.payload.whitelist.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 白名单管理处理器
 * 处理白名单的初始化、添加、删除、查询请求
 */
@Slf4j
@Component
public class WhitelistManageHandler extends AbstractMessageHandler<Object, Object> {

    @Autowired
    private String clientHostname;

    private final Map<String, Object> whitelistStore = new HashMap<>();

    @Override
    public String getSupportedTopic() {
        return LicenseConstants.TOPIC_WHITELIST_MGMT;
    }

    @Override
    protected Object doHandle(LicenseMessage<Object> message) throws Exception {
        Object payloadObj = message.getPayload();
        SoftwareType softwareType = message.getSoftwareType();
        OperationType operationType = message.getOperationType();
        
        // 根据软件类型处理不同的Payload
        switch (softwareType) {
            case FLEXNET:
                return handleFlexnetWhitelist((FlexnetWhitelistPayload) payloadObj, operationType);
            case SENTINEL:
                return handleSentinelWhitelist((SentinelWhitelistPayload) payloadObj, operationType);
            case LMX:
                return handleLmxWhitelist((LmxWhitelistPayload) payloadObj, operationType);
            default:
                throw new IllegalArgumentException("Unsupported software type: " + softwareType);
        }
    }

    private Object handleFlexnetWhitelist(FlexnetWhitelistPayload payload, OperationType operationType) throws Exception {
        String storeKey = buildStoreKey(SoftwareType.FLEXNET);
        
        log.info("Managing Flexnet whitelist operation={}", operationType);
        
        simulateDelay(100, 300);
        
        switch (operationType) {
            case INIT:
                return handleFlexnetInit(storeKey, payload);
            case ADD:
                return handleFlexnetAdd(storeKey, payload);
            case REMOVE:
                return handleFlexnetRemove(storeKey, payload);
            case QUERY:
                return handleFlexnetQuery(storeKey, payload);
            default:
                throw new UnsupportedOperationException("Unsupported operation: " + operationType);
        }
    }

    private Object handleSentinelWhitelist(SentinelWhitelistPayload payload, OperationType operationType) throws Exception {
        String storeKey = buildStoreKey(SoftwareType.SENTINEL);
        
        log.info("Managing Sentinel whitelist operation={}", operationType);
        
        simulateDelay(100, 300);
        
        switch (operationType) {
            case INIT:
                return handleSentinelInit(storeKey, payload);
            case ADD:
                return handleSentinelAdd(storeKey, payload);
            case REMOVE:
                return handleSentinelRemove(storeKey, payload);
            case QUERY:
                return handleSentinelQuery(storeKey, payload);
            default:
                throw new UnsupportedOperationException("Unsupported operation: " + operationType);
        }
    }

    private Object handleLmxWhitelist(LmxWhitelistPayload payload, OperationType operationType) throws Exception {
        String storeKey = buildStoreKey(SoftwareType.LMX);
        
        log.info("Managing LMX whitelist operation={}", operationType);
        
        simulateDelay(100, 300);
        
        switch (operationType) {
            case INIT:
                return handleLmxInit(storeKey, payload);
            case ADD:
                return handleLmxAdd(storeKey, payload);
            case REMOVE:
                return handleLmxRemove(storeKey, payload);
            case QUERY:
                return handleLmxQuery(storeKey, payload);
            default:
                throw new UnsupportedOperationException("Unsupported operation: " + operationType);
        }
    }

    @SuppressWarnings("unchecked")
    private Void handleFlexnetInit(String storeKey, FlexnetWhitelistPayload payload) {
        log.debug("Initializing Flexnet whitelist with {} users", 
                payload.getUsers() != null ? payload.getUsers().size() : 0);
        
        if (Boolean.TRUE.equals(payload.getOverwrite()) || !whitelistStore.containsKey(storeKey)) {
            whitelistStore.put(storeKey, new ArrayList<>());
        }
        
        if (payload.getUsers() != null) {
            ((List<Object>) whitelistStore.get(storeKey)).addAll(payload.getUsers());
        }
        
        log.info("Flexnet whitelist initialized with {} users", ((List<Object>) whitelistStore.get(storeKey)).size());
        return null;
    }

    @SuppressWarnings("unchecked")
    private Void handleSentinelInit(String storeKey, SentinelWhitelistPayload payload) {
        log.debug("Initializing Sentinel whitelist with {} users", 
                payload.getUsers() != null ? payload.getUsers().size() : 0);
        
        if (Boolean.TRUE.equals(payload.getOverwrite()) || !whitelistStore.containsKey(storeKey)) {
            whitelistStore.put(storeKey, new ArrayList<>());
        }
        
        if (payload.getUsers() != null) {
            ((List<Object>) whitelistStore.get(storeKey)).addAll(payload.getUsers());
        }
        
        log.info("Sentinel whitelist initialized with {} users", ((List<Object>) whitelistStore.get(storeKey)).size());
        return null;
    }

    @SuppressWarnings("unchecked")
    private Void handleLmxInit(String storeKey, LmxWhitelistPayload payload) {
        log.debug("Initializing LMX whitelist with {} users", 
                payload.getUsers() != null ? payload.getUsers().size() : 0);
        
        if (Boolean.TRUE.equals(payload.getOverwrite()) || !whitelistStore.containsKey(storeKey)) {
            whitelistStore.put(storeKey, new ArrayList<>());
        }
        
        if (payload.getUsers() != null) {
            ((List<Object>) whitelistStore.get(storeKey)).addAll(payload.getUsers());
        }
        
        log.info("LMX whitelist initialized with {} users", ((List<Object>) whitelistStore.get(storeKey)).size());
        return null;
    }

    @SuppressWarnings("unchecked")
    private Void handleFlexnetAdd(String storeKey, FlexnetWhitelistPayload payload) {
        if (payload.getUser() == null) {
            throw new IllegalArgumentException("User is required for add operation");
        }
        
        List<Object> whitelist = (List<Object>) whitelistStore.computeIfAbsent(storeKey, k -> new ArrayList<>());
        
        boolean exists = whitelist.stream()
                .anyMatch(u -> {
                    FlexnetWhitelistPayload.FlexnetWhitelistUser user = (FlexnetWhitelistPayload.FlexnetWhitelistUser) u;
                    return user.getUserId().equals(payload.getUser().getUserId());
                });
        
        if (!exists) {
            whitelist.add(payload.getUser());
            log.info("Added user [{}] to Flexnet whitelist", payload.getUser().getUsername());
        } else {
            log.warn("User [{}] already exists in Flexnet whitelist", payload.getUser().getUsername());
        }
        
        return null;
    }

    @SuppressWarnings("unchecked")
    private Void handleSentinelAdd(String storeKey, SentinelWhitelistPayload payload) {
        if (payload.getUser() == null) {
            throw new IllegalArgumentException("User is required for add operation");
        }
        
        List<Object> whitelist = (List<Object>) whitelistStore.computeIfAbsent(storeKey, k -> new ArrayList<>());
        
        boolean exists = whitelist.stream()
                .anyMatch(u -> {
                    SentinelWhitelistPayload.SentinelWhitelistUser user = (SentinelWhitelistPayload.SentinelWhitelistUser) u;
                    return user.getUserId().equals(payload.getUser().getUserId());
                });
        
        if (!exists) {
            whitelist.add(payload.getUser());
            log.info("Added user [{}] to Sentinel whitelist", payload.getUser().getUsername());
        } else {
            log.warn("User [{}] already exists in Sentinel whitelist", payload.getUser().getUsername());
        }
        
        return null;
    }

    @SuppressWarnings("unchecked")
    private Void handleLmxAdd(String storeKey, LmxWhitelistPayload payload) {
        if (payload.getUser() == null) {
            throw new IllegalArgumentException("User is required for add operation");
        }
        
        List<Object> whitelist = (List<Object>) whitelistStore.computeIfAbsent(storeKey, k -> new ArrayList<>());
        
        boolean exists = whitelist.stream()
                .anyMatch(u -> {
                    LmxWhitelistPayload.LmxWhitelistUser user = (LmxWhitelistPayload.LmxWhitelistUser) u;
                    return user.getUserId().equals(payload.getUser().getUserId());
                });
        
        if (!exists) {
            whitelist.add(payload.getUser());
            log.info("Added user [{}] to LMX whitelist", payload.getUser().getUsername());
        } else {
            log.warn("User [{}] already exists in LMX whitelist", payload.getUser().getUsername());
        }
        
        return null;
    }

    @SuppressWarnings("unchecked")
    private Void handleFlexnetRemove(String storeKey, FlexnetWhitelistPayload payload) {
        if (payload.getUser() == null) {
            throw new IllegalArgumentException("User is required for remove operation");
        }
        
        List<Object> whitelist = (List<Object>) whitelistStore.get(storeKey);
        if (whitelist == null) {
            log.warn("No Flexnet whitelist found");
            return null;
        }
        
        Iterator<Object> iterator = whitelist.iterator();
        boolean removed = false;
        while (iterator.hasNext()) {
            Object obj = iterator.next();
            if (obj instanceof FlexnetWhitelistPayload.FlexnetWhitelistUser) {
                FlexnetWhitelistPayload.FlexnetWhitelistUser user = 
                        (FlexnetWhitelistPayload.FlexnetWhitelistUser) obj;
                if (user.getUserId().equals(payload.getUser().getUserId())) {
                    iterator.remove();
                    removed = true;
                }
            }
        }
        
        if (removed) {
            log.info("Removed user [{}] from Flexnet whitelist", payload.getUser().getUsername());
        } else {
            log.warn("User [{}] not found in Flexnet whitelist", payload.getUser().getUsername());
        }
        
        return null;
    }

    @SuppressWarnings("unchecked")
    private Void handleSentinelRemove(String storeKey, SentinelWhitelistPayload payload) {
        if (payload.getUser() == null) {
            throw new IllegalArgumentException("User is required for remove operation");
        }
        
        List<Object> whitelist = (List<Object>) whitelistStore.get(storeKey);
        if (whitelist == null) {
            log.warn("No Sentinel whitelist found");
            return null;
        }
        
        Iterator<Object> iterator = whitelist.iterator();
        boolean removed = false;
        while (iterator.hasNext()) {
            Object obj = iterator.next();
            if (obj instanceof SentinelWhitelistPayload.SentinelWhitelistUser) {
                SentinelWhitelistPayload.SentinelWhitelistUser user = 
                        (SentinelWhitelistPayload.SentinelWhitelistUser) obj;
                if (user.getUserId().equals(payload.getUser().getUserId())) {
                    iterator.remove();
                    removed = true;
                }
            }
        }
        
        if (removed) {
            log.info("Removed user [{}] from Sentinel whitelist", payload.getUser().getUsername());
        } else {
            log.warn("User [{}] not found in Sentinel whitelist", payload.getUser().getUsername());
        }
        
        return null;
    }

    @SuppressWarnings("unchecked")
    private Void handleLmxRemove(String storeKey, LmxWhitelistPayload payload) {
        if (payload.getUser() == null) {
            throw new IllegalArgumentException("User is required for remove operation");
        }
        
        List<Object> whitelist = (List<Object>) whitelistStore.get(storeKey);
        if (whitelist == null) {
            log.warn("No LMX whitelist found");
            return null;
        }
        
        Iterator<Object> iterator = whitelist.iterator();
        boolean removed = false;
        while (iterator.hasNext()) {
            Object obj = iterator.next();
            if (obj instanceof LmxWhitelistPayload.LmxWhitelistUser) {
                LmxWhitelistPayload.LmxWhitelistUser user = (LmxWhitelistPayload.LmxWhitelistUser) obj;
                if (user.getUserId().equals(payload.getUser().getUserId())) {
                    iterator.remove();
                    removed = true;
                }
            }
        }
        
        if (removed) {
            log.info("Removed user [{}] from LMX whitelist", payload.getUser().getUsername());
        } else {
            log.warn("User [{}] not found in LMX whitelist", payload.getUser().getUsername());
        }
        
        return null;
    }

    @SuppressWarnings("unchecked")
    private List<FlexnetWhitelistPayload.FlexnetWhitelistUser> handleFlexnetQuery(
            String storeKey, FlexnetWhitelistPayload payload) {
        
        List<Object> whitelist = (List<Object>) whitelistStore.getOrDefault(storeKey, new ArrayList<>());
        
        String queryFilter = payload.getQueryFilter();
        if (queryFilter == null || queryFilter.isEmpty()) {
            List<FlexnetWhitelistPayload.FlexnetWhitelistUser> result = whitelist.stream()
                    .filter(u -> u instanceof FlexnetWhitelistPayload.FlexnetWhitelistUser)
                    .map(u -> (FlexnetWhitelistPayload.FlexnetWhitelistUser) u)
                    .collect(Collectors.toList());
            log.info("Query Flexnet whitelist, returning {} users", result.size());
            return result;
        }
        
        List<FlexnetWhitelistPayload.FlexnetWhitelistUser> filtered = whitelist.stream()
                .filter(u -> u instanceof FlexnetWhitelistPayload.FlexnetWhitelistUser)
                .map(u -> (FlexnetWhitelistPayload.FlexnetWhitelistUser) u)
                .filter(u -> u.getUsername().contains(queryFilter) ||
                            u.getUserId().contains(queryFilter) ||
                            u.getEmail().contains(queryFilter))
                .collect(Collectors.toList());
        
        log.info("Query Flexnet whitelist with filter [{}], returning {} users", 
                queryFilter, filtered.size());
        return filtered;
    }

    @SuppressWarnings("unchecked")
    private List<SentinelWhitelistPayload.SentinelWhitelistUser> handleSentinelQuery(
            String storeKey, SentinelWhitelistPayload payload) {
        
        List<Object> whitelist = (List<Object>) whitelistStore.getOrDefault(storeKey, new ArrayList<>());
        
        String queryFilter = payload.getQueryFilter();
        if (queryFilter == null || queryFilter.isEmpty()) {
            List<SentinelWhitelistPayload.SentinelWhitelistUser> result = whitelist.stream()
                    .filter(u -> u instanceof SentinelWhitelistPayload.SentinelWhitelistUser)
                    .map(u -> (SentinelWhitelistPayload.SentinelWhitelistUser) u)
                    .collect(Collectors.toList());
            log.info("Query Sentinel whitelist, returning {} users", result.size());
            return result;
        }
        
        List<SentinelWhitelistPayload.SentinelWhitelistUser> filtered = whitelist.stream()
                .filter(u -> u instanceof SentinelWhitelistPayload.SentinelWhitelistUser)
                .map(u -> (SentinelWhitelistPayload.SentinelWhitelistUser) u)
                .filter(u -> u.getUsername().contains(queryFilter) ||
                            u.getUserId().contains(queryFilter) ||
                            u.getEmail().contains(queryFilter))
                .collect(Collectors.toList());
        
        log.info("Query Sentinel whitelist with filter [{}], returning {} users", 
                queryFilter, filtered.size());
        return filtered;
    }

    @SuppressWarnings("unchecked")
    private List<LmxWhitelistPayload.LmxWhitelistUser> handleLmxQuery(
            String storeKey, LmxWhitelistPayload payload) {
        
        List<Object> whitelist = (List<Object>) whitelistStore.getOrDefault(storeKey, new ArrayList<>());
        
        String queryFilter = payload.getQueryFilter();
        if (queryFilter == null || queryFilter.isEmpty()) {
            List<LmxWhitelistPayload.LmxWhitelistUser> result = whitelist.stream()
                    .filter(u -> u instanceof LmxWhitelistPayload.LmxWhitelistUser)
                    .map(u -> (LmxWhitelistPayload.LmxWhitelistUser) u)
                    .collect(Collectors.toList());
            log.info("Query LMX whitelist, returning {} users", result.size());
            return result;
        }
        
        List<LmxWhitelistPayload.LmxWhitelistUser> filtered = whitelist.stream()
                .filter(u -> u instanceof LmxWhitelistPayload.LmxWhitelistUser)
                .map(u -> (LmxWhitelistPayload.LmxWhitelistUser) u)
                .filter(u -> u.getUsername().contains(queryFilter) ||
                            u.getUserId().contains(queryFilter) ||
                            u.getEmail().contains(queryFilter))
                .collect(Collectors.toList());
        
        log.info("Query LMX whitelist with filter [{}], returning {} users", 
                queryFilter, filtered.size());
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
