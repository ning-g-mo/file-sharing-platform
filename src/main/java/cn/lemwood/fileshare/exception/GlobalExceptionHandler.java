package cn.lemwood.fileshare.exception;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.multipart.MaxUploadSizeExceededException;
import org.springframework.web.servlet.NoHandlerFoundException;

import javax.servlet.http.HttpServletRequest;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;

/**
 * 全局异常处理器
 */
@RestControllerAdvice
public class GlobalExceptionHandler {
    
    private static final Logger logger = LoggerFactory.getLogger(GlobalExceptionHandler.class);
    
    /**
     * 处理文件分享平台自定义异常
     */
    @ExceptionHandler(FileShareException.class)
    public ResponseEntity<Map<String, Object>> handleFileShareException(FileShareException e, HttpServletRequest request) {
        logger.warn("文件分享异常: {} - {}", e.getErrorCode(), e.getMessage());
        
        Map<String, Object> response = createErrorResponse(
            e.getErrorCode(),
            e.getMessage(),
            HttpStatus.BAD_REQUEST.value(),
            request.getRequestURI()
        );
        
        return ResponseEntity.badRequest().body(response);
    }
    
    /**
     * 处理文件上传大小超限异常
     */
    @ExceptionHandler(MaxUploadSizeExceededException.class)
    public ResponseEntity<Map<String, Object>> handleMaxUploadSizeExceededException(MaxUploadSizeExceededException e, HttpServletRequest request) {
        logger.warn("文件上传大小超限: {}", e.getMessage());
        
        Map<String, Object> response = createErrorResponse(
            FileShareException.ErrorCodes.FILE_TOO_LARGE,
            "文件大小超过限制，请选择较小的文件",
            HttpStatus.PAYLOAD_TOO_LARGE.value(),
            request.getRequestURI()
        );
        
        return ResponseEntity.status(HttpStatus.PAYLOAD_TOO_LARGE).body(response);
    }
    
    /**
     * 处理404异常
     */
    @ExceptionHandler(NoHandlerFoundException.class)
    public ResponseEntity<Map<String, Object>> handleNoHandlerFoundException(NoHandlerFoundException e, HttpServletRequest request) {
        logger.warn("请求的资源不存在: {}", request.getRequestURI());
        
        Map<String, Object> response = createErrorResponse(
            "NOT_FOUND",
            "请求的资源不存在",
            HttpStatus.NOT_FOUND.value(),
            request.getRequestURI()
        );
        
        return ResponseEntity.notFound().build();
    }
    
    /**
     * 处理参数验证异常
     */
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<Map<String, Object>> handleIllegalArgumentException(IllegalArgumentException e, HttpServletRequest request) {
        logger.warn("参数验证失败: {}", e.getMessage());
        
        Map<String, Object> response = createErrorResponse(
            FileShareException.ErrorCodes.INVALID_PARAMETER,
            "参数验证失败: " + e.getMessage(),
            HttpStatus.BAD_REQUEST.value(),
            request.getRequestURI()
        );
        
        return ResponseEntity.badRequest().body(response);
    }
    
    /**
     * 处理运行时异常
     */
    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<Map<String, Object>> handleRuntimeException(RuntimeException e, HttpServletRequest request) {
        logger.error("运行时异常: {}", e.getMessage(), e);
        
        Map<String, Object> response = createErrorResponse(
            FileShareException.ErrorCodes.SYSTEM_ERROR,
            "系统内部错误，请稍后重试",
            HttpStatus.INTERNAL_SERVER_ERROR.value(),
            request.getRequestURI()
        );
        
        return ResponseEntity.internalServerError().body(response);
    }
    
    /**
     * 处理所有其他异常
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String, Object>> handleException(Exception e, HttpServletRequest request) {
        logger.error("未处理的异常: {}", e.getMessage(), e);
        
        Map<String, Object> response = createErrorResponse(
            "UNKNOWN_ERROR",
            "系统发生未知错误，请联系管理员",
            HttpStatus.INTERNAL_SERVER_ERROR.value(),
            request.getRequestURI()
        );
        
        return ResponseEntity.internalServerError().body(response);
    }
    
    /**
     * 创建错误响应
     */
    private Map<String, Object> createErrorResponse(String errorCode, String message, int status, String path) {
        Map<String, Object> response = new HashMap<>();
        response.put("success", false);
        response.put("errorCode", errorCode);
        response.put("message", message);
        response.put("status", status);
        response.put("path", path);
        response.put("timestamp", LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
        return response;
    }
}