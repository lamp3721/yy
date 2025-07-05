package com.yiyan.core.domain;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 核心领域实体：一言 (Sentence)
 * <p>
 * 代表从API获取的一句引言或一句话。
 * 这个类是纯粹的领域对象，仅包含“一言”的文本内容。
 */
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class Sentence {

    /**
     * “一言”的文本内容
     */
    private String text;

    /**
     * 工厂方法，用于创建一个新的Sentence实例。
     *
     * @param text 句子内容
     * @return Sentence 实例
     */
    public static Sentence of(String text) {
        return new Sentence(text);
    }

    @Override
    public String toString() {
        return text;
    }
} 