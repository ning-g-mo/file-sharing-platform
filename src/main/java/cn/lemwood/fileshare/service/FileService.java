package cn.lemwood.fileshare.service;

import cn.lemwood.fileshare.config.FileUploadConfig;
import cn.lemwood.fileshare.entity.FileInfo;
import cn.lemwood.fileshare.exception.FileShareException;
import cn.lemwood.fileshare.repository.FileInfoRepository;
import org.apache.commons.io.FilenameUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.PostConstruct;
import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * 文件服务类
 * 
 * @author lemwood
 */
@Service
public class FileService {

    private static final Logger logger = LoggerFactory.getLogger(FileService.class);

    @Autowired
    private FileInfoRepository fileInfoRepository;

    @Autowired
    private FileUploadConfig fileUploadConfig;

    private Path uploadPath;

    @PostConstruct
    public void init() {
        try {
            uploadPath = Paths.get(fileUploadConfig.getUpload().getPath()).toAbsolutePath().normalize();
            Files.createDirectories(uploadPath);
            logger.info("文件上传目录初始化成功: {}", uploadPath);
        } catch (IOException e) {
            logger.error("无法创建上传目录: {}", e.getMessage());
            throw new RuntimeException("无法创建上传目录", e);
        }
    }

    /**
     * 上传文件
     */
    public FileInfo uploadFile(MultipartFile file, String uploaderIp) throws IOException {
        // 验证文件
        validateFile(file);

        // 生成文件key和存储名称
        String fileKey = UUID.randomUUID().toString().replace("-", "");
        String originalName = file.getOriginalFilename();
        String extension = FilenameUtils.getExtension(originalName);
        String storedName = fileKey + "." + extension;

        // 保存文件到磁盘
        Path targetPath = uploadPath.resolve(storedName);
        Files.copy(file.getInputStream(), targetPath, StandardCopyOption.REPLACE_EXISTING);

        // 创建文件信息记录
        FileInfo fileInfo = new FileInfo(
                fileKey,
                originalName,
                storedName,
                file.getSize(),
                file.getContentType(),
                targetPath.toString(),
                uploaderIp
        );

        // 保存到数据库
        fileInfo = fileInfoRepository.save(fileInfo);
        logger.info("文件上传成功: {} -> {}", originalName, fileKey);

        return fileInfo;
    }

    /**
     * 根据文件key获取文件信息
     */
    public Optional<FileInfo> getFileInfo(String fileKey) {
        return fileInfoRepository.findByFileKey(fileKey);
    }

    /**
     * 下载文件
     */
    public Resource downloadFile(String fileKey) throws IOException {
        Optional<FileInfo> fileInfoOpt = fileInfoRepository.findByFileKey(fileKey);
        if (!fileInfoOpt.isPresent()) {
            throw new FileShareException(
                FileShareException.ErrorCodes.FILE_NOT_FOUND,
                "文件不存在"
            );
        }

        FileInfo fileInfo = fileInfoOpt.get();
        
        // 检查文件是否过期
        if (fileInfo.isExpired()) {
            throw new FileShareException(
                FileShareException.ErrorCodes.FILE_EXPIRED,
                "文件已过期"
            );
        }

        // 获取文件资源
        Path filePath = Paths.get(fileInfo.getFilePath());
        Resource resource = new UrlResource(filePath.toUri());

        if (!resource.exists() || !resource.isReadable()) {
            throw new FileShareException(
                FileShareException.ErrorCodes.FILE_NOT_READABLE,
                "文件不存在或无法读取"
            );
        }

        // 增加下载次数
        fileInfo.incrementDownloadCount();
        fileInfoRepository.save(fileInfo);

        logger.info("文件下载: {} ({})", fileInfo.getOriginalName(), fileKey);
        return resource;
    }

