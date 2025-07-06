package com.yiyan.infrastructure.adapter.api;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.yiyan.core.domain.Sentence;
import com.yiyan.core.repository.SentenceRepository;
import com.yiyan.infrastructure.config.ApiProperties;
import com.yiyan.infrastructure.adapter.api.parser.SentenceParserFactory;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
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

import java.util.ArrayList;
import lombok.RequiredArgsConstructor;

/**
 * SentenceRepository 的HTTP实现，负责从外部API获取"一言"数据。
 * <p>
 * 这个类是"适配器"模式的体现，它将外部API的特定协议（HTTP）和数据格式（JSON）
 * 适配到应用内部的领域模型（Sentence）。
 */
@Repository
@Slf4j
@RequiredArgsConstructor
public class HttpSentenceRepository implements SentenceRepository {

    /**
     * API配置属性，包含了所有API端点的信息和全局设置
     */
    private final ApiProperties apiProperties;
    /**
     * OkHttp客户端，用于执行HTTP请求
     */
    private final OkHttpClient httpClient;
    /**
     * 解析器工厂，根据API配置动态提供合适的解析器实例
     */
    private final SentenceParserFactory parserFactory;

    // 网络错误冷却状态
    private volatile boolean networkErrorCooldown = false;
    private volatile long networkErrorCooldownEndTimestamp = 0;
    private static final long NETWORK_COOLDOWN_DURATION_MS = 10_000; // 10秒冷却

    /**
     * 自定义的运行时异常，用于表示一个逻辑上的失败（例如，API返回的数据不符合预期），
     * 这种失败不应该触发熔断器。
     */
    private static class LogicalException extends RuntimeException {
        public LogicalException(String message) {
            super(message);
        }
    }

    /**
     * 实现从配置的API列表中随机获取一个"一言"的逻辑。
     *
     * @param skipValidation 如果为 true，则跳过业务逻辑校验。
     * @return 返回一个包含Sentence的可选值。
     * @throws IllegalStateException 如果API端点列表为空。
     */
    @Override
    public Optional<Sentence> fetchRandomSentence(boolean skipValidation) {
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

        // 注意：此处不再需要手动过滤 e.isDisabled()，因为熔断器会自动处理
        List<ApiProperties.ApiEndpoint> availableEndpoints = new ArrayList<>(apiProperties.getEndpoints());

        if (availableEndpoints.isEmpty()) {
            log.warn("🤷‍ API列表为空，无法获取数据。");
            return Optional.empty();
        }

        Collections.shuffle(availableEndpoints);

        // 2. 遍历所有可用的API，直到成功或全部失败
        for (ApiProperties.ApiEndpoint endpoint : availableEndpoints) {
            log.info("⏳ 尝试从API [{}] 获取数据...", endpoint.getName());
            try {
                // 将 skipValidation 参数传递给 attemptFetch
                Optional<Sentence> sentence = attemptFetch(endpoint, skipValidation);
                if (sentence.isPresent()) {
                    log.info("✅ 成功从 API [{}] 获取数据, URL: {}", endpoint.getName(), endpoint.getUrl());
                    return sentence; // 成功获取，立即返回
                }
                // 如果返回 Optional.empty()，说明是"数据"或"逻辑"错误，非网络问题，循环将继续尝试下一个API
            } catch (io.github.resilience4j.circuitbreaker.CallNotPermittedException e) {
                // 熔断器处于打开状态，直接跳过此API
                log.warn(" CIRCUIT_BREAKER is OPEN for API [{}]. Skipping.", endpoint.getName());
            } catch (IOException e) {
                // 3. 如果是网络问题，则进入冷却期并中断本次所有尝试
                log.warn("🚨 检测到网络连接问题 (API: {}). 将暂停获取 {} 秒。", endpoint.getName(), NETWORK_COOLDOWN_DURATION_MS / 1000);
                this.networkErrorCooldown = true;
                this.networkErrorCooldownEndTimestamp = System.currentTimeMillis() + NETWORK_COOLDOWN_DURATION_MS;
                break; // 中断 for 循环，不再尝试其他API
            } catch (Exception e) {
                // 捕获其他意料之外的异常，以防循环中断
                log.error("处理API [{}] 时发生意外错误: {}", endpoint.getName(), e.getMessage());
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
                // 执行一次尝试性获取，并跳过校验
                Optional<Sentence> sentenceOpt = attemptFetch(endpoint, true);
                if (sentenceOpt.isPresent()) {
                    status = "✅ OK";
                    // 将获取到的内容附加到原因中，用于日志输出
                    reason = "-> " + sentenceOpt.get().toString();
                    successCount++;
                } else {
                    // 如果返回空，意味着是逻辑失败，不应发生，但在自检中标记出来
                    status = "❌ FAILED";
                    reason = "返回空Optional，可能存在未捕获的逻辑错误";
                }
            } catch (LogicalException e) {
                status = "❌ FAILED";
                reason = "逻辑失败: " + e.getMessage();
            } catch (io.github.resilience4j.circuitbreaker.CallNotPermittedException e) {
                status = "OPEN";
                reason = "熔断器处于打开状态";
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

    @CircuitBreaker(name = "shared-api-breaker")
    private Optional<Sentence> attemptFetch(ApiProperties.ApiEndpoint endpoint, boolean skipValidation) throws IOException {
        // 准备请求头，并应用默认的 User-Agent
        okhttp3.Headers headers = buildHeaders(endpoint);

        Request request = new Request.Builder()
                .url(endpoint.getUrl())
                .headers(headers)
                .build();

        // IOException 将从此向上抛出，由 findRandomSentence 捕获
        try (Response response = httpClient.newCall(request).execute()) {
            ResponseBody body = response.body();
            if (!response.isSuccessful() || body == null) {
                // 对于不成功的HTTP状态码，抛出逻辑异常，避免触发熔断
                throw new LogicalException(String.format("API [%s] 请求失败, HTTP状态码: %d", endpoint.getName(), response.code()));
            }

            String responseBody = body.string();
            // 将 skipValidation 参数传递给解析器工厂
            return parserFactory.getParser(endpoint.getParser().getType())
                    .flatMap(parser -> parser.parse(responseBody, endpoint, skipValidation));
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
} 