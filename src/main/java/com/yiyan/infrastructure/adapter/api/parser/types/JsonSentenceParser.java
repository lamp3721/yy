package com.yiyan.infrastructure.adapter.api.parser.types;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.yiyan.application.service.SentenceParser;
import com.yiyan.core.domain.Sentence;
import com.yiyan.infrastructure.adapter.api.config.ApiProperties;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.io.IOException;
import java.util.Map;
import java.util.Optional;

@Component("json")
@Slf4j
@RequiredArgsConstructor
public class JsonSentenceParser implements SentenceParser {

    /**
     * Jackson的核心，用于将JSON字符串转换为JsonNode对象
     */
    private final ObjectMapper objectMapper;
    /**
     * API配置属性，用于获取如最大文本长度等全局配置
     */
    private final ApiProperties apiProperties;

    @Override
    public Optional<Sentence> parse(String responseBody, ApiProperties.ApiEndpoint endpoint, boolean skipValidation) {
        // HTML内容嗅探
        if (responseBody.trim().toLowerCase().matches("(?s)^<(!doctype|html).*")) {
            log.warn("⚠️ API [{}] (JSON Parser) 返回了HTML页面, 将丢弃.", endpoint.getName());
            return Optional.empty();
        }

        try {
            JsonNode root = objectMapper.readTree(responseBody);  // 将结果JSON字符串转换为JsonNode对象
            Map<String, String> mappings = endpoint.getParser().getMappings();  // 获取解析器配置

            String textPath = mappings.get("text");  // 获取解析器配置的text字段映射
            if (!StringUtils.hasText(textPath)) {
                log.warn("⚠️ API [{}] 的解析器配置缺少必需的 'text' 字段映射。", endpoint.getName());
                return Optional.empty();
            }

            // 获取JSON响应中指定路径的text
            String text = getNodeText(root, textPath);
            if (!StringUtils.hasText(text)) {
                log.warn("⚠️ API [{}] 的JSON响应中, 路径 '{}' 未找到或内容为空.", endpoint.getName(), textPath);
                return Optional.empty();
            }

            // 文本长度校验
            if (!skipValidation && text.length() > apiProperties.getMaxTextLength()) {
                log.warn("⚠️ API [{}] 返回的文本过长 ({} > {}), 将被丢弃.", endpoint.getName(), text.length(), apiProperties.getMaxTextLength());
                return Optional.empty();
            }

            // 作者
            String authorPath = mappings.get("author");  // 获取解析器配置的author字段映射
            String author = StringUtils.hasText(authorPath) ? getNodeText(root, authorPath) : null;

            return Optional.of(Sentence.of(text, author));

        } catch (IOException e) {
            log.error("❌ API [{}] 的JSON响应无法解析. Body: {}. 错误: {}", endpoint.getName(), getBodySnippet(responseBody), e.getMessage());
            return Optional.empty();
        }
    }

    /**
     * 获取JSON节点的文本
     * @param root JsonNode 结果
     * @param path 节点路径
     * @return  节点文本
     */
    private String getNodeText(JsonNode root, String path) {
        if (root == null || !StringUtils.hasText(path)) {
            return null;
        }
        // 如果路径以'/'开头，则使用JSON Pointer的at()方法；否则，使用常规的path()方法。
        // 这使得解析器能同时支持 "key" 和 "/pointer/to/key" 两种格式。
        // 如果开头是'/'，则使用 /a/b 取出子节点，否则使用常规的path()方法。
        JsonNode node = path.startsWith("/") ? root.at(path) : root.path(path);

        //如果节点存在，则返回节点的文本值
        return node.isMissingNode() ? null : node.textValue();
    }

    private String getBodySnippet(String body) {
        return body.length() > 100 ? body.substring(0, 100) + "..." : body;
    }
} 