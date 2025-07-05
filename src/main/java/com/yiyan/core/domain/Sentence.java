package com.yiyan.core.domain;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.util.StringUtils;

/**
 * 核心领域实体：一言 (Sentence)
 * <p>
 * 代表从API获取的一句引言或一句话。
 * 这个类是纯粹的领域对象，包含“一言”的文本内容和作者/出处。
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
     * “一言”的作者或出处
     */
    private String author;

    /**
     * 工厂方法，用于创建一个新的Sentence实例。
     *
     * @param text 句子内容
     * @return Sentence 实例
     */
    public static Sentence of(String text) {
        return new Sentence(text, null);
    }

    /**
     * 工厂方法，用于创建一个新的Sentence实例。
     *
     * @param text   句子内容
     * @param author 作者或出处
     * @return Sentence 实例
     */
    public static Sentence of(String text, String author) {
        return new Sentence(text, author);
    }

    @Override
    public String toString() {
        if (StringUtils.hasText(author)) {
            return String.format("%s —— %s", text, author);
        }
        return text;
    }
} 