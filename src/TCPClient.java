import java.io.*;
import java.net.Socket;

public class TCPClient {

    void getFileDir(String host, int port) throws IOException {
        // TODO 获得文件列表
        // 客户端和FTP服务器建立Socket连接
        Socket socket = new Socket(host, port);
        Reader reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        Writer writer = new PrintWriter(new OutputStreamWriter(socket.getOutputStream()));
        // 向服务器发送USER、PASS命令登录FTP服务器
        writer.write("USER root\r\n");
        writer.flush();
        // 使用PASV命令得到服务器监听的端口号，建立数据连接
        writer.write("PASV");
        writer.flush();
        String response = reader.readLine(); // 227 entering passive mode (h1,h2,h3,h4,p1,p2)
        // 使用p1*256+p2计算出数据端口，连接数据端口，准备接收数据

        Socket dataSocket = new Socket(ip, port1);
        // 使用List命令获得文件列表
        // 从数据端口中接收数据
        // 在下载完毕后断开数据连接并发送QUIT命令退出

    }

    void downloadFile() {
        // TODO 下载文件
        // 客户端和FTP服务器建立Socket连接
        // 向服务器发送USER、PASS命令登录FTP服务器
        // 使用PASV命令得到服务器监听的端口号，建立数据连接
        // 使用RETR命令下载文件
    }

    void uploadFile() {
        // TODO 上传文件
    }
}
