package com.yiyan.infrastructure.adapter.api;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.yiyan.core.domain.Sentence;
import com.yiyan.core.repository.SentenceRepository;
import com.yiyan.infrastructure.config.ApiProperties;
import lombok.extern.slf4j.Slf4j;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.ResponseBody;
import org.springframework.stereotype.Repository;
import org.springframework.util.StringUtils;

import java.io.IOException;
import java.time.Duration;
import java.time.Instant;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.Collectors;

/**
 * SentenceRepository 的HTTP实现，负责从外部API获取"一言"数据。
 * <p>
 * 这个类是"适配器"模式的体现，它将外部API的特定协议（HTTP）和数据格式（JSON）
 * 适配到应用内部的领域模型（Sentence）。
 */
@Repository
@Slf4j
public class HttpSentenceRepository implements SentenceRepository {

    private final ApiProperties apiProperties;
    private final OkHttpClient httpClient;
    private final ObjectMapper objectMapper;

    // 熔断机制参数
    private static final int FAILURE_THRESHOLD = 3; // 失败3次后熔断
    private static final Duration DISABLED_DURATION = Duration.ofMinutes(15); // 熔断15分钟
    private static final int FIND_ATTEMPTS_PER_CYCLE = 3; // 在单个任务周期内，最多尝试3个不同的API

    // 通过构造函数注入依赖
    public HttpSentenceRepository(ApiProperties apiProperties, OkHttpClient httpClient) {
        this.apiProperties = apiProperties;
        this.httpClient = httpClient;
        this.objectMapper = new ObjectMapper();
    }

    /**
     * 实现从配置的API列表中随机获取一个"一言"的逻辑。
     *
     * @return 返回一个包含Sentence的可选值。
     * @throws IllegalStateException 如果API端点列表为空。
     * @throws IOException           如果网络请求失败。
     */
    @Override
    public Optional<Sentence> findRandomSentence() {
        List<ApiProperties.ApiEndpoint> availableEndpoints = apiProperties.getEndpoints().stream()
                .filter(e -> !e.isDisabled())
                .collect(Collectors.toList());

        if (availableEndpoints.isEmpty()) {
            log.warn("所有API端点当前都处于熔断状态，无法获取数据。");
            return Optional.empty();
        }

        // 将可用端点随机排序，以确保每次尝试都是随机的
        Collections.shuffle(availableEndpoints);

        // 尝试最多N个不同的端点
        for (int i = 0; i < Math.min(FIND_ATTEMPTS_PER_CYCLE, availableEndpoints.size()); i++) {
            ApiProperties.ApiEndpoint endpoint = availableEndpoints.get(i);
            log.info("尝试从API [{}] 获取数据 (尝试 {}/{})", endpoint.getName(), i + 1, FIND_ATTEMPTS_PER_CYCLE);

            try {
                Optional<Sentence> sentence = attemptFetch(endpoint);
                if (sentence.isPresent()) {
                    log.info("成功从 API [{}] 获取数据, URL: {}", endpoint.getName(), endpoint.getUrl());
                    return sentence; // 成功获取，立即返回
                }
                // 如果返回空Optional，意味着本次尝试失败，循环将继续尝试下一个端点
            } catch (Exception e) {
                // 记录意外的解析或请求错误，然后继续尝试下一个
                handleFailure(endpoint, "执行请求或解析时发生意外错误: " + e.getMessage());
            }
        }

        log.warn("在当前任务周期内尝试了 {} 个API后，仍未能获取到有效的一言。", FIND_ATTEMPTS_PER_CYCLE);
        return Optional.empty(); // 所有尝试都失败了
    }

