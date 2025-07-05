package com.yiyan.application.service;

import com.yiyan.application.event.SentenceFetchedEvent;
import com.yiyan.core.repository.SentenceRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;

/**
 * 应用服务（用例实现），负责处理获取"一言"的核心业务逻辑。
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class SentenceService {

    private final SentenceRepository sentenceRepository;
    private final ApplicationEventPublisher eventPublisher;

    /**
     * 执行获取新"一言"并发布的任务。
     * <p>
     * 此方法会循环尝试，直到从存储库成功获取到一个有效的"一言"实例。
     * 获取成功后，它会发布一个 {@link SentenceFetchedEvent} 事件。
     */
    public void fetchNewSentence() {
        log.info("开始获取新的一言...");
        // 循环尝试，直到成功获取
        while (true) {
            try {
                sentenceRepository.findRandomSentence().ifPresent(sentence -> {
                    log.info("成功获取到新的一言: {}", sentence);
                    eventPublisher.publishEvent(new SentenceFetchedEvent(this, sentence));
                });
                // 成功获取并发布，跳出循环
                break;
            } catch (Exception e) {
                log.error("获取一言时发生错误，将在5秒后重试...", e);
                try {
                    // 短暂休眠后重试，避免因网络问题等导致CPU空转
                    Thread.sleep(5000);
                } catch (InterruptedException ie) {
                    log.warn("获取一言的重试等待被中断。");
                    Thread.currentThread().interrupt(); // 重新设置中断状态
                    break;
                }
            }
        }
    }
} 