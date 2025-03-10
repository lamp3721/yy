package org.example.Task;

import org.example.service.YTaskService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.util.Date;
import java.util.concurrent.ScheduledFuture;

/**
 * Start 类：用于动态安排并执行周期性任务。
 * 核心逻辑：执行一次任务后，根据随机延迟重新安排下一次执行。
 */
@Service
public class StartTask {

    // 注入 Spring 提供的任务调度器（基于线程池）
    @Autowired
    private TaskScheduler taskScheduler;

    // 注入实际要执行的任务逻辑封装类
    @Autowired
    private YTaskService yTaskService;

    // 保存当前安排的任务引用（用于取消任务）
    private ScheduledFuture<?> futureTask;

    // 最小/最大延迟时间（单位：毫秒）
    private static final long MIN_DELAY_MS = 5 * 60 * 1000;    // 5分钟
    private static final long MAX_DELAY_MS = 30 * 60 * 1000;   // 30分钟

    /**
     * 在 Bean 初始化完成后自动启动第一次任务
     */
    @PostConstruct
    public void initialize() {
        execute();// 启动第一次任务
    }

    /**
     * 任务执行逻辑：执行完后自动安排下一次任务
     */
    private void execute() {
        try {
            yTaskService.executeTask();  // 执行实际业务任务
        } catch (Exception e) {
            System.err.println("任务执行出错: " + e.getMessage());
            e.printStackTrace();
        }

        // 安排下一次任务
        scheduleNextExecution();
    }

    /**
     * 安排下一次任务执行时间（延迟为 5-30分钟的随机时间）
     */
    private void scheduleNextExecution() {
        // 生成 5~30 分钟之间的随机延迟
        long delay = MIN_DELAY_MS + (long) (Math.random() * (MAX_DELAY_MS - MIN_DELAY_MS));

        // 打印下一次执行时间
        System.out.println("[TaskScheduler] 下次任务将在 " + (delay / 60000) + " 分钟后执行");

        // 使用 TaskScheduler 安排下次执行
        futureTask = taskScheduler.schedule(this::execute, new Date(System.currentTimeMillis() + delay));
    }

    /**
     * 停止当前任务（可用于控制任务启停）
     */
    public void stopTask() {
        if (futureTask != null && !futureTask.isCancelled()) {
            futureTask.cancel(true);
            System.out.println("[TaskScheduler] 任务已手动取消");
        }
    }
}
