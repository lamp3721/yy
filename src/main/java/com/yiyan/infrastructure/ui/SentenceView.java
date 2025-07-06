package com.yiyan.infrastructure.ui;

import com.yiyan.core.domain.Sentence;

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
     * 执行窗口出现/更新时的动画序列。
     * @param updateAction 在动画过程中执行的更新操作。
     */
    void runDisplayAnimation(Runnable updateAction);

    /**
     * 将窗口发送到桌面底层。
     */
    void sendToBottom();

    /**
     * 使窗口在屏幕上水平居中。
     */
    void centerOnScreen();

    /**
     * 根据新的状态重新构建UI中的组件，例如菜单。
     * 这确保了UI元素（如复选框）能够反映最新的状态。
     *
     * @param isAuthorVisible 作者是否可见。
     */
    void rebuildUiForNewState(boolean isAuthorVisible);
} 