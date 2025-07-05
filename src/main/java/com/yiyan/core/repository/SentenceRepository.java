package com.yiyan.core.repository;

import com.yiyan.core.domain.Sentence;

import java.util.Optional;

/**
 * 领域存储库接口，定义了获取"一言"数据的契约。
 * <p>
 * 应用层依赖此接口来获取数据，而无需关心数据是来自网络API、数据库还是本地缓存。
 */
public interface SentenceRepository {

    /**
     * 从任意可用数据源随机获取一个"一言"实例。
     *
     * @return 返回一个包含Sentence的可选值。如果无法获取，则返回空的Optional。
     */
    Optional<Sentence> findRandomSentence();
} 