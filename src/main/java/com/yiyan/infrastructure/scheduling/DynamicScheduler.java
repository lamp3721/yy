package com.yiyan.infrastructure.scheduling;

import com.yiyan.application.service.SentenceService;
import com.yiyan.infrastructure.adapter.api.config.ApiConfigLoader;
import com.yiyan.config.SchedulerProperties;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.DependsOn;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.Instant;
import java.util.concurrent.ThreadLocalRandom;

/**
 * 动态任务调度器，负责以随机间隔执行"一言"获取任务。
 */
@Service
@DependsOn("apiConfigLoader")
@RequiredArgsConstructor
@Slf4j
public class DynamicScheduler {

    /**
     * Spring的任务调度器，用于安排和执行定时任务
     */
    private final TaskScheduler taskScheduler;
    /**
     * "一言"应用服务，负责核心的业务逻辑
     */
    private final SentenceService sentenceService;
    /**
     * 调度器配置属性，包含最小/最大延迟等
     */
    private final SchedulerProperties schedulerProperties;
    /**
     * API配置加载器，调度器需要等待其加载完成后再开始
     */
    private final ApiConfigLoader apiConfigLoader;

    /**
     * Spring Bean 初始化后，立即执行第一次任务。
     */
    @PostConstruct
    public void start() {
        // 使用一个新的线程来等待并启动调度器，避免阻塞Spring主启动线程
        new Thread(() -> {
            // 等待API列表加载完成
            while (!apiConfigLoader.isReady()) {
                try {
                    log.debug("等待API配置加载...");
                    Thread.sleep(100); // 短暂休眠，避免CPU空转
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    log.error("调度器启动等待线程被中断。", e);
                    return;
                }
            }
            log.info("API配置已加载，调度器启动。");
            // 立即执行一次，然后开始调度
            taskScheduler.schedule(this::runAndReschedule, Instant.now());
        }).start();
    }

    /**
     * 执行任务，并在完成后重新调度下一次执行。
     */
    private void runAndReschedule() {
        try {
            // 执行核心业务逻辑，定时任务需要执行校验
            sentenceService.fetchNewSentence(false);
        } catch (Exception e) {
            // 捕获所有异常，记录错误，但确保不中断调度循环
            log.error("❌ 任务周期执行失败: {}", e.getMessage());
        } finally {
            // 无论成功还是失败，都安排下一次执行
            scheduleNext();
        }
    }

    /**
     * 计算下一次执行的随机延迟并安排任务。
     */
    private void scheduleNext() {
        Duration minDelay = Duration.ofSeconds(schedulerProperties.getMinDelaySeconds());
        Duration maxDelay = Duration.ofSeconds(schedulerProperties.getMaxDelaySeconds());

        // 确保minDelay不大于maxDelay，避免负数异常
        if (minDelay.compareTo(maxDelay) > 0) {
            log.warn("配置错误：最小延迟时间大于最大延迟时间。将使用最小延迟作为固定延迟。");
            maxDelay = minDelay;
        }

        long delayMillis = ThreadLocalRandom.current().nextLong(
                minDelay.toMillis(), maxDelay.toMillis() + 1 // +1 使其包含上限
        );
        Instant nextExecutionTime = Instant.now().plusMillis(delayMillis);

        log.info("🕒 任务周期结束，下一次执行将在 {} 秒后。", Duration.ofMillis(delayMillis).getSeconds());

        taskScheduler.schedule(this::runAndReschedule, nextExecutionTime);
    }
} 