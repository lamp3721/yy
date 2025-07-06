package com.yiyan.infrastructure.ui;

import com.yiyan.core.domain.Sentence;
import jakarta.annotation.PostConstruct;
import lombok.Getter;

import org.springframework.util.StringUtils;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.InputStream;
import java.awt.datatransfer.StringSelection;

/**
 * UI主窗口 (JFrame)，负责创建和管理应用的基本窗口框架。
 */
@org.springframework.stereotype.Component
@Getter
public class MainFrame extends JFrame {

    private final JLabel sentenceLabel;
    private final JLabel authorLabel;
    private Font customFont;
    private Font authorFont;
    private UiController uiController; // 回调到UI控制器
    private boolean isPositionLocked = false; // 窗口位置锁定状态
    private boolean isAuthorVisible = false; // 是否显示作者，默认为false

    public MainFrame() {
        // 初始化窗口基本属性
        setTitle("一言");
        setType(JFrame.Type.UTILITY); // 不在任务栏显示图标
        setUndecorated(true); // 无边框
        setBackground(new Color(0, 0, 0, 0)); // 背景透明
        setAlwaysOnTop(true); // 初始时总在最前

        // 加载自定义字体
        loadCustomFont();

        // 创建用于显示文本的 JLabel
        sentenceLabel = new JLabel("正在获取一言...", SwingConstants.CENTER);
        sentenceLabel.setFont(customFont);
        sentenceLabel.setForeground(new Color(0, 255, 191));
        sentenceLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        // 创建用于显示作者的 JLabel
        authorLabel = new JLabel("", SwingConstants.RIGHT);
        authorLabel.setFont(authorFont);
        authorLabel.setForeground(new Color(200, 200, 200));
        authorLabel.setVisible(false); // 默认隐藏

        // 使用一个垂直的BoxLayout来布局主内容面板
        JPanel contentPanel = new JPanel(new BorderLayout());
        contentPanel.setOpaque(false);
        contentPanel.add(sentenceLabel, BorderLayout.CENTER);
        contentPanel.add(authorLabel, BorderLayout.SOUTH);
        sentenceLabel.setBorder(BorderFactory.createEmptyBorder(0, 0, 5, 0));

        // 将内容面板添加到窗口
        add(contentPanel, BorderLayout.CENTER);

        // 添加鼠标拖动功能
        setupMouseListener();

        // 创建右键弹出菜单
        createPopupMenu();
    }

    /**
     * 在Bean初始化后执行，完成窗口的最终设置并显示。
     */
    @PostConstruct
    private void init() {
        pack(); // 根据内容调整窗口大小
        setLocationRelativeTo(null); // 初始居中
        setVisible(true);
    }

    /**
     * 设置UI控制器，由UiController在初始化时调用。
     */
    public void setUiController(UiController uiController) {
        this.uiController = uiController;
    }

    /**
     * 设置窗口的文本内容，并自动调整大小。
     * @param sentence 要显示的"一言"对象。
     */
    public void updateSentence(Sentence sentence) {
        sentenceLabel.setText(" " + sentence.getText() + " ");

        // 根据isAuthorVisible状态和作者是否存在，共同决定是否显示authorLabel
        if (isAuthorVisible && StringUtils.hasText(sentence.getAuthor())) {
            authorLabel.setText("—— " + sentence.getAuthor());
            authorLabel.setVisible(true);
        } else {
            authorLabel.setVisible(false);
        }

        // 如果位置被锁定，则只重新计算大小，不移动位置
        if (isPositionLocked) {
            pack();
        } else {
            pack(); // 重新计算窗口大小
            centerOnScreen(); // 然后水平居中
        }
    }

    /**
     * 将窗口在屏幕上水平居中，除非位置被锁定。
     */
    public void centerOnScreen() {
        if (isPositionLocked) {
            return; // 如果位置锁定，则不执行任何操作
        }
        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        int newX = (screenSize.width - getWidth()) / 2;
        setLocation(newX, getY());
    }

    /**
     * 将窗口定位到屏幕垂直方向的三分之一处。
     */
    public void positionOnThirdOfScreen() {
        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        int newY = (screenSize.height - getHeight()) / 3;
        setLocation(getX(), newY);
    }

    /**
     * 加载自定义字体文件。
     */
    private void loadCustomFont() {
        try (InputStream is = MainFrame.class.getResourceAsStream("/fonts/最深的夜里最温柔.ttf")) {
            if (is == null) {
                throw new IllegalStateException("字体文件 /fonts/最深的夜里最温柔.ttf 未找到！");
            }
            Font createdFont = Font.createFont(Font.TRUETYPE_FONT, is);
            this.customFont = createdFont.deriveFont(Font.PLAIN, 24f);
            this.authorFont = createdFont.deriveFont(Font.PLAIN, 16f); // 作者字体稍小
        } catch (Exception e) {
            // 如果自定义字体加载失败，则使用默认字体
            this.customFont = new Font("SansSerif", Font.PLAIN, 24);
            this.authorFont = new Font("SansSerif", Font.PLAIN, 16);
            System.err.println("加载自定义字体失败，将使用默认字体: " + e.getMessage());
        }
    }

