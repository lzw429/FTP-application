import com.sun.xml.internal.ws.policy.privateutil.PolicyUtils;
import sun.nio.cs.Surrogate;

import java.io.*;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;
import java.nio.charset.Charset;
import java.util.Random;

public class ClientThread extends Thread {
    private Socket socketClient; // 客户端套接字
    private String dir; // 路径
    private final static Random generator = new Random();// 随机数生成器

    public ClientThread(Socket client, String F_DIR) {
        this.socketClient = client;
        this.dir = F_DIR;
    }

    public void run() {
        InputStream is = null;
        OutputStream os = null;
        try {
            is = socketClient.getInputStream();
            os = socketClient.getOutputStream();
        } catch (IOException e) {
            e.printStackTrace();
        }

        BufferedReader reader = new BufferedReader(new InputStreamReader(is,
                Charset.forName("UTF-8")));
        PrintWriter writer = new PrintWriter(os);
        String clientIP = socketClient.getInetAddress().toString().substring(1);
        String username = "";
        String password = "";
        String command = "";
        String arg = "";
        int port_high = 0;
        int port_low = 0;
        String recv_ip = "";//接收文件的IP地址
        boolean is_login = false;
        Socket tempSocket = null;

        while (true) {
            try {
                // 获取客户端的命令
                command = reader.readLine();
                if (command == null)
                    break;
            } catch (IOException e) {
                writer.println("331 Failed to get command");
                writer.flush();
                break;
            }

            // User 命令
            if (command.toUpperCase().startsWith("USER")) {
                username = command.substring(4).trim();
                if (username.equals("")) {
                    writer.println("501 Syntax error");
                    writer.flush();
                } else {
                    writer.println("331 Password required for " + username);
                    writer.flush();
                }
                is_login = false;
            }
            // Pass 命令
            else if (command.toUpperCase().startsWith("PASS")) {
                password = command.substring(4).trim();
                if (username.equals("root") && password.equals("root")) {
                    writer.println("230 Logged on");
                    writer.flush();
                    is_login = true;
                } else {
                    writer.println("530 Login or password incorrect!");
                    writer.flush();
                }
            }
            // PASV 命令
            else if (command.toUpperCase().startsWith("PASV")) {
                ServerSocket ss = null;
                while (true) {
                    // 获取服务器空闲端口，作为数据端口
                    port_high = 1 + generator.nextInt(20);
                    port_low = 1 + generator.nextInt(1000);
                    try {
                        ss = new ServerSocket(port_high * 256 + port_low);
                        break;
                    } catch (IOException e) {
                        e.printStackTrace();
                        continue;
                    }
                }
                InetAddress i = null;
                try {
                    i = InetAddress.getLocalHost();
                } catch (UnknownHostException e1) {
                    e1.printStackTrace();
                }
                writer.println("227 Entering Passive Mode (" + i.getHostAddress().replace(".", ",") + "," + port_high + "," + port_low + ")");
                writer.flush();
                try {
                    tempSocket = ss.accept();
                    ss.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

            // Size 命令：使用命令端口返回文件大小

            // REST 命令

            // RETR 命令：客户端从服务器下载文件
            else if (command.toUpperCase().startsWith("RETR")) {
                arg = command.substring(4).trim();
                if (arg.equals("")) {
                    writer.println("501 Syntax error");
                    writer.flush();
                    continue;
                }
                try {
                    writer.println("150 Opening data channel for file transfer.");
                    writer.flush();
                    RandomAccessFile outFile = null;
                    OutputStream outSocket = null;
                    try {
                        outFile = new RandomAccessFile(dir + "/" + arg, "r");// 命令参数arg是文件名，dir是服务器当前目录名；r是读模式
                        outSocket = tempSocket.getOutputStream();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    byte buffer[] = new byte[1024];
                    int length;
                    try {
                        while ((length = outFile.read(buffer)) != -1) {
                            outSocket.write(buffer, 0, length);
                        }
                        outSocket.close();
                        outFile.close();
                        tempSocket.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            // STOR 命令：客户端上传文件到服务器
            else if (command.toUpperCase().startsWith("STOR")) {
                arg = command.substring(4).trim();
                if (arg.equals("")) {
                    writer.println("501 Syntax error");
                    writer.flush();
                    continue;
                }
                try {
                    writer.println("150 Opening data channel for file transfer.");
                    writer.flush();
                    RandomAccessFile inFile = null;
                    InputStream inSocket = null;
                    try {
                        inFile = new RandomAccessFile(dir + "/" + arg, "rw");
                        inSocket = tempSocket.getInputStream();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    byte buffer[] = new byte[1024];
                    int length;
                    try {
                        while ((length = inSocket.read(buffer)) != -1) {
                            inFile.write(buffer, 0, length);
                        }
                        inSocket.close();
                        inFile.close();
                        tempSocket.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            // QUIT 命令：断开连接
            else if (command.toUpperCase().startsWith("QUIT")) {
                writer.println("221 Goodbye");
                writer.flush();
                try {
                    Thread.currentThread();
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
            // CWD 命令：设置用户的工作目录，即上传和下载文件的位置
            else if (command.toUpperCase().startsWith("CWD")) {

            }
            // LIST 命令：列出服务器指定目录下的文件信息，包括文件大小、文件最后修改时间和文件名称
            else if (command.toUpperCase().startsWith("LIST")) {
                try {
                    writer.println("150 Opening data channel for directory list.");
                    writer.flush();
                    PrintWriter dataWriter = null;
                    try {
                        dataWriter = new PrintWriter(tempSocket.getOutputStream(), true);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    Server.getFileInfo(dataWriter, dir);
                    try {
                        tempSocket.close();
                        dataWriter.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    writer.println("226 Transfer OK");
                    writer.flush();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }
}
