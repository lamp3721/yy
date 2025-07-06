package com.yiyan.infrastructure.ui;

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
     * 当用户请求退出应用时调用。
     */
    void onExitRequested();
} 