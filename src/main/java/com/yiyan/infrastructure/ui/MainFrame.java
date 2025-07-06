package com.yiyan.infrastructure.ui;

import com.yiyan.core.domain.Sentence;
import jakarta.annotation.PostConstruct;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.InputStream;
import java.util.Objects;

/**
 * 应用程序的主窗口（JFrame），实现了 SentenceView 接口。
 * <p>
 * 它作为MVP模式中的"视图（View）"，负责所有Swing组件的渲染和布局，
 * 并将用户交互事件通过 ViewCallback 接口委托给Presenter（UiController）处理。
 */
@Component
public class MainFrame extends JFrame implements SentenceView {
    private final JLabel sentenceLabel;
    private final JLabel authorLabel;

    // --- 依赖 ---
    private final AnimationService animationService;
    private final DesktopManager desktopManager;
    private final PopupMenuFactory popupMenuFactory;

    // --- MVP ---
    private ViewCallback callback;

    // --- 状态 ---
    private boolean isAuthorVisible = false;
    private JPopupMenu popupMenu;

    // --- 拖拽 ---
    private Point initialClick;

    public MainFrame(AnimationService animationService, DesktopManager desktopManager, PopupMenuFactory popupMenuFactory) {
        this.animationService = animationService;
        this.desktopManager = desktopManager;
        this.popupMenuFactory = popupMenuFactory;
        this.sentenceLabel = new JLabel();
        this.authorLabel = new JLabel();
        configureWindow();
        createLayoutAndLabels();
        addMouseListeners();
    }
    
    @Override
    public void setCallback(ViewCallback callback) {
        this.callback = callback;
    }

    private void configureWindow() {
        setTitle("一言");
        setUndecorated(true);
        setBackground(new Color(0, 0, 0, 0)); // 全透明背景
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setAlwaysOnTop(true);
        setType(Type.UTILITY); // 任务栏不可见
    }

    private void createLayoutAndLabels() {
        setLayout(new BorderLayout());

        // 主标签
        sentenceLabel.setHorizontalAlignment(SwingConstants.CENTER);
        sentenceLabel.setVerticalAlignment(SwingConstants.CENTER);
        sentenceLabel.setForeground(new Color(0, 255, 191)); // 恢复原来的颜色
        sentenceLabel.setFont(loadCustomFont());
        sentenceLabel.setBorder(BorderFactory.createEmptyBorder(10, 20, 10, 20));
        add(sentenceLabel, BorderLayout.CENTER);

        // 作者标签
        authorLabel.setHorizontalAlignment(SwingConstants.RIGHT);
        authorLabel.setVerticalAlignment(SwingConstants.CENTER);
        authorLabel.setForeground(new Color(200, 200, 200)); // 稍暗的颜色
        authorLabel.setFont(sentenceLabel.getFont().deriveFont(Font.PLAIN, 16f));
        authorLabel.setBorder(BorderFactory.createEmptyBorder(0, 0, 10, 25));
        authorLabel.setVisible(false); // 默认隐藏
        add(authorLabel, BorderLayout.SOUTH);
    }

    private Font loadCustomFont() {
        try (InputStream is = getClass().getResourceAsStream("/fonts/最深的夜里最温柔.ttf")) {
            return Font.createFont(Font.TRUETYPE_FONT, Objects.requireNonNull(is)).deriveFont(Font.PLAIN, 24f); // 恢复原来的字体大小
        } catch (Exception e) {
            System.err.println("字体加载失败，使用默认字体。错误: " + e.getMessage());
            return new Font("Serif", Font.PLAIN, 24);
        }
    }

    private void addMouseListeners() {
        MouseAdapter adapter = new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                if (SwingUtilities.isLeftMouseButton(e)) {
                    initialClick = e.getPoint();
                }
            }

            @Override
            public void mouseDragged(MouseEvent e) {
                if (SwingUtilities.isLeftMouseButton(e) && initialClick != null) {
                    // 新的拖动逻辑：只改变Y轴位置
                    Point currentLocationOnScreen = e.getLocationOnScreen();
                    int newY = currentLocationOnScreen.y - initialClick.y;
                    setLocation(getLocation().x, newY); // X轴位置保持不变
                }
            }

            @Override
            public void mouseReleased(MouseEvent e) {
                if (e.isPopupTrigger() && popupMenu != null) {
                    popupMenu.show(e.getComponent(), e.getX(), e.getY());
                }
                initialClick = null;
            }
        };
        addMouseListener(adapter);
        addMouseMotionListener(adapter);
    }

    private JPopupMenu createPopupMenu() {
        return popupMenuFactory.create(
                callback,
                new PopupMenuFactory.MenuInitialState(isAuthorVisible),
                () -> sentenceLabel.getText() + " " + authorLabel.getText()
        );
    }


    // --- SentenceView 接口实现 ---

    @Override
    public void setSentenceText(String text) {
        this.sentenceLabel.setText(text);
        pack(); // 根据新内容调整窗口大小
    }

    @Override
    public void setAuthorText(String author, boolean visible) {
        this.isAuthorVisible = visible; // 更新本地状态
        if (StringUtils.hasText(author) && visible) {
            this.authorLabel.setText(author);
            this.authorLabel.setVisible(true);
        } else {
            this.authorLabel.setVisible(false);
        }
        pack();
    }

    @Override
    public void runDisplayAnimation(Runnable updateAction) {
        animationService.runFadeSequence(this, updateAction);
    }

    @Override
    public void sendToBottom() {
        desktopManager.sendToBottom(this);
    }

    @Override
    public void centerOnScreen() {
        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        int x = (screenSize.width - this.getWidth()) / 2;
        setLocation(x, getLocation().y);
    }

    @Override
    public void rebuildUiForNewState(boolean isAuthorVisible) {
        this.isAuthorVisible = isAuthorVisible;
        // 此方法由Presenter在设置完回调后调用，因此callback不为null
        this.popupMenu = createPopupMenu();
    }
} 