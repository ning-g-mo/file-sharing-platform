package cn.lemwood.fileshare.entity;

import javax.persistence.*;
import java.time.LocalDateTime;

/**
 * 文件信息实体类
 * 
 * @author lemwood
 */
@Entity
@Table(name = "file_info")
public class FileInfo {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * 文件唯一标识符
     */
    @Column(name = "file_key", unique = true, nullable = false)
    private String fileKey;

    /**
     * 原始文件名
     */
    @Column(name = "original_name", nullable = false)
    private String originalName;

    /**
     * 存储文件名
     */
    @Column(name = "stored_name", nullable = false)
    private String storedName;

    /**
     * 文件大小（字节）
     */
    @Column(name = "file_size")
    private Long fileSize;

    /**
     * 文件类型
     */
    @Column(name = "content_type")
    private String contentType;

    /**
     * 文件路径
     */
    @Column(name = "file_path")
    private String filePath;

    /**
     * 上传时间
     */
    @Column(name = "upload_time")
    private LocalDateTime uploadTime;

    /**
     * 过期时间
     */
    @Column(name = "expire_time")
    private LocalDateTime expireTime;

    /**
     * 下载次数
     */
    @Column(name = "download_count")
    private Integer downloadCount = 0;

    /**
     * 上传者IP
     */
    @Column(name = "uploader_ip")
    private String uploaderIp;

    // 构造函数
    public FileInfo() {}

    public FileInfo(String fileKey, String originalName, String storedName, Long fileSize, 
                   String contentType, String filePath, String uploaderIp) {
        this.fileKey = fileKey;
        this.originalName = originalName;
        this.storedName = storedName;
        this.fileSize = fileSize;
        this.contentType = contentType;
        this.filePath = filePath;
        this.uploaderIp = uploaderIp;
        this.uploadTime = LocalDateTime.now();
        this.expireTime = LocalDateTime.now().plusHours(24);
    }

    // Getter和Setter方法
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getFileKey() {
        return fileKey;
    }

    public void setFileKey(String fileKey) {
        this.fileKey = fileKey;
    }

    public String getOriginalName() {
        return originalName;
    }

    public void setOriginalName(String originalName) {
        this.originalName = originalName;
    }

    public String getStoredName() {
        return storedName;
    }

    public void setStoredName(String storedName) {
        this.storedName = storedName;
    }

    public Long getFileSize() {
        return fileSize;
    }

    public void setFileSize(Long fileSize) {
        this.fileSize = fileSize;
    }

    public String getContentType() {
        return contentType;
    }

    public void setContentType(String contentType) {
        this.contentType = contentType;
    }

    public String getFilePath() {
        return filePath;
    }

    public void setFilePath(String filePath) {
        this.filePath = filePath;
    }

    public LocalDateTime getUploadTime() {
        return uploadTime;
    }

    public void setUploadTime(LocalDateTime uploadTime) {
        this.uploadTime = uploadTime;
    }

    public LocalDateTime getExpireTime() {
        return expireTime;
    }

    public void setExpireTime(LocalDateTime expireTime) {
        this.expireTime = expireTime;
    }

    public Integer getDownloadCount() {
        return downloadCount;
    }

    public void setDownloadCount(Integer downloadCount) {
        this.downloadCount = downloadCount;
    }

    public String getUploaderIp() {
        return uploaderIp;
    }

    public void setUploaderIp(String uploaderIp) {
        this.uploaderIp = uploaderIp;
    }

    /**
     * 检查文件是否已过期
     */
    public boolean isExpired() {
        return LocalDateTime.now().isAfter(expireTime);
    }

    /**
     * 增加下载次数
     */
    public void incrementDownloadCount() {
        this.downloadCount++;
    }
}