package com.license.common.payload.renew;

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
public class FlexnetUserRenewPayload implements BasePayload {

    private static final long serialVersionUID = 1L;

    private String userId;
    private String username;
    private String hostId;
    private String featureName;
    private String featureVersion;
    private Integer extendDays;
    private String reason;

    @Override
    public String getSoftwareType() {
        return "flexnet";
    }
}
