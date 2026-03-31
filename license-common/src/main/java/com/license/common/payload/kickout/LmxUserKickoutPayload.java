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
public class LmxUserKickoutPayload implements BasePayload {

    private static final long serialVersionUID = 1L;

    private String userId;
    private String username;
    private String computerName;
    private String ipAddress;
    private String productName;
    private String featureToken;
    private String reason;
    private Boolean force;

    @Override
    public String getSoftwareType() {
        return "lmx";
    }
}
