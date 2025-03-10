package org.example.window;

import com.google.common.eventbus.Subscribe;
import lombok.extern.slf4j.Slf4j;
import org.example.entity.Y;
import org.example.event.YEvent;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import javax.swing.*;
import java.awt.*;

// 显示逻辑处理服务类
@Service
@Slf4j // 使用 Lombok 提供的注解，自动生成日志记录器 log
public class Show {

    // 注入窗口对象，用于更新 UI 展示内容
    @Autowired
    private Window window;

    // 淡入和淡出的定时器
    private Timer fadeInTimer;   // 控制淡入效果
    private Timer fadeOutTimer;  // 控制淡出效果

    // 控制组件透明度的变量（0.0f 全透明，1.0f 不透明）
    private float alpha = 1.0f;

    // 定时器执行的时间间隔（单位：毫秒）
    private static final int FADE_INTERVAL = 50;

    // 每次定时器触发时改变透明度的步长
    private static final float FADE_STEP = 0.02f;

    // 文本的基础颜色（青绿色）
    private static final Color TEXT_COLOR = new Color(0, 255, 191);

    // 用于展示的事件数据
    private Y y;

    // 是否显示作者信息标志位（可根据业务扩展使用）
    public boolean openAuthor = false;

    // 初始化方法，Spring 容器启动后自动调用
    @PostConstruct
    public void init() {
        initTimers(); // 初始化淡入淡出动画定时器
    }

    // 事件监听方法，当接收到 YEvent 事件时触发
    @Subscribe
    public void handleStringEvent(YEvent yEvent) {
        System.out.println("接收到事件：" + yEvent);
        // 保存事件中传递的数据
        this.y = yEvent.getY();

        // 触发淡出动画
        fadeOut();
    }

    // 初始化淡入淡出动画定时器
    private void initTimers() {
        // 淡出动画逻辑
        fadeOutTimer = new Timer(FADE_INTERVAL, e -> {
            // 每次减少透明度
            alpha -= FADE_STEP;
            // 限制 alpha 最小值为 0（全透明）
            alpha = Math.max(0.0f, alpha);

            // 更新文本颜色（带 alpha 通道）
            updateTextColor();

            // 如果已经完全透明，则停止淡出动画并更新内容
            if (alpha <= 0.0f) {
                stopFadeOut();        // 停止淡出定时器
                updateWindowContent(); // 更新窗口内容
                fadeIn();              // 开始淡入动画
            }
        });

        // 淡入动画逻辑
        fadeInTimer = new Timer(FADE_INTERVAL, e -> {
            // 每次增加透明度
            alpha += FADE_STEP;
            // 限制 alpha 最大值为 1（不透明）
            alpha = Math.min(1.0f, alpha);

            // 更新文本颜色（带 alpha 通道）
            updateTextColor();

            // 如果已经完全不透明，则停止淡入动画
            if (alpha >= 1.0f) {
                fadeInTimer.stop();
            }
        });
    }

    // 更新文本颜色（包含透明度通道）
    private void updateTextColor() {
        // 将 alpha 转换为 0~255 的整数值
        int alphaValue = Math.round(alpha * 255);

        // 设置标签颜色（包含透明度）
        window.getLabel().setForeground(
                new Color(TEXT_COLOR.getRed(), TEXT_COLOR.getGreen(), TEXT_COLOR.getBlue(), alphaValue)
        );
    }

    // 停止淡出动画
    private void stopFadeOut() {
        fadeOutTimer.stop();
    }

    // 更新窗口展示的文本内容
    private void updateWindowContent() {
        window.bottom(); // 调整窗口布局或样式
        String newMsg = y.getMsg(); // 获取新的文本内容
        window.setText(" " + newMsg + " "); // 设置文本内容
        window.getFrame().pack(); // 自动调整窗口大小以适应内容
        window.setCenter(); // 居中显示窗口
    }

    // 开始淡入动画
    private void fadeIn() {
        alpha = 0.0f; // 设置初始透明度为 0
        fadeInTimer.start(); // 启动淡入动画
    }

    // 开始淡出动画
    public void fadeOut() {
        alpha = 1.0f; // 设置初始透明度为 1
        fadeOutTimer.start(); // 启动淡出动画
    }
}
