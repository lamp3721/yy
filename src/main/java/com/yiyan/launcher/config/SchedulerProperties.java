package com.yiyan.launcher.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * 调度器相关配置属性。
 * <p>
 * 从 application.yml 文件中读取以 'scheduler' 为前缀的配置项。
 */
@ConfigurationProperties(prefix = "scheduler")
@Data
public class SchedulerProperties {

    /**
     * 随机调度的最小延迟时间（秒）。
     */
    private long minDelaySeconds = 5;

    /**
     * 随机调度的最大延迟时间（秒）。
     */
    private long maxDelaySeconds = 15;
} 