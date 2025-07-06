package com.yiyan.infrastructure.adapter.api.parser;

import com.yiyan.application.service.SentenceParser;
import com.yiyan.core.domain.Sentence;
import com.yiyan.infrastructure.config.ApiProperties;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component("plain_text")
@Slf4j
@RequiredArgsConstructor
public class PlainTextSentenceParser implements SentenceParser {

    /**
     * API配置属性，用于获取如最大文本长度等全局配置
     */
    private final ApiProperties apiProperties;

    @Override
    public Optional<Sentence> parse(String responseBody, ApiProperties.ApiEndpoint endpoint, boolean skipValidation) {
        String trimmedBody = responseBody.trim();

        // HTML内容嗅探
        if (trimmedBody.toLowerCase().matches("(?s)^<(!doctype|html).*")) {
            log.warn("⚠️ API [{}] (Plain Text Parser) 返回了HTML页面, 将丢弃.", endpoint.getName());
            return Optional.empty();
        }

        // 长度校验
        if (!skipValidation && trimmedBody.length() > apiProperties.getMaxTextLength()) {
            log.warn("⚠️ API [{}] 返回的纯文本过长 ({} > {}), 将被丢弃.", endpoint.getName(), trimmedBody.length(), apiProperties.getMaxTextLength());
            return Optional.empty();
        }

        if (trimmedBody.isEmpty()) {
            log.warn("⚠️ API [{}] 返回了空的纯文本.", endpoint.getName());
            return Optional.empty();
        }

        return Optional.of(Sentence.of(trimmedBody));
    }
} 