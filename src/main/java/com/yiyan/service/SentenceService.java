package com.yiyan.service;

import java.util.concurrent.CompletableFuture;

public interface SentenceService {

    /**
     * 执行获取新"一言"并发布的任务。
     */
    void fetchNewSentence(boolean skipValidation);

    /**
     * 异步地手动请求一个新的"一言"。
     * @return 一个 CompletableFuture，在获取操作完成后结束。
     */
    CompletableFuture<Void> requestNewSentenceAsync();
}