    /**
     * 设置鼠标监听器以实现窗口拖动。
     */
    private void setupMouseListener() {
        MouseAdapter adapter = new MouseAdapter() {
            private Point offset;

            @Override
            public void mousePressed(MouseEvent e) {
                if (SwingUtilities.isLeftMouseButton(e)) {
                    offset = e.getPoint();
                }
                // 检查是否需要弹出菜单（在某些系统上，按下鼠标时触发）
                showPopupMenuIfNeeded(e);
            }

            @Override
            public void mouseReleased(MouseEvent e) {
                // 检查是否需要弹出菜单（在另一些系统上，释放鼠标时触发）
                showPopupMenuIfNeeded(e);
            }

            @Override
            public void mouseDragged(MouseEvent e) {
                // 防御性编程：如果拖拽事件来自窗口外部，offset可能为null
                if (offset == null || isPositionLocked || !SwingUtilities.isLeftMouseButton(e)) {
                    return;
                }
                // 根据鼠标拖动更新窗口位置（只允许Y轴移动）
                Point newPoint = e.getLocationOnScreen();
                int newY = newPoint.y - offset.y;

                // 保持X轴始终居中
                int currentX = getX();
                setLocation(currentX, newY);
                centerOnScreen(); // 拖动时也强制水平居中
            }

            private void showPopupMenuIfNeeded(MouseEvent e) {
                if (e.isPopupTrigger()) {
                    createPopupMenu().show(e.getComponent(), e.getX(), e.getY());
                }
            }
        };
        addMouseListener(adapter);
        addMouseMotionListener(adapter);
    }

    /**
     * 创建并组装右键弹出菜单。
     * @return 配置好的JPopupMenu实例。
     */
    private JPopupMenu createPopupMenu() {
        JPopupMenu popupMenu = new JPopupMenu();

        popupMenu.add(createRefreshMenuItem());
        popupMenu.add(createCopyMenuItem());
        popupMenu.addSeparator();
        popupMenu.add(createLockPositionMenuItem());
        popupMenu.add(createShowAuthorMenuItem());
        popupMenu.addSeparator();
        popupMenu.add(createExitMenuItem());

        return popupMenu;
    }

    /**
     * 创建"刷新"菜单项。
     */
    private JMenuItem createRefreshMenuItem() {
        JMenuItem refreshItem = new JMenuItem("刷新");
        refreshItem.addActionListener(e -> {
            if (uiController != null) {
                uiController.handleManualRefresh();
            }
        });
        return refreshItem;
    }

    /**
     * 创建"复制"菜单项。
     */
    private JMenuItem createCopyMenuItem() {
        JMenuItem copyItem = new JMenuItem("复制");
        copyItem.addActionListener(e -> {
            String textToCopy = sentenceLabel.getText().trim();
            if (authorLabel.isVisible()) {
                textToCopy += " " + authorLabel.getText();
            }
            StringSelection selection = new StringSelection(textToCopy);
            Toolkit.getDefaultToolkit().getSystemClipboard().setContents(selection, null);
        });
        return copyItem;
    }

    /**
     * 创建"锁定位置"菜单项。
     */
    private JCheckBoxMenuItem createLockPositionMenuItem() {
        JCheckBoxMenuItem lockPositionItem = new JCheckBoxMenuItem("锁定位置");
        lockPositionItem.setState(isPositionLocked);
        lockPositionItem.addActionListener(e -> isPositionLocked = lockPositionItem.getState());
        return lockPositionItem;
    }

    /**
     * 创建"显示作者"菜单项。
     */
    private JCheckBoxMenuItem createShowAuthorMenuItem() {
        JCheckBoxMenuItem showAuthorItem = new JCheckBoxMenuItem("显示作者");
        showAuthorItem.setState(isAuthorVisible);
        showAuthorItem.addActionListener(e -> {
            isAuthorVisible = showAuthorItem.getState();
            // 立即触发一次UI更新以应用更改
            if (uiController != null) {
                uiController.handleManualRefresh(); // 请求新数据以刷新显示
            }
        });
        return showAuthorItem;
    }

    /**
     * 创建"退出"菜单项。
     */
    private JMenuItem createExitMenuItem() {
        JMenuItem exitItem = new JMenuItem("退出");
        exitItem.addActionListener(e -> System.exit(0));
        return exitItem;
    }
} 