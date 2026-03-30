package com.license.client.config;

import com.license.client.listener.LicenseMessageListener;
import com.license.common.constant.LicenseConstants;
import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.spring.annotation.RocketMQMessageListener;
import org.apache.rocketmq.spring.core.RocketMQListener;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * 多Topic监听器配置
 * 为每个Topic配置独立的监听器
 */
@Slf4j
@Configuration
public class MultiTopicListenerConfig {

    /**
     * 用户管理Topic监听器
     */
    @Bean
    public UserMgmtTopicListener userMgmtTopicListener() {
        return new UserMgmtTopicListener();
    }

    /**
     * 服务管理Topic监听器
     */
    @Bean
    public ServiceMgmtTopicListener serviceMgmtTopicListener() {
        return new ServiceMgmtTopicListener();
    }

    /**
     * 白名单管理Topic监听器
     */
    @Bean
    public WhitelistMgmtTopicListener whitelistMgmtTopicListener() {
        return new WhitelistMgmtTopicListener();
    }

    /**
     * 文件管理Topic监听器
     */
    @Bean
    public FileMgmtTopicListener fileMgmtTopicListener() {
        return new FileMgmtTopicListener();
    }

    /**
     * 监控数据采集Topic监听器
     */
    @Bean
    public MonitorTopicListener monitorTopicListener() {
        return new MonitorTopicListener();
    }

    // ==================== 内部监听器类 ====================

    /**
     * 用户管理Topic监听器
     */
    @RocketMQMessageListener(
            topic = LicenseConstants.TOPIC_USER_MGMT,
            consumerGroup = "${license.client.hostname}-user-mgmt-group",
            selectorExpression = "*"
    )
    public static class UserMgmtTopicListener extends LicenseMessageListener {
    }

    /**
     * 服务管理Topic监听器
     */
    @RocketMQMessageListener(
            topic = LicenseConstants.TOPIC_SERVICE_MGMT,
            consumerGroup = "${license.client.hostname}-service-mgmt-group",
            selectorExpression = "*"
    )
    public static class ServiceMgmtTopicListener extends LicenseMessageListener {
    }

    /**
     * 白名单管理Topic监听器
     */
    @RocketMQMessageListener(
            topic = LicenseConstants.TOPIC_WHITELIST_MGMT,
            consumerGroup = "${license.client.hostname}-whitelist-mgmt-group",
            selectorExpression = "*"
    )
    public static class WhitelistMgmtTopicListener extends LicenseMessageListener {
    }

    /**
     * 文件管理Topic监听器
     */
    @RocketMQMessageListener(
            topic = LicenseConstants.TOPIC_FILE_MGMT,
            consumerGroup = "${license.client.hostname}-file-mgmt-group",
            selectorExpression = "*"
    )
    public static class FileMgmtTopicListener extends LicenseMessageListener {
    }

    /**
     * 监控数据采集Topic监听器
     */
    @RocketMQMessageListener(
            topic = LicenseConstants.TOPIC_MONITOR,
            consumerGroup = "${license.client.hostname}-monitor-group",
            selectorExpression = "*",
            consumeThreadNumber = 20  // 监控数据量大，增加消费线程数
    )
    public static class MonitorTopicListener extends LicenseMessageListener {
    }
}
