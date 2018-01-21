import java.io.*;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;
import java.nio.charset.Charset;
import java.util.Random;

public class ThreadOnServer extends Thread {
    private Socket socketClient; // 客户端套接字
    private String dir; // 路径
    private final static Random generator = new Random();// 随机数生成器

    public ThreadOnServer(Socket client, String F_DIR) {
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
        int downloadSkipSize = 0; // 下载给客户端跳过的字节数
        String recv_ip = "";//接收文件的IP地址
        boolean is_login = false;
        Socket tempSocket = null;

        boolean loop = true;
        while (loop) {
            try {
                // 获取客户端的命令
                command = reader.readLine();
                if (command == null)
                    break;
            } catch (IOException e) {
                e.printStackTrace();
                writer.println("331 Failed to get command");
                writer.flush();
                loop = false;
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
            } // end USER

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
            } // end PASS

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
                    tempSocket = ss.accept(); // 传输数据的临时套接字
                    ss.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            } // end PASV

            // Size 命令：使用命令端口返回文件大小
            else if (command.toUpperCase().startsWith("SIZE")) {
                arg = command.substring(4).trim(); // 参数是文件名
                if (arg.equals("")) {
                    writer.println("501 Syntax error");
                    writer.flush();
                    continue;
                }
                File file = new File(dir + "/" + arg);
                if (!file.exists()) // 文件不存在，即大小为0
                {
                    writer.println("213 0");
                    System.out.println(arg + " doesn't exist on this server right now.");
                    writer.flush();
                } else if (file.isFile()) // 文件存在，获取大小
                {
                    int size = (int) file.length();
                    writer.println("213 " + size);
                    System.out.println("The size of " + arg + " on this server is " + size + " bytes.");
                    writer.flush();
                }
            }// end SIZE

            // REST 命令
            else if (command.toUpperCase().startsWith("REST")) {
                arg = command.substring(4).trim(); // 参数是下载要跳过的字节数
                if (arg.equals("")) {
                    writer.println("501 Syntax error");
                    writer.flush();
                    continue;
                }
                downloadSkipSize = Integer.parseInt(arg);
                writer.println("211 Offset " + downloadSkipSize + " bytes has been set.");
                writer.flush();
            }

            // RETR 命令：客户端从服务器下载文件
            else if (command.toUpperCase().startsWith("RETR")) {
                arg = command.substring(4).trim(); // 参数是文件名
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
                        int skipBytes = outFile.skipBytes((int) downloadSkipSize);
                        System.out.println(skipBytes + " bytes of " + arg + " has been actually skipped.");
                        outSocket = tempSocket.getOutputStream();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    byte buffer[] = new byte[1024];
                    int len;
                    try {
                        while ((len = outFile.read(buffer)) != -1) {
                            outSocket.write(buffer, 0, len);
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
            }// end RETR

            // STOR 命令：客户端上传文件到服务器
            else if (command.toUpperCase().startsWith("STOR")) {
                arg = command.substring(4).trim(); // 参数是文件名
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
                        inFile = new RandomAccessFile(dir + "/" + arg, "rw");// 命令参数arg是文件名，dir是服务器当前目录名
                        inSocket = tempSocket.getInputStream();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    byte buffer[] = new byte[1024];
                    int len;
                    try {
                        while ((len = inSocket.read(buffer)) != -1) {
                            inFile.write(buffer, 0, len);
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
            }// end STOR

            // MKD 命令：创建目录
            else if (command.toUpperCase().startsWith("MKD")) {
                arg = command.substring(3).trim(); // 参数是文件名
                File newDir = new File(dir + "/" + arg);
                if (!newDir.mkdir()) // 创建文件夹
                {
                    writer.println("Directory " + newDir.getName() + " cannot be created.");
                    writer.flush();
                } else {
                    writer.println("Directory " + newDir.getName() + " has been created.");
                    writer.flush();
                }

            }

            // QUIT 命令：断开连接
            else if (command.toUpperCase().startsWith("QUIT")) {
                writer.println("221 Goodbye");
                writer.flush();
                loop = false;
                try {
                    Thread.currentThread();
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }// end QUIT

            // CWD 命令：设置用户的工作目录，即上传和下载文件的位置
            else if (command.toUpperCase().startsWith("CWD")) {
                arg = command.substring(3).trim(); // 参数是工作目录
                if (arg.equals("")) {
                    writer.println("250 Broken client detected, missing argument to CWD.");
                    writer.flush();
                    continue;
                }
                File file = new File(arg);
                if (file.exists()) {
                    dir = arg;
                    writer.println("250 CWD successful. " + dir + " is current directory.");
                    writer.flush();
                } else {
                    writer.println("550 CWD failed. " + arg + " : directory not found.");
                    writer.flush();
                }
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
                    tempSocket.close();
                    dataWriter.close();

                    writer.println("226 Transfer OK");
                    writer.flush();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }// end LIST
        }// end while

        // 客户端中断连接后，本线程中断连接
        try {
            reader.close();
            writer.close();
            socketClient.close();
            if (tempSocket != null)
                tempSocket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
