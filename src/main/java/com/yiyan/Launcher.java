package com.yiyan;

import com.yiyan.infrastructure.config.ApiProperties;
import com.yiyan.infrastructure.config.HttpClientConfig;
import com.yiyan.launcher.config.SchedulerProperties;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Import;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * Spring Boot 应用程序主入口。
 * <p>
 * 使用 @SpringBootApplication 注解，这是一个集成了 @Configuration, @EnableAutoConfiguration, 和 @ComponentScan 的便捷注解。
 * 使用 @EnableConfigurationProperties 来显式地启用对特定配置属性类的支持。
 * 使用 @Import 来导入额外的配置类。
 */
@SpringBootApplication
@EnableScheduling
@EnableConfigurationProperties({ApiProperties.class, SchedulerProperties.class})
@Import(HttpClientConfig.class)
@EnableAsync(proxyTargetClass = true)
public class Launcher {

    public static void main(String[] args) {
        // 使用 SpringApplicationBuilder 配置并启动应用
        new SpringApplicationBuilder(Launcher.class)
                .headless(false) // 必须设置为false，否则AWT/Swing将无法启动
                .run(args);
    }
} 