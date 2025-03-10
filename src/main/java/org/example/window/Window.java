package org.example.window;



import com.sun.jna.Native;
import com.sun.jna.Pointer;
import com.sun.jna.platform.win32.User32;
import com.sun.jna.platform.win32.WinDef;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.IOException;
import java.io.InputStream;

import static com.sun.jna.platform.win32.WinUser.SWP_NOMOVE;
import static com.sun.jna.platform.win32.WinUser.SWP_NOSIZE;


// 窗口
@Service
public class Window {

    private JFrame frame;

    private JLabel label;

    private Font font;

    private boolean isCentered = true;  // 是否居中

    private boolean fixed = false;//是否固定

    //设置文字
    public void setText(String text) {
        label.setText(text);
    }

    public JFrame getFrame() {
        return frame;
    }

    public JLabel getLabel() {
        return label;
    }





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

        // 自动调整窗口大小以适应标签文本
        frame.pack();

        frame.setLocationRelativeTo(null); // 居中显示

        // 在容器上添加鼠标监听器，以实现拖动窗口
        frame.addMouseListener(mouseAdapter);
        frame.addMouseMotionListener(mouseAdapter);

        frame.setVisible(true);
        down();// 偏移
    }


    // 设置左右居中
    public void setCenter() {
        //窗口
        int width = frame.getWidth();

        // 获取屏幕大小
        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        int screenWidth = screenSize.width;

        // 计算窗口水平中心点
        int centerX = (screenWidth - width) / 2;

        // 获取窗口在屏幕上的位置
        Point windowLocation = frame.getLocationOnScreen();

        // 计算窗口的新位置，使其水平居中
        frame.setLocation(centerX, windowLocation.y);
    }

    //向下偏移至1/3
    public void down() {
        int height = frame.getHeight();
        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        int screenHeight = screenSize.height;

        //计算屏幕高度1/3的高度
        int centerY = (screenHeight - height) / 3;

        // 获取窗口在屏幕上的位置
        Point windowLocation = frame.getLocationOnScreen();

        frame.setLocation(windowLocation.x, centerY);
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
            font = customFont.deriveFont(Font.PLAIN, 24);  // 设置字体样式和大小

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
            if (isCentered) {
                x = frame.getLocation().x;
            }
            int y = e.getYOnScreen() - offset.y;

            if (!fixed) { //未固定
                frame.setLocation(x, y);
            }

        }
    };


    //立即置底
    public void bottom() {
        frame.setAlwaysOnTop(false);
        // 获取窗口句柄
        WinDef.HWND hwnd = new WinDef.HWND();
        hwnd.setPointer(Native.getComponentPointer(frame));
        // 调用 Windows API，将窗口置于底层
        User32.INSTANCE.SetWindowPos(hwnd, new WinDef.HWND(Pointer.createConstant(1)), 0, 0, 0, 0, SWP_NOSIZE | SWP_NOMOVE);
    }

}
