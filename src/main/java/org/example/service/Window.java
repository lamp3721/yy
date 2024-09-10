package org.example.service;


import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.IOException;
import java.io.InputStream;

@Service
public class Window {

    public JFrame frame;
    
    public JLabel label;
    
    public Font font;


    @PostConstruct
    public void window() {

        // 加载字体
        loadingFonts();
        
        frame = new JFrame("一言窗口");
        
        // 设置窗口的类型为 Type.UTILITY，以在启动时不显示在任务栏上
        frame.setType(JFrame.Type.UTILITY);

        // 设置窗口的内容面板为透明
        frame.setUndecorated(true);
        frame.setBackground(new Color(0, 0, 0, 0)); // 设置背景透明

        // 创建一个标签来显示文字
        label = new JLabel("qq:2932349894");
        
        // 设置字体
        label.setFont(font);
        
        // 设置文本颜色为RGB颜色(0, 255, 191)
        label.setForeground(new Color(0, 255, 191));

        // 添加标签到容器
        frame.add(label, BorderLayout.CENTER);
        
        frame.setLocationRelativeTo(null); // 居中显示

        // 自动调整窗口大小以适应标签文本
        frame.pack();

        // 在容器上添加鼠标监听器，以实现拖动窗口
        frame.addMouseListener(mouseAdapter);
        frame.addMouseMotionListener(mouseAdapter);


        frame.setVisible(true);
        
    }

    private void loadingFonts() {
        // 使用ClassLoader加载字体文件
        try (InputStream is = Window.class.getResourceAsStream("/fonts/最深的夜里最温柔.ttf")) {
            if (is == null) {
                System.out.println("字体文件未找到！");
                return;
            }
            // 从资源流中加载字体
            Font customFont = Font.createFont(Font.TRUETYPE_FONT, is);
            font = customFont.deriveFont(Font.PLAIN, 30);  // 设置字体样式和大小

            // 在这里可以使用 customFont 进行绘制
            System.out.println("自定义字体加载成功！");
        } catch (FontFormatException | IOException e) {
            e.printStackTrace();
            System.out.println("加载字体时出现错误！");
        }
    }

    // 创建一个鼠标监听器来实现窗口的拖动
    MouseAdapter mouseAdapter = new MouseAdapter() {
        private Point offset;
        @Override
        public void mousePressed(MouseEvent e) {
            offset = e.getPoint();
        }

        @Override
        public void mouseDragged(MouseEvent e) {
            int x = e.getXOnScreen() - offset.x;
            int y = e.getYOnScreen() - offset.y;
            frame.setLocation(x, y);
        }
    };
}
