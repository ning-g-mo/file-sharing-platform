package cn.lemwood.fileshare.exception;

/**
 * 文件分享平台自定义异常
 */
public class FileShareException extends RuntimeException {
    
    private final String errorCode;
    
    public FileShareException(String message) {
        super(message);
        this.errorCode = "UNKNOWN_ERROR";
    }
    
    public FileShareException(String errorCode, String message) {
        super(message);
        this.errorCode = errorCode;
    }
    
    public FileShareException(String message, Throwable cause) {
        super(message, cause);
        this.errorCode = "UNKNOWN_ERROR";
    }
    
    public FileShareException(String errorCode, String message, Throwable cause) {
        super(message, cause);
        this.errorCode = errorCode;
    }
    
    public String getErrorCode() {
        return errorCode;
    }
    
    // 常用错误代码
    public static class ErrorCodes {
        public static final String FILE_NOT_FOUND = "FILE_NOT_FOUND";
        public static final String FILE_EXPIRED = "FILE_EXPIRED";
        public static final String FILE_TOO_LARGE = "FILE_TOO_LARGE";
        public static final String INVALID_FILE_TYPE = "INVALID_FILE_TYPE";
        public static final String UPLOAD_FAILED = "UPLOAD_FAILED";
        public static final String DOWNLOAD_FAILED = "DOWNLOAD_FAILED";
        public static final String STORAGE_FULL = "STORAGE_FULL";
        public static final String INVALID_PARAMETER = "INVALID_PARAMETER";
        public static final String SYSTEM_ERROR = "SYSTEM_ERROR";
        public static final String FILE_NOT_READABLE = "FILE_NOT_READABLE";
        public static final String EMPTY_FILE = "EMPTY_FILE";
        public static final String INVALID_FILE_NAME = "INVALID_FILE_NAME";
    }
}