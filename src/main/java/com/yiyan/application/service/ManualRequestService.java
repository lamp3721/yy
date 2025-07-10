package com.yiyan.application.service;

import java.util.concurrent.CompletableFuture;
/**
 * 应用服务接口，定义了手动触发操作的契约。
 */
public interface ManualRequestService {
    /**
     * 异步地手动请求一个新的"一言"。
     * @return 一个 CompletableFuture，在获取操作完成后结束。
     */

}
