import javax.swing.*;
import java.awt.*;
import java.awt.datatransfer.StringSelection;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.format.TextStyle;
import java.util.Locale;

public class ClockFace extends JPanel {

    private static final DateTimeFormatter COPY_FORMAT = DateTimeFormatter.ofPattern("HH:mm");
    // private static final DateTimeFormatter COPY_FORMAT_0 = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    // ------------------------------------------------1111 = 年|月日|时分|秒
    private static final DateTimeFormatter COPY_FORMAT_1111 = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    // ------------------------------------------------0110 = 月日 时分 无年无秒
    private static final DateTimeFormatter COPY_FORMAT_0110 = DateTimeFormatter.ofPattern("MM-dd HH:mm");
    // ------------------------------------------------0010 = 时分
    private static final DateTimeFormatter COPY_FORMAT_Time = DateTimeFormatter.ofPattern("HH:mm");
    // ------------------------------------------------0100 = 月日
    private static final DateTimeFormatter COPY_FORMAT_Date = DateTimeFormatter.ofPattern("MM-dd");
    // ------------------------------------------------0100.1 = 月日星
    // private static final DateTimeFormatter COPY_FORMAT_WeekDate = DateTimeFormatter.ofPattern("MM-dd星期");

    // 复制后的短暂提示文字,为 null 时不显示
    private String copyFeedback;
    private Timer feedbackTimer;

    // 复制时间的公共动作:右键菜单项与 Ctrl+C 快捷键共用同一实现
    private final AbstractAction copyAction = new AbstractAction("复制时间（Ctrl+C）") {
        @Override
        public void actionPerformed(java.awt.event.ActionEvent e) {
            copyTimeToClipboard();
        }
    };

