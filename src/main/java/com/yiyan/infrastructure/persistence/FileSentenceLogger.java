package com.yiyan.infrastructure.persistence;

import com.yiyan.event.SentenceFetchedEvent;
import com.yiyan.domain.Sentence;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * 文件日志记录器，负责将获取到的"一言"持久化到本地文件。
 * <p>
 * 它通过监听 {@link SentenceFetchedEvent} 事件来触发，实现了持久化逻辑与核心业务的解耦。
 * 每条记录都会附带时间戳。
 */
@Component
@Slf4j
public class FileSentenceLogger {

    private static final String LOG_FILE = "yiyan_log.txt";
    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    /**
     * 监听 SentenceFetchedEvent 事件，并将获取到的"一言"写入文件。
     *
     * @param event 包含新获取的"一言"的事件对象。
     */
    @EventListener
    public void onSentenceFetched(SentenceFetchedEvent event) {
        Sentence sentence = event.getSentence();
        String timestamp = LocalDateTime.now().format(FORMATTER);
        String logEntry = String.format("[%s] %s", timestamp, sentence.toString());

        log.info("📝 记录新的一言到文件: {}", logEntry);

        // 使用 try-with-resources 确保写入器被正确关闭
        try (FileWriter fw = new FileWriter(LOG_FILE, StandardCharsets.UTF_8, true);
             BufferedWriter bw = new BufferedWriter(fw);
             PrintWriter out = new PrintWriter(bw)) {
            out.println(logEntry);
        } catch (IOException e) {
            log.error("❌ 无法将一言写入日志文件 '{}': {}", LOG_FILE, e.getMessage());
        }
    }
} 