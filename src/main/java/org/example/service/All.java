package org.example.service;

import lombok.extern.slf4j.Slf4j;
import org.example.URL.YiYanApi;
import org.example.entity.Y;
import org.example.pool.YPool;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.TimeUnit;
@Service
@Slf4j
public class All {

    @Autowired
    private List<YiYanApi> ys;

    // 创建线程池，假设最大并发数为5
    private static final ExecutorService EXECUTOR = Executors.newFixedThreadPool(5);

    @PostConstruct
    public void setAll() {
        // 遍历每个YiYanApi实例并提交任务到线程池
        ys.forEach(y -> {
            EXECUTOR.submit(() -> {
                try {
                    if (y == null) {
                        System.out.println("YiYanApi 实例为空！");
                        return;
                    }

                    // 获取连接
                    Y conn = y.conn();
                    if (conn == null) {
                        System.out.println("连接为空："+y);
                        return;
                    }

                    // 判断连接状态
                    if (conn.getStatus() == 0) {
                        System.out.println(conn.getUrId()+" 连接失败："+ conn);
                    } else {
                        System.out.println(conn.getUrId()+" 成功:" + conn.getMsg());
                    }

                    YPool.returnY(conn);  // 归还连接
                } catch (Exception e) {
                    log.error("执行任务时出错", e);
                }
            });
        });

        // 关闭线程池，等待所有任务完成
        cleanUp();
    }

    // 获取随机的YiYanApi实例
    public YiYanApi getY() {
        if (ys == null || ys.isEmpty()) {
            throw new IllegalStateException("没有任何 YiYanApi 实现类可用");
        }
        int index = ThreadLocalRandom.current().nextInt(ys.size());
        return ys.get(index);
    }

    // 清理线程池资源
    private void cleanUp() {
        try {
            EXECUTOR.shutdown();  // 启动平缓关闭
            if (!EXECUTOR.awaitTermination(60, TimeUnit.SECONDS)) {
                EXECUTOR.shutdownNow();  // 强制关闭线程池
            }
        } catch (InterruptedException e) {
            EXECUTOR.shutdownNow();
            Thread.currentThread().interrupt();
        }
    }
}
