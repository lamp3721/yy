package com.yiyan.infrastructure.scheduling;

import com.yiyan.application.service.SentenceService;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.Instant;
import java.util.concurrent.ThreadLocalRandom;

/**
 * 动态任务调度器，负责以随机间隔执行"一言"获取任务。
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class DynamicScheduler {

    private final TaskScheduler taskScheduler;
    private final SentenceService sentenceService;

    // 随机延迟范围
    private static final Duration MIN_DELAY = Duration.ofMinutes(5);
    private static final Duration MAX_DELAY = Duration.ofMinutes(30);

    /**
     * Spring Bean 初始化后，立即执行第一次任务。
     */
    @PostConstruct
    public void start() {
        // 立即执行一次，然后开始调度
        taskScheduler.schedule(this::runAndReschedule, Instant.now());
    }

    /**
     * 执行任务，并在完成后重新调度下一次执行。
     */
    private void runAndReschedule() {
        try {
            // 执行核心业务逻辑
            sentenceService.fetchNewSentence();
        } catch (Exception e) {
            // 捕获所有异常，记录错误，但确保不中断调度循环
            log.error("执行一言获取任务时发生错误: {}", e.getMessage());
        } finally {
            // 无论成功还是失败，都安排下一次执行
            scheduleNext();
        }
    }

    /**
     * 计算下一次执行的随机延迟并安排任务。
     */
    private void scheduleNext() {
        long delayMillis = ThreadLocalRandom.current().nextLong(
                MIN_DELAY.toMillis(), MAX_DELAY.toMillis()
        );
        Instant nextExecutionTime = Instant.now().plusMillis(delayMillis);

        log.info("任务完成，下一次执行将在 {} 分钟后。", Duration.ofMillis(delayMillis).toMinutes());

        taskScheduler.schedule(this::runAndReschedule, nextExecutionTime);
    }
} 