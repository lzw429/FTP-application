import javax.swing.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.IOException;
import java.net.ConnectException;

public class startWindow {
    private JPanel mainPanel;
    private JButton connectButton;
    private JTextField host;
    private JTextField port;
    private JButton fileChooseButton;
    private JButton downloadButton;
    private JButton uploadButton;
    private JPasswordField passwordField;
    private JLabel statusLabel;
    private JButton disConnectButton;
    private JTextField usernameField;
    private JList fileList;
    private JFileChooser fc = new JFileChooser();
    private Client client = new Client();


    public startWindow() {
        fileChooseButton.addMouseListener(new MouseAdapter() {
            // 文件选择按钮被点击
            @Override
            public void mouseClicked(MouseEvent e) {
                super.mouseClicked(e);
                fc.showOpenDialog(new JPanel());

            }
        });
        connectButton.addMouseListener(new MouseAdapter() {
            // 连接按钮被按下
            @Override
            public void mouseClicked(MouseEvent e) {
                super.mouseClicked(e);
                statusLabel.setText("正在连接...");
                try {
                    client.connectServer(host.getText(), Integer.parseInt(port.getText()));
                    statusLabel.setText("已连接");
                } catch (ConnectException e2) {
                    JOptionPane.showMessageDialog(null, "连接异常", "警告", JOptionPane.WARNING_MESSAGE);
                    statusLabel.setText("连接异常");
                } catch (IOException e1) {
                    e1.printStackTrace();
                } catch (Exception e3) {
                    e3.printStackTrace();
                    statusLabel.setText("连接异常");
                }
            }
        });
        downloadButton.addMouseListener(new MouseAdapter() {
            // 下载按钮被按下
            @Override
            public void mouseClicked(MouseEvent e) {
                super.mouseClicked(e);
            }
        });
        uploadButton.addMouseListener(new MouseAdapter() {
            // 上传按钮被按下
            @Override
            public void mouseClicked(MouseEvent e) {
                super.mouseClicked(e);
            }
        });
        fileChooseButton.addMouseListener(new MouseAdapter() {
        });
        disConnectButton.addMouseListener(new MouseAdapter() {
            // 断开按钮被按下
            @Override
            public void mouseClicked(MouseEvent e) {
                super.mouseClicked(e);
                if (!statusLabel.getText().equals("已连接"))
                    return;
                try {
                    client.disConnect();
                    statusLabel.setText("已断开");
                } catch (IOException e1) {
                    e1.printStackTrace();
                }
            }
        });
    }

    public static void main(String[] args) {
        JFrame frame = new JFrame("startWindow");
        frame.setContentPane(new startWindow().mainPanel);
        frame.setDefaultCloseOperation(JFrame.HIDE_ON_CLOSE);
        frame.pack();
        frame.setVisible(true);
    }
}
