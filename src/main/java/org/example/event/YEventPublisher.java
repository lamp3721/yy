package org.example.event;

import com.google.common.eventbus.EventBus;
import lombok.extern.slf4j.Slf4j;
import org.example.entity.Y;
import org.example.event.YEvent;
import org.example.log.RecordLog;
import org.example.window.Show;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;

/**
 * 事件发布器 —— 封装 EventBus，统一负责事件的发布与监听器注册
 */
@Component
@Slf4j
public class YEventPublisher {

    private final EventBus eventBus = new EventBus();

    @Resource
    private Show show;              // UI 展示更新
    @Resource
    private RecordLog recordLog;    // 数据记录日志

    /**
     * 初始化注册监听器
     */
    @PostConstruct
    public void registerListeners() {
        eventBus.register(show);
        eventBus.register(recordLog);
        log.info("事件监听器已注册完成");
    }

    /**
     * 发布 YEvent 事件
     */
    public void publish(Y y) {
        YEvent event = new YEvent();
        event.setY(y);
        eventBus.post(event);
    }
}
