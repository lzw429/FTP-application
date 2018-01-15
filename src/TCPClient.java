import java.io.*;
import java.net.Socket;

public class TCPClient {

    void getFileDir(String host, int port) throws IOException {
        // TODO 获得文件列表
        // 客户端和FTP服务器建立Socket连接
        Socket socket = new Socket(host, port);
        BufferedReader reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        PrintWriter writer = new PrintWriter(new OutputStreamWriter(socket.getOutputStream()));
        // 向服务器发送USER、PASS命令登录FTP服务器
        writer.println("USER root");
        writer.flush();
        String response = reader.readLine();
        writer.println("PASS root");
        writer.flush();
        response = reader.readLine();
        // 使用PASV命令得到服务器监听的端口号，建立数据连接
        writer.println("PASV");
        writer.flush();
        response = reader.readLine(); // "227 entering passive mode (h1,h2,h3,h4,p1,p2)"
        // 使用p1*256+p2计算出数据端口，连接数据端口，准备接收数据
        String ip = "";
        int port1 = 0;
        Socket dataSocket = new Socket(ip, port1);
        // 使用List命令获得文件列表
        writer.println("List");
        writer.flush();
        response = reader.readLine();
        // 从数据端口中接收数据
        DataInputStream dis = new DataInputStream(dataSocket.getInputStream());
        String s = "";
        while ((s = dis.readLine()) != null) {
            String l = new String(s.getBytes("ISO-8859-1"), "utf-8");
            System.out.println(l);
        }
        // 在下载完毕后断开数据连接并发送QUIT命令退出
        writer.println("QUIT");
        writer.flush();
        response = reader.readLine();
    }

    void downloadFile() {
        // TODO 下载文件
        // 客户端和FTP服务器建立Socket连接
        // 向服务器发送USER、PASS命令登录FTP服务器
        // 使用PASV命令得到服务器监听的端口号，建立数据连接
        // 使用RETR命令下载文件
        // 从数据端口中接收数据，保存到本地磁盘
        // 在下载完毕后断开数据连接并发送QUIT命令退出
    }

    void uploadFile() {
        // TODO 上传文件
    }
}
