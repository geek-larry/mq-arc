package com.license.server.controller;

import com.license.common.enums.SoftwareType;
import com.license.common.message.LicenseResponse;
import com.license.common.payload.*;
import com.license.common.payload.kickout.*;
import com.license.common.payload.service.*;
import com.license.common.payload.whitelist.*;
import com.license.server.service.LicenseMgmtService;
import com.license.server.util.PayloadFactory;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 许可管理 REST API 控制器
 */
@Slf4j
@RestController
@RequestMapping("/api/license")
@RequiredArgsConstructor
public class LicenseMgmtController {

    private final LicenseMgmtService licenseMgmtService;

    // ==================== 用户管理 ====================

    @PostMapping("/{softwareType}/{hostname}/users/kickout")
    public ResponseEntity<LicenseResponse<Void>> kickoutUser(
            @PathVariable SoftwareType softwareType,
            @PathVariable String hostname,
            @RequestBody Object payload) {
        log.info("API: Kickout user from {} on {}", softwareType, hostname);
        BasePayload typedPayload = PayloadFactory.createKickoutPayload(softwareType, payload);
        LicenseResponse<Void> response = licenseMgmtService.kickoutUser(softwareType, hostname, typedPayload);
        return ResponseEntity.ok(response);
    }

    // ==================== 服务管理 ====================

