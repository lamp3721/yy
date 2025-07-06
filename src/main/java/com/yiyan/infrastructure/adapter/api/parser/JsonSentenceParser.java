package com.yiyan.infrastructure.adapter.api.parser;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.yiyan.application.service.SentenceParser;
import com.yiyan.core.domain.Sentence;
import com.yiyan.infrastructure.adapter.api.HttpSentenceRepository;
import com.yiyan.infrastructure.config.ApiProperties;
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

    private final ObjectMapper objectMapper;
    private final ApiProperties apiProperties; // 注入ApiProperties以获取maxTextLength等全局配置

    @Override
    public Optional<Sentence> parse(String responseBody, ApiProperties.ApiEndpoint endpoint, boolean skipValidation) {
        // HTML内容嗅探
        if (responseBody.trim().toLowerCase().matches("(?s)^<(!doctype|html).*")) {
            log.warn("⚠️ API [{}] (JSON Parser) 返回了HTML页面, 将丢弃.", endpoint.getName());
            return Optional.empty();
        }

        try {
            JsonNode root = objectMapper.readTree(responseBody);
            Map<String, String> mappings = endpoint.getParser().getMappings();

            String textPath = mappings.get("text");
            if (!StringUtils.hasText(textPath)) {
                log.warn("⚠️ API [{}] 的解析器配置缺少必需的 'text' 字段映射。", endpoint.getName());
                return Optional.empty();
            }

            String text = getNodeText(root, textPath);
            if (!StringUtils.hasText(text)) {
                log.warn("⚠️ API [{}] 的JSON响应中, 路径 '{}' 未找到或内容为空.", endpoint.getName(), textPath);
                return Optional.empty();
            }

            if (!skipValidation && text.length() > apiProperties.getMaxTextLength()) {
                log.warn("⚠️ API [{}] 返回的文本过长 ({} > {}), 将被丢弃.", endpoint.getName(), text.length(), apiProperties.getMaxTextLength());
                return Optional.empty();
            }

            String authorPath = mappings.get("author");
            String author = StringUtils.hasText(authorPath) ? getNodeText(root, authorPath) : null;

            return Optional.of(Sentence.of(text, author));

        } catch (IOException e) {
            log.error("❌ API [{}] 的JSON响应无法解析. Body: {}. 错误: {}", endpoint.getName(), getBodySnippet(responseBody), e.getMessage());
            return Optional.empty();
        }
    }

    private String getNodeText(JsonNode root, String path) {
        if (root == null || !StringUtils.hasText(path)) {
            return null;
        }
        JsonNode node = path.startsWith("/") ? root.at(path) : root.path(path);
        return node.isMissingNode() ? null : node.textValue();
    }

    private String getBodySnippet(String body) {
        return body.length() > 100 ? body.substring(0, 100) + "..." : body;
    }
} 