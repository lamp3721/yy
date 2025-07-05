package com.yiyan.infrastructure.ui;

import com.yiyan.application.event.SentenceFetchedEvent;
import com.yiyan.core.domain.Sentence;
import lombok.RequiredArgsConstructor;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Controller;

import javax.swing.*;

/**
 * UI控制器，负责监听应用事件并协调UI组件的更新。
 * <p>
 * 这是连接应用层（事件）和基础设施层（UI视图）的桥梁。
 */
@Controller
@RequiredArgsConstructor
public class UiController {

    private final MainFrame mainFrame;
    private final AnimationService animationService;
    private final DesktopManager desktopManager;

    /**
     * 监听"一言"获取成功事件。
     *
     * @param event 包含新"一言"数据的事件。
     */
    @EventListener
    public void onSentenceFetched(SentenceFetchedEvent event) {
        // 确保UI更新在Swing的事件调度线程上执行
        SwingUtilities.invokeLater(() -> updateUiWithAnimation(event.getSentence()));
    }

    /**
     * 使用动画更新UI。
     *
     * @param sentence 最新的"一言"数据。
     */
    private void updateUiWithAnimation(Sentence sentence) {
        // 定义文本更新操作
        Runnable updateAction = () -> {
            // 将窗口置于底层
            desktopManager.sendToBottom(mainFrame);
            // 更新文本内容和作者
            mainFrame.updateSentence(sentence);
            // 水平居中，但保持Y轴位置不变
            mainFrame.centerOnScreen();
        };

        // 执行淡出后淡入的动画序列
        animationService.runFadeSequence(mainFrame, updateAction);
    }
} 