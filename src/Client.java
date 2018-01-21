import sun.awt.image.ImageWatched;

import java.io.*;
import java.net.Socket;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
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
        // 获得文件列表，目录为服务器当前工作目录，如需更改可通过changeDir方法
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
            String[] fileInfo = getFileInfo(line);
            int fileType = 0;
            try {
                if (fileInfo[1].equals("f"))
                    fileType = FileInfo.FILE_TYPE;
                else if (fileInfo[1].equals("d"))
                    fileType = FileInfo.DIR_TYPE;
                FileInfo file = new FileInfo(Integer.parseInt(fileInfo[0]), fileType, fileInfo[2], fileInfo[3]);
                files.add(file);
                System.out.println(file);// 标准输出文件信息
            } catch (NumberFormatException e) {
                e.printStackTrace();
            }
        }
        dataReader.close();
        response = reader.readLine();
        System.out.println(response); // 226 Transfer OK
        return files;
    }

    void downloadFile(String filename, String localPath) throws IOException {
        // 下载文件
        // 使用PASV命令得到服务器监听的端口号，建立数据连接
        System.out.println("下载到目录 " + localPath);
        PASV();// dataSocket

        // 如果本地存在该文件，使用REST命令指定偏移量，实现下载断点续传
        File downloaded = new File(localPath + "/" + filename);
        long size = 0;
        if (downloaded.exists() && downloaded.isFile())
            size = downloaded.length();
        writer.println("REST " + size);// 将size告知服务器，即使size为0；服务器只会传来size后的部分
        writer.flush();
        response = reader.readLine();
        System.out.println(response);

        // 使用RETR命令下载文件
        writer.println("RETR " + filename);
        writer.flush();
        response = reader.readLine();
        System.out.println(response);// 150 Opening data channel for file transfer.

        BufferedInputStream dataReader = new BufferedInputStream(dataSocket.getInputStream());// 读数据端口
        RandomAccessFile fileWriter = new RandomAccessFile(downloaded, "rw"); // 写到本地磁盘
        byte[] buffer = new byte[1024];
        int len;
        while ((len = dataReader.read(buffer)) != -1) {
            fileWriter.write(buffer, 0, len);
        }
        // 在下载完毕后断开数据连接
        dataReader.close();
        fileWriter.close();
        System.out.println(filename + " 下载完成");
    }

    void downloadDir(FileInfo dir, String localPath, String serverPath) throws IOException {
        // 下载文件夹
        // 参数 dir是将下载的文件夹，localPath是下载到本地的路径，serverPath是文件夹在服务器的路径
        System.out.println("下载到目录 " + localPath);

        HashMap<FileInfo, String> pathMap = new HashMap<>();
        File dirFile = new File(localPath + "/" + dir.getFileName());
        dirFile.mkdir(); // 创建该目录
        pathMap.put(dir, "/" + dir.getFileName());

        LinkedList<FileInfo> list = new LinkedList<>();
        changeDir(serverPath + pathMap.get(dir));
        PASV();
        ArrayList<FileInfo> fileInfo = getFileDir();
        for (FileInfo f : fileInfo) {
            if (f.getType() == FileInfo.DIR_TYPE) // f是文件夹类型
            {
                list.add(f);
                pathMap.put(f, pathMap.get(dir) + "/" + f.getFileName());
            } else // f是文件类型
            {
                downloadFile(f.getFileName(), localPath + pathMap.get(dir));
            }
        }
        FileInfo curFileInfo;
        while (!list.isEmpty()) {
            curFileInfo = list.removeFirst(); // 队列中的一定是文件夹
            File curFile = new File(localPath + pathMap.get(curFileInfo));
            curFile.mkdir();

            changeDir(serverPath + pathMap.get(curFileInfo));
            PASV();
            fileInfo = getFileDir();
            for (FileInfo f : fileInfo) {
                if (f.getType() == FileInfo.DIR_TYPE) // 文件夹被放入队列
                {
                    list.add(f);
                    pathMap.put(f, pathMap.get(curFileInfo) + "/" + f.getFileName());
                } else // 文件直接下载
                {
                    downloadFile(f.getFileName(), localPath + pathMap.get(curFileInfo));
                }
            }
        }
        changeDir(serverPath);
        System.out.println("文件夹 " + dir.getFileName() + " 下载完成");
    }

    void uploadFile(File file) throws IOException {
        // 上传文件
        String fileName = file.getName();
        System.out.println("上传文件 " + fileName);
        PASV(); // dataSocket

        // 使用SIZE命令获得文件大小，以实现上传的断点续传
        writer.println("SIZE " + fileName);
        writer.flush();
        response = reader.readLine();
        long size = getSize(response); // 获得在服务器上的文件大小
        System.out.println("The size of " + fileName + " on the server is " + size + " bytes.");

        writer.println("STOR " + fileName);
        writer.flush();
        response = reader.readLine();
        System.out.println(response); // 150 Opening data channel for file transfer.

        BufferedInputStream fileReader = new BufferedInputStream(new FileInputStream(file));// 读本地文件
        BufferedOutputStream dataWriter = new BufferedOutputStream(dataSocket.getOutputStream()); // 写到服务器
        long skipSize = fileReader.skip(size); // 上传断点续传
        System.out.println(skipSize + " bytes has been actually skipped.");
        byte[] buffer = new byte[1024];
        int len;
        while ((len = fileReader.read(buffer)) != -1) {
            dataWriter.write(buffer, 0, len);
        }
        // 在上传完毕后断开数据连接
        dataWriter.close();
        fileReader.close();
        System.out.println(fileName + " 上传完成");
    }

    void uploadDir(File dir, String dirPath) throws IOException {
        // 上传文件夹
        // 参数 dir：被上传的文件夹；dirPath：文件夹dir在服务器的路径
        HashMap<File, String> pathMap = new HashMap<>(); // 存储每个文件的相对于服务器上dirPath的路径
        String dirName = dir.getName();
        pathMap.put(dir, "/" + dirName);
        System.out.println("上传文件夹 " + dirName);

        writer.println("MKD " + dirName); // 创建文件夹
        writer.flush();
        response = reader.readLine(); // 响应创建文件夹的结果
        System.out.println(response);

        LinkedList<File> list = new LinkedList<File>();
        File[] files = dir.listFiles();
        for (File file : files) {
            if (file.isDirectory()) { // 文件夹被放入队列
                list.add(file);
                pathMap.put(file, pathMap.get(dir) + "/" + file.getName());
            } else { // 文件直接上传
                changeDir(dirPath + pathMap.get(file.getParentFile()));
                uploadFile(new File(file.getPath()));
            }
        }
        File curFile;
        while (!list.isEmpty()) {
            curFile = list.removeFirst(); // 队列中的一定是文件夹
            changeDir(dirPath + pathMap.get(curFile.getParentFile()));
            writer.println("MKD " + curFile.getName()); // 创建文件夹
            writer.flush();
            response = reader.readLine(); // 响应创建文件夹的结果
            System.out.println(response);

            changeDir(dirPath + pathMap.get(curFile));
            files = curFile.listFiles(); // files是文件夹curFile的子结点
            for (File file : files) {
                if (file.isDirectory()) // 文件夹被放入队列
                {
                    list.add(file);
                    pathMap.put(file, pathMap.get(curFile) + "/" + file.getName());
                } else // 文件直接上传
                {
                    uploadFile(new File(file.getPath()));
                }
            }
        }
        changeDir(dirPath);
        System.out.println("文件夹 " + dirName + " 上传完成");
    }

    void disConnect() throws IOException {
        // 关闭控制连接
        writer.println("QUIT");
        writer.flush();
        response = reader.readLine();
        System.out.println(response);
    }

    ArrayList<FileInfo> changeDir(String path) throws IOException {
        // 改变服务器工作目录
        // 参数path：要转到的服务器目录
        // 返回转到后的目录的文件列表
        writer.println("CWD " + path);
        writer.flush();
        response = reader.readLine();
        System.out.println(response);
        String status = response.substring(0, 3);
        if (status.equals("250")) {
            PASV();
            return getFileDir();
        } else
            return new ArrayList<FileInfo>();
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
        // 通过正则表达式，从response获得文件信息
        String re1 = "(\\d+)";    // Integer 1
        String re2 = "( )";    // White Space 1
        String re3 = "(.)";    // Any Single Character 1
        String re4 = "( )";    // White Space 2
        String re5 = "((?:2|1)\\d{3}(?:-|\\/)(?:(?:0[1-9])|(?:1[0-2]))(?:-|\\/)(?:(?:0[1-9])|(?:[1-2][0-9])|(?:3[0-1]))(?:T|\\s)(?:(?:[0-1][0-9])|(?:2[0-3])):(?:[0-5][0-9]):(?:[0-5][0-9]))";    // Time Stamp 1
        String re6 = "( )";    // White Space 3
        String re7 = "(.*)";    // Filename or Directory name

        Pattern p = Pattern.compile(re1 + re2 + re3 + re4 + re5 + re6 + re7, Pattern.CASE_INSENSITIVE | Pattern.DOTALL);
        Matcher m = p.matcher(txt);
        String res[] = new String[4];
        if (m.find()) {
            res[0] = m.group(1);
            res[1] = m.group(3);
            res[2] = m.group(5);
            res[3] = m.group(7);
        }
        return res;
    }

    private int getSize(String txt) {
        // 通过正则表达式，从response中获取size
        String re1 = "(213)";    // Integer Number 1
        String re2 = "( )";    // White Space 1
        String re3 = "(\\d+)";    // Integer Number 2

        Pattern p = Pattern.compile(re1 + re2 + re3, Pattern.CASE_INSENSITIVE | Pattern.DOTALL);
        Matcher m = p.matcher(txt);
        if (m.find()) {
            return Integer.parseInt(m.group(3));
        }
        return 0;
    }

    private void PASV() throws IOException {
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
