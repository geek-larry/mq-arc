package com.license.common.enums;

import lombok.Getter;

/**
 * 软件类型枚举
 */
@Getter
public enum SoftwareType {
    FLEXNET("flexnet", "FlexNet Publisher"),
    SENTINEL("sentinel", "Sentinel RMS"),
    LMX("lmx", "LM-X License Manager");

    private final String code;
    private final String description;

    SoftwareType(String code, String description) {
        this.code = code;
        this.description = description;
    }

    public static SoftwareType fromCode(String code) {
        for (SoftwareType type : values()) {
            if (type.code.equalsIgnoreCase(code)) {
                return type;
            }
        }
        throw new IllegalArgumentException("Unknown software type: " + code);
    }
}
