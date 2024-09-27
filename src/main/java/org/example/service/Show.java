package org.example.service;

import com.google.common.eventbus.Subscribe;
import org.example.entity.Y;
import org.example.event.YEvent;
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

    private Timer fadeInTimer;  // 淡入定时器
    private Timer fadeOutTimer;  // 淡出定时器
    private float alpha = 1.0f; // 初始为不透明

    Y y;
    
    public boolean openAuthor = false;  // 是否显示作者

    // 监听事件
    @Subscribe
    public void handleStringEvent(YEvent yEvent) {
        this.y = yEvent.getY();
        fadeOut();//淡出
    }


    @PostConstruct
    public void init() {
        //初始化定时器
        initTimers();
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

    // 初始化定时器
    private void initTimers() {

        // 淡出定时器
        fadeOutTimer = new Timer(50, new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                alpha -= 0.02f; // 减少透明度
                alpha = Math.max(0.0f, alpha); // 最小为0

                window.label.setForeground(new Color(0, 255, 191, Math.round(alpha * 255)));

                if (alpha <= 0.0f) {
                    
                    fadeOutTimer.stop(); // 停止淡出
                    
                    // 置底
                    window.bottom();
                    
                    String newMsg;
                    if (openAuthor && y.getAuthor().length() > 0) {
                        newMsg = y.getMsg() + " -- " + y.getAuthor();
                    } else {
                        newMsg = y.getMsg();
                    }
                    
                    window.label.setText(" " + newMsg + " ");
                    window.frame.pack();
                    if(window.isCentered){
                        window.setCenter();
                    }
                    

                    fadeIn();//淡入
                }
                
            }

        });

        // 淡入定时器
        fadeInTimer = new Timer(50, new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                alpha += 0.02f; // 增加透明度
                alpha = Math.min(1.0f, alpha); // 最大为1

                window.label.setForeground(new Color(0, 255, 191, Math.round(alpha * 255)));

                if (alpha >= 1.0f) {
                    fadeInTimer.stop(); // 停止淡入
                }
            }
        });
    }


}
