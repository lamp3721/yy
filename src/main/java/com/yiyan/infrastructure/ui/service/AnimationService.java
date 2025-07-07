package com.yiyan.infrastructure.ui.service;

import org.springframework.stereotype.Service;

import javax.swing.*;
import java.awt.*;

/**
 * 负责处理UI动画，例如淡入淡出效果。
 */
@Service
public class AnimationService {

    private static final int FADE_STEP_DELAY = 15; // 每步动画的延迟（毫秒）
    private static final float FADE_STEP_AMOUNT = 0.05f; // 每步动画改变的不透明度

    /**
     * 执行一个完整的淡出（如果需要）-> 更新 -> 淡入的动画序列。
     * @param frame        要应用动画的JFrame。
     * @param updateAction 在淡出后、淡入前要执行的操作（例如更新文本）。
     */
    public void runFadeSequence(JFrame frame, Runnable updateAction) {
        // 根据窗口当前状态决定是否需要淡入和淡出
        boolean shouldFadeOut = frame.isVisible() && frame.getOpacity() > 0;
        boolean shouldFadeIn = true; // 通常我们总是希望淡入

        // --- 创建淡入计时器 ---
        Timer fadeInTimer = new Timer(FADE_STEP_DELAY, e -> {
            float newOpacity = frame.getOpacity() + FADE_STEP_AMOUNT;
            if (newOpacity >= 1.0f) {
                frame.setOpacity(1.0f);
                ((Timer) e.getSource()).stop();
            } else {
                frame.setOpacity(newOpacity);
            }
        });

        // --- 定义核心更新逻辑 ---
        Runnable coreUpdateAndFadeIn = () -> {
            // 步骤1: 确保窗口可见
            if (!frame.isVisible()) {
                frame.setVisible(true);
            }
            // 步骤2: 在下一个事件循环中执行UI更新
            if (updateAction != null) {
                SwingUtilities.invokeLater(updateAction);
            }
            // 步骤3: 在UI更新后的下一个事件循环中开始淡入
            if (shouldFadeIn) {
                SwingUtilities.invokeLater(fadeInTimer::start);
            }
        };

        // --- 启动动画序列 ---
        if (shouldFadeOut) {
            // 如果需要淡出，则创建并启动淡出计时器
            Timer fadeOutTimer = new Timer(FADE_STEP_DELAY, e -> {
                float newOpacity = frame.getOpacity() - FADE_STEP_AMOUNT;
                if (newOpacity <= 0.0f) {
                    frame.setOpacity(0.0f);
                    ((Timer) e.getSource()).stop();
                    // 淡出完成后，执行核心逻辑
                    coreUpdateAndFadeIn.run();
                } else {
                    frame.setOpacity(newOpacity);
                }
            });
            fadeOutTimer.start();
        } else {
            // 如果不需要淡出，直接执行核心逻辑
            coreUpdateAndFadeIn.run();
        }
    }
} 