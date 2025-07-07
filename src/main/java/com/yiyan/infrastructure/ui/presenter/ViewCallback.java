package com.yiyan.infrastructure.ui.presenter;

import com.yiyan.infrastructure.ui.dto.HorizontalAlignment;

/**
 * 定义了视图（View）回调到展示器（Presenter）的契约。
 * 当用户在UI上进行操作时，View通过此接口通知Presenter。
 */
public interface ViewCallback {

    /**
     * 当用户请求手动刷新时调用。
     */
    void onRefreshRequested();

    /**
     * 当用户改变"显示作者"状态时调用。
     * @param isVisible 最新的可见状态。
     */
    void onAuthorVisibilityChanged(boolean isVisible);

    /**
     * 当用户切换"允许左右拖动"状态时调用。
     * @param isEnabled 最新的可用状态。
     */
    void onHorizontalDragToggled(boolean isEnabled);

    /**
     * 当用户改变对齐方式时调用。
     * @param alignment 最新的对齐方式。
     */
    void onAlignmentChanged(HorizontalAlignment alignment);

    /**
     * 当用户切换"锁定位置"状态时调用。
     * @param isLocked 窗口是否被锁定。
     */
    void onLockPositionToggled(boolean isLocked);

    /**
     * 当用户请求退出应用时调用。
     */
    void onExitRequested();
} 