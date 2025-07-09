package com.yiyan.application.service;

import com.yiyan.core.domain.Sentence;
import com.yiyan.infrastructure.adapter.api.config.ApiProperties;

import java.util.Optional;

/**
 * "一言"响应解析器接口。
 * <p>
 * 定义了将字符串形式的API响应体，根据特定规则，解析为领域对象 Sentence 的契约。
 */
public interface SentenceParser {

    /**
     * 解析响应体字符串。
     *
     * @param responseBody   API返回的原始响应体字符串。
     * @param endpoint       当前API的端点配置，包含解析所需的元数据（如mappings）。
     * @param skipValidation 如果为 true，则应跳过所有业务逻辑校验（如长度限制）。
     * @return 如果解析成功并符合业务规则，则返回一个包含Sentence的Optional；否则返回空的Optional。
     */
    Optional<Sentence> parse(String responseBody, ApiProperties.ApiEndpoint endpoint, boolean skipValidation);
} 