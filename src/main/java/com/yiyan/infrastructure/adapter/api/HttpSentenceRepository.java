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
import java.util.concurrent.CompletableFuture;
import org.springframework.scheduling.annotation.Async;

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

    // 网络错误冷却状态
    private volatile boolean networkErrorCooldown = false;
    private volatile long networkErrorCooldownEndTimestamp = 0;
    private static final long NETWORK_COOLDOWN_DURATION_MS = 10_000; // 10秒冷却

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
     */
    @Override
    public Optional<Sentence> findRandomSentence() {
        // 1. 检查是否处于网络错误冷却状态
        if (networkErrorCooldown) {
            if (System.currentTimeMillis() < networkErrorCooldownEndTimestamp) {
                log.info("⏰ 网络错误冷却中，跳过本次获取任务。");
                return Optional.empty();
            } else {
                log.info("🟢 网络冷却期结束，恢复正常获取。");
                networkErrorCooldown = false; // 冷却期结束
            }
        }

        List<ApiProperties.ApiEndpoint> availableEndpoints = apiProperties.getEndpoints().stream()
                .filter(e -> !e.isDisabled())
                .collect(Collectors.toList());

        if (availableEndpoints.isEmpty()) {
            log.warn("⛔ 所有API端点当前都处于熔断状态，无法获取数据。");
            return Optional.empty();
        }

        Collections.shuffle(availableEndpoints);

        // 2. 遍历所有可用的API，直到成功或全部失败
        for (ApiProperties.ApiEndpoint endpoint : availableEndpoints) {
            log.info("⏳ 尝试从API [{}] 获取数据...", endpoint.getName());
            try {
                // attemptFetch 现在会抛出 IOException
                Optional<Sentence> sentence = attemptFetch(endpoint);
                if (sentence.isPresent()) {
                    log.info("✅ 成功从 API [{}] 获取数据, URL: {}", endpoint.getName(), endpoint.getUrl());
                    return sentence; // 成功获取，直接返回
                }
                // 如果返回 Optional.empty()，说明是"数据"或"逻辑"错误，非网络问题，循环将继续尝试下一个API
            } catch (IOException e) {
                // 3. 如果是网络问题，则进入冷却期并中断本次所有尝试
                handleFailure(endpoint, "网络错误: " + e.getMessage());
                log.warn("🚨 检测到网络连接问题 (API: {}). 将暂停获取 {} 秒。", endpoint.getName(), NETWORK_COOLDOWN_DURATION_MS / 1000);
                this.networkErrorCooldown = true;
                this.networkErrorCooldownEndTimestamp = System.currentTimeMillis() + NETWORK_COOLDOWN_DURATION_MS;
                break; // 中断 for 循环，不再尝试其他API
            } catch (Exception e) {
                // 捕获其他意料之外的异常，以防循环中断
                handleFailure(endpoint, "处理时发生意外错误: " + e.getMessage());
            }
        }

        if (!networkErrorCooldown) {
            log.warn("🤷 尝试了所有可用API后，仍未能获取到有效的一言。");
        }
        return Optional.empty();
    }

    /**
     * 异步执行所有API端点的健康检查。
     * <p>
     * 此方法在单独的线程中运行，不会阻塞主应用启动。
     * 它会测试每个端点，并记录其状态。
     */
    @Async
    public void checkAllApisAsync() {
        log.info("--- 开始API自检 ---");
        List<ApiProperties.ApiEndpoint> allEndpoints = apiProperties.getEndpoints();
        long successCount = 0;

        for (int i = 0; i < allEndpoints.size(); i++) {
            ApiProperties.ApiEndpoint endpoint = allEndpoints.get(i);
            String status;
            String reason = "";
            try {
                // 执行一次尝试性获取
                Optional<Sentence> sentenceOpt = attemptFetch(endpoint);
                if (sentenceOpt.isPresent()) {
                    status = "✅ OK";
                    // 将获取到的内容附加到原因中，用于日志输出
                    reason = "-> " + sentenceOpt.get().toString();
                    successCount++;
                } else {
                    // attemptFetch 内部处理了非200状态码等逻辑错误，并返回empty
                    status = "❌ FAILED";
                    reason = "返回数据无效或解析失败";
                }
            } catch (IOException e) {
                // 网络层面的异常
                status = "❌ FAILED";
                reason = "网络错误: " + e.getMessage();
            } catch (Exception e) {
                // 其他未知异常
                status = "❌ FAILED";
                reason = "未知错误: " + e.getMessage();
            }
            log.info("[{}/{}] [{}] -> {}{}", i + 1, allEndpoints.size(), endpoint.getName(), status, reason.isEmpty() ? "" : " (" + reason + ")");
        }

        log.info("--- API自检完成: {}/{} 个API可用 ---", successCount, allEndpoints.size());
    }

    private Optional<Sentence> attemptFetch(ApiProperties.ApiEndpoint endpoint) throws IOException {
        // 准备请求头，并应用默认的 User-Agent
        okhttp3.Headers headers = buildHeaders(endpoint);

        Request request = new Request.Builder()
                .url(endpoint.getUrl())
                .headers(headers)
                .build();

        // IOException 将从此向上抛出，由 findRandomSentence 捕获
        try (Response response = httpClient.newCall(request).execute()) {
            if (!response.isSuccessful()) {
                handleFailure(endpoint, "HTTP状态码: " + response.code());
                return Optional.empty();
            }

            ResponseBody body = response.body();
            if (body == null) {
                handleFailure(endpoint, "响应体为 null");
                return Optional.empty();
            }

            String contentType = response.header("Content-Type", ""); // Default to empty string if null
            String responseBody = body.string(); // 此处也可能抛出IOException

            if (responseBody.trim().isEmpty()) {
                handleFailure(endpoint, "响应体为空白");
                return Optional.empty();
            }

            endpoint.recordSuccess();

            return parseSentence(responseBody, contentType, endpoint);
        }
    }

    /**
     * 构建请求头。
     * 如果端点配置中没有提供User-Agent，则使用默认的User-Agent。
     * @param endpoint API端点配置
     * @return OkHttp的Headers对象
     */
    private okhttp3.Headers buildHeaders(ApiProperties.ApiEndpoint endpoint) {
        Map<String, String> endpointHeaders = new java.util.HashMap<>();

        // 1. 复制端点自定义的头信息
        if (endpoint.getHeaders() != null) {
            endpointHeaders.putAll(endpoint.getHeaders());
        }

        // 2. 检查是否存在User-Agent（不区分大小写），如果不存在，则添加默认值
        boolean hasUserAgent = endpointHeaders.keySet().stream()
                .anyMatch(key -> key.equalsIgnoreCase("User-Agent"));

        if (!hasUserAgent && StringUtils.hasText(apiProperties.getDefaultUserAgent())) {
            endpointHeaders.put("User-Agent", apiProperties.getDefaultUserAgent());
        }

        return okhttp3.Headers.of(endpointHeaders);
    }

    private Optional<Sentence> parseSentence(String responseBody, String contentType, ApiProperties.ApiEndpoint endpoint) {
        ApiProperties.ParserConfig parserConfig = endpoint.getParser();
        try {
            // --- JSON Parser Logic ---
            if ("json".equalsIgnoreCase(parserConfig.getType())) {
                // 增加HTML内容嗅探
                if (responseBody.trim().toLowerCase().matches("(?s)^<(!doctype|html).*")) {
                    log.error("❌ API [{}] 配置为JSON类型, 但返回了HTML页面, 请检查API有效性或更换. Body: {}", endpoint.getName(), getBodySnippet(responseBody));
                    return Optional.empty();
                }

                // 1. Content-Type validation for JSON. Allow both 'application/json' and 'text/json'.
                if (contentType == null || !contentType.toLowerCase().contains("json")) {
                    log.warn("⚠️ API [{}] 期望JSON类型但收到了'{}'类型(不含'json'), 将丢弃. Body: {}",
                            endpoint.getName(), contentType, getBodySnippet(responseBody));
                    return Optional.empty();
                }

                JsonNode root = objectMapper.readTree(responseBody);
                Map<String, String> mappings = parserConfig.getMappings();

                String textPath = mappings.get("text");
                if (!StringUtils.hasText(textPath)) {
                    log.error("API [{}] 的解析器配置缺少必需的 'text' 字段映射。", endpoint.getName());
                    return Optional.empty();
                }
                String text = getNodeText(root, textPath);
                if (!StringUtils.hasText(text)) {
                    log.warn("⚠️ API [{}] 的JSON响应中, 路径 '{}' 未找到或内容为空. Body: {}",
                            endpoint.getName(), textPath, getBodySnippet(responseBody));
                    return Optional.empty();
                }

                if (text.length() > apiProperties.getMaxTextLength()) {
                    log.warn("⚠️ API [{}] 返回的文本过长 ({} > {}), 将被丢弃. 内容: '{}'",
                            endpoint.getName(), text.length(), apiProperties.getMaxTextLength(), text);
                    return Optional.empty();
                }

                String authorPath = mappings.get("author");
                String author = StringUtils.hasText(authorPath) ? getNodeText(root, authorPath) : null;
                return Optional.of(Sentence.of(text, author));
            }

            // --- Plain Text Parser Logic ---
            if ("plain_text".equalsIgnoreCase(parserConfig.getType())) {
                String trimmedBody = responseBody.trim();
                // 1. Heuristic check for HTML content
                if (trimmedBody.toLowerCase().matches("(?s)^<(!doctype|html).*")) {
                    log.warn("⚠️ API [{}] 期望纯文本但返回了HTML页面, 将丢弃. Body: {}", endpoint.getName(), getBodySnippet(responseBody));
                    return Optional.empty();
                }

                // 2. Length validation
                if (trimmedBody.length() > apiProperties.getMaxTextLength()) {
                    log.warn("⚠️ API [{}] 返回的纯文本过长 ({} > {}), 将被丢弃. 内容: '{}'",
                            endpoint.getName(), trimmedBody.length(), apiProperties.getMaxTextLength(), getBodySnippet(trimmedBody));
                    return Optional.empty();
                }

                return Optional.of(Sentence.of(trimmedBody));
            }

        } catch (IOException e) { // Covers JsonProcessingException
            log.error("❌ API [{}] 的响应无法解析. Body: {}. 错误: {}",
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
        log.warn("❌ API [{}] 请求失败 (URL: {}), 失败次数: {}, 原因: {}",
                endpoint.getName(), endpoint.getUrl(), endpoint.getFailureCount(), reason);

        if (endpoint.getFailureCount() >= FAILURE_THRESHOLD) {
            endpoint.setDisabledUntil(Instant.now().plus(DISABLED_DURATION));
            log.error("⛔ API [{}] (URL: {}) 已连续失败 {} 次，将被禁用 {} 分钟。",
                    endpoint.getName(), endpoint.getUrl(), FAILURE_THRESHOLD, DISABLED_DURATION.toMinutes());
        }
    }
} 