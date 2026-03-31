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
public class LmxWhitelistPayload implements BasePayload {

    private static final long serialVersionUID = 1L;

    private List<LmxWhitelistUser> users;
    private LmxWhitelistUser user;
    private String queryFilter;
    private Boolean overwrite;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class LmxWhitelistUser implements Serializable {
        private static final long serialVersionUID = 1L;

        private String userId;
        private String username;
        private String computerName;
        private String ipAddress;
        private String email;
        private List<String> allowedProducts;
        private String expirationDate;
        private String notes;
    }

    @Override
    public String getSoftwareType() {
        return "lmx";
    }
}
