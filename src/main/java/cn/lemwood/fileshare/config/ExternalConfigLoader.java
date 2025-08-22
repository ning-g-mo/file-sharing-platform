package cn.lemwood.fileshare.config;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.env.EnvironmentPostProcessor;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.PropertySource;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.StandardOpenOption;

/**
 * 外部配置文件加载器
 * 在Spring Boot启动时优先加载外部配置文件
 */
public class ExternalConfigLoader implements EnvironmentPostProcessor {

    @Override
    public void postProcessEnvironment(ConfigurableEnvironment environment, SpringApplication application) {
        // 检查外部配置文件是否存在
        File externalConfig = new File("./config/database.yml");
        if (externalConfig.exists()) {
            try {
                System.out.println("发现外部配置文件: " + externalConfig.getAbsolutePath());
                
                // 创建外部配置文件资源
                Resource resource = new FileSystemResource(externalConfig);
                
                // 使用YAML属性源工厂加载配置
                YamlPropertySourceFactory factory = new YamlPropertySourceFactory();
                PropertySource<?> propertySource = factory.createPropertySource("external-database-config", 
                    new org.springframework.core.io.support.EncodedResource(resource));
                
                // 将外部配置添加到环境中，并设置高优先级
                environment.getPropertySources().addFirst(propertySource);
                
                System.out.println("外部配置文件加载成功");
            } catch (Exception e) {
                System.err.println("加载外部配置文件失败: " + e.getMessage());
                e.printStackTrace();
            }
        } else {
            System.out.println("未找到外部配置文件，正在创建默认配置文件");
            createDefaultConfigFile(externalConfig);
            
            // 创建完成后尝试加载
            if (externalConfig.exists()) {
                try {
                    Resource resource = new FileSystemResource(externalConfig);
                    YamlPropertySourceFactory factory = new YamlPropertySourceFactory();
                    PropertySource<?> propertySource = factory.createPropertySource("external-database-config", 
                        new org.springframework.core.io.support.EncodedResource(resource));
                    environment.getPropertySources().addFirst(propertySource);
                    System.out.println("默认配置文件创建并加载成功");
                } catch (Exception e) {
                    System.err.println("加载默认配置文件失败: " + e.getMessage());
                    e.printStackTrace();
                }
            }
        }
    }
    
    /**
     * 创建默认配置文件
     */
    private void createDefaultConfigFile(File configFile) {
        try {
            // 确保config目录存在
            File configDir = configFile.getParentFile();
            if (!configDir.exists()) {
                configDir.mkdirs();
                System.out.println("创建配置目录: " + configDir.getAbsolutePath());
            }
            
            // 创建默认配置内容
            String defaultConfig = "database:\n" +
                    "  default:\n" +
                    "    type: sqlite\n" +
                    "    url: jdbc:sqlite:./data/fileshare.db\n" +
                    "    driver-class-name: org.sqlite.JDBC\n" +
                    "  \n" +
                    "  development:\n" +
                    "    type: sqlite\n" +
                    "    url: jdbc:sqlite:./data/fileshare_dev.db\n" +
                    "    driver-class-name: org.sqlite.JDBC\n" +
                    "  \n" +
                    "  test:\n" +
                    "    type: sqlite\n" +
                    "    url: jdbc:sqlite:./data/fileshare_test.db\n" +
                    "    driver-class-name: org.sqlite.JDBC\n" +
                    "  \n" +
                    "  production:\n" +
                    "    type: sqlite\n" +
                    "    url: jdbc:sqlite:./data/fileshare_prod.db\n" +
                    "    driver-class-name: org.sqlite.JDBC\n" +
                    "  \n" +
                    "  development-mysql:\n" +
                    "    type: mysql\n" +
                    "    url: jdbc:mysql://localhost:3306/fileshare_dev?useSSL=false&serverTimezone=UTC&allowPublicKeyRetrieval=true\n" +
                    "    driver-class-name: com.mysql.cj.jdbc.Driver\n" +
                    "    username: root\n" +
                    "    password: password\n" +
                    "  \n" +
                    "  test-mysql:\n" +
                    "    type: mysql\n" +
                    "    url: jdbc:mysql://localhost:3306/fileshare_test?useSSL=false&serverTimezone=UTC&allowPublicKeyRetrieval=true\n" +
                    "    driver-class-name: com.mysql.cj.jdbc.Driver\n" +
                    "    username: root\n" +
                    "    password: password\n" +
                    "  \n" +
                    "  production-mysql:\n" +
                    "    type: mysql\n" +
                    "    url: jdbc:mysql://localhost:3306/fileshare_prod?useSSL=false&serverTimezone=UTC&allowPublicKeyRetrieval=true\n" +
                    "    driver-class-name: com.mysql.cj.jdbc.Driver\n" +
                    "    username: root\n" +
                    "    password: password\n";
            
            // 写入配置文件
            Files.write(configFile.toPath(), defaultConfig.getBytes("UTF-8"), 
                       StandardOpenOption.CREATE, StandardOpenOption.WRITE, StandardOpenOption.TRUNCATE_EXISTING);
            
            System.out.println("默认配置文件已创建: " + configFile.getAbsolutePath());
            
        } catch (IOException e) {
            System.err.println("创建默认配置文件失败: " + e.getMessage());
            e.printStackTrace();
        }
    }
}