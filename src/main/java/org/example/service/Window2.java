package org.example.service;

import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.io.InputStream;


@Service
public class Window2 {

    public JFrame frame;

    public JLabel label;

    public Font font;
    
    
    public boolean isOpen = true;


    private Timer fadeInTimer;  // 淡入定时器
    private Timer fadeOutTimer;  // 淡出定时器
    private float alpha = 0.0f; // 初始为不透明
    

    @PostConstruct
    public void window() {

        // 加载字体
        loadingFonts();

        // 初始化定时器
        initTimers();

        frame = new JFrame("big窗口");


        // 设置窗口的类型为 Type.UTILITY，以在启动时不显示在任务栏上
        frame.setType(JFrame.Type.UTILITY);

        // 设置窗口的内容面板为透明
        frame.setUndecorated(true);
        frame.setBackground(new Color(0, 0, 0, 0)); // 设置背景透明

        // 创建一个标签来显示文字
        label = new JLabel("");


        // 设置字体
        label.setFont(font);

        // 设置文本颜色为RGB颜色(0, 255, 191)
        label.setForeground(new Color(0, 255, 191));


        // 添加标签到容器
        frame.add(label, BorderLayout.CENTER);
        
        // 自动调整窗口大小以适应标签文本
        frame.pack();

        frame.setLocationRelativeTo(null); // 居中显示

        label.setForeground(new Color(0, 255, 191, Math.round(0 * 255)));
        
        
        
        frame.setVisible(false);

    }
    
    // 显示大文字
    public  void big(String msg){
        if(!isOpen){
            return;
        }
        label.setText(msg);
        frame.setAlwaysOnTop(true);
        frame.pack();
        frame.setLocationRelativeTo(null); // 居中显示
        
        fadeIn();
        frame.setVisible(true);
    }
    

    // 加载字体
    private void loadingFonts() {
        // 使用ClassLoader加载字体文件
        try (InputStream is = Window.class.getResourceAsStream("/fonts/最深的夜里最温柔.ttf")) {
            if (is == null) {
                System.out.println("字体文件未找到！");
                return;
            }
            // 从资源流中加载字体
            Font customFont = Font.createFont(Font.TRUETYPE_FONT, is);
            font = customFont.deriveFont(Font.PLAIN, 40);  // 设置字体样式和大小

            // 在这里可以使用 customFont 进行绘制
            System.out.println("自定义字体加载成功！");
        } catch (FontFormatException | IOException e) {
            e.printStackTrace();
            System.out.println("加载字体时出现错误！");
        }
    }
    



    // 淡入
    private void fadeIn() {
        alpha = 0.0f; // 从透明开始
        fadeInTimer.start(); // 启动淡入定时器
    }

    // 淡出
    public void fadeOut() {
        alpha = 1.0f; // 从不透明开始
        fadeOutTimer.start(); // 启动淡出定时器
    }


    private void initTimers() {


        // 淡入定时器
        fadeInTimer = new Timer(50, new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                alpha += 0.02f; // 增加透明度
                alpha = Math.min(1.0f, alpha); // 最大为1

                label.setForeground(new Color(0, 255, 191, Math.round(alpha * 255)));

                if (alpha >= 1.0f) {
                    fadeInTimer.stop(); // 停止淡入
                    try {Thread.sleep(3000);} catch (InterruptedException ex) {throw new RuntimeException(ex);}
                    fadeOut();// 开始淡出
                }
            }
        });

        // 淡出定时器
        fadeOutTimer = new Timer(50, new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                alpha -= 0.016f; // 减少透明度
                alpha = Math.max(0.0f, alpha); // 最小为0

                label.setForeground(new Color(0, 255, 191, Math.round(alpha * 255)));

                if (alpha <= 0.0f) {
                    fadeOutTimer.stop(); // 停止淡出
                    frame.setVisible(false);
                }

            }

        });

        
    }
}
