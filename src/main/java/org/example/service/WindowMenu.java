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
    Show show;

    public JFrame frame;

    public JLabel label;

    public Font font;

    public JPopupMenu popupMenu = new JPopupMenu();
    
    public JMenuItem menuItem = new JMenuItem("刷新");
    public JMenuItem menuAuthor = new JMenuItem("作者");
    public JMenuItem menuCentered = new JMenuItem("居中");
    


    @PostConstruct
    public void init(){
        frame = window.frame;
        label = window.label;
        window.font = font;

        // 菜单添加菜单选项
        popupMenu.add(menuItem);
        popupMenu.add(menuAuthor);
        popupMenu.add(menuCentered);



        // frame添加右键菜单
        frame.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (SwingUtilities.isRightMouseButton(e)) {
                    popupMenu.show(frame, e.getX(), e.getY());
                }
            }
        });

        //刷新按钮
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
        
        //居中
        menuCentered.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                show.isCentered = !show.isCentered;
                show.fadeOut();
            }
        });

    }
}
