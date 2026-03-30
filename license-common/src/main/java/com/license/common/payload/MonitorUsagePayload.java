package com.license.common.payload;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.List;

/**
 * 监控数据采集请求Payload
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MonitorUsagePayload implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 监控对象列表
     */
    private List<MonitorTarget> targets;

    /**
     * 是否批量采集
     */
    private Boolean batchMode;

    /**
     * 采集时间戳
     */
    private Long collectTimestamp;

    /**
     * 监控对象
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class MonitorTarget implements Serializable {
        private static final long serialVersionUID = 1L;

        /**
         * 软件名称
         */
        private String softwareName;

        /**
         * 特征码
         */
        private String featureCode;

        /**
         * 版本号
         */
        private String version;
    }

    /**
     * 监控响应数据
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class UsageData implements Serializable {
        private static final long serialVersionUID = 1L;

        /**
         * 软件名称
         */
        private String softwareName;

        /**
         * 特征码
         */
        private String featureCode;

        /**
         * 总许可数
         */
        private Integer totalLicenses;

        /**
         * 已使用许可数
         */
        private Integer usedLicenses;

        /**
         * 剩余许可数
         */
        private Integer availableLicenses;

        /**
         * 使用详情
         */
        private List<UsageDetail> usageDetails;

        /**
         * 采集时间
         */
        private String collectTime;
    }

    /**
     * 使用详情
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class UsageDetail implements Serializable {
        private static final long serialVersionUID = 1L;

        private String userId;
        private String username;
        private String host;
        private String display;
        private String version;
        private String serverHost;
        private String port;
        private String handle;
        private String checkoutTime;
        private String idleTime;
    }
}
