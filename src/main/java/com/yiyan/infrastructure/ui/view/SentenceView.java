package com.yiyan.infrastructure.ui.view;

import com.yiyan.core.domain.Sentence;
import com.yiyan.infrastructure.ui.dto.HorizontalAlignment;
import com.yiyan.infrastructure.ui.presenter.ViewCallback;

/**
 * 定义了UI视图（View）必须实现的契约。
 * Presenter (UiController) 通过此接口来命令View更新其状态和显示。
 */
public interface SentenceView {

    /**
     * 更新"一言"主文本。
     * @param text 要显示的文本。
     */
    void setSentenceText(String text);

    /**
     * 更新作者文本并控制其可见性。
     * @param author   作者名。如果为null或空，则应隐藏作者标签。
     * @param visible  是否可见。
     */
    void setAuthorText(String author, boolean visible);

    /**
     * 将View附加到一个回调接口上。
     * View通过此回调与Presenter通信。
     * @param callback 回调接口实例。
     */
    void setCallback(ViewCallback callback);

    /**
     * 设置窗口是否启用水平拖动。
     * @param enabled 如果为true，则窗口可水平拖动；否则不可。
     */
    void setHorizontalDragEnabled(boolean enabled);

    /**
     * 执行窗口出现/更新时的动画序列。
     * @param updateAction 在动画过程中执行的更新操作。
     */
    void runDisplayAnimation(Runnable updateAction);

    /**
     * 将窗口发送到桌面底层。
     */
    void sendToBottom();

    /**
     * 根据指定的对齐方式在屏幕上定位窗口。
     * @param alignment 水平对齐方式。
     */
    void alignOnScreen(HorizontalAlignment alignment);

    /**
     * 设置窗口的锁定状态。
     * @param locked 如果为true，则窗口被锁定，无法移动。
     */
    void setLocked(boolean locked);

    /**
     * 将窗口临时置顶，并在指定延迟后沉到底部。
     * @param delayInSeconds 延迟时间（秒）。
     */
    void bringToTopAndSendToBottomAfterDelay(int delayInSeconds);

    /**
     * 取消任何正在进行的临时置顶计时器。
     */
    void cancelTemporaryTopTimer();

    /**
     * 根据新的状态重新构建UI中的组件，例如菜单。
     * 这确保了UI元素（如复选框）能够反映最新的状态。
     *
     * @param isAuthorVisible        作者是否可见。
     * @param isHorizontalDragEnabled 窗口当前是否启用了水平拖动。
     * @param alignment              当前的对齐方式。
     * @param isLocked               窗口当前是否被锁定。
     * @param isTemporaryTopEnabled  是否启用了临时置顶功能。
     */
    void rebuildUiForNewState(boolean isAuthorVisible, boolean isHorizontalDragEnabled, HorizontalAlignment alignment, boolean isLocked, boolean isTemporaryTopEnabled);
} 