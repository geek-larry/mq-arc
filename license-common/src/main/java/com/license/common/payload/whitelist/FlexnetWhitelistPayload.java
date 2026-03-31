package com.license.common.payload.whitelist;

import com.license.common.payload.BasePayload;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FlexnetWhitelistPayload implements BasePayload {

    private static final long serialVersionUID = 1L;

    private List<FlexnetWhitelistUser> users;
    private FlexnetWhitelistUser user;
    private String queryFilter;
    private Boolean overwrite;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class FlexnetWhitelistUser implements Serializable {
        private static final long serialVersionUID = 1L;

        private String userId;
        private String username;
        private String hostId;
        private String email;
        private List<String> allowedFeatures;
        private String expirationDate;
        private String notes;
    }

    @Override
    public String getSoftwareType() {
        return "flexnet";
    }
}
