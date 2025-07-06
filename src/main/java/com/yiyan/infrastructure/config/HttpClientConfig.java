package com.yiyan.infrastructure.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import okhttp3.OkHttpClient;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Duration;

/**
 * HTTP客户端配置类。
 */
@Configuration
public class HttpClientConfig {

    /**
     * 创建一个全局共享的 OkHttpClient Bean。
     * <p>
     * 使用共享实例可以有效管理和复用HTTP连接，提升性能。
     * 同时，配置了合理的超时时间，以防止应用因等待无响应的API而永久阻塞。
     *
     * @return 配置好的 OkHttpClient 实例。
     */
    @Bean
    public OkHttpClient okHttpClient() {
        return new OkHttpClient.Builder()
                .connectTimeout(Duration.ofSeconds(10)) // 连接超时
                .readTimeout(Duration.ofSeconds(10))    // 读取超时
                .writeTimeout(Duration.ofSeconds(10))   // 写入超时
                .build();
    }

    /**
     * 创建一个全局共享的 ObjectMapper Bean。
     * <p>
     * 用于JSON的序列化和反序列化，例如在 ApiConfigLoader 中加载外部API列表。
     * @return ObjectMapper 实例。
     */
    @Bean
    public ObjectMapper objectMapper() {
        return new ObjectMapper();
    }
} 