package com.license.common.payload;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * 文件管理请求Payload
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FileManagePayload implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 文件路径
     */
    private String filePath;

    /**
     * 文件名
     */
    private String fileName;

    /**
     * 文件内容（Base64编码，用于更新操作）
     */
    private String fileContentBase64;

    /**
     * 文件类型
     */
    private FileType fileType;

    /**
     * 查询日期范围（用于日志查询）
     */
    private String startDate;
    private String endDate;

    /**
     * 文件类型枚举
     */
    public enum FileType {
        LICENSE,    // 许可文件
        LOG,        // 日志文件
        CONFIG,     // 配置文件
        BACKUP      // 备份文件
    }
}
