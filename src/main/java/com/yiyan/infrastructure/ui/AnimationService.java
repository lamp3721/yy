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

    /**
     * 执行一个完整的淡出后淡入的动画序列。
     *
     * @param container         要应用动画的容器 (e.g., JFrame, JPanel)。
     * @param updateAction 在淡出完成、淡入开始之前执行的操作，通常用于更新文本。
     */
    public void runFadeSequence(Container container, Runnable updateAction) {
        // 创建淡出定时器
        Timer fadeOutTimer = new Timer(FADE_INTERVAL_MS, e -> {
            float newAlpha = getAlpha(container) - FADE_STEP;
            setAlpha(container, newAlpha);

            if (newAlpha <= 0.0f) {
                ((Timer) e.getSource()).stop();
                // 淡出完成后，更新文本内容，然后开始淡入
                if (updateAction != null) {
                    updateAction.run();
                }
                runFadeIn(container);
            }
        });
        fadeOutTimer.setInitialDelay(0);
        fadeOutTimer.start();
    }

    /**
     * 执行淡入动画。
     * @param container     应用动画的容器。
     */
    private void runFadeIn(Container container) {
        // 创建淡入定时器
        Timer fadeInTimer = new Timer(FADE_INTERVAL_MS, e -> {
            float newAlpha = getAlpha(container) + FADE_STEP;
            setAlpha(container, newAlpha);

            if (newAlpha >= 1.0f) {
                ((Timer) e.getSource()).stop();
            }
        });
        fadeInTimer.setInitialDelay(0);
        fadeInTimer.start();
    }

    /**
     * 递归地为容器及其所有子组件设置透明度。
     */
    private void setAlpha(Container container, float alpha) {
        alpha = Math.max(0.0f, Math.min(1.0f, alpha));
        
        // JFrames need special handling for transparency
        if (container instanceof JFrame) {
             ((JFrame) container).setOpacity(alpha);
        }

        for (Component comp : container.getComponents()) {
            if (comp.isLightweight()) { // only for swing components
                Color color = comp.getForeground();
                if (color != null) {
                    comp.setForeground(new Color(color.getRed(), color.getGreen(), color.getBlue(), (int) (alpha * 255)));
                }
            }
            if (comp instanceof Container) {
                setAlpha((Container) comp, alpha);
            }
        }
    }

    /**
     * 获取容器的透明度 (以JFrame的为准)。
     */
    private float getAlpha(Container container) {
        if (container instanceof JFrame) {
            return ((JFrame) container).getOpacity();
        }
        // Fallback for other containers, might not be accurate
        return container.getForeground() != null ? container.getForeground().getAlpha() / 255.0f : 1.0f;
    }
} 