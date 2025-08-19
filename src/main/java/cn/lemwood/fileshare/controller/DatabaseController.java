package cn.lemwood.fileshare.controller;

import cn.lemwood.fileshare.service.DatabaseSwitchService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 数据库管理控制器
 * 提供数据库信息查询和管理功能的REST API
 */
@RestController
@RequestMapping("/database")
public class DatabaseController {

    @Autowired
    private DatabaseSwitchService databaseSwitchService;

    /**
     * 获取当前数据库信息
     */
    @GetMapping("/current")
    public ResponseEntity<Map<String, Object>> getCurrentDatabase() {
        Map<String, Object> response = new HashMap<>();
        
        try {
            response.put("type", databaseSwitchService.getCurrentDatabaseType());
            response.put("url", databaseSwitchService.getCurrentDatabaseUrl());
            response.put("dialect", databaseSwitchService.getCurrentDatabaseDialect());
            response.put("success", true);
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "获取数据库信息失败: " + e.getMessage());
            return ResponseEntity.internalServerError().body(response);
        }
    }

    /**
     * 获取所有可用的数据库配置
     */
    @GetMapping("/available")
    public ResponseEntity<Map<String, Object>> getAvailableDatabases() {
        Map<String, Object> response = new HashMap<>();
        
        try {
            List<DatabaseSwitchService.DatabaseInfo> databases = 
                databaseSwitchService.getAvailableDatabases();
            
            response.put("databases", databases);
            response.put("count", databases.size());
            response.put("success", true);
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "获取数据库配置失败: " + e.getMessage());
            return ResponseEntity.internalServerError().body(response);
        }
    }

    /**
     * 检查数据库连接状态
     */
    @GetMapping("/status")
    public ResponseEntity<Map<String, Object>> getDatabaseStatus() {
        Map<String, Object> response = new HashMap<>();
        
        try {
            DatabaseSwitchService.DatabaseStatus status = 
                databaseSwitchService.checkDatabaseStatus();
            
            response.put("status", status);
            response.put("success", true);
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "检查数据库状态失败: " + e.getMessage());
            return ResponseEntity.internalServerError().body(response);
        }
    }

    /**
     * 获取数据库统计信息
     */
    @GetMapping("/stats")
    public ResponseEntity<Map<String, Object>> getDatabaseStats() {
        Map<String, Object> response = new HashMap<>();
        
        try {
            // 获取当前数据库信息
            String currentType = databaseSwitchService.getCurrentDatabaseType();
            List<DatabaseSwitchService.DatabaseInfo> availableDbs = 
                databaseSwitchService.getAvailableDatabases();
            
            // 统计不同类型的数据库数量
            long sqliteCount = availableDbs.stream()
                .filter(db -> "sqlite".equalsIgnoreCase(db.getType()))
                .count();
            long mysqlCount = availableDbs.stream()
                .filter(db -> "mysql".equalsIgnoreCase(db.getType()))
                .count();
            
            Map<String, Object> stats = new HashMap<>();
            stats.put("currentType", currentType);
            stats.put("totalConfigurations", availableDbs.size());
            stats.put("sqliteConfigurations", sqliteCount);
            stats.put("mysqlConfigurations", mysqlCount);
            stats.put("supportedTypes", new String[]{"SQLite", "MySQL"});
            
            response.put("stats", stats);
            response.put("success", true);
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "获取数据库统计信息失败: " + e.getMessage());
            return ResponseEntity.internalServerError().body(response);
        }
    }

    /**
     * 获取数据库配置帮助信息
     */
    @GetMapping("/help")
    public ResponseEntity<Map<String, Object>> getDatabaseHelp() {
        Map<String, Object> response = new HashMap<>();
        
        Map<String, Object> help = new HashMap<>();
        help.put("description", "文件分享平台支持SQLite和MySQL数据库");
        help.put("defaultDatabase", "SQLite (无需额外配置)");
        help.put("mysqlSetup", "需要在database.yml中配置MySQL连接信息");
        
        Map<String, String> endpoints = new HashMap<>();
        endpoints.put("/database/current", "获取当前数据库信息");
        endpoints.put("/database/available", "获取所有可用数据库配置");
        endpoints.put("/database/status", "检查数据库连接状态");
        endpoints.put("/database/stats", "获取数据库统计信息");
        
        help.put("endpoints", endpoints);
        
        response.put("help", help);
        response.put("success", true);
        
        return ResponseEntity.ok(response);
    }
}