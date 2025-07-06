package com.yiyan.infrastructure.ui;

import org.springframework.stereotype.Component;
import javax.swing.*;
import java.awt.*;
import java.awt.datatransfer.StringSelection;

/**
 * 负责创建和配置UI中的弹出菜单。
 */
@Component
public class PopupMenuFactory {

    /**
     * 创建一个配置好的右键弹出菜单。
     * @param callback 回调接口，用于将用户操作通知给Presenter。
     * @param initialState 菜单项的初始状态。
     * @param textProvider 一个函数，用于获取需要复制的文本。
     * @return JPopupMenu 实例。
     */
    public JPopupMenu create(ViewCallback callback, MenuInitialState initialState, TextProvider textProvider) {
        JPopupMenu popupMenu = new JPopupMenu();

        // 刷新
        JMenuItem refreshItem = new JMenuItem("刷新");
        refreshItem.addActionListener(e -> callback.onRefreshRequested());
        popupMenu.add(refreshItem);

        // 复制
        JMenuItem copyItem = new JMenuItem("复制");
        copyItem.addActionListener(e -> {
            StringSelection selection = new StringSelection(textProvider.getText());
            Toolkit.getDefaultToolkit().getSystemClipboard().setContents(selection, null);
        });
        popupMenu.add(copyItem);

        popupMenu.addSeparator();

        // 锁定位置
        JCheckBoxMenuItem lockPositionItem = new JCheckBoxMenuItem("锁定位置");
        lockPositionItem.setState(initialState.isPositionLocked());
        lockPositionItem.addActionListener(e -> callback.onLockStateChanged(lockPositionItem.getState()));
        popupMenu.add(lockPositionItem);

        // 显示作者
        JCheckBoxMenuItem showAuthorItem = new JCheckBoxMenuItem("显示作者");
        showAuthorItem.setState(initialState.isAuthorVisible());
        showAuthorItem.addActionListener(e -> callback.onAuthorVisibilityChanged(showAuthorItem.getState()));
        popupMenu.add(showAuthorItem);

        popupMenu.addSeparator();

        // 退出
        JMenuItem exitItem = new JMenuItem("退出");
        exitItem.addActionListener(e -> callback.onExitRequested());
        popupMenu.add(exitItem);

        return popupMenu;
    }

    /**
     * 用于向菜单提供动态文本的函数式接口。
     */
    @FunctionalInterface
    public interface TextProvider {
        String getText();
    }

    /**
     * 用于传递菜单项初始状态的记录类。
     */
    public record MenuInitialState(boolean isPositionLocked, boolean isAuthorVisible) {}
} 