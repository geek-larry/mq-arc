package com.license.common.payload;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * 用户踢出请求Payload
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserKickoutPayload implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 用户ID
     */
    private String userId;

    /**
     * 用户名
     */
    private String username;

    /**
     * 会话ID
     */
    private String sessionId;

    /**
     * 特征码（用于标识特定许可）
     */
    private String featureCode;

    /**
     * 踢出原因
     */
    private String reason;

    /**
     * 是否强制踢出
     */
    private Boolean force;
}
