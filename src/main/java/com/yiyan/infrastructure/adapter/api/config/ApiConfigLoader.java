package com.yiyan.infrastructure.adapter.api.config;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.io.InputStream;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * 负责在应用启动时从外部文件加载API端点配置。
 */
@Component("apiConfigLoader")
@Slf4j
@RequiredArgsConstructor
public class ApiConfigLoader implements CommandLineRunner {

    /**
     * API配置属性，用于存储从配置文件加载的API端点列表
     */
    private final ApiProperties apiProperties;
    /**
     * Spring资源加载器，用于根据路径加载文件
     */
    private final ResourceLoader resourceLoader;
    /**
     * Jackson的ObjectMapper，用于将JSON文件内容反序列化为Java对象
     */
    private final ObjectMapper objectMapper;
    private final AtomicBoolean ready = new AtomicBoolean(false);

    @Override
    public void run(String... args) throws Exception {
        String path = apiProperties.getApiListPath();
        if (!StringUtils.hasText(path)) {
            log.warn("⚠️ API列表文件路径 'yiyan.api-list-path' 未配置, 将不会加载任何外部API。");
            apiProperties.setEndpoints(Collections.emptyList());
            ready.set(true); // 即使没有配置，也标记为就绪状态
            return;
        }

        log.info("🔍 正在从路径 '{}' 加载API列表...", path);

        try {
            Resource resource = resourceLoader.getResource(path);
            try (InputStream inputStream = resource.getInputStream()) {
                // 使用ObjectMapper将JSON文件内容反序列化为List<ApiEndpoint>
                List<ApiProperties.ApiEndpoint> loadedEndpoints = objectMapper.readValue(inputStream, new TypeReference<>() {});
                apiProperties.setEndpoints(loadedEndpoints);
                log.info("✅ 成功加载了 {} 个API端点。", loadedEndpoints.size());
            }
        } catch (Exception e) {
            log.error("❌ 加载API列表文件 '{}' 失败。请检查文件是否存在、路径是否正确以及JSON格式是否规范。", path, e);
            // 在加载失败时设置为空列表，避免后续出现空指针异常
            apiProperties.setEndpoints(Collections.emptyList());
            // 抛出异常以可能地中止应用启动，因为这是一个关键的配置错误
            throw new IllegalStateException("无法加载API配置文件: " + path, e);
        } finally {
            ready.set(true); // 确保无论成功或失败，都更新就绪状态
        }
    }

    public boolean isReady() {
        return ready.get();
    }
} 