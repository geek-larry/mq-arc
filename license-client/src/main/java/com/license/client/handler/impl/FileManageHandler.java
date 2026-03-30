package com.license.client.handler.impl;

import com.license.client.handler.AbstractMessageHandler;
import com.license.common.enums.OperationType;
import com.license.common.message.LicenseMessage;
import com.license.common.payload.FileManagePayload;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 文件管理处理器
 * 处理许可文件和日志文件的查询、更新、下载请求
 */
@Slf4j
@Component
public class FileManageHandler extends AbstractMessageHandler<FileManagePayload, Object> {

    @Autowired
    private String clientHostname;

    // 模拟文件存储
    private final Map<String, String> fileStore = new ConcurrentHashMap<>();

    @Override
    public String getOperationType() {
        return "*";
    }

    @Override
    public String getSoftwareType() {
        return "*";
    }

    @Override
    public boolean supports(LicenseMessage<?> message) {
        if (message.getOperationType() == null) {
            return false;
        }
        return message.getOperationType() == OperationType.LICENSE_QUERY ||
               message.getOperationType() == OperationType.LICENSE_UPDATE ||
               message.getOperationType() == OperationType.LICENSE_DOWNLOAD ||
               message.getOperationType() == OperationType.LOG_QUERY ||
               message.getOperationType() == OperationType.LOG_DOWNLOAD;
    }

    @Override
    protected Object doHandle(LicenseMessage<FileManagePayload> message) throws Exception {
        FileManagePayload payload = message.getPayload();
        OperationType operationType = message.getOperationType();
        
        log.info("Managing file: operation={}, fileName={}", operationType, payload.getFileName());
        
        // 模拟处理延迟
        simulateDelay(100, 500);
        
        switch (operationType) {
            case LICENSE_QUERY:
                return handleLicenseQuery(payload);
            case LICENSE_UPDATE:
                return handleLicenseUpdate(payload);
            case LICENSE_DOWNLOAD:
                return handleLicenseDownload(payload);
            case LOG_QUERY:
                return handleLogQuery(payload);
            case LOG_DOWNLOAD:
                return handleLogDownload(payload);
            default:
                throw new UnsupportedOperationException("Unsupported operation: " + operationType);
        }
    }

    /**
     * 查询许可文件
     */
    private List<String> handleLicenseQuery(FileManagePayload payload) {
        log.debug("Querying license files in path: {}", payload.getFilePath());
        
        List<String> files = new ArrayList<>();
        
        // 模拟文件列表
        files.add("license_flexnet_2024.lic");
        files.add("license_sentinel_2024.lic");
        files.add("license_lmx_2024.lic");
        files.add("license_backup_2023.lic");
        
        log.info("Found {} license files", files.size());
        return files;
    }

    /**
     * 更新许可文件
     */
    private Void handleLicenseUpdate(FileManagePayload payload) {
        if (payload.getFileName() == null || payload.getFileContentBase64() == null) {
            throw new IllegalArgumentException("File name and content are required for update");
        }
        
        log.debug("Updating license file: {}", payload.getFileName());
        
        // 模拟保存文件
        String fileKey = buildFileKey("license", payload.getFileName());
        fileStore.put(fileKey, payload.getFileContentBase64());
        
        // 模拟90%成功率
        if (!simulateRandomSuccess(0.9)) {
            throw new RuntimeException("Failed to update license file: " + payload.getFileName());
        }
        
        log.info("License file [{}] updated successfully", payload.getFileName());
        return null;
    }

    /**
     * 下载许可文件
     */
    private String handleLicenseDownload(FileManagePayload payload) {
        if (payload.getFileName() == null) {
            throw new IllegalArgumentException("File name is required for download");
        }
        
        log.debug("Downloading license file: {}", payload.getFileName());
        
        // 模拟从存储中获取文件
        String fileKey = buildFileKey("license", payload.getFileName());
        String content = fileStore.get(fileKey);
        
        if (content == null) {
            // 模拟生成文件内容
            content = Base64.getEncoder().encodeToString(
                    ("This is a simulated license file content for " + payload.getFileName()).getBytes()
            );
        }
        
        log.info("License file [{}] downloaded, size: {} bytes", 
                payload.getFileName(), content.length());
        return content;
    }

    /**
     * 查询日志文件
     */
    private List<String> handleLogQuery(FileManagePayload payload) {
        log.debug("Querying log files from {} to {}", payload.getStartDate(), payload.getEndDate());
        
        List<String> files = new ArrayList<>();
        
        // 模拟日志文件列表
        files.add("flexnet_2024-01-15.log");
        files.add("flexnet_2024-01-14.log");
        files.add("sentinel_2024-01-15.log");
        files.add("lmx_2024-01-15.log");
        files.add("system_2024-01-15.log");
        
        log.info("Found {} log files", files.size());
        return files;
    }

    /**
     * 下载日志文件
     */
    private String handleLogDownload(FileManagePayload payload) {
        if (payload.getFileName() == null) {
            throw new IllegalArgumentException("File name is required for download");
        }
        
        log.debug("Downloading log file: {}", payload.getFileName());
        
        // 模拟生成日志内容
        String logContent = generateSimulatedLogContent(payload.getFileName());
        String base64Content = Base64.getEncoder().encodeToString(logContent.getBytes());
        
        log.info("Log file [{}] downloaded, size: {} bytes", 
                payload.getFileName(), base64Content.length());
        return base64Content;
    }

    /**
     * 生成模拟日志内容
     */
    private String generateSimulatedLogContent(String fileName) {
        StringBuilder sb = new StringBuilder();
        sb.append("=== License Server Log: ").append(fileName).append(" ===\n");
        sb.append("2024-01-15 08:00:00 INFO  Server started\n");
        sb.append("2024-01-15 08:00:05 INFO  License loaded: feature=CAD, count=100\n");
        sb.append("2024-01-15 09:30:12 INFO  User login: user=user001, host=workstation01\n");
        sb.append("2024-01-15 10:15:33 WARN  High memory usage: 85%\n");
        sb.append("2024-01-15 12:00:00 INFO  Daily statistics: checkouts=50, denials=2\n");
        sb.append("2024-01-15 15:45:21 ERROR Failed to checkout license: user=user002, feature=CAM\n");
        sb.append("2024-01-15 18:00:00 INFO  Server status: normal\n");
        return sb.toString();
    }

    private String buildFileKey(String type, String fileName) {
        return clientHostname + ":" + type + ":" + fileName;
    }

    @Override
    protected String getClientHostname() {
        return clientHostname;
    }
}