    private Optional<Sentence> attemptFetch(ApiProperties.ApiEndpoint endpoint) throws IOException {
        Request request = new Request.Builder()
                .url(endpoint.getUrl())
                .headers(okhttp3.Headers.of(endpoint.getHeaders() != null ? endpoint.getHeaders() : new java.util.HashMap<>()))
                .build();

        try (Response response = httpClient.newCall(request).execute()) {
            if (!response.isSuccessful()) {
                handleFailure(endpoint, "HTTP状态码: " + response.code());
                return Optional.empty();
            }

            ResponseBody body = response.body();
            String responseBody = (body != null) ? body.string() : "";
            if (responseBody.trim().isEmpty()) {
                handleFailure(endpoint, "响应体为空");
                return Optional.empty();
            }
            
            endpoint.recordSuccess(); // 请求成功，重置失败计数器

            return parseSentence(responseBody, endpoint);

        } catch (IOException e) {
            handleFailure(endpoint, "网络错误: " + e.getMessage());
            // 对于仓库层，不向上抛出IO异常，而是返回空Optional，由服务层决定如何处理
            return Optional.empty();
        }
    }

    private Optional<Sentence> parseSentence(String responseBody, ApiProperties.ApiEndpoint endpoint) {
        ApiProperties.ParserConfig parserConfig = endpoint.getParser();
        try {
            if ("plain_text".equalsIgnoreCase(parserConfig.getType())) {
                if (responseBody.length() > 2000) {
                    log.warn("API [{}] 返回的纯文本响应过长 ({} chars)，可能非预期内容。", endpoint.getName(), responseBody.length());
                }
                return Optional.of(Sentence.of(responseBody));
            }

            if ("json".equalsIgnoreCase(parserConfig.getType())) {
                JsonNode root = objectMapper.readTree(responseBody);
                Map<String, String> mappings = parserConfig.getMappings();

                // "text" 字段是必需的
                String textPath = mappings.get("text");
                if (!StringUtils.hasText(textPath)) {
                    log.error("API [{}] 的解析器配置缺少必需的 'text' 字段映射。", endpoint.getName());
                    return Optional.empty();
                }

                String text = getNodeText(root, textPath);
                if (!StringUtils.hasText(text)) {
                    log.warn("API [{}] 的JSON响应中, 路径 '{}' 未找到或内容为空. Body: {}",
                            endpoint.getName(), textPath, getBodySnippet(responseBody));
                    return Optional.empty();
                }

                // "author" 字段是可选的
                String authorPath = mappings.get("author");
                String author = StringUtils.hasText(authorPath) ? getNodeText(root, authorPath) : null;

                return Optional.of(Sentence.of(text, author));
            }
        } catch (IOException e) { // 包括 JsonProcessingException
            log.error("API [{}] 的响应无法解析为JSON. Body: {}. 错误: {}",
                    endpoint.getName(), getBodySnippet(responseBody), e.getMessage());
        }
        return Optional.empty();
    }

    private String getNodeText(JsonNode root, String path) {
        if (root == null || !StringUtils.hasText(path)) {
            return null;
        }
        JsonNode node = path.startsWith("/") ? root.at(path) : root.path(path);
        // 使用 .textValue() 代替 .asText()。
        // .textValue() 只在节点是真实文本时返回值，对于JSON null、对象、数组等均返回null。
        // 这能有效避免将 "null" 字符串或 "{...}" 作为一言内容。
        return node.isMissingNode() ? null : node.textValue();
    }

    /**
     * 获取响应体内容的片段，用于日志记录。
     * @param body 响应体字符串
     * @return 最多前300个字符的片段
     */
    private String getBodySnippet(String body) {
        return body.substring(0, Math.min(body.length(), 300));
    }

    /**
     * 处理API请求失败的逻辑。
     * @param endpoint 失败的端点
     * @param reason   失败原因
     */
    private void handleFailure(ApiProperties.ApiEndpoint endpoint, String reason) {
        endpoint.recordFailure();
        log.warn("API [{}] 请求失败 (URL: {}), 失败次数: {}, 原因: {}",
                endpoint.getName(), endpoint.getUrl(), endpoint.getFailureCount(), reason);

        if (endpoint.getFailureCount() >= FAILURE_THRESHOLD) {
            endpoint.setDisabledUntil(Instant.now().plus(DISABLED_DURATION));
            log.error("API [{}] (URL: {}) 已连续失败 {} 次，将被禁用 {} 分钟。",
                    endpoint.getName(), endpoint.getUrl(), FAILURE_THRESHOLD, DISABLED_DURATION.toMinutes());
        }
    }
} 