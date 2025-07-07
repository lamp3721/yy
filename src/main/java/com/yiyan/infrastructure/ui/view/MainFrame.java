package com.yiyan.infrastructure.ui.view;

import com.yiyan.core.domain.Sentence;
import com.yiyan.infrastructure.ui.component.PopupMenuFactory;
import com.yiyan.infrastructure.ui.dto.HorizontalAlignment;
import com.yiyan.infrastructure.ui.presenter.ViewCallback;
import com.yiyan.infrastructure.ui.service.AnimationService;
import com.yiyan.infrastructure.ui.service.DesktopManager;
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

    /**
     * 动画服务，用于实现窗口的淡入淡出效果
     */
    private final AnimationService animationService;
    /**
     * 桌面管理器，处理窗口在桌面上的层级关系
     */
    private final DesktopManager desktopManager;
    /**
     * 右键弹出菜单工厂，用于创建自定义的上下文菜单
     */
    private final PopupMenuFactory popupMenuFactory;

    // --- MVP ---
    private ViewCallback callback;

    // --- 字体 ---
    private Font customFont;
    private Font fallbackFont;

    // --- 状态 ---
    private boolean isAuthorVisible = false;
    private JPopupMenu popupMenu;
    private boolean isHorizontalDragEnabled = false;
    private HorizontalAlignment alignment = HorizontalAlignment.CENTER;
    private boolean isLocked = false;
    private boolean isTemporaryTopEnabled = true;

    // --- 拖拽 ---
    private Point initialClick;
    private Timer temporaryTopTimer;

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
        setTitle("qq:2932349894");
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
                if (isLocked) return; // 如果锁定，则禁止拖动

                if (SwingUtilities.isLeftMouseButton(e) && initialClick != null) {
                    Point currentLocationOnScreen = e.getLocationOnScreen();
                    int newY = currentLocationOnScreen.y - initialClick.y;
                    int newX = getLocation().x; // 默认X坐标不变

                    if (isHorizontalDragEnabled) {
                        newX = currentLocationOnScreen.x - initialClick.x;
                    }

                    setLocation(newX, newY);
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
                new PopupMenuFactory.MenuInitialState(isAuthorVisible, isHorizontalDragEnabled, alignment, isLocked, isTemporaryTopEnabled),
                () -> sentenceLabel.getText() + " " + authorLabel.getText()
        );
    }


    // --- SentenceView 接口实现 ---

    @Override
    public void setSentenceText(String text) {
        String html = buildHtmlWithFallback(text, customFont, fallbackFont, sentenceLabel.getForeground());
        this.sentenceLabel.setText(html);
        //重新计算窗口大小
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
    public void alignOnScreen(HorizontalAlignment alignment) {
        if (isLocked) return; // 如果锁定，则禁止对齐
        this.alignment = alignment; // 同步状态
        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        int y = getLocation().y; // 保持当前的Y坐标
        int x;

        switch (alignment) {
            case LEFT -> x = 0;
            case RIGHT -> x = screenSize.width - getWidth();
            case CENTER -> x = (screenSize.width - getWidth()) / 2;
            default -> x = getLocation().x; // 保持不变以防意外
        }

        setLocation(x, y);
    }

    @Override
    public void bringToTopAndSendToBottomAfterDelay(int delayInSeconds) {
        // 如果当前有正在运行的计时器，先停止它，以重置计时
        if (temporaryTopTimer != null && temporaryTopTimer.isRunning()) {
            temporaryTopTimer.stop();
        }

        // 立即置顶
        setAlwaysOnTop(true);

        // 创建一个一次性的计时器
        temporaryTopTimer = new Timer(delayInSeconds * 1000, e -> {
            // 5秒后执行：取消置顶并沉到底部
            setAlwaysOnTop(false);
            desktopManager.sendToBottom(this);
        });
        temporaryTopTimer.setRepeats(false);
        temporaryTopTimer.start();
    }

    @Override
    public void cancelTemporaryTopTimer() {
        if (temporaryTopTimer != null && temporaryTopTimer.isRunning()) {
            temporaryTopTimer.stop();
        }
        // 确保在取消时，窗口的置顶状态也被重置
        if (isAlwaysOnTop()) {
            setAlwaysOnTop(false);
        }
    }

    @Override
    public void setLocked(boolean locked) {
        this.isLocked = locked;
    }

    @Override
    public void rebuildUiForNewState(boolean isAuthorVisible, boolean isHorizontalDragEnabled, HorizontalAlignment alignment, boolean isLocked, boolean isTemporaryTopEnabled) {
        this.isAuthorVisible = isAuthorVisible;
        this.isHorizontalDragEnabled = isHorizontalDragEnabled;
        this.alignment = alignment;
        this.isLocked = isLocked;
        this.isTemporaryTopEnabled = isTemporaryTopEnabled;
        // 此方法由Presenter在设置完回调后调用，因此callback不为null
        this.popupMenu = createPopupMenu();
    }

    @Override
    public void setHorizontalDragEnabled(boolean enabled) {
        this.isHorizontalDragEnabled = enabled;
    }
} 