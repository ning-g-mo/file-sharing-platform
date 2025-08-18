package cn.lemwood.fileshare.repository;

import cn.lemwood.fileshare.entity.FileInfo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * 文件信息数据访问接口
 * 
 * @author lemwood
 */
@Repository
public interface FileInfoRepository extends JpaRepository<FileInfo, Long> {

    /**
     * 根据文件key查找文件信息
     */
    Optional<FileInfo> findByFileKey(String fileKey);

    /**
     * 查找所有已过期的文件
     */
    @Query("SELECT f FROM FileInfo f WHERE f.expireTime < :currentTime")
    List<FileInfo> findExpiredFiles(LocalDateTime currentTime);

    /**
     * 根据上传者IP查找文件列表
     */
    List<FileInfo> findByUploaderIpOrderByUploadTimeDesc(String uploaderIp);

    /**
     * 统计总文件数量
     */
    long count();

    /**
     * 统计总文件大小
     */
    @Query("SELECT COALESCE(SUM(f.fileSize), 0) FROM FileInfo f")
    Long getTotalFileSize();

    /**
     * 查找最近上传的文件
     */
    List<FileInfo> findTop10ByOrderByUploadTimeDesc();

    /**
     * 删除已过期的文件记录
     */
    void deleteByExpireTimeBefore(LocalDateTime expireTime);

    /**
     * 根据存储文件名查找文件
     */
    Optional<FileInfo> findByStoredName(String storedName);
}