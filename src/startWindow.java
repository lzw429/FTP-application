import javax.sound.sampled.Line;
import javax.swing.*;
import java.awt.event.InputMethodListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;
import java.io.IOException;
import java.net.ConnectException;
import java.util.ArrayList;

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

    private JFileChooser fc = new JFileChooser();
    private Client client = new Client();

    private DefaultListModel fileListModel;
    private JList<FileInfo> fileList;
    private JLabel dataStatus;

    private startWindow() { // 启动窗口构造方法
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
                    ArrayList<FileInfo> files = client.connectServer(host.getText(), Integer.parseInt(port.getText()));
                    statusLabel.setText("已连接");
                    fileListModel = new DefaultListModel();
                    for (FileInfo fileInfo : files)
                        fileListModel.addElement(fileInfo);
                    fileList.setModel(fileListModel);
                } catch (ConnectException e2) {
                    JOptionPane.showMessageDialog(null, "连接异常", "警告", JOptionPane.WARNING_MESSAGE);
                    statusLabel.setText("连接异常");
                } catch (IOException e1) {
                    e1.printStackTrace();
                    statusLabel.setText("读写异常");
                } catch (Exception e3) {
                    e3.printStackTrace();
                    statusLabel.setText("未知异常");
                }
            }
        });

        downloadButton.addMouseListener(new MouseAdapter() {
            // 下载按钮被按下
            @Override
            public void mouseClicked(MouseEvent e) {
                super.mouseClicked(e);
                if (!statusLabel.getText().equals("已连接")) {
                    JOptionPane.showMessageDialog(null, "请连接服务器", "警告", JOptionPane.WARNING_MESSAGE);
                    return;
                }
                FileInfo file = fileList.getSelectedValue(); // 将被下载的文件
                JFileChooser fc = new JFileChooser();
                fc.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);// 只能选择目录
                int getDir = fc.showOpenDialog(new JPanel()); // 选择下载目录
                if (getDir == JFileChooser.APPROVE_OPTION)// 如果选中目录
                {
                    String downloadPath = fc.getSelectedFile().getPath();
                    try {
                        client.downloadFile(file.getFileName(), downloadPath);
                        dataStatus.setText("下载完成");
                    } catch (IOException e1) {
                        e1.printStackTrace();
                        statusLabel.setText("读写异常");
                    }
                }
            }
        });

        uploadButton.addMouseListener(new MouseAdapter() {
            // 上传按钮被按下
            @Override
            public void mouseClicked(MouseEvent e) {
                super.mouseClicked(e);
                //  client.uploadFile();
            }
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
                    fileList.setModel(new DefaultListModel<>());
                } catch (IOException e1) {
                    e1.printStackTrace();
                }
            }
        });
    }

    public static void main(String[] args) {
        JFrame frame = new JFrame("FTP");
        frame.setContentPane(new startWindow().mainPanel);
        frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        frame.pack();
        frame.setVisible(true);
    }
}
