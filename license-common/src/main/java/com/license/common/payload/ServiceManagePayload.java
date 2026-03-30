package com.license.common.payload;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.Map;

/**
 * 服务管理请求Payload
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ServiceManagePayload implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 服务名称
     */
    private String serviceName;

    /**
     * 服务端口
     */
    private Integer port;

    /**
     * 启动参数
     */
    private Map<String, String> startupParams;

    /**
     * 超时时间（秒）
     */
    private Integer timeoutSeconds;

    /**
     * 是否等待服务完全启动/停止
     */
    private Boolean waitForCompletion;
}
