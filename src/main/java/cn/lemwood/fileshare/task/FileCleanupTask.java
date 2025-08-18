package cn.lemwood.fileshare.task;

import cn.lemwood.fileshare.service.FileService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * 文件清理定时任务
 * 定期清理过期文件
 */
@Component
public class FileCleanupTask {
    
    private static final Logger logger = LoggerFactory.getLogger(FileCleanupTask.class);
    
    @Autowired
    private FileService fileService;
    
    /**
     * 每小时执行一次文件清理任务
     * cron表达式: 0 0 * * * ? (每小时的0分0秒执行)
     */
    @Scheduled(cron = "0 0 * * * ?")
    public void cleanupExpiredFiles() {
        logger.info("开始执行文件清理任务...");
        
        try {
            int deletedCount = fileService.cleanupExpiredFiles();
            logger.info("文件清理任务完成，共删除 {} 个过期文件", deletedCount);
        } catch (Exception e) {
            logger.error("文件清理任务执行失败", e);
        }
    }
    
    /**
     * 每30分钟执行一次文件清理任务（备用）
     * 确保文件能够及时清理
     */
    @Scheduled(fixedRate = 30 * 60 * 1000) // 30分钟
    public void cleanupExpiredFilesBackup() {
        logger.debug("执行备用文件清理任务...");
        
        try {
            int deletedCount = fileService.cleanupExpiredFiles();
            if (deletedCount > 0) {
                logger.info("备用清理任务完成，共删除 {} 个过期文件", deletedCount);
            }
        } catch (Exception e) {
            logger.error("备用文件清理任务执行失败", e);
        }
    }
    
    /**
     * 应用启动后延迟5分钟执行一次清理
     * 清理应用重启期间可能积累的过期文件
     */
    @Scheduled(initialDelay = 5 * 60 * 1000, fixedDelay = Long.MAX_VALUE)
    public void initialCleanup() {
        logger.info("执行应用启动后的初始文件清理...");
        
        try {
            int deletedCount = fileService.cleanupExpiredFiles();
            logger.info("初始文件清理完成，共删除 {} 个过期文件", deletedCount);
        } catch (Exception e) {
            logger.error("初始文件清理失败", e);
        }
    }
}