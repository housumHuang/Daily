import javax.swing.*;
import java.awt.*;
import java.time.LocalTime;

public class ClockFace extends JPanel {

    public ClockFace() {
        // 双击钟面:关闭钟面窗口,打开主界面
        addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseClicked(java.awt.event.MouseEvent e) {
                if (e.getClickCount() >= 2) {
                    javax.swing.SwingUtilities.windowForComponent(e.getComponent()).dispose();
                    MainFrame.showMain();
                }
            }
        });
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

        // 当前时间
        LocalTime now = LocalTime.now();
        int hour = now.getHour() % 12;
        int minute = now.getMinute();
        int second = now.getSecond();

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
        frame.setSize(300, 320);
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
