package cn.lemwood.fileshare;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;

/**
 * 文件分享平台主启动类
 * 
 * @author lemwood
 */
@SpringBootApplication
@EnableScheduling
public class FileShareApplication {

    public static void main(String[] args) {
        // 设置外部配置文件路径
        setupExternalConfig();
        
        SpringApplication.run(FileShareApplication.class, args);
        System.out.println("\n" +
                "  ______ _ _        _____ _                     \n" +
                " |  ____(_) |      / ____| |                    \n" +
                " | |__   _| | ___ | (___ | |__   __ _ _ __ ___   \n" +
                " |  __| | | |/ _ \\ \\___ \\| '_ \\ / _` | '__/ _ \\  \n" +
                " | |    | | |  __/ ____) | | | | (_| | | |  __/  \n" +
                " |_|    |_|_|\\___||_____/|_| |_|\\__,_|_|  \\___|  \n" +
                "                                                 \n" +
                "文件分享平台启动成功！访问地址: http://localhost:8080/\n");
    }
    
    /**
     * 设置外部配置文件
     */
    private static void setupExternalConfig() {
        try {
            // 获取JAR文件所在目录
            String jarDir = System.getProperty("user.dir");
            Path externalConfigPath = Paths.get(jarDir, "application.yml");
            
            // 如果外部配置文件不存在，从classpath复制一份
            if (!Files.exists(externalConfigPath)) {
                // 从classpath读取默认配置文件
                try (var inputStream = FileShareApplication.class.getClassLoader()
                        .getResourceAsStream("application.yml")) {
                    if (inputStream != null) {
                        Files.copy(inputStream, externalConfigPath, StandardCopyOption.REPLACE_EXISTING);
                        System.out.println("已创建外部配置文件: " + externalConfigPath.toString());
                    }
                }
            }
            
            // 设置Spring Boot使用外部配置文件
            System.setProperty("spring.config.location", 
                "classpath:/application.yml,file:" + externalConfigPath.toString());
            
        } catch (IOException e) {
            System.err.println("设置外部配置文件失败: " + e.getMessage());
        }
    }
}