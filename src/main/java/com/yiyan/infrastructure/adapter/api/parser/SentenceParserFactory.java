package com.yiyan.infrastructure.adapter.api.parser;

import com.yiyan.application.service.SentenceParser;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.Optional;

/**
 * 解析器工厂，用于根据类型获取对应的 SentenceParser 实例。
 */
@Component
@Slf4j
public class SentenceParserFactory {

    /**
     * 存储所有已注册的解析器Bean的Map，
     * Spring通过构造函数自动注入所有SentenceParser接口的实现。
     * Key是Bean的名称(例如 "json"), Value是对应的解析器实例。
     */
    private final Map<String, SentenceParser> parsers;

    /**
     * 通过构造函数注入，Spring会自动将所有 SentenceParser 类型的Bean注入到这个Map中。
     * Key是Bean的名称（例如，我们定义的 "json", "plain_text"），Value是Bean实例。
     * @param parsers 包含所有解析器实现的Map。
     */
    public SentenceParserFactory(Map<String, SentenceParser> parsers) {
        this.parsers = parsers;
        log.info("加载的解析器: {}", parsers.keySet());
    }

    /**
     * 根据类型获取解析器。
     * @param type 解析器类型 (e.g., "json", "plain_text")。
     * @return 包含解析器的Optional，如果找不到则为空。
     */
    public Optional<SentenceParser> getParser(String type) {
        SentenceParser parser = parsers.get(type.toLowerCase());
        if (parser == null) {
            log.warn("未找到类型为 '{}' 的解析器。", type);
        }
        return Optional.ofNullable(parser);
    }
} 