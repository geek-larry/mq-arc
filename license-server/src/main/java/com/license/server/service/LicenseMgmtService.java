package com.license.server.service;

import com.license.common.constant.LicenseConstants;
import com.license.common.enums.OperationType;
import com.license.common.enums.SoftwareType;
import com.license.common.message.LicenseResponse;
import com.license.common.payload.*;
import com.license.common.payload.kickout.*;
import com.license.common.payload.service.*;
import com.license.common.payload.whitelist.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 许可管理服务
 * 提供各类许可管理操作的业务接口
 * 
 * 功能：
 * - 用户踢出管理
 * - 许可服务管理（启动、停止、重启）
 * - 白名单管理（初始化、添加、删除、查询）
 * - 文件管理（查询、更新、下载）
 * - 监控数据采集
 * 
 * 设计模式：服务模式
 * 
 * @author License System Team
 * @version 1.0.0
 */
@Slf4j
@Service
public class LicenseMgmtService {

    @Autowired
    private MessagePublishService messagePublishService;

    // ==================== 用户管理 ====================

    /**
     * 踢出浮动许可用户
     * 
     * @param softwareType 软件类型
     * @param hostname 目标主机名
     * @param payload 踢出参数（根据软件类型不同而不同）
     * @return 许可响应
     */
    public LicenseResponse<Void> kickoutUser(SoftwareType softwareType, String hostname, 
                                              Object payload) {
        log.info("Request to kickout user from {} on {}", softwareType, hostname);
        
        return messagePublishService.sendAndWait(
                LicenseConstants.TOPIC_USER_MGMT,
                softwareType,
                OperationType.KICKOUT,
                hostname,
                payload,
                Void.class
        );
    }

    // ==================== 服务管理 ====================

    /**
     * 启动许可服务
     * 
     * @param softwareType 软件类型
     * @param hostname 目标主机名
     * @param payload 服务管理参数（根据软件类型不同而不同）
     * @return 许可响应
     */
    public LicenseResponse<Void> startService(SoftwareType softwareType, String hostname,
                                               Object payload) {
        log.info("Request to start {} service on {}", softwareType, hostname);
        
        return messagePublishService.sendAndWait(
                LicenseConstants.TOPIC_SERVICE_MGMT,
                softwareType,
                OperationType.START,
                hostname,
                payload,
                Void.class
        );
    }

    /**
     * 停止许可服务
     * 
     * @param softwareType 软件类型
     * @param hostname 目标主机名
     * @param payload 服务管理参数（根据软件类型不同而不同）
     * @return 许可响应
     */
    public LicenseResponse<Void> stopService(SoftwareType softwareType, String hostname,
                                              Object payload) {
        log.info("Request to stop {} service on {}", softwareType, hostname);
        
        return messagePublishService.sendAndWait(
                LicenseConstants.TOPIC_SERVICE_MGMT,
                softwareType,
                OperationType.STOP,
                hostname,
                payload,
                Void.class
        );
    }

    /**
     * 重启许可服务
     * 
     * @param softwareType 软件类型
     * @param hostname 目标主机名
     * @param payload 服务管理参数（根据软件类型不同而不同）
     * @return 许可响应
     */
    public LicenseResponse<Void> restartService(SoftwareType softwareType, String hostname,
                                                 Object payload) {
        log.info("Request to restart {} service on {}", softwareType, hostname);
        
        return messagePublishService.sendAndWait(
                LicenseConstants.TOPIC_SERVICE_MGMT,
                softwareType,
                OperationType.RESTART,
                hostname,
                payload,
                Void.class
        );
    }

    // ==================== 白名单管理 ====================

    /**
     * 初始化白名单
     * 
     * @param softwareType 软件类型
     * @param hostname 目标主机名
     * @param payload 白名单参数（根据软件类型不同而不同）
     * @return 许可响应
     */
    public LicenseResponse<Void> initWhitelist(SoftwareType softwareType, String hostname,
                                                Object payload) {
        log.info("Request to init {} whitelist on {}", softwareType, hostname);
        
        return messagePublishService.sendAndWait(
                LicenseConstants.TOPIC_WHITELIST_MGMT,
                softwareType,
                OperationType.INIT,
                hostname,
                payload,
                Void.class
        );
    }

    /**
     * 添加白名单用户
     * 
     * @param softwareType 软件类型
     * @param hostname 目标主机名
     * @param payload 白名单参数（根据软件类型不同而不同）
     * @return 许可响应
     */
    public LicenseResponse<Void> addWhitelistUser(SoftwareType softwareType, String hostname,
                                                   Object payload) {
        log.info("Request to add {} whitelist user on {}", softwareType, hostname);
        
        return messagePublishService.sendAndWait(
                LicenseConstants.TOPIC_WHITELIST_MGMT,
                softwareType,
                OperationType.ADD,
                hostname,
                payload,
                Void.class
        );
    }

    /**
     * 删除白名单用户
     * 
     * @param softwareType 软件类型
     * @param hostname 目标主机名
     * @param payload 白名单参数（根据软件类型不同而不同）
     * @return 许可响应
     */
    public LicenseResponse<Void> removeWhitelistUser(SoftwareType softwareType, String hostname,
                                                      Object payload) {
        log.info("Request to remove {} whitelist user on {}", softwareType, hostname);
        
        return messagePublishService.sendAndWait(
                LicenseConstants.TOPIC_WHITELIST_MGMT,
                softwareType,
                OperationType.REMOVE,
                hostname,
                payload,
                Void.class
        );
    }

