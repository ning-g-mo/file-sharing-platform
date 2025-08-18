package cn.lemwood.fileshare.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import java.util.Arrays;
import java.util.List;

/**
 * 文件上传配置类
 * 
 * @author lemwood
 */
@Configuration
@ConfigurationProperties(prefix = "file")
public class FileUploadConfig {

    private Upload upload = new Upload();
    private Cleanup cleanup = new Cleanup();

    public static class Upload {
        private String path = "./uploads";
        private long maxSize = 104857600L; // 100MB
        private String allowedTypes = ".jpg,.jpeg,.png,.gif,.pdf,.doc,.docx,.xls,.xlsx,.ppt,.pptx,.txt,.zip,.rar,.7z,.mp4,.avi,.mov";

        public String getPath() {
            return path;
        }

        public void setPath(String path) {
            this.path = path;
        }

        public long getMaxSize() {
            return maxSize;
        }

        public void setMaxSize(long maxSize) {
            this.maxSize = maxSize;
        }

        public String getAllowedTypes() {
            return allowedTypes;
        }

        public void setAllowedTypes(String allowedTypes) {
            this.allowedTypes = allowedTypes;
        }

        public List<String> getAllowedTypesList() {
            return Arrays.asList(allowedTypes.split(","));
        }
    }

    public static class Cleanup {
        private long interval = 3600000L; // 1小时
        private int retentionHours = 24; // 24小时

        public long getInterval() {
            return interval;
        }

        public void setInterval(long interval) {
            this.interval = interval;
        }

        public int getRetentionHours() {
            return retentionHours;
        }

        public void setRetentionHours(int retentionHours) {
            this.retentionHours = retentionHours;
        }
    }

    public Upload getUpload() {
        return upload;
    }

    public void setUpload(Upload upload) {
        this.upload = upload;
    }

    public Cleanup getCleanup() {
        return cleanup;
    }

    public void setCleanup(Cleanup cleanup) {
        this.cleanup = cleanup;
    }
}