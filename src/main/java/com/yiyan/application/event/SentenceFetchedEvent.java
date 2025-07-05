package com.yiyan.application.event;

import com.yiyan.core.domain.Sentence;
import lombok.Getter;
import org.springframework.context.ApplicationEvent;

/**
 * 应用事件，在成功获取到新的“一言”时发布。
 * <p>
 * 这是一种解耦机制，允许应用中的不同部分（如UI、日志记录）对同一事件做出响应，而无需直接相互调用。
 */
@Getter
public class SentenceFetchedEvent extends ApplicationEvent {

    /**
     * 获取到的“一言”实体
     */
    private final Sentence sentence;

    /**
     * 创建一个新的 SentenceFetchedEvent.
     *
     * @param source   事件源对象，通常是发布事件的服务。
     * @param sentence 获取到的“一言”数据。
     */
    public SentenceFetchedEvent(Object source, Sentence sentence) {
        super(source);
        this.sentence = sentence;
    }
} 