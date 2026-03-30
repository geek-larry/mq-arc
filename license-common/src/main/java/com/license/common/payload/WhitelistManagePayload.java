package com.license.common.payload;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.List;

/**
 * 白名单管理请求Payload
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class WhitelistManagePayload implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 用户列表（批量操作时使用）
     */
    private List<WhitelistUser> users;

    /**
     * 单个用户（单条操作时使用）
     */
    private WhitelistUser user;

    /**
     * 查询条件
     */
    private String queryCondition;

    /**
     * 是否覆盖已有配置（初始化时使用）
     */
    private Boolean overwrite;

    /**
     * 白名单用户
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class WhitelistUser implements Serializable {
        private static final long serialVersionUID = 1L;

        private String userId;
        private String username;
        private String department;
        private String email;
        private List<String> allowedFeatures;
        private String expireDate;
        private String remark;
    }
}
