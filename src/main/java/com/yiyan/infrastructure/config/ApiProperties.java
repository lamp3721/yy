package com.yiyan.infrastructure.config;

import com.yiyan.core.domain.Sentence;
import com.fasterxml.jackson.databind.JsonNode;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import java.util.List;
import java.util.Map;
import java.util.function.Function;

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
     * 存储所有API端点配置的列表
     */
    private List<ApiEndpoint> endpoints;

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
         * 解析器的类型，对应 parsers 映射中的键。
         * 用于指定如何从该API的JSON响应中提取"一言"数据。
         */
        private String parserType;

        /**
         * 请求头，用于模拟浏览器或其他客户端
         */
        private Map<String, String> headers;
    }

    /**
     * JSON解析器注册表，定义了如何从不同结构的JSON中提取Sentence。
     * 这是一个静态映射，键是解析器类型，值是一个函数，
     * 该函数接收一个JsonNode并返回一个Sentence。
     */
    public static final Map<String, Function<JsonNode, Sentence>> PARSERS = Map.ofEntries(
            // 适用于 'hitokoto.cn' API 的解析器: { "hitokoto": "...", "creator": "..." }
            Map.entry("hitokoto", json -> Sentence.of(
                    json.path("hitokoto").asText(),
                    json.path("creator").asText()
            )),
            // 适用于 tenapi.cn: { "data": { "hitokoto": "...", "from": "..." } }
            Map.entry("tenapi", json -> Sentence.of(
                    json.at("/data/hitokoto").asText(),
                    json.at("/data/from").asText()
            )),
            // 适用于 api.xygeng.cn: { "data": { "name": "...", "origin": "..." } }
            Map.entry("xygeng", json -> Sentence.of(
                    json.at("/data/name").asText(),
                    json.at("/data/origin").asText()
            )),
            // 适用于 iamwawa.cn: { "msg": "...", "title": "..." }
            Map.entry("iamwawa", json -> Sentence.of(
                    json.path("msg").asText(),
                    json.path("title").asText()
            )),
            // 适用于 api.songzixian.com: { "content": "...", "author": "..." }
            Map.entry("content_author", json -> Sentence.of(
                    json.path("content").asText(),
                    json.path("author").asText()
            )),
            // 适用于 v1.jinrishici.com: { "content": "...", "origin": "..." }
            Map.entry("content_origin", json -> Sentence.of(
                    json.path("content").asText(),
                    json.path("origin").asText()
            )),
            // 适用于 api.mu-jie.cc (Stray Birds): [ { "zw": "...", "source": "..." } ]
            Map.entry("stray_birds", json -> {
                if (json.isArray() && json.size() > 0) {
                    JsonNode first = json.get(0);
                    return Sentence.of(first.path("zw").asText(), first.path("source").asText());
                }
                return null; // or return a default sentence
            }),
            // 通用解析器：只有文本，没有作者
            Map.entry("text_only", json -> Sentence.of(json.path("text").asText())),
            Map.entry("msg_only", json -> Sentence.of(json.path("msg").asText())),
            Map.entry("data_text_only", json -> Sentence.of(json.at("/data/text").asText())),
            Map.entry("result_only", json -> Sentence.of(json.path("Result").asText())),
            Map.entry("data_only", json -> Sentence.of(json.path("data").asText()))
    );
} 