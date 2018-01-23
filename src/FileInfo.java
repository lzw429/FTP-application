/**
 * 为FTP客户端描述文件信息
 *
 * @author 舒意恒
 * @see Client
 */

public class FileInfo {

    private String fileName;
    private String fileDate;
    private int fileSize; // 文件大小，以字节计
    private int type;
    public static final int FILE_TYPE = 0; // 文件类型
    public static final int DIR_TYPE = 1; // 文件夹类型

    // Getter and Setter
    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public String getFileDate() {
        return fileDate;
    }

    public void setFileDate(String fileDate) {
        this.fileDate = fileDate;
    }

    public int getFileSize() {
        return fileSize;
    }

    public void setFileSize(int fileSize) {
        this.fileSize = fileSize;
    }

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }

    // 成员方法
    public String toString() {
        if (type == FILE_TYPE)
            return "[文件]" + fileName + " " + fileDate + " " + fileSize + "字节";
        return "[文件夹]" + fileName + " " + fileDate + " " + fileSize + "字节";
    }

    // 构造方法
    FileInfo(int fileSize, int type, String fileDate, String fileName) {
        this.fileName = fileName;
        this.fileDate = fileDate;
        this.fileSize = fileSize;
        this.type = type;
    }
}
