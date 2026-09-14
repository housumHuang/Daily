import javax.swing.*;
import java.awt.*;

/**
 * 主界面:钟面双击关闭后进入此处。
 */
public class MainFrame {

    public static void showMain() {
        JFrame frame = new JFrame("主界面");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(600, 400);

        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(new Color(245, 247, 250));

        JLabel title = new JLabel("主界面", SwingConstants.CENTER);
        title.setFont(new Font("SansSerif", Font.BOLD, 24));
        title.setBorder(BorderFactory.createEmptyBorder(40, 0, 40, 0));
        panel.add(title, BorderLayout.NORTH);

        JLabel hint = new JLabel("双击钟面进入此处", SwingConstants.CENTER);
        hint.setFont(new Font("SansSerif", Font.PLAIN, 14));
        hint.setForeground(new Color(90, 90, 90));
        panel.add(hint, BorderLayout.CENTER);

        JButton back = new JButton("返回钟面");
        back.addActionListener(e -> {
            frame.dispose();
            ClockFace.main(null);
        });
        JPanel south = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        south.setOpaque(false);
        south.add(back);
        panel.add(south, BorderLayout.SOUTH);

        frame.setContentPane(panel);
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
    }
}
