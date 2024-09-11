package org.example.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.concurrent.ConcurrentTaskScheduler;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;

@ComponentScan("org.example")
@Configuration
@EnableScheduling
public class JavaConfiguration {
    
    @Bean
    public TaskScheduler taskScheduler() {
        return new ConcurrentTaskScheduler(); // 或者使用其他 TaskScheduler 实现
    }

}