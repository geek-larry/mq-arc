package com.license.server.util;

import com.license.common.enums.SoftwareType;
import com.license.common.payload.BasePayload;
import com.license.common.payload.kickout.*;
import com.license.common.payload.service.*;
import com.license.common.payload.whitelist.*;

public class PayloadFactory {

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

    public static BasePayload createServiceManagePayload(SoftwareType softwareType, Object params) {
        switch (softwareType) {
            case FLEXNET:
                return convertToFlexnetServicePayload(params);
            case SENTINEL:
                return convertToSentinelServicePayload(params);
            case LMX:
                return convertToLmxServicePayload(params);
            default:
                throw new IllegalArgumentException("Unsupported software type: " + softwareType);
        }
    }

    public static BasePayload createWhitelistPayload(SoftwareType softwareType, Object params) {
        switch (softwareType) {
            case FLEXNET:
                return convertToFlexnetWhitelistPayload(params);
            case SENTINEL:
                return convertToSentinelWhitelistPayload(params);
            case LMX:
                return convertToLmxWhitelistPayload(params);
            default:
                throw new IllegalArgumentException("Unsupported software type: " + softwareType);
        }
    }

    private static FlexnetUserKickoutPayload convertToFlexnetKickoutPayload(Object params) {
        if (params instanceof FlexnetUserKickoutPayload) {
            return (FlexnetUserKickoutPayload) params;
        }
        throw new IllegalArgumentException("Invalid payload type for Flexnet kickout");
    }

    private static SentinelUserKickoutPayload convertToSentinelKickoutPayload(Object params) {
        if (params instanceof SentinelUserKickoutPayload) {
            return (SentinelUserKickoutPayload) params;
        }
        throw new IllegalArgumentException("Invalid payload type for Sentinel kickout");
    }

    private static LmxUserKickoutPayload convertToLmxKickoutPayload(Object params) {
        if (params instanceof LmxUserKickoutPayload) {
            return (LmxUserKickoutPayload) params;
        }
        throw new IllegalArgumentException("Invalid payload type for LMX kickout");
    }

    private static FlexnetServiceManagePayload convertToFlexnetServicePayload(Object params) {
        if (params instanceof FlexnetServiceManagePayload) {
            return (FlexnetServiceManagePayload) params;
        }
        throw new IllegalArgumentException("Invalid payload type for Flexnet service");
    }

    private static SentinelServiceManagePayload convertToSentinelServicePayload(Object params) {
        if (params instanceof SentinelServiceManagePayload) {
            return (SentinelServiceManagePayload) params;
        }
        throw new IllegalArgumentException("Invalid payload type for Sentinel service");
    }

    private static LmxServiceManagePayload convertToLmxServicePayload(Object params) {
        if (params instanceof LmxServiceManagePayload) {
            return (LmxServiceManagePayload) params;
        }
        throw new IllegalArgumentException("Invalid payload type for LMX service");
    }

    private static FlexnetWhitelistPayload convertToFlexnetWhitelistPayload(Object params) {
        if (params instanceof FlexnetWhitelistPayload) {
            return (FlexnetWhitelistPayload) params;
        }
        throw new IllegalArgumentException("Invalid payload type for Flexnet whitelist");
    }

    private static SentinelWhitelistPayload convertToSentinelWhitelistPayload(Object params) {
        if (params instanceof SentinelWhitelistPayload) {
            return (SentinelWhitelistPayload) params;
        }
        throw new IllegalArgumentException("Invalid payload type for Sentinel whitelist");
    }

    private static LmxWhitelistPayload convertToLmxWhitelistPayload(Object params) {
        if (params instanceof LmxWhitelistPayload) {
            return (LmxWhitelistPayload) params;
        }
        throw new IllegalArgumentException("Invalid payload type for LMX whitelist");
    }
}
