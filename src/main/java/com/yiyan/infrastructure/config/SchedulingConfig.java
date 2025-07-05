package com.yiyan.infrastructure.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;

/**
 * 任务调度器配置类。
 */
@Configuration
public class SchedulingConfig {

    /**
     * 创建一个线程池任务调度器 Bean。
     * <p>
     * 明确地定义一个 TaskScheduler Bean 可以让我们更好地控制线程池的大小和行为，
     * 而不是依赖 Spring Boot 的自动配置。
     *
     * @return 配置好的 TaskScheduler 实例。
     */
    @Bean
    public TaskScheduler taskScheduler() {
        ThreadPoolTaskScheduler scheduler = new ThreadPoolTaskScheduler();
        scheduler.setPoolSize(2); // 对于我们的应用，2个线程足够了
        scheduler.setThreadNamePrefix("yiyan-scheduler-");
        scheduler.setDaemon(true); // 设置为守护线程，以便主程序退出时它们也能退出
        return scheduler;
    }
} 