    public ClockFace() {
        // 双击钟面:关闭钟面窗口,打开主界面;右键弹出复制菜单
        addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseClicked(java.awt.event.MouseEvent e) {
                if (e.getClickCount() >= 2) {
                    javax.swing.SwingUtilities.windowForComponent(e.getComponent()).dispose();
                    MainFrame.showMain();
                }
            }

            @Override
            public void mousePressed(java.awt.event.MouseEvent e) {
                showPopupIfNeeded(e);
            }

            @Override
            public void mouseReleased(java.awt.event.MouseEvent e) {
                showPopupIfNeeded(e);
            }
        });

        // Ctrl+C 快捷键:窗口有焦点时即可触发,无需先点击钟面
        getInputMap(WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke("control C"), "copyTime");
        getActionMap().put("copyTime", copyAction);
    }

    // 右键弹出上下文菜单(不同平台在 pressed 或 released 触发)
    private void showPopupIfNeeded(java.awt.event.MouseEvent e) {
        if (!e.isPopupTrigger()) {
            return;
        }
        JPopupMenu menu = new JPopupMenu();
        JMenuItem copyItem = new JMenuItem(copyAction);   // 文案含快捷键提示
        menu.add(copyItem);
        menu.show(e.getComponent(), e.getX(), e.getY());
    }

    private void copyTimeToClipboard() {
        // 以按下 Ctrl+C / 点击菜单项的这一刻的时间为准
        String text = LocalDateTime.now().format(COPY_FORMAT);
        Toolkit.getDefaultToolkit().getSystemClipboard().setContents(new StringSelection(text), null);
        showCopyFeedback();
    }

    // 文字提示约 2 秒后恢复
    private void showCopyFeedback() {
        copyFeedback = "已复制";
        if (feedbackTimer != null) {
            feedbackTimer.stop();
        }
        feedbackTimer = new Timer(2000, e -> {
            copyFeedback = null;
            repaint();
        });
        feedbackTimer.setRepeats(false);
        feedbackTimer.start();
        repaint();
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g;
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        int width = getWidth();
        int height = getHeight();
        int cx = width / 2;
        int cy = height / 2;
        // int radius = Math.min(width, height) / 2 - 40;
        int radius = Math.min(width, height) / 2 - 20;

        // 当前时间
        LocalTime now = LocalTime.now();
        int hour = now.getHour() % 12;
        int minute = now.getMinute();
        int second = now.getSecond();

        // ========== 钟面上方显示日期(三行): 公历 / 星期 / 农历 ==========
        LocalDate today = LocalDate.now();
        // 第一行:中文公历月份-公历日 (例如:九月-15)
        // String[] cnMonths = {"一月","二月","三月","四月","五月","六月","七月","八月","九月","十月","十一月","十二月"};
        // String line1 = cnMonths[today.getMonthValue() - 1] + "-" + today.getDayOfMonth();
        String line1 = today.getMonthValue() + "-" + today.getDayOfMonth();
        line1 += " " + today.getDayOfWeek().getDisplayName(TextStyle.FULL, Locale.CHINA);
        // 第二行:时分
        String line2 = hour + ":" + minute;
        // 第三行:农历年-农历月-农历日 (例如:丙午-八月-初五)
        String line3 = LunarCalendar.of(today).toString();

        g2.setFont(new Font("SansSerif", Font.BOLD, 13));
        FontMetrics dfm = g2.getFontMetrics();
        int lineH = dfm.getHeight() + 3;
        int startY = cy - radius - lineH * 3 + 2;   // 从钟面顶部往上排三行
        g2.setColor(Color.BLACK);
        // String[] lines = {line1, line2, line3};
        String[] lines = {line1, line2, line3};
        for (int i = 0; i < 3; i++) {
            int lx = cx - dfm.stringWidth(lines[i]) / 2;
            g2.drawString(lines[i], lx, startY + i * lineH + dfm.getAscent());
        }

        // 表盘外圈
        g2.setColor(Color.WHITE);
        g2.fillOval(cx - radius, cy - radius, radius * 2, radius * 2);
        g2.setColor(Color.BLACK);
        g2.setStroke(new BasicStroke(3));
        g2.drawOval(cx - radius, cy - radius, radius * 2, radius * 2);

        // 刻度
        for (int i = 0; i < 60; i++) {
            double angle = Math.toRadians(i * 6 - 90);
            boolean isHour = (i % 5 == 0);
            int outer = radius - 5;
            int inner = isHour ? radius - 25 : radius - 12;
            int x1 = (int) (cx + outer * Math.cos(angle));
            int y1 = (int) (cy + outer * Math.sin(angle));
            int x2 = (int) (cx + inner * Math.cos(angle));
            int y2 = (int) (cy + inner * Math.sin(angle));
            g2.setStroke(new BasicStroke(isHour ? 3 : 1));
            g2.drawLine(x1, y1, x2, y2);
        }

        // 数字
        g2.setFont(new Font("SansSerif", Font.BOLD, 18));
        FontMetrics fm = g2.getFontMetrics();
        for (int i = 1; i <= 12; i++) {
            double angle = Math.toRadians(i * 30 - 90);
            int r = radius - 45;
            int x = (int) (cx + r * Math.cos(angle));
            int y = (int) (cy + r * Math.sin(angle));
            String num = String.valueOf(i);
            int tw = fm.stringWidth(num);
            g2.drawString(num, x - tw / 2, y + fm.getAscent() / 2);
        }

        // 时针
        double hAngle = Math.toRadians((hour + minute / 60.0) * 30 - 90);
        drawHand(g2, cx, cy, hAngle, (int) (radius * 0.5), 6, Color.BLACK);

        // 分针
        double mAngle = Math.toRadians((minute + second / 60.0) * 6 - 90);
        drawHand(g2, cx, cy, mAngle, (int) (radius * 0.75), 4, Color.BLUE);

        // 秒针
        double sAngle = Math.toRadians(second * 6 - 90);
        drawHand(g2, cx, cy, sAngle, (int) (radius * 0.85), 2, Color.RED);

        // 中心圆点
        g2.setColor(Color.BLACK);
        g2.fillOval(cx - 6, cy - 6, 12, 12);

        // 复制成功后的短暂提示(钟面下方)
        if (copyFeedback != null) {
            g2.setFont(new Font("SansSerif", Font.BOLD, 13));
            FontMetrics cfm = g2.getFontMetrics();
            g2.setColor(new Color(0x2E, 0x7D, 0x32));
            int fx = cx - cfm.stringWidth(copyFeedback) / 2;
            int fy = cy + radius + cfm.getAscent() + 12;
            g2.drawString(copyFeedback, fx, fy);
        }
    }

    private void drawHand(Graphics2D g2, int cx, int cy, double angle, int length, int width, Color color) {
        int x = (int) (cx + length * Math.cos(angle));
        int y = (int) (cy + length * Math.sin(angle));
        g2.setColor(color);
        g2.setStroke(new BasicStroke(width, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        g2.drawLine(cx, cy, x, y);
    }

    public static void main(String[] args) {
        JFrame frame = new JFrame("Clock Face");
        // 双击后通过 dispose() 关闭,主界面会接管窗口
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        // frame.setSize(420, 440);
        frame.setSize(300, 450);   // 窗口加高,为上方三行日期文字留出空间
        frame.setContentPane(new ClockFace());
        // 窗口置于屏幕左下角
        java.awt.GraphicsConfiguration gc = frame.getGraphicsConfiguration();
        java.awt.Rectangle bounds = gc.getBounds();
        java.awt.Insets insets = java.awt.Toolkit.getDefaultToolkit().getScreenInsets(gc);
        int x = bounds.x + insets.left;
        int y = bounds.y + bounds.height - insets.bottom - frame.getHeight();
        frame.setLocation(x, y);
        // 每秒刷新一次,让钟走起来
        new Timer(1000, e -> frame.repaint()).start();
        frame.setVisible(true);
    }
}
