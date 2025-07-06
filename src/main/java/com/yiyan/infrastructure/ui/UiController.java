package com.yiyan.infrastructure.ui;

import com.yiyan.application.event.SentenceFetchedEvent;
import com.yiyan.application.service.ManualRequestService;
import com.yiyan.core.domain.Sentence;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import javax.swing.*;

/**
 * UI的展示器（Presenter），负责连接模型（应用服务）和视图（SentenceView）。
 * <p>
 * 它实现了 ViewCallback 接口来接收来自视图的用户操作，
 * 并通过持有 SentenceView 接口来命令视图更新。
 */
@Component
@RequiredArgsConstructor
public class UiController implements ViewCallback {

    // --- 模型层依赖 ---
    private final ManualRequestService manualRequestService;

    // --- 视图层依赖 ---
    private final SentenceView view; // 依赖接口而非实现
    private final DesktopManager desktopManager;
    private final AnimationService animationService;

    // --- UI状态 ---
    private boolean isPositionLocked = false;
    private boolean isAuthorVisible = false;

    /**
     * 初始化Presenter，将自身作为回调注入到View中。
     */
    @PostConstruct
    public void initialize() {
        view.setCallback(this);
        // 回调设置完毕后，立即命令View根据初始状态构建其UI组件
        view.rebuildUiForNewState(this.isPositionLocked, this.isAuthorVisible);
    }

    /**
     * 监听"一言"获取成功事件。
     */
    @EventListener
    public void onSentenceFetched(SentenceFetchedEvent event) {
        SwingUtilities.invokeLater(() -> updateViewWithAnimation(event.getSentence()));
    }

    /**
     * 使用动画更新视图。
     */
    private void updateViewWithAnimation(Sentence sentence) {
        Runnable updateAction = () -> {
            view.sendToBottom();
            view.setSentenceText(" " + sentence.getText() + " ");
            view.setAuthorText(
                    StringUtils.hasText(sentence.getAuthor()) ? "—— " + sentence.getAuthor() : null,
                    isAuthorVisible
            );
            if (!isPositionLocked) {
                view.centerOnScreen();
            }
        };
        view.runDisplayAnimation(updateAction);
    }

    // --- ViewCallback 接口实现 ---

    @Override
    public void onRefreshRequested() {
        manualRequestService.requestNewSentenceAsync();
    }

    @Override
    public void onLockStateChanged(boolean isLocked) {
        this.isPositionLocked = isLocked;
        // 状态变更后，命令View重建UI以反映新状态
        view.rebuildUiForNewState(this.isPositionLocked, this.isAuthorVisible);
    }

    @Override
    public void onAuthorVisibilityChanged(boolean isVisible) {
        this.isAuthorVisible = isVisible;
        // 状态变更后，命令View重建UI以反映新状态
        view.rebuildUiForNewState(this.isPositionLocked, this.isAuthorVisible);
        // 手动刷新一次以应用作者可见性变更
        manualRequestService.requestNewSentenceAsync();
    }

    @Override
    public void onExitRequested() {
        System.exit(0);
    }
} 