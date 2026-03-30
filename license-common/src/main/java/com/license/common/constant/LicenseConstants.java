package com.license.common.constant;

/**
 * 许可管理系统常量定义
 */
public final class LicenseConstants {

    private LicenseConstants() {}

    // ==================== Topic 定义 ====================
    /**
     * 浮动许可用户管理 Topic
     */
    public static final String TOPIC_USER_MGMT = "license-user-mgmt";

    /**
     * 许可服务管理 Topic
     */
    public static final String TOPIC_SERVICE_MGMT = "license-service-mgmt";

    /**
     * 许可白名单管理 Topic
     */
    public static final String TOPIC_WHITELIST_MGMT = "license-whitelist-mgmt";

    /**
     * 许可文件及日志管理 Topic
     */
    public static final String TOPIC_FILE_MGMT = "license-file-mgmt";

    /**
     * 许可监控数据采集 Topic
     */
    public static final String TOPIC_MONITOR = "license-monitor";

    // ==================== 软件类型 ====================
    public static final String SOFTWARE_FLEXNET = "flexnet";
    public static final String SOFTWARE_SENTINEL = "sentinel";
    public static final String SOFTWARE_LMX = "lmx";

    public static final String[] ALL_SOFTWARE_TYPES = {
        SOFTWARE_FLEXNET, SOFTWARE_SENTINEL, SOFTWARE_LMX
    };

    // ==================== 用户管理操作 ====================
    public static final String OP_KICKOUT = "kickout";

    // ==================== 服务管理操作 ====================
    public static final String OP_START = "start";
    public static final String OP_STOP = "stop";
    public static final String OP_RESTART = "restart";

    // ==================== 白名单操作 ====================
    public static final String OP_INIT = "init";
    public static final String OP_ADD = "add";
    public static final String OP_REMOVE = "remove";
    public static final String OP_QUERY = "query";

    // ==================== 文件管理操作 ====================
    public static final String OP_LICENSE_QUERY = "license:query";
    public static final String OP_LICENSE_UPDATE = "license:update";
    public static final String OP_LICENSE_DOWNLOAD = "license:download";
    public static final String OP_LOG_QUERY = "log:query";
    public static final String OP_LOG_DOWNLOAD = "log:download";

    // ==================== 监控操作 ====================
    public static final String OP_USAGE = "usage";

    // ==================== 消息属性键 ====================
    public static final String MSG_KEY_BUSINESS_KEY = "businessKey";
    public static final String MSG_KEY_HOSTNAME = "hostname";
    public static final String MSG_KEY_SOFTWARE_TYPE = "softwareType";
    public static final String MSG_KEY_OPERATION = "operation";
    public static final String MSG_KEY_TIMESTAMP = "timestamp";
    public static final String MSG_KEY_CORRELATION_ID = "correlationId";

    // ==================== 响应状态 ====================
    public static final String STATUS_SUCCESS = "SUCCESS";
    public static final String STATUS_FAILED = "FAILED";
    public static final String STATUS_TIMEOUT = "TIMEOUT";
    public static final String STATUS_PENDING = "PENDING";

    // ==================== 重试配置 ====================
    public static final int MAX_RETRY_TIMES = 3;
    public static final long RETRY_INTERVAL_MS = 5000;
    public static final long MESSAGE_TIMEOUT_MS = 30000;

    // ==================== 消费者组 ====================
    public static final String CONSUMER_GROUP_CLIENT = "license-client-consumer-group";
    public static final String CONSUMER_GROUP_SERVER = "license-server-consumer-group";
}
