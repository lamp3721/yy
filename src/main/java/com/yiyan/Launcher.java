package com.yiyan;

import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * 应用主入口（启动器）。
 */
@SpringBootApplication
@EnableScheduling
public class Launcher {

    public static void main(String[] args) {
        // 使用 SpringApplicationBuilder 配置并启动应用
        new SpringApplicationBuilder(Launcher.class)
                .headless(false) // 必须设置为false，否则AWT/Swing将无法启动
                .run(args);
    }
} 