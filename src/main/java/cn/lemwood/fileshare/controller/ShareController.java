package cn.lemwood.fileshare.controller;

import cn.lemwood.fileshare.entity.FileInfo;
import cn.lemwood.fileshare.service.FileService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

/**
 * 文件分享控制器
 * 
 * @author lemwood
 */
@Controller
@RequestMapping("/share")
public class ShareController {

    private static final Logger logger = LoggerFactory.getLogger(ShareController.class);
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    @Autowired
    private FileService fileService;
    
    @Value("${app.api.context-path:/api}")
    private String apiContextPath;

    /**
     * 文件分享页面
     */
    @GetMapping("/{fileKey}")
    public String sharePage(@PathVariable String fileKey, Model model) {
        Optional<FileInfo> fileInfoOpt = fileService.getFileInfo(fileKey);
        
        if (!fileInfoOpt.isPresent()) {
            model.addAttribute("error", "文件不存在");
            model.addAttribute("errorCode", "FILE_NOT_FOUND");
            return "share-error";
        }
        
        FileInfo fileInfo = fileInfoOpt.get();
        if (fileInfo.isExpired()) {
            model.addAttribute("error", "文件已过期");
            model.addAttribute("errorCode", "FILE_EXPIRED");
            return "share-error";
        }
        
        // 添加文件信息到模型
        model.addAttribute("fileInfo", fileInfo);
        model.addAttribute("fileName", fileInfo.getOriginalName());
        model.addAttribute("fileSize", formatFileSize(fileInfo.getFileSize()));
        model.addAttribute("uploadTime", fileInfo.getUploadTime().format(DATE_FORMATTER));
        model.addAttribute("expireTime", fileInfo.getExpireTime().format(DATE_FORMATTER));
        model.addAttribute("downloadCount", fileInfo.getDownloadCount());
        model.addAttribute("downloadUrl", apiContextPath + "/files/download/" + fileKey);
        
        return "share";
    }

    /**
     * 获取分享文件信息API
     */
    @GetMapping("/api/{fileKey}")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> getShareFileInfo(@PathVariable String fileKey) {
        Map<String, Object> response = new HashMap<>();
        
        Optional<FileInfo> fileInfoOpt = fileService.getFileInfo(fileKey);
        if (!fileInfoOpt.isPresent()) {
            response.put("success", false);
            response.put("message", "文件不存在");
            response.put("errorCode", "FILE_NOT_FOUND");
            return ResponseEntity.notFound().build();
        }
        
        FileInfo fileInfo = fileInfoOpt.get();
        if (fileInfo.isExpired()) {
            response.put("success", false);
            response.put("message", "文件已过期");
            response.put("errorCode", "FILE_EXPIRED");
            return ResponseEntity.status(HttpStatus.GONE).body(response);
        }
        
        Map<String, Object> fileData = new HashMap<>();
        fileData.put("fileKey", fileInfo.getFileKey());
        fileData.put("originalName", fileInfo.getOriginalName());
        fileData.put("fileSize", fileInfo.getFileSize());
        fileData.put("fileSizeFormatted", formatFileSize(fileInfo.getFileSize()));
        fileData.put("contentType", fileInfo.getContentType());
        fileData.put("uploadTime", fileInfo.getUploadTime().format(DATE_FORMATTER));
        fileData.put("expireTime", fileInfo.getExpireTime().format(DATE_FORMATTER));
        fileData.put("downloadCount", fileInfo.getDownloadCount());
        fileData.put("downloadUrl", apiContextPath + "/files/download/" + fileInfo.getFileKey());
        
        response.put("success", true);
        response.put("data", fileData);
        return ResponseEntity.ok(response);
    }

    /**
     * 直接下载分享文件
     */
    @GetMapping("/download/{fileKey}")
    public ResponseEntity<Resource> downloadShareFile(@PathVariable String fileKey) {
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
            logger.error("分享文件下载失败: {}", e.getMessage());
            return ResponseEntity.badRequest().build();
        }
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
}