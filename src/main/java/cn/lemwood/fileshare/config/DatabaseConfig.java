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

import javax.annotation.PostConstruct;
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

    @Value("${spring.profiles.active:default}")
    private String activeProfile;

    /**
     * 手动加载配置文件
     */
    @PostConstruct
    public void loadConfiguration() {
        try {
            // 尝试加载外部配置文件
            File externalConfig = new File("./config/database.yml");
            if (externalConfig.exists()) {
                System.out.println("使用外部配置文件: " + externalConfig.getAbsolutePath());
                loadYamlConfiguration(externalConfig);
            } else {
                System.out.println("外部配置文件不存在，使用内置配置文件");
            }
        } catch (Exception e) {
            System.err.println("加载外部配置文件失败: " + e.getMessage());
        }
    }

    private void loadYamlConfiguration(File configFile) {
        // 这里可以添加自定义的配置加载逻辑
        // 由于Spring的限制，我们主要通过Environment来读取配置
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
        
        // 获取数据库配置
        String url = env.getProperty(configPrefix + ".url", 
                                   env.getProperty("database.default.url"));
        String driverClassName = env.getProperty(configPrefix + ".driver-class-name", 
                                               env.getProperty("database.default.driver-class-name"));
        String username = env.getProperty(configPrefix + ".username");
        String password = env.getProperty(configPrefix + ".password");
        
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
        if ("dev".equals(activeProfile) || "development".equals(activeProfile)) {
            return "development";
        } else if ("test".equals(activeProfile)) {
            return "test";
        } else if ("prod".equals(activeProfile) || "production".equals(activeProfile)) {
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