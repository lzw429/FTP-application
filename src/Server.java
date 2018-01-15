import java.io.File;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.text.SimpleDateFormat;
import java.util.Date;

public class Server {
    public static void main(String[] args) {
        final String F_DIR = "C:\\Users\\舒意恒\\Documents\\GitHub\\network-application\\server_dir";
        final int PORT = 4290;
        System.out.println("Server port: " + PORT);
        try {
            ServerSocket ss = new ServerSocket(PORT);
            while (true) {
                // 接受客户端请求
                Socket client = ss.accept();
                // 创建服务线程
                new ClientThread(client, F_DIR).start();
                System.out.println(client.toString() + "  " + F_DIR);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void getFileInfo(PrintWriter writer, String path) {
        // TODO 获取服务器上的文件信息
        File dir = new File(path);
        if (!dir.isDirectory()) {
            writer.println("500 No such file or directory.");
            System.out.println("500 No such file or directory.  " + path);
        }

        File files[] = dir.listFiles();
        String fileDate;
        for (File file : files) {
            fileDate = new SimpleDateFormat("yyyy/MM/dd hh:mm:ss").format(new Date(file.lastModified()));
            if (file.isDirectory()) {
                writer.println(fileDate + " " + file.getName());
            } else {
                writer.println(file.length() + " " + fileDate + " " + file.getName());
            }
            writer.flush();
        }
    }
}
