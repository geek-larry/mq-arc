package com.license.common.payload.service;

import com.license.common.payload.BasePayload;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SentinelServiceManagePayload implements BasePayload {

    private static final long serialVersionUID = 1L;

    private String serverName;
    private String licenseFile;
    private Integer port;
    private String accessMode;
    private Map<String, String> serverOptions;
    private Integer timeoutSeconds;
    private Boolean waitForCompletion;

    @Override
    public String getSoftwareType() {
        return "sentinel";
    }
}
