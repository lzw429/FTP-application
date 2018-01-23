import java.io.File;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.LinkedList;

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

    public static void getFileInfo(PrintWriter writer, String path) {
        // 获取服务器上的文件信息
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
            fileDate = new SimpleDateFormat("yyyy/MM/dd hh:mm:ss").format(new Date(file.lastModified())); // 能获取文件或文件夹的最后修改时间
            if (file.isDirectory()) {
                // 如果file是文件夹，层次遍历以获得整个文件夹的大小
                File[] filesInDir = file.listFiles();
                LinkedList<File> list = new LinkedList<>();
                int dirLength = 0;
                if (filesInDir != null && filesInDir.length != 0) {
                    for (File f : filesInDir) {
                        if (f.isDirectory()) {
                            list.add(f);
                        } else {
                            dirLength += f.length();
                        }
                    }
                    File curFile;
                    while (!list.isEmpty()) {
                        curFile = list.removeFirst(); // curFile 必定是文件夹
                        File[] fL = curFile.listFiles();
                        for (File f : fL) {
                            if (f.isDirectory())
                                list.add(f);
                            else dirLength += f.length();
                        }
                    }
                }
                writer.println(dirLength + " d " + fileDate + " " + file.getName());
                writer.flush();
            } else {
                // 如果file是文件
                writer.println(file.length() + " f " + fileDate + " " + file.getName());
                writer.flush();
            }
        }
    }
}