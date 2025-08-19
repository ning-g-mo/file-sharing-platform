package cn.lemwood.fileshare.config;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.env.EnvironmentPostProcessor;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.PropertySource;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;

import java.io.File;

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
            System.out.println("未找到外部配置文件，使用内置配置");
        }
    }
}