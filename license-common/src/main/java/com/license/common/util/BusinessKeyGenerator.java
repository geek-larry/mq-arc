package com.license.common.util;

import com.license.common.enums.OperationType;
import com.license.common.enums.SoftwareType;

import java.util.UUID;

/**
 * BusinessKey 生成器
 * 格式: {softwareType}:{hostname}:{operation}:{timestamp}:{shortUuid}
 */
public class BusinessKeyGenerator {

    private BusinessKeyGenerator() {}

    /**
     * 生成BusinessKey
     */
    public static String generate(SoftwareType softwareType, String hostname, OperationType operationType) {
        return String.format("%s:%s:%s:%d:%s",
                softwareType != null ? softwareType.getCode() : "unknown",
                hostname != null ? hostname : "unknown",
                operationType != null ? operationType.getCode() : "unknown",
                System.currentTimeMillis(),
                generateShortUuid());
    }

    /**
     * 解析BusinessKey
     */
    public static BusinessKeyInfo parse(String businessKey) {
        if (businessKey == null || businessKey.isEmpty()) {
            return null;
        }

        String[] parts = businessKey.split(":");
        if (parts.length < 5) {
            return null;
        }

        return BusinessKeyInfo.builder()
                .softwareType(parts[0])
                .hostname(parts[1])
                .operation(parts[2])
                .timestamp(Long.parseLong(parts[3]))
                .shortUuid(parts[4])
                .build();
    }

    /**
     * 从BusinessKey中提取软件类型
     */
    public static String extractSoftwareType(String businessKey) {
        BusinessKeyInfo info = parse(businessKey);
        return info != null ? info.getSoftwareType() : null;
    }

    /**
     * 从BusinessKey中提取主机名
     */
    public static String extractHostname(String businessKey) {
        BusinessKeyInfo info = parse(businessKey);
        return info != null ? info.getHostname() : null;
    }

    /**
     * 生成短UUID（8位）
     */
    private static String generateShortUuid() {
        return UUID.randomUUID().toString().replace("-", "").substring(0, 8);
    }

    /**
     * BusinessKey信息
     */
    public static class BusinessKeyInfo {
        private String softwareType;
        private String hostname;
        private String operation;
        private Long timestamp;
        private String shortUuid;

        public static BusinessKeyInfo builder() {
            return new BusinessKeyInfo();
        }

        public BusinessKeyInfo softwareType(String softwareType) {
            this.softwareType = softwareType;
            return this;
        }

        public BusinessKeyInfo hostname(String hostname) {
            this.hostname = hostname;
            return this;
        }

        public BusinessKeyInfo operation(String operation) {
            this.operation = operation;
            return this;
        }

        public BusinessKeyInfo timestamp(Long timestamp) {
            this.timestamp = timestamp;
            return this;
        }

        public BusinessKeyInfo shortUuid(String shortUuid) {
            this.shortUuid = shortUuid;
            return this;
        }

        public BusinessKeyInfo build() {
            return this;
        }

        public String getSoftwareType() { return softwareType; }
        public String getHostname() { return hostname; }
        public String getOperation() { return operation; }
        public Long getTimestamp() { return timestamp; }
        public String getShortUuid() { return shortUuid; }
    }
}
