package com.yiyan.infrastructure.ui;

import jakarta.annotation.PostConstruct;
import lombok.Getter;
import org.springframework.stereotype.Component;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.InputStream;

/**
 * UI主窗口 (JFrame)，负责创建和管理应用的基本窗口框架。
 */
@Component
@Getter
public class MainFrame extends JFrame {

    private final JLabel sentenceLabel;
    private Font customFont;

    public MainFrame() {
        // 初始化窗口基本属性
        setTitle("一言");
        setType(JFrame.Type.UTILITY); // 不在任务栏显示图标
        setUndecorated(true); // 无边框
        setBackground(new Color(0, 0, 0, 0)); // 背景透明
        setAlwaysOnTop(true); // 初始时总在最前

        // 加载自定义字体
        loadCustomFont();

        // 创建用于显示文本的 JLabel
        sentenceLabel = new JLabel("正在获取一言...", SwingConstants.CENTER);
        sentenceLabel.setFont(customFont);
        sentenceLabel.setForeground(new Color(0, 255, 191)); // 默认文本颜色

        // 将标签添加到窗口
        add(sentenceLabel, BorderLayout.CENTER);

        // 添加鼠标拖动功能
        setupMouseListener();
    }

    /**
     * 在Bean初始化后执行，完成窗口的最终设置并显示。
     */
    @PostConstruct
    private void init() {
        pack(); // 根据内容调整窗口大小
        setLocationRelativeTo(null); // 初始居中
        setVisible(true);
    }

    /**
     * 设置窗口的文本内容，并自动调整大小。
     * @param text 要显示的文本。
     */
    public void updateText(String text) {
        sentenceLabel.setText(" " + text + " ");
        pack(); // 重新计算窗口大小
    }

    /**
     * 将窗口在屏幕上水平居中。
     */
    public void centerOnScreen() {
        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        int newX = (screenSize.width - getWidth()) / 2;
        setLocation(newX, getY());
    }

    /**
     * 将窗口定位到屏幕垂直方向的三分之一处。
     */
    public void positionOnThirdOfScreen() {
        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        int newY = (screenSize.height - getHeight()) / 3;
        setLocation(getX(), newY);
    }

    /**
     * 加载自定义字体文件。
     */
    private void loadCustomFont() {
        try (InputStream is = MainFrame.class.getResourceAsStream("/fonts/最深的夜里最温柔.ttf")) {
            if (is == null) {
                throw new IllegalStateException("字体文件 /fonts/最深的夜里最温柔.ttf 未找到！");
            }
            Font createdFont = Font.createFont(Font.TRUETYPE_FONT, is);
            this.customFont = createdFont.deriveFont(Font.PLAIN, 24f);
        } catch (Exception e) {
            // 如果自定义字体加载失败，则使用默认字体
            this.customFont = new Font("SansSerif", Font.PLAIN, 24);
            System.err.println("加载自定义字体失败，将使用默认字体: " + e.getMessage());
        }
    }

    /**
     * 设置鼠标监听器以实现窗口拖动。
     */
    private void setupMouseListener() {
        MouseAdapter adapter = new MouseAdapter() {
            private Point offset;

            @Override
            public void mousePressed(MouseEvent e) {
                offset = e.getPoint();
            }

            @Override
            public void mouseDragged(MouseEvent e) {
                // 根据鼠标拖动更新窗口位置（只允许Y轴移动）
                Point newPoint = e.getLocationOnScreen();
                int newY = newPoint.y - offset.y;

                // 保持X轴始终居中
                int currentX = getX();
                setLocation(currentX, newY);
                centerOnScreen(); // 拖动时也强制水平居中
            }
        };
        addMouseListener(adapter);
        addMouseMotionListener(adapter);
    }
} 