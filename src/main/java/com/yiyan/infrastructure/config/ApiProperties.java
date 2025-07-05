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
     * 存储所有API端点配置的列表
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

        // --- 熔断机制所需的状态字段 ---
        /**
         * 瞬态的失败计数器，不从配置文件读取。
         */
        private transient int failureCount = 0;

        /**
         * 瞬态的禁用截止时间，不从配置文件读取。
         */
        private transient Instant disabledUntil = Instant.EPOCH;

        /**
         * 判断此端点当前是否处于禁用（熔断）状态。
         *
         * @return 如果当前时间晚于禁用截止时间，则返回 false（可用）。
         */
        public boolean isDisabled() {
            return Instant.now().isBefore(disabledUntil);
        }

        /**
         * 记录一次失败。
         * 如果达到失败阈值，则设置禁用时间。
         */
        public void recordFailure() {
            this.failureCount++;
        }

        /**
         * 记录一次成功，重置失败计数器。
         */
        public void recordSuccess() {
            this.failureCount = 0;
            this.disabledUntil = Instant.EPOCH; // 如果之前被禁用了，则解除
        }

        /**
         * 获取当前失败次数。
         */
        public int getFailureCount() {
            return failureCount;
        }

        /**
         * 设置此端点的禁用截止时间。
         * @param disabledUntil 禁用到的时刻。
         */
        public void setDisabledUntil(Instant disabledUntil) {
            this.disabledUntil = disabledUntil;
        }
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

        public String getType() {
            return type;
        }

        public void setType(String type) {
            this.type = type;
        }

        public Map<String, String> getMappings() {
            return mappings;
        }

        public void setMappings(Map<String, String> mappings) {
            this.mappings = mappings;
        }

        @Deprecated
        public void setTextPath(String textPath) {
            // 保持向后兼容，如果旧的textPath存在，则自动转换为新的mappings格式
            if (StringUtils.hasText(textPath)) {
                this.mappings.put("text", textPath);
            }
        }
    }

    public int getMaxTextLength() {
        return maxTextLength;
    }

    public void setMaxTextLength(int maxTextLength) {
        this.maxTextLength = maxTextLength;
    }

    public List<ApiEndpoint> getEndpoints() {
        return endpoints;
    }
} 