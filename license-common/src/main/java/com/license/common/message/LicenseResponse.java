package com.license.common.message;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * 许可管理响应消息
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LicenseResponse implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 对应请求的消息ID
     */
    private String requestId;

    /**
     * 响应状态
     */
    private String status;

    /**
     * 响应消息
     */
    private String message;

    /**
     * 响应数据
     */
    private Object data;

    /**
     * 时间戳
     */
    private Long timestamp;
}
