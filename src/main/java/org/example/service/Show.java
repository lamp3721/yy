package org.example.service;

import org.example.pojo.Y;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;


// 显示
@Service
public class Show {
    
    @Resource
    Window window;

    public JFrame frame;
    public JLabel label;
    
    public boolean isCentered = true;  // 是否居中

    private Timer fadeInTimer;
    private Timer fadeOutTimer;
    private float alpha = 1.0f; // 初始为不透明
    
    private Y y; // 新消息内容
    public boolean openAuthor = true;  // 是否显示作者，默认显示
    
    public void updateShow(Y y){
        this.y = y;
        //淡出 并 更新
        fadeOut();
    }
    
    //刷新
    public void refresh(){
        //淡出
        fadeOut();
    }


    @PostConstruct
    public void init(){
        frame = window.frame;
        label = window.label;
        
        //淡出定时器
        initTimers();
    }
    
    // 设置居中
    private void setCenter(){
        if (isCentered) {
            //窗口
            int width = frame.getWidth();

            // 获取屏幕大小
            Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
            int screenWidth = screenSize.width;

            // 计算窗口水平中心点
            int centerX = (screenWidth - width) / 2;

            // 获取窗口在屏幕上的位置
            Point windowLocation = frame.getLocationOnScreen();

            int windowY = windowLocation.y;

            // 计算窗口的新位置，使其水平居中
            int newWindowX = centerX;

            frame.setLocation(newWindowX, windowY);
        }
        System.gc();
    }


    // 初始化定时器
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
                }
            }
        });

        // 淡出定时器
        fadeOutTimer = new Timer(50, new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                alpha -= 0.02f; // 减少透明度
                alpha = Math.max(0.0f, alpha); // 最小为0

                label.setForeground(new Color(0, 255, 191, Math.round(alpha * 255)));

                if (alpha <= 0.0f) {
                    fadeOutTimer.stop(); // 停止淡出
                    String newMsg;
                    if(openAuthor && y.getAuthor().length() > 0){
                        newMsg = y.getMsg() + " -- " + y.getAuthor();
                    }else{
                        newMsg = y.getMsg();
                    }
                    label.setText(" "+newMsg+" ");
                    // 重新计算窗口大小
                    frame.pack();
                    // 重新居中
                    setCenter();
                    // 淡入
                    fadeIn();
                    
                }
            }
        });
    }

    // 淡入
    private void fadeIn() {
        alpha = 0.0f; // 从透明开始
        fadeInTimer.start(); // 启动淡入定时器
    }

    // 淡出
    private void fadeOut() {
        alpha = 1.0f; // 从不透明开始
        fadeOutTimer.start(); // 启动淡出定时器
    }

}
