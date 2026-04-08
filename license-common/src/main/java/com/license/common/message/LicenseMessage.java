package com.license.common.message;

import com.license.common.enums.OperationType;
import com.license.common.enums.SoftwareType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
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
     * 消息体（具体业务数据）
     */
    private T payload;

    /**
     * 创建消息构建器
     */
    public static <T> LicenseMessageBuilder<T> builder() {
        return new LicenseMessageBuilder<T>()
                .messageId(UUID.randomUUID().toString());
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
}
