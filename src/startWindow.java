import javax.swing.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

public class startWindow {
    private JPanel panel1;
    private JButton 连接Button;
    private JTextField host;
    private JTextField port;
    private JTextArea textArea1;
    private JButton fileChooseButton;
    private JFileChooser fc = new JFileChooser();


    public startWindow() {
        fileChooseButton.addMouseListener(new MouseAdapter() {// 文件选择按钮被点击
            @Override
            public void mouseClicked(MouseEvent e) {
                super.mouseClicked(e);
                fc.showOpenDialog(new JPanel());

            }
        });
    }

    public static void main(String[] args) {
        JFrame frame = new JFrame("startWindow");
        frame.setContentPane(new startWindow().panel1);
        frame.setDefaultCloseOperation(JFrame.HIDE_ON_CLOSE);
        frame.pack();
        frame.setVisible(true);


    }
}
