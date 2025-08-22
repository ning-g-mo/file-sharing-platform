package cn.lemwood.fileshare.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.jdbc.DataSourceBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.PropertySource;
import org.springframework.core.env.Environment;
import org.springframework.beans.factory.annotation.Autowired;


import javax.sql.DataSource;
import java.io.File;
import java.nio.file.Paths;

/**
 * 数据库配置类
 * 读取独立的数据库配置文件并创建数据源
 */
@Configuration
public class DatabaseConfig {

    @Autowired
    private Environment env;

    /**
     * 获取当前激活的配置文件
     */
    private String getActiveProfile() {
        String profile = env.getProperty("spring.profiles.active");
        return profile != null ? profile : "default";
    }

    /**
     * 创建主数据源
     * 根据当前激活的环境配置选择对应的数据库配置
     */
    @Bean
    @Primary
    public DataSource dataSource() {
        // 确定配置前缀
        String configPrefix = "database." + getEffectiveProfile();
        
        // 获取数据库配置，提供默认值
        String url = env.getProperty(configPrefix + ".url", 
                                   env.getProperty("database.default.url", "jdbc:sqlite:./data/fileshare.db"));
        String driverClassName = env.getProperty(configPrefix + ".driver-class-name", 
                                               env.getProperty("database.default.driver-class-name", "org.sqlite.JDBC"));
        String username = env.getProperty(configPrefix + ".username");
        String password = env.getProperty(configPrefix + ".password");
        
        // 验证必要的配置
        if (url == null || url.trim().isEmpty()) {
            throw new IllegalStateException("数据库URL配置不能为空，配置前缀: " + configPrefix);
        }
        if (driverClassName == null || driverClassName.trim().isEmpty()) {
            throw new IllegalStateException("数据库驱动类名配置不能为空，配置前缀: " + configPrefix);
        }
        
        System.out.println("使用数据库配置 - URL: " + url + ", Driver: " + driverClassName);
        
        // 确保数据库文件目录存在（仅对SQLite）
        ensureDatabaseDirectoryExists(url);
        
        // 创建数据源构建器
        DataSourceBuilder<?> builder = DataSourceBuilder.create()
                .url(url)
                .driverClassName(driverClassName);
        
        // 如果有用户名和密码配置，则添加（主要用于MySQL）
        if (username != null && !username.isEmpty()) {
            builder.username(username);
        }
        if (password != null && !password.isEmpty()) {
            builder.password(password);
        }
        
        return builder.build();
    }

    /**
     * 获取有效的配置环境
     */
    private String getEffectiveProfile() {
        String profile = getActiveProfile();
        if ("dev".equals(profile) || "development".equals(profile)) {
            return "development";
        } else if ("test".equals(profile)) {
            return "test";
        } else if ("prod".equals(profile) || "production".equals(profile)) {
            return "production";
        }
        return "default";
    }

    /**
     * 确保SQLite数据库文件目录存在
     */
    private void ensureDatabaseDirectoryExists(String url) {
        if (url != null && url.startsWith("jdbc:sqlite:")) {
            String dbPath = url.substring("jdbc:sqlite:".length());
            if (!dbPath.equals(":memory:")) {
                File dbFile = new File(dbPath);
                File parentDir = dbFile.getParentFile();
                if (parentDir != null && !parentDir.exists()) {
                    boolean created = parentDir.mkdirs();
                    if (created) {
                        System.out.println("创建数据库目录: " + parentDir.getAbsolutePath());
                    }
                }
            }
        }
    }

    /**
     * 获取数据库类型
     */
    public String getDatabaseType() {
        String configPrefix = "database." + getEffectiveProfile();
        return env.getProperty(configPrefix + ".type", 
                             env.getProperty("database.default.type", "sqlite"));
    }

    /**
     * 获取数据库URL
     */
    public String getDatabaseUrl() {
        String configPrefix = "database." + getEffectiveProfile();
        return env.getProperty(configPrefix + ".url", 
                             env.getProperty("database.default.url"));
    }

    /**
     * 获取数据库方言
     */
    public String getDatabaseDialect() {
        String dbType = getDatabaseType();
        return env.getProperty("dialects." + dbType, 
                             "org.sqlite.hibernate.dialect.SQLiteDialect");
    }

    /**
     * 检查是否为MySQL数据库
     */
    public boolean isMySQLDatabase() {
        return "mysql".equalsIgnoreCase(getDatabaseType());
    }

    /**
     * 检查是否为SQLite数据库
     */
    public boolean isSQLiteDatabase() {
        return "sqlite".equalsIgnoreCase(getDatabaseType());
    }
}