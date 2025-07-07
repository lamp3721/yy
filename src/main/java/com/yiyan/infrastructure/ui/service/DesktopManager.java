package com.yiyan.infrastructure.ui.service;

import com.sun.jna.Native;
import com.sun.jna.Pointer;
import com.sun.jna.platform.win32.User32;
import com.sun.jna.platform.win32.WinDef;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import javax.swing.*;
import java.awt.*;

import static com.sun.jna.platform.win32.WinUser.SWP_NOMOVE;
import static com.sun.jna.platform.win32.WinUser.SWP_NOSIZE;

/**
 * 桌面管理器，封装了与操作系统桌面环境交互的底层逻辑。
 * <p>
 * 目前主要使用 JNA (Java Native Access) 来调用 Windows API，以实现将窗口置于底层的效果。
 */
@Service
@Slf4j
public class DesktopManager {

    private static final boolean IS_WINDOWS = System.getProperty("os.name").toLowerCase().startsWith("windows");
    private static final Logger LOGGER = LoggerFactory.getLogger(DesktopManager.class);

    /**
     * 将指定的窗口置于桌面底层。
     * <p>
     * 此功能仅在 Windows 系统上有效。在其他操作系统上，此方法将不执行任何操作。
     *
     * @param window 要置底的窗口。
     */
    public void sendToBottom(Window window) {
        if (!IS_WINDOWS) {
            log.warn("当前非 Windows 系统，无法执行窗口置底操作。");
            return;
        }

        // 防御性检查：确保组件已在屏幕上显示，拥有可用的本地窗口句柄。
        if (!window.isDisplayable()) {
            LOGGER.warn("无法将窗口置底，因为它当前不可显示。将跳过此操作。");
            return;
        }

        try {
            // JNA 调用 Windows API 将窗口置底
            window.setAlwaysOnTop(false);
            WinDef.HWND hwnd = new WinDef.HWND(Native.getComponentPointer(window));
            User32.INSTANCE.SetWindowPos(
                    hwnd,
                    new WinDef.HWND(Pointer.createConstant(1)), // HWND_BOTTOM
                    0, 0, 0, 0,
                    SWP_NOSIZE | SWP_NOMOVE
            );
            log.info("窗口已成功置于底层。");
        } catch (Exception e) {
            log.error("使用 JNA 将窗口置底时发生错误。", e);
        }
    }
} 