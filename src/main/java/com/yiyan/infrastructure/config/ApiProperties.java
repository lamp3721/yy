package com.yiyan.infrastructure.config;

import com.yiyan.core.domain.Sentence;
import com.fasterxml.jackson.databind.JsonNode;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.util.StringUtils;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.HashMap;
import java.util.ArrayList;

/**
 * API端点配置类，通过 @ConfigurationProperties 从 application.yml/properties 文件中加载配置。
 * <p>
 * 这种方式将API配置与代码分离，实现了高度的可配置性。
 */
@Data
@Configuration
@ConfigurationProperties(prefix = "yiyan")
public class ApiProperties {

    /**
     * "一言"文本的最大允许长度。超过此长度的将被丢弃。
     */
    private int maxTextLength = 60;

    /**
     * 用于模拟浏览器的默认 User-Agent。
     * 如果单个端点没有指定自己的headers，则会使用此值。
     */
    private String defaultUserAgent = "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/108.0.0.0 Safari/537.36";

    /**
     * 外部API列表文件的路径。
     * 支持 "classpath:" 和 "file:" 前缀。
     */
    private String apiListPath;

    /**
     * 存储所有API端点配置的列表。
     * 这个列表现在由 ApiConfigLoader 在应用启动时动态填充。
     */
    private List<ApiEndpoint> endpoints = new ArrayList<>();

    /**
     * 定义单个API端点的配置
     */
    @Data
    public static class ApiEndpoint {
        /**
         * API的名称，用于日志记录和识别
         */
        private String name;

        /**
         * API的请求URL
         */
        private String url;

        /**
         * 解析器的配置
         */
        private ParserConfig parser;

        /**
         * 请求头，用于模拟浏览器或其他客户端
         */
        private Map<String, String> headers;
    }

    /**
     * 新增的内部类，用于定义解析规则
     */
    @Data
    public static class ParserConfig {
        /**
         * 解析类型, "json" 或 "plain_text"
         */
        private String type = "json"; // 默认为json解析

        /**
         * 字段映射关系。
         * Key 是领域对象的字段名 (如 "text", "author")。
         * Value 是在JSON响应中的路径 (如 "hitokoto", "/data/content")。
         */
        private Map<String, String> mappings = new HashMap<>();
    }
} 