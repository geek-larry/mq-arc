package com.license.common.message;

import com.license.common.enums.MessageStatus;
import com.license.common.enums.OperationType;
import com.license.common.enums.SoftwareType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.UUID;

/**
 * 许可管理消息基类
 * 所有消息类型的统一封装
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LicenseMessage<T> implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 消息唯一标识
     */
    private String messageId;

    /**
     * 业务标识Key
     * 格式: {softwareType}:{hostname}:{operation}:{timestamp}:{uuid}
     */
    private String businessKey;

    /**
     * 关联ID，用于请求-响应匹配
     */
    private String correlationId;

    /**
     * 目标服务器主机名
     */
    private String hostname;

    /**
     * 软件类型
     */
    private SoftwareType softwareType;

    /**
     * 操作类型
     */
    private OperationType operationType;

    /**
     * Topic
     */
    private String topic;

    /**
     * Tag
     */
    private String tag;

    /**
     * 消息状态
     */
    private MessageStatus status;

    /**
     * 消息创建时间
     */
    private LocalDateTime createTime;

    /**
     * 消息发送时间
     */
    private LocalDateTime sendTime;

    /**
     * 消息处理时间
     */
    private LocalDateTime processTime;

    /**
     * 消息完成时间
     */
    private LocalDateTime completeTime;

    /**
     * 重试次数
     */
    private int retryCount;

    /**
     * 消息体（具体业务数据）
     */
    private T payload;

    /**
     * 扩展属性
     */
    private Map<String, String> properties;

    /**
     * 错误信息（失败时填充）
     */
    private String errorMessage;

    /**
     * 错误码（失败时填充）
     */
    private String errorCode;

    /**
     * 创建消息构建器
     */
    public static <T> LicenseMessageBuilder<T> builder() {
        return new LicenseMessageBuilder<T>()
                .messageId(UUID.randomUUID().toString())
                .correlationId(UUID.randomUUID().toString())
                .status(MessageStatus.PENDING)
                .createTime(LocalDateTime.now())
                .retryCount(0);
    }

    /**
     * 生成BusinessKey
     */
    public String generateBusinessKey() {
        return String.format("%s:%s:%s:%d:%s",
                softwareType != null ? softwareType.getCode() : "unknown",
                hostname != null ? hostname : "unknown",
                operationType != null ? operationType.getCode() : "unknown",
                System.currentTimeMillis(),
                messageId.substring(0, 8));
    }

    /**
     * 更新BusinessKey
     */
    public void updateBusinessKey() {
        this.businessKey = generateBusinessKey();
    }

    /**
     * 生成Tag
     */
    public String generateTag() {
        if (softwareType == null || operationType == null) {
            return "unknown";
        }
        return softwareType.getCode() + ":" + operationType.getCode();
    }

    /**
     * 标记为发送
     */
    public void markAsSent() {
        this.sendTime = LocalDateTime.now();
    }

    /**
     * 标记为处理中
     */
    public void markAsProcessing() {
        this.status = MessageStatus.PROCESSING;
        this.processTime = LocalDateTime.now();
    }

    /**
     * 标记为成功
     */
    public void markAsSuccess() {
        this.status = MessageStatus.SUCCESS;
        this.completeTime = LocalDateTime.now();
    }

    /**
     * 标记为失败
     */
    public void markAsFailed(String errorCode, String errorMessage) {
        this.status = MessageStatus.FAILED;
        this.errorCode = errorCode;
        this.errorMessage = errorMessage;
        this.completeTime = LocalDateTime.now();
    }

    /**
     * 标记为重试
     */
    public void markAsRetrying() {
        this.status = MessageStatus.RETRYING;
        this.retryCount++;
    }

    /**
     * 标记为超时
     */
    public void markAsTimeout() {
        this.status = MessageStatus.TIMEOUT;
        this.completeTime = LocalDateTime.now();
    }

    /**
     * 是否需要重试
     */
    public boolean shouldRetry(int maxRetryTimes) {
        return retryCount < maxRetryTimes && !status.isFinal();
    }
}
