package com.yiyan.service;

import com.yiyan.event.SentenceFetchedEvent;
import com.yiyan.domain.Sentence;
import com.yiyan.infrastructure.adapter.SentenceRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;

import java.util.Optional;
import java.util.concurrent.CompletableFuture;

/**
 * 应用服务（用例实现），负责处理获取"一言"的核心业务逻辑。
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class SentenceServiceImpl implements SentenceService {

    /**
     * "一言"数据仓库，负责从不同来源获取"一言"
     */
    private final SentenceRepository sentenceRepository;
    /**
     * Spring应用事件发布器，用于在获取到新的"一言"后通知其他组件
     */
    private final ApplicationEventPublisher eventPublisher;

    @Override
    public CompletableFuture<Void> requestNewSentenceAsync() {
        return CompletableFuture.runAsync(() -> {
            try {
                // 手动刷新时，强制执行校验
                fetchNewSentence(false);
            } catch (Exception e) {
                log.error("手动刷新失败: {}", e.getMessage());
                // 在这里可以考虑发布一个失败事件，让UI给出提示
            }
        });
    }

    /**
     * 执行获取新"一言"并发布的任务。
     * <p>
     * 此方法会尝试获取一次"一言"。如果成功，则发布一个 {@link SentenceFetchedEvent} 事件。
     * 如果失败（例如，由于网络问题或API返回错误），它会向上抛出异常，
     * 由调用方（如调度器）来处理重试逻辑。
     *
     * @param skipValidation 如果为 true，则在获取过程中跳过所有业务逻辑校验（如长度限制）。
     * @throws RuntimeException 如果获取"一言"时发生任何错误。
     */
    @Override
    public void fetchNewSentence(boolean skipValidation) {
        log.info("🚀 开始尝试获取新的一言 (跳过校验: {})...", skipValidation);

        // 调用数据仓库层获取"一言"
        Optional<Sentence> sentenceOpt = sentenceRepository.fetchRandomSentence(skipValidation);

        if (sentenceOpt.isPresent()) {
            Sentence sentence = sentenceOpt.get();
            log.info("✨ 成功获取到封装后的一言对象: {}", sentence);
            eventPublisher.publishEvent(new SentenceFetchedEvent(this, sentence));
        } else {
            // 如果仓库层返回一个空的Optional，意味着没有获取到有效数据。
            // 抛出异常，以便调度器知道本次尝试失败。
            log.warn("🤷‍ 本次未能从任何API获取到有效的一言数据。");
            throw new IllegalStateException("未能从任何数据源获取到有效的一言。");
        }
    }
} 