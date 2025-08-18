package cn.lemwood.fileshare.service;

import cn.lemwood.fileshare.config.FileUploadConfig;
import cn.lemwood.fileshare.entity.FileInfo;
import cn.lemwood.fileshare.repository.FileInfoRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.File;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 系统监控服务
 * 提供系统状态监控和统计信息
 */
@Service
public class SystemMonitorService {
    
    private static final Logger logger = LoggerFactory.getLogger(SystemMonitorService.class);
    
    @Autowired
    private FileInfoRepository fileInfoRepository;
    
    @Autowired
    private FileUploadConfig fileUploadConfig;
    
    /**
     * 获取系统状态信息
     */
    public Map<String, Object> getSystemStatus() {
        Map<String, Object> status = new HashMap<>();
        
        try {
            // 基本统计信息
            long totalFiles = fileInfoRepository.count();
            long totalSize = fileInfoRepository.getTotalFileSize();
            
            // 过期文件统计
            List<FileInfo> expiredFiles = fileInfoRepository.findExpiredFiles(LocalDateTime.now());
            long expiredCount = expiredFiles.size();
            long expiredSize = expiredFiles.stream().mapToLong(FileInfo::getFileSize).sum();
            
            // 磁盘空间信息
            File uploadDir = new File(fileUploadConfig.getUploadPath());
            long totalSpace = uploadDir.getTotalSpace();
            long freeSpace = uploadDir.getFreeSpace();
            long usedSpace = totalSpace - freeSpace;
            
            // 系统信息
            Runtime runtime = Runtime.getRuntime();
            long maxMemory = runtime.maxMemory();
            long totalMemory = runtime.totalMemory();
            long freeMemory = runtime.freeMemory();
            long usedMemory = totalMemory - freeMemory;
            
            status.put("timestamp", LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
            status.put("status", "healthy");
            
            // 文件统计
            Map<String, Object> fileStats = new HashMap<>();
            fileStats.put("totalFiles", totalFiles);
            fileStats.put("totalSize", totalSize);
            fileStats.put("totalSizeFormatted", formatFileSize(totalSize));
            fileStats.put("expiredFiles", expiredCount);
            fileStats.put("expiredSize", expiredSize);
            fileStats.put("expiredSizeFormatted", formatFileSize(expiredSize));
            status.put("files", fileStats);
            
            // 磁盘空间
            Map<String, Object> diskStats = new HashMap<>();
            diskStats.put("totalSpace", totalSpace);
            diskStats.put("totalSpaceFormatted", formatFileSize(totalSpace));
            diskStats.put("freeSpace", freeSpace);
            diskStats.put("freeSpaceFormatted", formatFileSize(freeSpace));
            diskStats.put("usedSpace", usedSpace);
            diskStats.put("usedSpaceFormatted", formatFileSize(usedSpace));
            diskStats.put("usagePercentage", Math.round((double) usedSpace / totalSpace * 100));
            status.put("disk", diskStats);
            
            // 内存信息
            Map<String, Object> memoryStats = new HashMap<>();
            memoryStats.put("maxMemory", maxMemory);
            memoryStats.put("maxMemoryFormatted", formatFileSize(maxMemory));
            memoryStats.put("totalMemory", totalMemory);
            memoryStats.put("totalMemoryFormatted", formatFileSize(totalMemory));
            memoryStats.put("usedMemory", usedMemory);
            memoryStats.put("usedMemoryFormatted", formatFileSize(usedMemory));
            memoryStats.put("freeMemory", freeMemory);
            memoryStats.put("freeMemoryFormatted", formatFileSize(freeMemory));
            memoryStats.put("usagePercentage", Math.round((double) usedMemory / maxMemory * 100));
            status.put("memory", memoryStats);
            
            // 配置信息
            Map<String, Object> config = new HashMap<>();
            config.put("uploadPath", fileUploadConfig.getUploadPath());
            config.put("maxFileSize", fileUploadConfig.getMaxFileSize());
            config.put("maxFileSizeFormatted", formatFileSize(fileUploadConfig.getMaxFileSize()));
            config.put("allowedTypes", fileUploadConfig.getAllowedTypes());
            config.put("cleanupInterval", fileUploadConfig.getCleanupInterval());
            config.put("fileRetentionHours", fileUploadConfig.getFileRetentionHours());
            status.put("config", config);
            
        } catch (Exception e) {
            logger.error("获取系统状态失败", e);
            status.put("status", "error");
            status.put("error", e.getMessage());
        }
        
        return status;
    }
    
    /**
     * 检查系统健康状况
     */
    public boolean isSystemHealthy() {
        try {
            // 检查数据库连接
            fileInfoRepository.count();
            
            // 检查上传目录
            File uploadDir = new File(fileUploadConfig.getUploadPath());
            if (!uploadDir.exists() || !uploadDir.canWrite()) {
                return false;
            }
            
            // 检查磁盘空间（至少保留1GB空间）
            long freeSpace = uploadDir.getFreeSpace();
            if (freeSpace < 1024 * 1024 * 1024) {
                logger.warn("磁盘空间不足，剩余空间: {}", formatFileSize(freeSpace));
                return false;
            }
            
            return true;
        } catch (Exception e) {
            logger.error("系统健康检查失败", e);
            return false;
        }
    }
    
    /**
     * 手动清理过期文件
     */
    public int cleanExpiredFiles() {
        logger.info("开始手动清理过期文件...");
        
        try {
            List<FileInfo> expiredFiles = fileInfoRepository.findExpiredFiles(LocalDateTime.now());
            int deletedCount = 0;
            
            for (FileInfo fileInfo : expiredFiles) {
                try {
                    // 删除物理文件
                    File file = new File(fileInfo.getFilePath());
                    if (file.exists() && file.delete()) {
                        logger.debug("删除物理文件: {}", fileInfo.getFilePath());
                    }
                    
                    // 删除数据库记录
                    fileInfoRepository.delete(fileInfo);
                    deletedCount++;
                    
                } catch (Exception e) {
                    logger.error("删除文件失败: {}", fileInfo.getOriginalName(), e);
                }
            }
            
            logger.info("手动清理完成，共删除 {} 个过期文件", deletedCount);
            return deletedCount;
            
        } catch (Exception e) {
            logger.error("手动清理过期文件失败", e);
            throw new RuntimeException("清理过期文件失败: " + e.getMessage());
        }
    }
    
    /**
     * 优化存储空间
     */
    public Map<String, Object> optimizeStorage() {
        logger.info("开始优化存储空间...");
        
        Map<String, Object> result = new HashMap<>();
        long totalFreedSpace = 0;
        int cleanedFiles = 0;
        
        try {
            // 1. 清理过期文件
            int expiredDeleted = cleanExpiredFiles();
            cleanedFiles += expiredDeleted;
            
            // 2. 清理孤儿文件（数据库中不存在但物理文件存在）
            File uploadDir = new File(fileUploadConfig.getUploadPath());
            if (uploadDir.exists() && uploadDir.isDirectory()) {
                File[] files = uploadDir.listFiles();
                if (files != null) {
                    for (File file : files) {
                        if (file.isFile()) {
                            // 检查数据库中是否存在对应记录
                            String fileName = file.getName();
                            boolean existsInDb = fileInfoRepository.findByStoredName(fileName).isPresent();
                            
                            if (!existsInDb) {
                                long fileSize = file.length();
                                if (file.delete()) {
                                    totalFreedSpace += fileSize;
                                    cleanedFiles++;
                                    logger.debug("删除孤儿文件: {}", fileName);
                                }
                            }
                        }
                    }
                }
            }
            
            // 3. 清理空目录
            cleanEmptyDirectories(uploadDir);
            
            result.put("cleanedFiles", cleanedFiles);
            result.put("freedSpace", formatFileSize(totalFreedSpace));
            result.put("freedSpaceBytes", totalFreedSpace);
            
            logger.info("存储优化完成，清理了 {} 个文件，释放了 {} 空间", cleanedFiles, formatFileSize(totalFreedSpace));
            
        } catch (Exception e) {
            logger.error("存储优化失败", e);
            throw new RuntimeException("存储优化失败: " + e.getMessage());
        }
        
        return result;
    }
    
    /**
     * 清理空目录
     */
    private void cleanEmptyDirectories(File directory) {
        if (directory == null || !directory.isDirectory()) {
            return;
        }
        
        File[] files = directory.listFiles();
        if (files != null) {
            for (File file : files) {
                if (file.isDirectory()) {
                    cleanEmptyDirectories(file);
                    // 如果目录为空，删除它
                    if (file.list() != null && file.list().length == 0) {
                        if (file.delete()) {
                            logger.debug("删除空目录: {}", file.getPath());
                        }
                    }
                }
            }
        }
    }
    
    /**
     * 格式化文件大小
     */
    private String formatFileSize(long size) {
        if (size <= 0) return "0 B";
        
        String[] units = {"B", "KB", "MB", "GB", "TB"};
        int digitGroups = (int) (Math.log10(size) / Math.log10(1024));
        
        return String.format("%.2f %s", 
            size / Math.pow(1024, digitGroups), 
            units[digitGroups]);
    }
}