    @PostMapping("/{softwareType}/{hostname}/services/start")
    public ResponseEntity<LicenseResponse<Void>> startService(
            @PathVariable SoftwareType softwareType,
            @PathVariable String hostname,
            @RequestBody Object payload) {
        log.info("API: Start {} service on {}", softwareType, hostname);
        BasePayload typedPayload = PayloadFactory.createServiceManagePayload(softwareType, payload);
        LicenseResponse<Void> response = licenseMgmtService.startService(softwareType, hostname, typedPayload);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/{softwareType}/{hostname}/services/stop")
    public ResponseEntity<LicenseResponse<Void>> stopService(
            @PathVariable SoftwareType softwareType,
            @PathVariable String hostname,
            @RequestBody Object payload) {
        log.info("API: Stop {} service on {}", softwareType, hostname);
        BasePayload typedPayload = PayloadFactory.createServiceManagePayload(softwareType, payload);
        LicenseResponse<Void> response = licenseMgmtService.stopService(softwareType, hostname, typedPayload);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/{softwareType}/{hostname}/services/restart")
    public ResponseEntity<LicenseResponse<Void>> restartService(
            @PathVariable SoftwareType softwareType,
            @PathVariable String hostname,
            @RequestBody Object payload) {
        log.info("API: Restart {} service on {}", softwareType, hostname);
        BasePayload typedPayload = PayloadFactory.createServiceManagePayload(softwareType, payload);
        LicenseResponse<Void> response = licenseMgmtService.restartService(softwareType, hostname, typedPayload);
        return ResponseEntity.ok(response);
    }

    // ==================== 白名单管理 ====================

    @PostMapping("/{softwareType}/{hostname}/whitelist/init")
    public ResponseEntity<LicenseResponse<Void>> initWhitelist(
            @PathVariable SoftwareType softwareType,
            @PathVariable String hostname,
            @RequestBody Object payload) {
        log.info("API: Init {} whitelist on {}", softwareType, hostname);
        BasePayload typedPayload = PayloadFactory.createWhitelistPayload(softwareType, payload);
        LicenseResponse<Void> response = licenseMgmtService.initWhitelist(softwareType, hostname, typedPayload);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/{softwareType}/{hostname}/whitelist/users")
    public ResponseEntity<LicenseResponse<Void>> addWhitelistUser(
            @PathVariable SoftwareType softwareType,
            @PathVariable String hostname,
            @RequestBody Object payload) {
        log.info("API: Add {} whitelist user on {}", softwareType, hostname);
        BasePayload typedPayload = PayloadFactory.createWhitelistPayload(softwareType, payload);
        LicenseResponse<Void> response = licenseMgmtService.addWhitelistUser(softwareType, hostname, typedPayload);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{softwareType}/{hostname}/whitelist/users")
    public ResponseEntity<LicenseResponse<Void>> removeWhitelistUser(
            @PathVariable SoftwareType softwareType,
            @PathVariable String hostname,
            @RequestBody Object payload) {
        log.info("API: Remove {} whitelist user on {}", softwareType, hostname);
        BasePayload typedPayload = PayloadFactory.createWhitelistPayload(softwareType, payload);
        LicenseResponse<Void> response = licenseMgmtService.removeWhitelistUser(softwareType, hostname, typedPayload);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{softwareType}/{hostname}/whitelist/users")
    public ResponseEntity<LicenseResponse<List<?>>> queryWhitelistUsers(
            @PathVariable SoftwareType softwareType,
            @PathVariable String hostname,
            @RequestParam(required = false) String queryCondition) {
        log.info("API: Query {} whitelist users on {}", softwareType, hostname);
        BasePayload typedPayload = PayloadFactory.createWhitelistPayload(softwareType, 
                WhitelistManagePayload.builder().queryCondition(queryCondition).build());
        LicenseResponse<List<?>> response = licenseMgmtService.queryWhitelistUsers(softwareType, hostname, typedPayload);
        return ResponseEntity.ok(response);
    }

    // ==================== 文件管理 ====================

    @GetMapping("/{hostname}/files/licenses")
    public ResponseEntity<LicenseResponse<List<String>>> queryLicenseFiles(
            @PathVariable String hostname,
            @RequestParam(required = false) String filePath) {
        log.info("API: Query license files on {}", hostname);
        FileManagePayload payload = FileManagePayload.builder()
                .filePath(filePath)
                .build();
        LicenseResponse<List<String>> response = licenseMgmtService.queryLicenseFiles(hostname, payload);
        return ResponseEntity.ok(response);
    }

    @PutMapping("/{hostname}/files/licenses")
    public ResponseEntity<LicenseResponse<Void>> updateLicenseFile(
            @PathVariable String hostname,
            @RequestBody FileManagePayload payload) {
        log.info("API: Update license file on {}: {}", hostname, payload.getFileName());
        LicenseResponse<Void> response = licenseMgmtService.updateLicenseFile(hostname, payload);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/{hostname}/files/licenses/download")
    public ResponseEntity<LicenseResponse<String>> downloadLicenseFile(
            @PathVariable String hostname,
            @RequestBody FileManagePayload payload) {
        log.info("API: Download license file on {}: {}", hostname, payload.getFileName());
        LicenseResponse<String> response = licenseMgmtService.downloadLicenseFile(hostname, payload);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{hostname}/files/logs")
    public ResponseEntity<LicenseResponse<List<String>>> queryLogFiles(
            @PathVariable String hostname,
            @RequestParam(required = false) String startDate,
            @RequestParam(required = false) String endDate) {
        log.info("API: Query log files on {}", hostname);
        FileManagePayload payload = FileManagePayload.builder()
                .startDate(startDate)
                .endDate(endDate)
                .build();
        LicenseResponse<List<String>> response = licenseMgmtService.queryLogFiles(hostname, payload);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/{hostname}/files/logs/download")
    public ResponseEntity<LicenseResponse<String>> downloadLogFile(
            @PathVariable String hostname,
            @RequestBody FileManagePayload payload) {
        log.info("API: Download log file on {}: {}", hostname, payload.getFileName());
        LicenseResponse<String> response = licenseMgmtService.downloadLogFile(hostname, payload);
        return ResponseEntity.ok(response);
    }

    // ==================== 监控数据采集 ====================

    @PostMapping("/{softwareType}/{hostname}/monitor/usage")
    public ResponseEntity<LicenseResponse<List<MonitorUsagePayload.UsageData>>> collectUsageData(
            @PathVariable SoftwareType softwareType,
            @PathVariable String hostname,
            @RequestBody MonitorUsagePayload payload) {
        log.info("API: Collect {} usage data on {}", softwareType, hostname);
        LicenseResponse<List<MonitorUsagePayload.UsageData>> response = 
                licenseMgmtService.collectUsageData(softwareType, hostname, payload);
        return ResponseEntity.ok(response);
    }
}
