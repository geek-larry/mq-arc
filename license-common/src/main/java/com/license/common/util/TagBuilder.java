package com.license.common.util;

import com.license.common.enums.OperationType;
import com.license.common.enums.SoftwareType;

/**
 * Tag 构建器
 * 格式: {softwareType}:{operation}
 */
public class TagBuilder {

    private TagBuilder() {}

    /**
     * 构建Tag
     */
    public static String build(SoftwareType softwareType, OperationType operationType) {
        if (softwareType == null || operationType == null) {
            return "unknown";
        }
        return softwareType.getCode() + ":" + operationType.getCode();
    }

    /**
     * 构建Tag（字符串参数）
     */
    public static String build(String softwareType, String operation) {
        if (softwareType == null || operation == null) {
            return "unknown";
        }
        return softwareType + ":" + operation;
    }

    /**
     * 解析Tag
     */
    public static TagInfo parse(String tag) {
        if (tag == null || tag.isEmpty()) {
            return null;
        }

        String[] parts = tag.split(":");
        if (parts.length < 2) {
            return null;
        }

        return new TagInfo(parts[0], parts[1]);
    }

    /**
     * 获取软件类型通配符Tag
     * 用于订阅某软件类型的所有操作
     */
    public static String getSoftwareWildcardTag(SoftwareType softwareType) {
        return softwareType.getCode() + ":*";
    }

    /**
     * 获取操作类型通配符Tag
     * 用于订阅某操作类型的所有软件
     */
    public static String getOperationWildcardTag(OperationType operationType) {
        return "*:" + operationType.getCode();
    }

    /**
     * Tag信息
     */
    public static class TagInfo {
        private final String softwareType;
        private final String operation;

        public TagInfo(String softwareType, String operation) {
            this.softwareType = softwareType;
            this.operation = operation;
        }

        public String getSoftwareType() { return softwareType; }
        public String getOperation() { return operation; }
    }
}
