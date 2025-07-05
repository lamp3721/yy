package com.yiyan.core.domain;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 核心领域实体：一言 (Sentence)
 * <p>
 * 代表从API获取的一句引言或一句话。
 * 这个类是纯粹的领域对象，不包含任何框架或技术细节的注解。
 */
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class Sentence {

    /**
     * 内容
     */
    private String text;

    /**
     * 作者或来源
     */
    private String author;

    /**
     * 创建一个包含基本信息的句子
     * @param text 句子内容
     * @return Sentence 实例
     */
    public static Sentence of(String text) {
        return new Sentence(text, "未知来源");
    }

    /**
     * 创建一个完整的句子
     * @param text 句子内容
     * @param author 作者或来源
     * @return Sentence 实例
     */
    public static Sentence of(String text, String author) {
        if (author == null || author.trim().isEmpty()) {
            author = "未知来源";
        }
        return new Sentence(text, author);
    }

    @Override
    public String toString() {
        return String.format("'%s' — %s", text, author);
    }
} 