    /**
     * 删除文件
     */
    public boolean deleteFile(String fileKey) {
        Optional<FileInfo> fileInfoOpt = fileInfoRepository.findByFileKey(fileKey);
        if (!fileInfoOpt.isPresent()) {
            return false;
        }

        FileInfo fileInfo = fileInfoOpt.get();
        
        try {
            // 删除物理文件
            Path filePath = Paths.get(fileInfo.getFilePath());
            Files.deleteIfExists(filePath);
            
            // 删除数据库记录
            fileInfoRepository.delete(fileInfo);
            
            logger.info("文件删除成功: {} ({})", fileInfo.getOriginalName(), fileKey);
            return true;
        } catch (IOException e) {
            logger.error("删除文件失败: {}", e.getMessage());
            return false;
        }
    }

    /**
     * 获取用户上传的文件列表
     */
    public List<FileInfo> getUserFiles(String uploaderIp) {
        return fileInfoRepository.findByUploaderIpOrderByUploadTimeDesc(uploaderIp);
    }

    /**
     * 获取最近上传的文件
     */
    public List<FileInfo> getRecentFiles() {
        return fileInfoRepository.findTop10ByOrderByUploadTimeDesc();
    }

    /**
     * 清理过期文件
     */
    public int cleanupExpiredFiles() {
        List<FileInfo> expiredFiles = fileInfoRepository.findExpiredFiles(LocalDateTime.now());
        int deletedCount = 0;

        for (FileInfo fileInfo : expiredFiles) {
            try {
                // 删除物理文件
                Path filePath = Paths.get(fileInfo.getFilePath());
                Files.deleteIfExists(filePath);
                
                // 删除数据库记录
                fileInfoRepository.delete(fileInfo);
                deletedCount++;
                
                logger.debug("清理过期文件: {} ({})", fileInfo.getOriginalName(), fileInfo.getFileKey());
            } catch (IOException e) {
                logger.error("清理过期文件失败: {}", e.getMessage());
            }
        }

        if (deletedCount > 0) {
            logger.info("清理过期文件完成，共删除 {} 个文件", deletedCount);
        }
        
        return deletedCount;
    }

    /**
     * 获取系统统计信息
     */
    public SystemStats getSystemStats() {
        long totalFiles = fileInfoRepository.count();
        Long totalSize = fileInfoRepository.getTotalFileSize();
        return new SystemStats(totalFiles, totalSize != null ? totalSize : 0L);
    }

    /**
     * 验证上传文件
     */
    private void validateFile(MultipartFile file) {
        if (file.isEmpty()) {
            throw new FileShareException(
                FileShareException.ErrorCodes.EMPTY_FILE,
                "文件不能为空"
            );
        }

        // 检查文件大小
        if (file.getSize() > fileUploadConfig.getUpload().getMaxSize()) {
            throw new FileShareException(
                FileShareException.ErrorCodes.FILE_TOO_LARGE,
                "文件大小超过限制"
            );
        }

        // 检查文件类型
        String originalName = file.getOriginalFilename();
        if (originalName == null || originalName.trim().isEmpty()) {
            throw new FileShareException(
                FileShareException.ErrorCodes.INVALID_FILE_NAME,
                "文件名不能为空"
            );
        }

        String extension = "." + FilenameUtils.getExtension(originalName).toLowerCase();
        if (!fileUploadConfig.getUpload().getAllowedTypesList().contains(extension)) {
            throw new FileShareException(
                FileShareException.ErrorCodes.INVALID_FILE_TYPE,
                "不支持的文件类型: " + extension
            );
        }
    }

    /**
     * 系统统计信息类
     */
    public static class SystemStats {
        private long totalFiles;
        private long totalSize;

        public SystemStats(long totalFiles, long totalSize) {
            this.totalFiles = totalFiles;
            this.totalSize = totalSize;
        }

        public long getTotalFiles() {
            return totalFiles;
        }

        public long getTotalSize() {
            return totalSize;
        }
    }
}