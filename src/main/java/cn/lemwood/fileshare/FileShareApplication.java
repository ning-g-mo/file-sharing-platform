package cn.lemwood.fileshare;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * 文件分享平台主启动类
 * 
 * @author lemwood
 */
@SpringBootApplication
@EnableScheduling
public class FileShareApplication {

    public static void main(String[] args) {
        SpringApplication.run(FileShareApplication.class, args);
        System.out.println("\n" +
                "  ______ _ _        _____ _                     \n" +
                " |  ____(_) |      / ____| |                    \n" +
                " | |__   _| | ___ | (___ | |__   __ _ _ __ ___   \n" +
                " |  __| | | |/ _ \\ \\___ \\| '_ \\ / _` | '__/ _ \\  \n" +
                " | |    | | |  __/ ____) | | | | (_| | | |  __/  \n" +
                " |_|    |_|_|\\___||_____/|_| |_|\\__,_|_|  \\___|  \n" +
                "                                                 \n" +
                "文件分享平台启动成功！访问地址: http://localhost:8080\n");
    }
}