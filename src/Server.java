import java.io.File;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
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
                new ThreadOnServer(client, F_DIR).start();
                System.out.println(client.toString() + "  " + F_DIR);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void getFileInfo(PrintWriter writer, String path) throws UnsupportedEncodingException {
        // TODO 获取服务器上的文件信息
        File dir = new File(path);
        if (!dir.isDirectory()) {
            writer.println("500 No such file or directory.");
            writer.flush();
            System.out.println("500 No such file or directory.  " + path);
        }

        String files[] = dir.list();
        if (files == null)
            return;

        String fileDate;
        for (String fileName : files) {
            File file = new File(path + "/" + fileName);
            fileDate = new SimpleDateFormat("yyyy/MM/dd hh:mm:ss").format(new Date(file.lastModified()));
            String filename = new String(file.getName().getBytes("utf-8"), "ISO-8859-1");
            if (file.isDirectory()) { // file是文件夹
                File[] filesInDir = file.listFiles();
                int dirLength = 0;
                assert filesInDir != null;
                for (int i = 0; i != filesInDir.length; i++) {
                    dirLength += filesInDir[i].length();
                }
                writer.println(dirLength + " " + fileDate + " directory:" + filename);
                writer.flush();
            } else { // file是文件
                writer.println(file.length() + " " + fileDate + " " + filename);
                writer.flush();
            }
        }
    }
}

