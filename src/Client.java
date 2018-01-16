import java.io.*;
import java.net.Socket;
import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Client {
    private Socket socket; // 控制端口
    private BufferedReader reader;
    private PrintWriter writer;
    private String response; // 来自服务器的应答
    private Socket dataSocket; // 数据端口

    ArrayList<FileInfo> connectServer(String host, int port) throws IOException {
        // 客户端和FTP服务器建立Socket连接
        socket = new Socket(host, port);
        reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        writer = new PrintWriter(new OutputStreamWriter(socket.getOutputStream()));

        // 向服务器发送USER、PASS命令登录FTP服务器
        writer.println("USER root");
        writer.flush();
        response = reader.readLine();
        System.out.println(response);

        writer.println("PASS root");
        writer.flush();
        response = reader.readLine();
        System.out.println(response);

        PASV();
        return getFileDir();
    }

    private ArrayList<FileInfo> getFileDir() throws IOException {
        // TODO 获得文件列表
        ArrayList<FileInfo> files = new ArrayList<>();
        // 使用List命令获得文件列表
        writer.println("List");
        writer.flush();
        response = reader.readLine();
        // 从数据端口中接收数据
        System.out.println(response); // 150 Opening data channel for directory list
        BufferedReader dataReader = new BufferedReader(new InputStreamReader(dataSocket.getInputStream()));
        String line = "";
        while ((line = dataReader.readLine()) != null) {
            String lineBytes = new String(line.getBytes("ISO-8859-1"), "utf-8");
            String[] fileInfo = getFileInfo(lineBytes);
            FileInfo file = new FileInfo(fileInfo[2], fileInfo[1], Integer.parseInt(fileInfo[0]));
            files.add(file);
            System.out.println(file);// 标准输出文件信息
        }
        dataReader.close();
        response = reader.readLine();
        System.out.println(response); // 226 Transfer OK
        return files;
    }

    void downloadFile(String filename, String localPath) throws IOException {
        // TODO 下载文件
        // 客户端和FTP服务器建立Socket连接
        // 向服务器发送USER、PASS命令登录FTP服务器
        // 使用PASV命令得到服务器监听的端口号，建立数据连接
        // 使用RETR命令下载文件
        System.out.println("下载到目录 " + localPath);
        PASV();
        writer.println("RETR " + filename);
        writer.flush();
        response = reader.readLine();
        System.out.println(response);// 150 Opening data channel for file transfer.
        File newFile = new File(localPath + "/" + filename);
        BufferedReader dataReader = new BufferedReader(new InputStreamReader(dataSocket.getInputStream()));// 读数据端口
        BufferedWriter fileWriter = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(newFile)));// 写到本地磁盘
        String line = "";
        while ((line = dataReader.readLine()) != null) {
            String lineBytes = new String(line.getBytes("ISO-8859-1"), "utf-8");
            fileWriter.write(lineBytes + "\n");
        }
        dataReader.close();
        fileWriter.close();
        System.out.println("下载完成");
        // 在下载完毕后断开数据连接并发送QUIT命令退出
    }

    void uploadFile(File file) throws IOException {
        // TODO 上传文件
        writer.println("STOR "); // filename??
        writer.flush();
        response = reader.readLine();
        System.out.println(response);// 150 Opening data channel for file transfer.

    }

    void disConnect() throws IOException {
        // 在下载完毕后断开数据连接并发送QUIT命令退出
        writer.println("QUIT");
        writer.flush();
        response = reader.readLine();
        System.out.println(response);
    }

    private String[] getSocket(String txt) {
        // 通过正则表达式，从response中获得dataSocket
        String re1 = ".*?";    // Non-greedy match on filler
        String re2 = "\\d+";    // Uninteresting: int
        String re3 = ".*?";    // Non-greedy match on filler
        String re4 = "(\\d+)";    // Integer Number 1
        String re5 = ".*?";    // Non-greedy match on filler
        String re6 = "(\\d+)";    // Integer Number 2
        String re7 = ".*?";    // Non-greedy match on filler
        String re8 = "(\\d+)";    // Integer Number 3
        String re9 = ".*?";    // Non-greedy match on filler
        String re10 = "(\\d+)";    // Integer Number 4
        String re11 = ".*?";    // Non-greedy match on filler
        String re12 = "(\\d+)";    // Integer Number 5
        String re13 = ".*?";    // Non-greedy match on filler
        String re14 = "(\\d+)";    // Integer Number 6

        Pattern p = Pattern.compile(re1 + re2 + re3 + re4 + re5 + re6 + re7 + re8 + re9 + re10 + re11 + re12 + re13 + re14, Pattern.CASE_INSENSITIVE | Pattern.DOTALL);
        Matcher m = p.matcher(txt);
        String res[] = new String[6];
        if (m.find()) {
            res[0] = m.group(1);
            res[1] = m.group(2);
            res[2] = m.group(3);
            res[3] = m.group(4);
            res[4] = m.group(5);
            res[5] = m.group(6);
        }
        return res;
    }

    private String[] getFileInfo(String txt) {
        String re1 = "(\\d+)";    // Integer Number 1
        String re2 = ".*?";    // Non-greedy match on filler
        String re3 = "((?:2|1)\\d{3}(?:-|\\/)(?:(?:0[1-9])|(?:1[0-2]))(?:-|\\/)(?:(?:0[1-9])|(?:[1-2][0-9])|(?:3[0-1]))(?:T|\\s)(?:(?:[0-1][0-9])|(?:2[0-3])):(?:[0-5][0-9]):(?:[0-5][0-9]))";    // Time Stamp 1
        String re4 = ".*?";    // Non-greedy match on filler
        String re5 = "((?:[a-z][a-z\\.\\d_]+)\\.(?:[a-z\\d]{3}))(?![\\w\\.])";    // File Name 1

        Pattern p = Pattern.compile(re1 + re2 + re3 + re4 + re5, Pattern.CASE_INSENSITIVE | Pattern.DOTALL);
        Matcher m = p.matcher(txt);
        String res[] = new String[3];
        if (m.find()) {
            res[0] = m.group(1);
            res[1] = m.group(2);
            res[2] = m.group(3);
        }
        return res;
    }

    void PASV() throws IOException {
        // 使用PASV命令得到服务器监听的端口号，建立数据连接
        writer.println("PASV");
        writer.flush();
        response = reader.readLine(); // "227 entering passive mode (h1,h2,h3,h4,p1,p2)"
        System.out.println(response);

        // 使用p1*256+p2计算出数据端口，连接数据端口，准备接收数据
        String[] socket = getSocket(response);
        String dataSocket_IP = socket[0] + '.' + socket[1] + '.' + socket[2] + '.' + socket[3];
        int dataSocket_port = Integer.parseInt(socket[4]) * 256 + Integer.parseInt(socket[5]);
        dataSocket = new Socket(dataSocket_IP, dataSocket_port);
    }
}

