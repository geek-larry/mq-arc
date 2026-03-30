package com.license.client.handler.impl;

import com.license.client.handler.AbstractMessageHandler;
import com.license.common.enums.OperationType;
import com.license.common.enums.SoftwareType;
import com.license.common.message.LicenseMessage;
import com.license.common.payload.MonitorUsagePayload;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

/**
 * 监控数据采集处理器
 * 处理许可使用情况的数据采集请求
 * 
 * 优化建议：对于60个监控对象、每5分钟采集一次的场景
 * 1. 使用批量采集减少消息数量
 * 2. 支持压缩传输大数据量
 * 3. 支持增量采集减少数据传输
 */
@Slf4j
@Component
public class MonitorUsageHandler extends AbstractMessageHandler<MonitorUsagePayload, 
        List<MonitorUsagePayload.UsageData>> {

    @Autowired
    private String clientHostname;

    @Override
    public String getOperationType() {
        return OperationType.USAGE.getCode();
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
        return message.getOperationType() == OperationType.USAGE;
    }

    @Override
    protected List<MonitorUsagePayload.UsageData> doHandle(LicenseMessage<MonitorUsagePayload> message) 
            throws Exception {
        MonitorUsagePayload payload = message.getPayload();
        SoftwareType softwareType = message.getSoftwareType();
        
        List<MonitorUsagePayload.MonitorTarget> targets = payload.getTargets();
        boolean batchMode = Boolean.TRUE.equals(payload.getBatchMode());
        
        log.info("Collecting usage data for {}: targets={}, batchMode={}",
                softwareType, targets != null ? targets.size() : 0, batchMode);
        
        // 模拟处理延迟（批量采集时延迟稍长）
        if (batchMode && targets != null && targets.size() > 10) {
            simulateDelay(500, 1500);
        } else {
            simulateDelay(200, 800);
        }
        
        List<MonitorUsagePayload.UsageData> results = new ArrayList<>();
        
        if (targets == null || targets.isEmpty()) {
            // 如果没有指定目标，模拟采集所有
            results.addAll(simulateCollectAll(softwareType));
        } else {
            // 采集指定目标
            for (MonitorUsagePayload.MonitorTarget target : targets) {
                MonitorUsagePayload.UsageData data = simulateCollectUsage(softwareType, target);
                if (data != null) {
                    results.add(data);
                }
            }
        }
        
        log.info("Collected {} usage data entries for {}", results.size(), softwareType);
        return results;
    }

    /**
     * 模拟采集所有监控对象
     */
    private List<MonitorUsagePayload.UsageData> simulateCollectAll(SoftwareType softwareType) {
        List<MonitorUsagePayload.UsageData> results = new ArrayList<>();
        
        // 模拟采集20个监控对象
        for (int i = 0; i < 20; i++) {
            MonitorUsagePayload.MonitorTarget target = MonitorUsagePayload.MonitorTarget.builder()
                    .softwareName(softwareType.getCode() + "_app_" + i)
                    .featureCode("FEATURE_" + i)
                    .version("2024.1")
                    .build();
            
            MonitorUsagePayload.UsageData data = simulateCollectUsage(softwareType, target);
            if (data != null) {
                results.add(data);
            }
        }
        
        return results;
    }

    /**
     * 模拟采集单个监控对象的使用数据
     */
    private MonitorUsagePayload.UsageData simulateCollectUsage(SoftwareType softwareType,
                                                                MonitorUsagePayload.MonitorTarget target) {
        // 模拟95%成功率
        if (!simulateRandomSuccess(0.95)) {
            log.warn("Failed to collect usage data for {}:{}", 
                    softwareType, target.getSoftwareName());
            return null;
        }
        
        int totalLicenses = 100;
        int usedLicenses = (int) (Math.random() * 80);
        int availableLicenses = totalLicenses - usedLicenses;
        
        List<MonitorUsagePayload.UsageDetail> details = new ArrayList<>();
        
        // 生成使用详情
        for (int i = 0; i < usedLicenses && i < 10; i++) {
            details.add(MonitorUsagePayload.UsageDetail.builder()
                    .userId("user" + String.format("%03d", i))
                    .username("User " + i)
                    .host("workstation" + i + ".company.com")
                    .display(":" + (i + 1))
                    .version("2024.1")
                    .serverHost(clientHostname)
                    .port("27000")
                    .handle(String.valueOf(1000 + i))
                    .checkoutTime(LocalDateTime.now().minusMinutes((long) (Math.random() * 120)).toString())
                    .idleTime(String.valueOf((int) (Math.random() * 60)) + "m")
                    .build());
        }
        
        return MonitorUsagePayload.UsageData.builder()
                .softwareName(target.getSoftwareName())
                .featureCode(target.getFeatureCode())
                .totalLicenses(totalLicenses)
                .usedLicenses(usedLicenses)
                .availableLicenses(availableLicenses)
                .usageDetails(details)
                .collectTime(LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME))
                .build();
    }

    @Override
    protected String getClientHostname() {
        return clientHostname;
    }
}
