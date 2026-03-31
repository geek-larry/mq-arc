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
public class FlexnetUserKickoutPayload implements BasePayload {

    private static final long serialVersionUID = 1L;

    private String userId;
    private String username;
    private String hostId;
    private String displayName;
    private String featureName;
    private String featureVersion;
    private String reason;
    private Boolean force;

    @Override
    public String getSoftwareType() {
        return "flexnet";
    }
}
