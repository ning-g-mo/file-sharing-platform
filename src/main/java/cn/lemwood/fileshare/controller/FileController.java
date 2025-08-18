package cn.lemwood.fileshare.controller;

import cn.lemwood.fileshare.entity.FileInfo;
import cn.lemwood.fileshare.service.FileService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * 文件控制器
 * 
 * @author lemwood
 */
@RestController
@RequestMapping("/files")
public class FileController {

    private static final Logger logger = LoggerFactory.getLogger(FileController.class);
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    @Autowired
    private FileService fileService;

    /**
     * 上传文件
     */
    @PostMapping("/upload")
    public ResponseEntity<Map<String, Object>> uploadFile(
            @RequestParam("file") MultipartFile file,
            HttpServletRequest request) {
        
        Map<String, Object> response = new HashMap<>();
        
        try {
            String uploaderIp = getClientIpAddress(request);
            FileInfo fileInfo = fileService.uploadFile(file, uploaderIp);
            
            response.put("success", true);
            response.put("message", "文件上传成功");
            response.put("data", buildFileInfoResponse(fileInfo));
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            logger.error("文件上传失败: {}", e.getMessage());
            response.put("success", false);
            response.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }

    /**
     * 获取文件信息
     */
    @GetMapping("/info/{fileKey}")
    public ResponseEntity<Map<String, Object>> getFileInfo(@PathVariable String fileKey) {
        Map<String, Object> response = new HashMap<>();
        
        Optional<FileInfo> fileInfoOpt = fileService.getFileInfo(fileKey);
        if (!fileInfoOpt.isPresent()) {
            response.put("success", false);
            response.put("message", "文件不存在");
            return ResponseEntity.notFound().build();
        }
        
        FileInfo fileInfo = fileInfoOpt.get();
        if (fileInfo.isExpired()) {
            response.put("success", false);
            response.put("message", "文件已过期");
            return ResponseEntity.status(HttpStatus.GONE).body(response);
        }
        
        response.put("success", true);
        response.put("data", buildFileInfoResponse(fileInfo));
        return ResponseEntity.ok(response);
    }

    /**
     * 下载文件
     */
    @GetMapping("/download/{fileKey}")
    public ResponseEntity<Resource> downloadFile(@PathVariable String fileKey) {
        try {
            Optional<FileInfo> fileInfoOpt = fileService.getFileInfo(fileKey);
            if (!fileInfoOpt.isPresent()) {
                return ResponseEntity.notFound().build();
            }
            
            FileInfo fileInfo = fileInfoOpt.get();
            if (fileInfo.isExpired()) {
                return ResponseEntity.status(HttpStatus.GONE).build();
            }
            
            Resource resource = fileService.downloadFile(fileKey);
            
            // 设置响应头
            String encodedFileName = URLEncoder.encode(fileInfo.getOriginalName(), StandardCharsets.UTF_8.toString())
                    .replaceAll("\\+", "%20");
            
            return ResponseEntity.ok()
                    .contentType(MediaType.parseMediaType(fileInfo.getContentType()))
                    .header(HttpHeaders.CONTENT_DISPOSITION, 
                            "attachment; filename=\"" + encodedFileName + "\"")
                    .body(resource);
                    
        } catch (Exception e) {
            logger.error("文件下载失败: {}", e.getMessage());
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * 删除文件
     */
    @DeleteMapping("/{fileKey}")
    public ResponseEntity<Map<String, Object>> deleteFile(@PathVariable String fileKey) {
        Map<String, Object> response = new HashMap<>();
        
        boolean deleted = fileService.deleteFile(fileKey);
        if (deleted) {
            response.put("success", true);
            response.put("message", "文件删除成功");
            return ResponseEntity.ok(response);
        } else {
            response.put("success", false);
            response.put("message", "文件不存在或删除失败");
            return ResponseEntity.badRequest().body(response);
        }
    }

    /**
     * 获取用户文件列表
     */
    @GetMapping("/my-files")
    public ResponseEntity<Map<String, Object>> getMyFiles(HttpServletRequest request) {
        Map<String, Object> response = new HashMap<>();
        
        try {
            String uploaderIp = getClientIpAddress(request);
            List<FileInfo> files = fileService.getUserFiles(uploaderIp);
            
            response.put("success", true);
            response.put("data", files.stream()
                    .map(this::buildFileInfoResponse)
                    .toArray());
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            logger.error("获取用户文件列表失败: {}", e.getMessage());
            response.put("success", false);
            response.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }

    /**
     * 获取最近上传的文件
     */
    @GetMapping("/recent")
    public ResponseEntity<Map<String, Object>> getRecentFiles() {
        Map<String, Object> response = new HashMap<>();
        
        try {
            List<FileInfo> files = fileService.getRecentFiles();
            
            response.put("success", true);
            response.put("data", files.stream()
                    .map(this::buildPublicFileInfoResponse)
                    .toArray());
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            logger.error("获取最近文件列表失败: {}", e.getMessage());
            response.put("success", false);
            response.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }

    /**
     * 获取系统统计信息
     */
    @GetMapping("/stats")
    public ResponseEntity<Map<String, Object>> getSystemStats() {
        Map<String, Object> response = new HashMap<>();
        
        try {
            FileService.SystemStats stats = fileService.getSystemStats();
            
            Map<String, Object> statsData = new HashMap<>();
            statsData.put("totalFiles", stats.getTotalFiles());
            statsData.put("totalSize", stats.getTotalSize());
            statsData.put("totalSizeFormatted", formatFileSize(stats.getTotalSize()));
            
            response.put("success", true);
            response.put("data", statsData);
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            logger.error("获取系统统计信息失败: {}", e.getMessage());
            response.put("success", false);
            response.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }

    /**
     * 构建文件信息响应对象
     */
    private Map<String, Object> buildFileInfoResponse(FileInfo fileInfo) {
        Map<String, Object> data = new HashMap<>();
        data.put("fileKey", fileInfo.getFileKey());
        data.put("originalName", fileInfo.getOriginalName());
        data.put("fileSize", fileInfo.getFileSize());
        data.put("fileSizeFormatted", formatFileSize(fileInfo.getFileSize()));
        data.put("contentType", fileInfo.getContentType());
        data.put("uploadTime", fileInfo.getUploadTime().format(DATE_FORMATTER));
        data.put("expireTime", fileInfo.getExpireTime().format(DATE_FORMATTER));
        data.put("downloadCount", fileInfo.getDownloadCount());
        data.put("downloadUrl", "/api/files/download/" + fileInfo.getFileKey());
        data.put("shareUrl", "http://localhost:8080/share/" + fileInfo.getFileKey());
        data.put("expired", fileInfo.isExpired());
        return data;
    }

    /**
     * 构建公开文件信息响应对象（不包含敏感信息）
     */
    private Map<String, Object> buildPublicFileInfoResponse(FileInfo fileInfo) {
        Map<String, Object> data = new HashMap<>();
        data.put("fileKey", fileInfo.getFileKey());
        data.put("originalName", fileInfo.getOriginalName());
        data.put("fileSize", fileInfo.getFileSize());
        data.put("fileSizeFormatted", formatFileSize(fileInfo.getFileSize()));
        data.put("uploadTime", fileInfo.getUploadTime().format(DATE_FORMATTER));
        data.put("downloadCount", fileInfo.getDownloadCount());
        return data;
    }

    /**
     * 格式化文件大小
     */
    private String formatFileSize(long size) {
        if (size < 1024) {
            return size + " B";
        } else if (size < 1024 * 1024) {
            return String.format("%.1f KB", size / 1024.0);
        } else if (size < 1024 * 1024 * 1024) {
            return String.format("%.1f MB", size / (1024.0 * 1024.0));
        } else {
            return String.format("%.1f GB", size / (1024.0 * 1024.0 * 1024.0));
        }
    }

    /**
     * 获取客户端IP地址
     */
    private String getClientIpAddress(HttpServletRequest request) {
        String xForwardedFor = request.getHeader("X-Forwarded-For");
        if (xForwardedFor != null && !xForwardedFor.isEmpty() && !"unknown".equalsIgnoreCase(xForwardedFor)) {
            return xForwardedFor.split(",")[0].trim();
        }
        
        String xRealIp = request.getHeader("X-Real-IP");
        if (xRealIp != null && !xRealIp.isEmpty() && !"unknown".equalsIgnoreCase(xRealIp)) {
            return xRealIp;
        }
        
        return request.getRemoteAddr();
    }
}