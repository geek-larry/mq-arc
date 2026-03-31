package com.license.common.payload.kickout;

import com.license.common.payload.BasePayload;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SentinelUserKickoutPayload implements BasePayload {

    private static final long serialVersionUID = 1L;

    private String sessionId;
    private String userId;
    private String username;
    private String clientIp;
    private String clientHost;
    private String featureId;
    private String featureName;
    private String reason;
    private Boolean force;

    @Override
    public String getSoftwareType() {
        return "sentinel";
    }
}
