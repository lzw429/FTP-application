public class FileInfo {

    private String fileName;
    private String fileDate;
    private int fileSize; // 文件大小，以字节计

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

    // 成员方法
    public String toString() {
        return fileName + " " + fileDate + " " + fileSize + "bytes";
    }

    // 构造方法
    FileInfo(String fileName, String fileDate, int fileSize) {
        this.fileName = fileName;
        this.fileDate = fileDate;
        this.fileSize = fileSize;
    }
}
