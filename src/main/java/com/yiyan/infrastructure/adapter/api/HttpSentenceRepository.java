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

            return parseSentence(responseBody, endpoint.getParser());

        } catch (IOException e) {
            handleFailure(endpoint, "网络错误: " + e.getMessage());
            // 对于仓库层，不向上抛出IO异常，而是返回空Optional，由服务层决定如何处理
            return Optional.empty();
        }
    }

    private Optional<Sentence> parseSentence(String responseBody, ApiProperties.ParserConfig parserConfig) {
        try {
            if ("plain_text".equalsIgnoreCase(parserConfig.getType())) {
                return Optional.of(Sentence.of(responseBody));
            }

            if ("json".equalsIgnoreCase(parserConfig.getType())) {
                JsonNode root = objectMapper.readTree(responseBody);
                String text = getNodeText(root, parserConfig.getTextPath());
                String author = getNodeText(root, parserConfig.getAuthorPath());

                if (StringUtils.hasText(text)) {
                    return Optional.of(Sentence.of(text, author));
                }
            }
        } catch (IOException e) {
            log.error("解析响应体失败: {}", e.getMessage());
        }
        return Optional.empty();
    }

    private String getNodeText(JsonNode root, String path) {
        if (root == null || !StringUtils.hasText(path)) {
            return null;
        }
        // 如果路径以'/'开头，则使用JSON Pointer的at()方法；否则，使用常规的path()方法。
        // 这使得解析器能同时支持 "key" 和 "/pointer/to/key" 两种格式。
        JsonNode node = path.startsWith("/") ? root.at(path) : root.path(path);
        return node.isMissingNode() ? null : node.asText();
    }

    /**
     * 处理API请求失败的逻辑。
     * @param endpoint 失败的端点
     * @param reason   失败原因
     */
    private void handleFailure(ApiProperties.ApiEndpoint endpoint, String reason) {
        endpoint.recordFailure();
        log.warn("API [{}] 请求失败 (第 {} 次): {}", endpoint.getName(), endpoint.getFailureCount(), reason);

        if (endpoint.getFailureCount() >= FAILURE_THRESHOLD) {
            endpoint.setDisabledUntil(Instant.now().plus(DISABLED_DURATION));
            log.error("API [{}] 已连续失败 {} 次，将被禁用 {} 分钟。",
                    endpoint.getName(), FAILURE_THRESHOLD, DISABLED_DURATION.toMinutes());
        }
    }
} 