package com.yiyan.infrastructure.adapter.api;

import com.yiyan.core.domain.Sentence;
import com.yiyan.core.repository.SentenceRepository;
import com.yiyan.infrastructure.config.ApiProperties;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.ResponseBody;
import org.springframework.stereotype.Repository;

import java.io.IOException;
import java.util.Optional;
import java.util.concurrent.ThreadLocalRandom;
import java.util.function.Function;

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

    // 通过构造函数注入依赖
    public HttpSentenceRepository(ApiProperties apiProperties) {
        this.apiProperties = apiProperties;
        this.httpClient = new OkHttpClient(); // 在实际生产中，建议使用共享的Client Bean
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
        if (apiProperties.getEndpoints() == null || apiProperties.getEndpoints().isEmpty()) {
            throw new IllegalStateException("未配置任何API端点。");
        }

        // 随机选择一个API端点
        ApiProperties.ApiEndpoint endpoint = apiProperties.getEndpoints().get(
                ThreadLocalRandom.current().nextInt(apiProperties.getEndpoints().size())
        );

        log.info("尝试从API [{}] 获取数据，URL: {}", endpoint.getName(), endpoint.getUrl());

        // 构建HTTP请求
        Request.Builder requestBuilder = new Request.Builder().url(endpoint.getUrl());
        if (endpoint.getHeaders() != null) {
            endpoint.getHeaders().forEach(requestBuilder::header);
        }
        Request request = requestBuilder.build();

        // 执行请求并处理响应
        try (Response response = httpClient.newCall(request).execute()) {
            if (!response.isSuccessful()) {
                log.warn("API [{}] 请求失败，状态码: {}", endpoint.getName(), response.code());
                return Optional.empty();
            }

            ResponseBody body = response.body();
            if (body == null) {
                log.warn("API [{}] 响应体为空。", endpoint.getName());
                return Optional.empty();
            }

            String responseBody = body.string();
            if (responseBody.trim().isEmpty()) {
                log.warn("API [{}] 响应体内容为空。", endpoint.getName());
                return Optional.empty();
            }

            // 为纯文本响应类型增加特殊处理
            if ("plain_text".equals(endpoint.getParserType())) {
                return Optional.of(Sentence.of(responseBody));
            }

            // 解析JSON并转换为Sentence对象
            Function<JsonNode, Sentence> parser = ApiProperties.PARSERS.get(endpoint.getParserType());
            if (parser == null) {
                log.error("未找到API [{}] 指定的解析器类型: {}", endpoint.getName(), endpoint.getParserType());
                return Optional.empty();
            }

            JsonNode jsonNode = objectMapper.readTree(responseBody);
            // 使用 ofNullable 避免在解析函数返回 null 时抛出异常
            return Optional.ofNullable(parser.apply(jsonNode));

        } catch (IOException e) {
            log.error("访问API [{}] 时发生网络错误: {}", endpoint.getName(), e.getMessage());
            // 将受检异常包装成运行时异常向上抛出，由应用服务层统一处理
            throw new RuntimeException("API请求失败", e);
        }
    }
} 