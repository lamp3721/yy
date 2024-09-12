package org.example.service;

import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

@Component
public class WindowMenu {

    @Resource
    Window window;
    
    @Resource
    Window2 window2;

    @Resource
    Show show;

    public JFrame frame;

    public JLabel label;

    public Font font;

    public JPopupMenu popupMenu = new JPopupMenu();

    public JMenuItem menuItem = new JMenuItem("刷新");
    public JMenuItem menuAuthor = new JMenuItem("作者");
    public JMenuItem menuCentered = new JMenuItem("左右居中");
    public JMenuItem menuFixed = new JMenuItem("固定");
    public JMenuItem menuBig = new JMenuItem("big");


    @PostConstruct
    public void init() {
        frame = window.frame;
        label = window.label;
        window.font = font;

        // 菜单添加菜单选项
        popupMenu.add(menuItem);
        popupMenu.add(menuAuthor);
        popupMenu.add(menuCentered);
        popupMenu.add(menuFixed);
        popupMenu.add(menuBig);


        // frame添加右键菜单
        frame.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (SwingUtilities.isRightMouseButton(e)) {
                    popupMenu.show(frame, e.getX(), e.getY());
                }
            }
        });

        //刷新
        menuItem.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                show.fadeOut();
            }
        });
        

        //作者菜单项
        menuAuthor.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                show.openAuthor = !show.openAuthor;
                show.fadeOut();
            }
        });

        //左右居中
        menuCentered.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                window.setCenter();
                window.isCentered = !window.isCentered;

            }
        });
        

        //固定
        menuFixed.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                window.fixed = !window.fixed;
            }
        });
        
        menuBig.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                window2.isOpen = !window2.isOpen;
                window2.big(""+window2.isOpen);
            }
        });

    }
}
