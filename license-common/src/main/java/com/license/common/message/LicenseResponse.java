package com.license.common.message;

import com.license.common.enums.MessageStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 许可管理响应消息
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LicenseResponse<T> implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 响应消息ID
     */
    private String responseId;

    /**
     * 对应的请求消息ID
     */
    private String requestMessageId;

    /**
     * 关联ID，与请求消息一致
     */
    private String correlationId;

    /**
     * 业务标识Key
     */
    private String businessKey;

    /**
     * 处理状态
     */
    private MessageStatus status;

    /**
     * 响应数据
     */
    private T data;

    /**
     * 错误码
     */
    private String errorCode;

    /**
     * 错误信息
     */
    private String errorMessage;

    /**
     * 处理耗时（毫秒）
     */
    private Long processTimeMillis;

    /**
     * 响应时间
     */
    private LocalDateTime responseTime;

    /**
     * 处理服务器主机名
     */
    private String processedBy;

    /**
     * 创建成功响应
     */
    public static <T> LicenseResponse<T> success(String requestMessageId, String correlationId, 
                                                  String businessKey, T data, String processedBy) {
        return LicenseResponse.<T>builder()
                .responseId(java.util.UUID.randomUUID().toString())
                .requestMessageId(requestMessageId)
                .correlationId(correlationId)
                .businessKey(businessKey)
                .status(MessageStatus.SUCCESS)
                .data(data)
                .responseTime(LocalDateTime.now())
                .processedBy(processedBy)
                .build();
    }

    /**
     * 创建失败响应
     */
    public static <T> LicenseResponse<T> failure(String requestMessageId, String correlationId,
                                                  String businessKey, String errorCode, 
                                                  String errorMessage, String processedBy) {
        return LicenseResponse.<T>builder()
                .responseId(java.util.UUID.randomUUID().toString())
                .requestMessageId(requestMessageId)
                .correlationId(correlationId)
                .businessKey(businessKey)
                .status(MessageStatus.FAILED)
                .errorCode(errorCode)
                .errorMessage(errorMessage)
                .responseTime(LocalDateTime.now())
                .processedBy(processedBy)
                .build();
    }

    /**
     * 是否成功
     */
    public boolean isSuccess() {
        return status == MessageStatus.SUCCESS;
    }
}
