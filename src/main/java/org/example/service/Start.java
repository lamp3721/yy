package org.example.service;

import org.example.service.Switch;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.util.concurrent.ScheduledFuture;

@Service
public class Start {

    @Autowired
    private TaskScheduler taskScheduler;  // 使用 Spring 的任务调度器

    @Autowired
    private Switch s;  // 注入实际执行任务的类

    private ScheduledFuture<?> futureTask;

    // 初始化时安排首次任务执行
    @PostConstruct
    public void initialize() {
        execute();
    }


    // 执行任务并在任务完成后重新安排下次任务
    public void execute() {
        s.execute();  // 调用 Switch 类中的 execute 方法

        // 在执行完任务后重新安排下次任务
        scheduleNextExecution();
    }
    
    // 重新安排任务，时间为 5 到 30 分钟的随机延迟
    private void scheduleNextExecution() {
        // 计算 5 到 30 分钟之间的随机时间（以毫秒为单位）
        long delay = (long) (Math.random() * 1500000 + 300000); // 300000ms = 5分钟，1500000ms = 25分钟

        System.out.println("Next execution in: " + delay / 60000 + " minutes");

        // 使用 TaskScheduler 安排下次任务执行
        futureTask = taskScheduler.schedule(this::execute, new java.util.Date(System.currentTimeMillis() + delay));
    }

    // 如果你想在某个时刻手动停止任务执行
    public void stopTask() {
        if (futureTask != null && !futureTask.isCancelled()) {
            futureTask.cancel(true);
        }
    }
}
