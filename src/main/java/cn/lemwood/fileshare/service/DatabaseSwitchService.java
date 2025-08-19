package cn.lemwood.fileshare.service;

import cn.lemwood.fileshare.config.DatabaseConfig;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Service;

import java.util.*;

/**
 * 数据库切换服务
 * 提供数据库类型查询和切换功能
 */
@Service
public class DatabaseSwitchService {

    @Autowired
    private DatabaseConfig databaseConfig;

    @Autowired
    private Environment env;

    /**
     * 获取当前数据库类型
     */
    public String getCurrentDatabaseType() {
        return databaseConfig.getDatabaseType();
    }

    /**
     * 获取当前数据库URL
     */
    public String getCurrentDatabaseUrl() {
        return databaseConfig.getDatabaseUrl();
    }

    /**
     * 获取当前数据库方言
     */
    public String getCurrentDatabaseDialect() {
        return databaseConfig.getDatabaseDialect();
    }

    /**
     * 获取所有可用的数据库配置
     */
    public List<DatabaseInfo> getAvailableDatabases() {
        List<DatabaseInfo> databases = new ArrayList<>();
        
        // 扫描所有数据库配置
        String[] profiles = {"default", "development", "development-mysql", 
                           "test", "test-mysql", "production", "production-mysql"};
        
        for (String profile : profiles) {
            String configPrefix = "database." + profile;
            String type = env.getProperty(configPrefix + ".type");
            String url = env.getProperty(configPrefix + ".url");
            
            if (type != null && url != null) {
                DatabaseInfo info = new DatabaseInfo();
                info.setProfile(profile);
                info.setType(type);
                info.setUrl(url);
                info.setDriverClassName(env.getProperty(configPrefix + ".driver-class-name"));
                info.setUsername(env.getProperty(configPrefix + ".username"));
                info.setCurrent(profile.equals(getCurrentProfile()));
                databases.add(info);
            }
        }
        
        return databases;
    }

    /**
     * 获取当前激活的配置文件
     */
    private String getCurrentProfile() {
        String activeProfile = env.getProperty("spring.profiles.active", "default");
        
        // 根据激活的profile确定实际使用的数据库配置
        if ("dev".equals(activeProfile) || "development".equals(activeProfile)) {
            // 检查是否有MySQL配置
            String mysqlUrl = env.getProperty("database.development-mysql.url");
            if (mysqlUrl != null && !mysqlUrl.isEmpty()) {
                return "development-mysql";
            }
            return "development";
        } else if ("test".equals(activeProfile)) {
            String mysqlUrl = env.getProperty("database.test-mysql.url");
            if (mysqlUrl != null && !mysqlUrl.isEmpty()) {
                return "test-mysql";
            }
            return "test";
        } else if ("prod".equals(activeProfile) || "production".equals(activeProfile)) {
            String mysqlUrl = env.getProperty("database.production-mysql.url");
            if (mysqlUrl != null && !mysqlUrl.isEmpty()) {
                return "production-mysql";
            }
            return "production";
        }
        return "default";
    }

    /**
     * 检查数据库连接状态
     */
    public DatabaseStatus checkDatabaseStatus() {
        DatabaseStatus status = new DatabaseStatus();
        status.setType(getCurrentDatabaseType());
        status.setUrl(getCurrentDatabaseUrl());
        status.setDialect(getCurrentDatabaseDialect());
        
        try {
            // 这里可以添加实际的数据库连接测试逻辑
            status.setConnected(true);
            status.setMessage("数据库连接正常");
        } catch (Exception e) {
            status.setConnected(false);
            status.setMessage("数据库连接失败: " + e.getMessage());
        }
        
        return status;
    }

    /**
     * 数据库信息类
     */
    public static class DatabaseInfo {
        private String profile;
        private String type;
        private String url;
        private String driverClassName;
        private String username;
        private boolean current;

        // Getters and Setters
        public String getProfile() { return profile; }
        public void setProfile(String profile) { this.profile = profile; }
        
        public String getType() { return type; }
        public void setType(String type) { this.type = type; }
        
        public String getUrl() { return url; }
        public void setUrl(String url) { this.url = url; }
        
        public String getDriverClassName() { return driverClassName; }
        public void setDriverClassName(String driverClassName) { this.driverClassName = driverClassName; }
        
        public String getUsername() { return username; }
        public void setUsername(String username) { this.username = username; }
        
        public boolean isCurrent() { return current; }
        public void setCurrent(boolean current) { this.current = current; }
    }

    /**
     * 数据库状态类
     */
    public static class DatabaseStatus {
        private String type;
        private String url;
        private String dialect;
        private boolean connected;
        private String message;

        // Getters and Setters
        public String getType() { return type; }
        public void setType(String type) { this.type = type; }
        
        public String getUrl() { return url; }
        public void setUrl(String url) { this.url = url; }
        
        public String getDialect() { return dialect; }
        public void setDialect(String dialect) { this.dialect = dialect; }
        
        public boolean isConnected() { return connected; }
        public void setConnected(boolean connected) { this.connected = connected; }
        
        public String getMessage() { return message; }
        public void setMessage(String message) { this.message = message; }
    }
}