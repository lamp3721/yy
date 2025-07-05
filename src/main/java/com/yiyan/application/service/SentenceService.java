package com.yiyan.application.service;

import com.yiyan.application.event.SentenceFetchedEvent;
import com.yiyan.core.domain.Sentence;
import com.yiyan.core.repository.SentenceRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;

import java.util.Optional;

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
     * 此方法会尝试获取一次"一言"。如果成功，则发布一个 {@link SentenceFetchedEvent} 事件。
     * 如果失败（例如，由于网络问题或API返回错误），它会向上抛出异常，
     * 由调用方（如调度器）来处理重试逻辑。
     *
     * @throws RuntimeException 如果获取"一言"时发生任何错误。
     */
    public void fetchNewSentence() {
        log.info("开始尝试获取新的一言...");
        Optional<Sentence> sentenceOpt = sentenceRepository.findRandomSentence();

        if (sentenceOpt.isPresent()) {
            Sentence sentence = sentenceOpt.get();
            log.info("成功获取到新的一言: {}", sentence);
            eventPublisher.publishEvent(new SentenceFetchedEvent(this, sentence));
        } else {
            // 如果仓库层返回一个空的Optional，意味着没有获取到有效数据。
            // 抛出异常，以便调度器知道本次尝试失败。
            log.warn("本次未能获取到有效的一言数据。");
            throw new IllegalStateException("未能从任何数据源获取到有效的一言。");
        }
    }
} 