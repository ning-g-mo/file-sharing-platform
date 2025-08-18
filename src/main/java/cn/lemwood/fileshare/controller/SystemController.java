package cn.lemwood.fileshare.controller;

import cn.lemwood.fileshare.service.SystemMonitorService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

/**
 * 系统监控控制器
 * 提供系统状态和健康检查接口
 */
@RestController
@RequestMapping("/api/system")
public class SystemController {
    
    @Autowired
    private SystemMonitorService systemMonitorService;
    
    /**
     * 获取系统状态信息
     */
    @GetMapping("/status")
    public ResponseEntity<Map<String, Object>> getSystemStatus() {
        try {
            Map<String, Object> status = systemMonitorService.getSystemStatus();
            return ResponseEntity.ok(status);
        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("status", "error");
            errorResponse.put("message", "获取系统状态失败: " + e.getMessage());
            return ResponseEntity.internalServerError().body(errorResponse);
        }
    }
    
    /**
     * 健康检查接口
     */
    @GetMapping("/health")
    public ResponseEntity<Map<String, Object>> healthCheck() {
        Map<String, Object> response = new HashMap<>();
        
        try {
            boolean isHealthy = systemMonitorService.isSystemHealthy();
            
            response.put("status", isHealthy ? "UP" : "DOWN");
            response.put("healthy", isHealthy);
            response.put("timestamp", System.currentTimeMillis());
            
            if (isHealthy) {
                response.put("message", "系统运行正常");
                return ResponseEntity.ok(response);
            } else {
                response.put("message", "系统存在问题");
                return ResponseEntity.status(503).body(response);
            }
        } catch (Exception e) {
            response.put("status", "DOWN");
            response.put("healthy", false);
            response.put("message", "健康检查失败: " + e.getMessage());
            response.put("timestamp", System.currentTimeMillis());
            return ResponseEntity.status(503).body(response);
        }
    }
    
    /**
     * 获取系统信息摘要
     */
    @GetMapping("/info")
    public ResponseEntity<Map<String, Object>> getSystemInfo() {
        Map<String, Object> info = new HashMap<>();
        
        try {
            // 应用信息
            info.put("application", "文件分享平台");
            info.put("version", "1.0.0");
            info.put("description", "支持24小时自动删除的在线文件分享平台");
            
            // Java信息
            info.put("javaVersion", System.getProperty("java.version"));
            info.put("javaVendor", System.getProperty("java.vendor"));
            
            // 系统信息
            info.put("osName", System.getProperty("os.name"));
            info.put("osVersion", System.getProperty("os.version"));
            info.put("osArch", System.getProperty("os.arch"));
            
            // 运行时信息
            Runtime runtime = Runtime.getRuntime();
            info.put("availableProcessors", runtime.availableProcessors());
            
            return ResponseEntity.ok(info);
        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("error", "获取系统信息失败: " + e.getMessage());
            return ResponseEntity.internalServerError().body(errorResponse);
        }
    }
    
    /**
     * 手动清理过期文件
     */
    @PostMapping("/clean-expired")
    public ResponseEntity<Map<String, Object>> cleanExpiredFiles() {
        Map<String, Object> response = new HashMap<>();
        
        try {
            int deletedCount = systemMonitorService.cleanExpiredFiles();
            
            response.put("success", true);
            response.put("message", "清理完成");
            
            Map<String, Object> data = new HashMap<>();
            data.put("deletedCount", deletedCount);
            response.put("data", data);
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "清理失败: " + e.getMessage());
            return ResponseEntity.internalServerError().body(response);
        }
    }
    
    /**
     * 优化存储空间
     */
    @PostMapping("/optimize-storage")
    public ResponseEntity<Map<String, Object>> optimizeStorage() {
        Map<String, Object> response = new HashMap<>();
        
        try {
            Map<String, Object> result = systemMonitorService.optimizeStorage();
            
            response.put("success", true);
            response.put("message", "优化完成");
            response.put("data", result);
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "优化失败: " + e.getMessage());
            return ResponseEntity.internalServerError().body(response);
        }
    }
}