package com.yiyan.infrastructure.ui;

import org.springframework.stereotype.Service;

import javax.swing.*;
import java.awt.*;

/**
 * UI动画服务，专门负责处理组件的淡入淡出效果。
 */
@Service
public class AnimationService {

    private static final int FADE_INTERVAL_MS = 25; // 动画定时器间隔
    private static final float FADE_STEP = 0.05f;   // 透明度变化步长

    private Timer fadeInTimer;
    private Timer fadeOutTimer;
    private float currentAlpha = 0.0f; // 当前透明度

    /**
     * 执行一个完整的淡出后淡入的动画序列。
     *
     * @param label           要应用动画的JLabel。
     * @param textUpdateAction 在淡出完成、淡入开始之前执行的操作，通常用于更新文本。
     */
    public void runFadeSequence(JLabel label, Runnable textUpdateAction) {
        // 停止任何正在进行的动画
        stopTimers();

        // 基础颜色
        Color baseColor = label.getForeground();

        // 创建淡出定时器
        fadeOutTimer = new Timer(FADE_INTERVAL_MS, e -> {
            currentAlpha = Math.max(0.0f, currentAlpha - FADE_STEP);
            updateLabelColor(label, baseColor, currentAlpha);

            if (currentAlpha <= 0.0f) {
                fadeOutTimer.stop();
                // 淡出完成后，更新文本内容，然后开始淡入
                textUpdateAction.run();
                runFadeIn(label, baseColor);
            }
        });

        // 开始淡出
        currentAlpha = 1.0f;
        fadeOutTimer.start();
    }

    /**
     * 执行淡入动画。
     * @param label     应用动画的JLabel。
     * @param baseColor 文本的基础颜色。
     */
    private void runFadeIn(JLabel label, Color baseColor) {
        // 创建淡入定时器
        fadeInTimer = new Timer(FADE_INTERVAL_MS, e -> {
            currentAlpha = Math.min(1.0f, currentAlpha + FADE_STEP);
            updateLabelColor(label, baseColor, currentAlpha);

            if (currentAlpha >= 1.0f) {
                fadeInTimer.stop();
            }
        });
        
        // 开始淡入
        fadeInTimer.start();
    }

    /**
     * 更新标签的颜色和透明度。
     */
    private void updateLabelColor(JLabel label, Color baseColor, float alpha) {
        int alphaValue = (int) (alpha * 255);
        label.setForeground(new Color(baseColor.getRed(), baseColor.getGreen(), baseColor.getBlue(), alphaValue));
    }

    /**
     * 停止所有动画定时器。
     */
    private void stopTimers() {
        if (fadeInTimer != null && fadeInTimer.isRunning()) {
            fadeInTimer.stop();
        }
        if (fadeOutTimer != null && fadeOutTimer.isRunning()) {
            fadeOutTimer.stop();
        }
    }
} 