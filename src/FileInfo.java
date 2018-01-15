public class FileInfo {

    private String fileName;
    private String fileDate;
    private int fileSize; // 文件大小，以字节计

    public String toString() {
        return fileName + " " + fileDate + " " + fileSize + "bytes";
    }

    FileInfo(String fileName, String fileDate, int fileSize) {
        this.fileName = fileName;
        this.fileDate = fileDate;
        this.fileSize = fileSize;
    }
}
