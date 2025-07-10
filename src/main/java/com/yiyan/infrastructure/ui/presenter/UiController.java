package com.yiyan.infrastructure.ui.presenter;

import com.yiyan.event.SentenceFetchedEvent;
import com.yiyan.service.SentenceService;
import com.yiyan.domain.Sentence;
import com.yiyan.infrastructure.ui.dto.HorizontalAlignment;
import com.yiyan.infrastructure.ui.service.AnimationService;
import com.yiyan.infrastructure.ui.service.DesktopManager;
import com.yiyan.infrastructure.ui.view.SentenceView;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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
@Slf4j
public class UiController implements ViewCallback {

    /**
     * 手动请求服务，用于触发异步获取新的"一言"
     */
    private final SentenceService sentenceService;

    /**
     * "一言"视图，UI控制器通过此接口更新界面
     */
    private final SentenceView view; // 依赖接口而非实现
    /**
     * 桌面管理器，用于处理窗口在桌面上的位置和行为
     */
    private final DesktopManager desktopManager;
    /**
     * 动画服务，提供UI元素的动画效果
     */
    private final AnimationService animationService;

    // --- UI状态 ---
    private boolean isAuthorVisible = false;
    private boolean isHorizontalDragEnabled = false;
    private HorizontalAlignment alignment = HorizontalAlignment.CENTER;
    private boolean isLocked = false;
    private boolean isTemporaryTopEnabled = true;

    /**
     * 初始化Presenter，将自身作为回调注入到View中。
     */
    @PostConstruct
    public void initialize() {
        view.setCallback(this);
        // 回调设置完毕后，立即命令View根据初始状态构建其UI组件
        view.rebuildUiForNewState(this.isAuthorVisible, this.isHorizontalDragEnabled, this.alignment, this.isLocked, this.isTemporaryTopEnabled);
    }

    /**
     * 监听"一言"获取成功事件。
     */
    @EventListener
    public void onSentenceFetched(SentenceFetchedEvent event) {
        // 使用 SwingUtilities.invokeLater() 方法，确保更新UI操作在EDT线程中执行
        SwingUtilities.invokeLater(() -> updateViewWithAnimation(event.getSentence()));
    }

    /**
     * 使用动画更新视图。
     */
    private void updateViewWithAnimation(Sentence sentence) {
        log.info("准备更新视图，当前\"临时置顶\"状态为：{}", this.isTemporaryTopEnabled);
        // 创建一个Runnable对象，用于更新UI
        Runnable updateAction = () -> {
            // BUG修复：当功能禁用时，不应主动将窗口沉底，应保持其当前层级。
            // if (!isTemporaryTopEnabled) {
            //     log.info("-> \"临时置顶\"已禁用，执行沉底操作。");
            //     view.sendToBottom();
            // }
            view.setSentenceText(" " + sentence.getText() + " ");
            view.setAuthorText(
                    StringUtils.hasText(sentence.getAuthor()) ? "—— " + sentence.getAuthor() : null,
                    isAuthorVisible
            );
            view.alignOnScreen(this.alignment);

            if (isTemporaryTopEnabled) {
                log.info("-> \"临时置顶\"已启用，执行置顶5秒后沉底的操作。");
                view.bringToTopAndSendToBottomAfterDelay(5);
            }
        };
        view.runDisplayAnimation(updateAction);
    }

    // --- ViewCallback 接口实现 ---

    @Override
    public void onRefreshRequested() {
        sentenceService.requestNewSentenceAsync();
    }

    @Override
    public void onAuthorVisibilityChanged(boolean isVisible) {
        this.isAuthorVisible = isVisible;
        // 状态变更后，命令View重建UI以反映新状态
        view.rebuildUiForNewState(this.isAuthorVisible, this.isHorizontalDragEnabled, this.alignment, this.isLocked, this.isTemporaryTopEnabled);
        // 手动刷新一次以应用作者可见性变更
        sentenceService.requestNewSentenceAsync();
    }

    @Override
    public void onHorizontalDragToggled(boolean isEnabled) {
        if (isLocked) return; // 如果已锁定，则不处理
        this.isHorizontalDragEnabled = isEnabled;
        view.setHorizontalDragEnabled(this.isHorizontalDragEnabled);
        // 重建UI以更新菜单状态
        view.rebuildUiForNewState(isAuthorVisible, isHorizontalDragEnabled, alignment, isLocked, isTemporaryTopEnabled);
    }

    @Override
    public void onAlignmentChanged(HorizontalAlignment alignment) {
        if (isLocked) return; // 如果已锁定，则不处理
        this.alignment = alignment;
        view.alignOnScreen(this.alignment);
    }

    @Override
    public void onLockPositionToggled(boolean isLocked) {
        this.isLocked = isLocked;
        view.setLocked(this.isLocked);
        // 锁定状态改变后，需要立即重建UI以更新菜单项的启用/禁用状态
        view.rebuildUiForNewState(isAuthorVisible, isHorizontalDragEnabled, alignment, this.isLocked, isTemporaryTopEnabled);
    }

    @Override
    public void onTemporaryTopToggled(boolean isEnabled) {
        log.info("接收到\"临时置顶\"状态切换事件，新状态为：{}", isEnabled);
        this.isTemporaryTopEnabled = isEnabled;

        // 如果功能被禁用，立即取消任何正在运行的定时器
        if (!isEnabled) {
            view.cancelTemporaryTopTimer();
        }

        // 重建UI以更新菜单状态
        view.rebuildUiForNewState(isAuthorVisible, isHorizontalDragEnabled, alignment, isLocked, this.isTemporaryTopEnabled);
    }

    @Override
    public void onExitRequested() {
        System.exit(0);
    }
} 