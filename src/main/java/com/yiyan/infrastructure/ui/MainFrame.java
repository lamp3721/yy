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

    // --- 字体 ---
    private Font customFont;
    private Font fallbackFont;

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
        loadFonts(); // 先加载字体
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
        sentenceLabel.setForeground(new Color(0, 255, 191));
        sentenceLabel.setFont(customFont);
        sentenceLabel.setBorder(BorderFactory.createEmptyBorder(10, 20, 10, 20));
        add(sentenceLabel, BorderLayout.CENTER);

        // 作者标签
        authorLabel.setHorizontalAlignment(SwingConstants.RIGHT);
        authorLabel.setVerticalAlignment(SwingConstants.CENTER);
        authorLabel.setForeground(new Color(200, 200, 200));
        authorLabel.setFont(customFont.deriveFont(Font.PLAIN, 16f));
        authorLabel.setBorder(BorderFactory.createEmptyBorder(0, 0, 10, 25));
        authorLabel.setVisible(false);
        add(authorLabel, BorderLayout.SOUTH);
    }

    private void loadFonts() {
        fallbackFont = new Font("SansSerif", Font.PLAIN, 24); // 定义备用字体
        try (InputStream is = getClass().getResourceAsStream("/fonts/最深的夜里最温柔.ttf")) {
            customFont = Font.createFont(Font.TRUETYPE_FONT, Objects.requireNonNull(is)).deriveFont(Font.PLAIN, 24f);
        } catch (Exception e) {
            System.err.println("自定义字体加载失败，将仅使用默认字体。错误: " + e.getMessage());
            customFont = fallbackFont; // 如果加载失败，则将自定义字体设置为备用字体
        }
    }

    /**
     * 构建带有字体回退机制的HTML字符串。
     * 利用CSS的font-family属性，实现自定义字体和系统备用字体的混合渲染。
     *
     * @param text         要显示的原始文本。
     * @param primaryFont  首选字体（通常是自定义字体）。
     * @param fallbackFont 当首选字体不支持某些字符时使用的备用字体。
     * @param color        文本的颜色。
     * @return             一个包含CSS和文本内容的HTML字符串，可直接用于JLabel。
     */
    private String buildHtmlWithFallback(String text, Font primaryFont, Font fallbackFont, Color color) {
        if (!StringUtils.hasText(text)) {
            return "<html></html>";
        }

        String primaryFamily = primaryFont.getFamily();
        String fallbackFamily = fallbackFont.getFamily();
        int size = primaryFont.getSize();

        // 简单的HTML转义
        String escapedText = text.replace("&", "&amp;")
                                 .replace("<", "&lt;")
                                 .replace(">", "&gt;");

        // 颜色通过参数传入
        return String.format(
                "<html><head><style>" +
                        "body { font-family: '%s', '%s'; font-size: %dpt; color: rgb(%d, %d, %d); }" +
                        "</style></head>" +
                        "<body>%s</body></html>",
                primaryFamily,
                fallbackFamily,
                size,
                color.getRed(),
                color.getGreen(),
                color.getBlue(),
                escapedText
        );
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
        String html = buildHtmlWithFallback(text, customFont, fallbackFont, sentenceLabel.getForeground());
        this.sentenceLabel.setText(html);
        pack();
    }

    @Override
    public void setAuthorText(String author, boolean visible) {
        this.isAuthorVisible = visible;
        if (StringUtils.hasText(author) && visible) {
            Font authorPrimaryFont = customFont.deriveFont(Font.PLAIN, 16f);
            Font authorFallbackFont = fallbackFont.deriveFont(Font.PLAIN, 16f);
            String html = buildHtmlWithFallback(author, authorPrimaryFont, authorFallbackFont, authorLabel.getForeground());
            this.authorLabel.setText(html);
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