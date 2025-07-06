package com.yiyan.infrastructure.scheduling;

import com.yiyan.infrastructure.adapter.api.HttpSentenceRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.stereotype.Component;

/**
 * 应用启动后的API自检触发器。
 * <p>
 * 当Spring应用完全就绪时，此类会异步触发对所有配置的API端点进行一次健康检查。
 */
@Component
@Slf4j
@RequiredArgsConstructor
public class ApiSelfCheckRunner implements ApplicationListener<ApplicationReadyEvent> {

    /**
     * "一言"的HTTP数据仓库，自检程序将调用其API检查方法
     */
    private final HttpSentenceRepository sentenceRepository;

    @Override
    public void onApplicationEvent(ApplicationReadyEvent event) {
        log.info("🚀 应用已就绪，开始在后台执行API自检...");
        sentenceRepository.checkAllApisAsync();
    }
} 