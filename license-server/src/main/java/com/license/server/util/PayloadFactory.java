package com.license.server.util;

import com.license.common.enums.SoftwareType;
import com.license.common.payload.BasePayload;
import com.license.common.payload.kickout.*;

/**
 * Payload工厂类
 * 负责根据软件类型创建对应的Payload对象
 * 
 * 功能：
 * - 类型安全的Payload转换
 * - 支持三种许可软件类型：Flexnet、Sentinel、LMX
 * - 提供踢出操作的Payload创建
 * 
 * 设计模式：工厂模式
 * 
 * @author License System Team
 * @version 1.0.0
 */
public class PayloadFactory {

    /**
     * 创建踢出操作Payload
     * 根据软件类型返回对应的踢出Payload对象
     * 
     * @param softwareType 软件类型
     * @param params 参数对象（必须是特定类型的Payload）
     * @return 对应的Payload对象
     * @throws IllegalArgumentException 如果软件类型不支持或参数类型不匹配
     */
    public static BasePayload createKickoutPayload(SoftwareType softwareType, Object params) {
        switch (softwareType) {
            case FLEXNET:
                return convertToFlexnetKickoutPayload(params);
            case SENTINEL:
                return convertToSentinelKickoutPayload(params);
            case LMX:
                return convertToLmxKickoutPayload(params);
            default:
                throw new IllegalArgumentException("Unsupported software type: " + softwareType);
        }
    }

    /**
     * 转换为Flexnet踢出Payload
     * 
     * @param params 参数对象
     * @return Flexnet踢出Payload
     * @throws IllegalArgumentException 如果参数类型不匹配
     */
    private static FlexnetUserKickoutPayload convertToFlexnetKickoutPayload(Object params) {
        if (params instanceof FlexnetUserKickoutPayload) {
            return (FlexnetUserKickoutPayload) params;
        }
        throw new IllegalArgumentException("Invalid payload type for Flexnet kickout, expected: FlexnetUserKickoutPayload");
    }

    /**
     * 转换为Sentinel踢出Payload
     * 
     * @param params 参数对象
     * @return Sentinel踢出Payload
     * @throws IllegalArgumentException 如果参数类型不匹配
     */
    private static SentinelUserKickoutPayload convertToSentinelKickoutPayload(Object params) {
        if (params instanceof SentinelUserKickoutPayload) {
            return (SentinelUserKickoutPayload) params;
        }
        throw new IllegalArgumentException("Invalid payload type for Sentinel kickout, expected: SentinelUserKickoutPayload");
    }

    /**
     * 转换为LMX踢出Payload
     * 
     * @param params 参数对象
     * @return LMX踢出Payload
     * @throws IllegalArgumentException 如果参数类型不匹配
     */
    private static LmxUserKickoutPayload convertToLmxKickoutPayload(Object params) {
        if (params instanceof LmxUserKickoutPayload) {
            return (LmxUserKickoutPayload) params;
        }
        throw new IllegalArgumentException("Invalid payload type for LMX kickout, expected: LmxUserKickoutPayload");
    }
}
