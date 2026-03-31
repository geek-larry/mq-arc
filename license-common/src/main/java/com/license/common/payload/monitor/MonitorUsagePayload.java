package com.license.common.payload.monitor;

import com.license.common.payload.BasePayload;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MonitorUsagePayload implements BasePayload {

    private static final long serialVersionUID = 1L;

    private String softwareType;
    private String serverName;
    private String productName;
    private String featureName;
    private Integer totalLicenses;
    private Integer usedLicenses;
    private Integer availableLicenses;
    private List<LicenseUser> activeUsers;
    private Map<String, Object> additionalInfo;
    private Long timestamp;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class LicenseUser implements Serializable {
        private static final long serialVersionUID = 1L;

        private String userId;
        private String username;
        private String hostName;
        private String ipAddress;
        private String featureName;
        private Integer licenseCount;
        private Long checkoutTime;
        private Long lastUpdateTime;
    }

    @Override
    public String getSoftwareType() {
        return softwareType;
    }
}