    /**
     * 查询白名单用户
     * 
     * @param softwareType 软件类型
     * @param hostname 目标主机名
     * @param payload 白名单参数（根据软件类型不同而不同）
     * @return 许可响应，包含白名单用户列表
     */
    @SuppressWarnings("unchecked")
    public LicenseResponse<List<?>> queryWhitelistUsers(
            SoftwareType softwareType, String hostname, Object payload) {
        log.info("Request to query {} whitelist users on {}", softwareType, hostname);
        
        return messagePublishService.sendAndWait(
                LicenseConstants.TOPIC_WHITELIST_MGMT,
                softwareType,
                OperationType.QUERY,
                hostname,
                payload,
                List.class
        );
    }

    // ==================== 文件管理 ====================

    /**
     * 查询许可文件
     * 
     * @param hostname 目标主机名
     * @param payload 文件管理参数
     * @return 许可响应，包含文件列表
     */
    public LicenseResponse<List<String>> queryLicenseFiles(String hostname, FileManagePayload payload) {
        log.info("Request to query license files on {}", hostname);
        
        return messagePublishService.sendAndWait(
                LicenseConstants.TOPIC_FILE_MGMT,
                null,  // 文件管理不涉及特定软件类型
                OperationType.LICENSE_QUERY,
                hostname,
                payload,
                List.class
        );
    }

    /**
     * 更新许可文件
     * 
     * @param hostname 目标主机名
     * @param payload 文件管理参数
     * @return 许可响应
     */
    public LicenseResponse<Void> updateLicenseFile(String hostname, FileManagePayload payload) {
        log.info("Request to update license file on {}: {}", hostname, payload.getFileName());
        
        return messagePublishService.sendAndWait(
                LicenseConstants.TOPIC_FILE_MGMT,
                null,
                OperationType.LICENSE_UPDATE,
                hostname,
                payload,
                Void.class
        );
    }

    /**
     * 下载许可文件
     * 
     * @param hostname 目标主机名
     * @param payload 文件管理参数
     * @return 许可响应，包含Base64编码的文件内容
     */
    public LicenseResponse<String> downloadLicenseFile(String hostname, FileManagePayload payload) {
        log.info("Request to download license file on {}: {}", hostname, payload.getFileName());
        
        return messagePublishService.sendAndWait(
                LicenseConstants.TOPIC_FILE_MGMT,
                null,
                OperationType.LICENSE_DOWNLOAD,
                hostname,
                payload,
                String.class  // Base64编码的文件内容
        );
    }

    /**
     * 查询日志文件
     * 
     * @param hostname 目标主机名
     * @param payload 文件管理参数
     * @return 许可响应，包含文件列表
     */
    public LicenseResponse<List<String>> queryLogFiles(String hostname, FileManagePayload payload) {
        log.info("Request to query log files on {}", hostname);
        
        return messagePublishService.sendAndWait(
                LicenseConstants.TOPIC_FILE_MGMT,
                null,
                OperationType.LOG_QUERY,
                hostname,
                payload,
                List.class
        );
    }

    /**
     * 下载日志文件
     * 
     * @param hostname 目标主机名
     * @param payload 文件管理参数
     * @return 许可响应，包含Base64编码的文件内容
     */
    public LicenseResponse<String> downloadLogFile(String hostname, FileManagePayload payload) {
        log.info("Request to download log file on {}: {}", hostname, payload.getFileName());
        
        return messagePublishService.sendAndWait(
                LicenseConstants.TOPIC_FILE_MGMT,
                null,
                OperationType.LOG_DOWNLOAD,
                hostname,
                payload,
                String.class
        );
    }

    // ==================== 监控数据采集 ====================

    /**
     * 采集许可使用数据
     * 针对大数据量场景，使用批量发送优化
     * 
     * @param softwareType 软件类型
     * @param hostname 目标主机名
     * @param payload 监控数据参数
     * @return 许可响应，包含使用数据列表
     */
    public LicenseResponse<List<MonitorUsagePayload.UsageData>> collectUsageData(
            SoftwareType softwareType, String hostname, MonitorUsagePayload payload) {
        log.info("Request to collect {} usage data on {}, targets: {}",
                softwareType, hostname, 
                payload.getTargets() != null ? payload.getTargets().size() : 0);
        
        return messagePublishService.sendAndWait(
                LicenseConstants.TOPIC_MONITOR,
                softwareType,
                OperationType.USAGE,
                hostname,
                payload,
                List.class
        );
    }

    /**
     * 批量采集多个服务器的监控数据（异步发送）
     * 
     * @param softwareType 软件类型
     * @param hostnames 主机名列表
     * @param payload 监控数据参数
     */
    public void collectUsageDataAsync(SoftwareType softwareType, List<String> hostnames,
                                       MonitorUsagePayload payload) {
        log.info("Async request to collect {} usage data for {} servers",
                softwareType, hostnames.size());
        
        for (String hostname : hostnames) {
            messagePublishService.sendAsync(
                    LicenseConstants.TOPIC_MONITOR,
                    softwareType,
                    OperationType.USAGE,
                    hostname,
                    payload,
                    null
            );
        }
    }
}
