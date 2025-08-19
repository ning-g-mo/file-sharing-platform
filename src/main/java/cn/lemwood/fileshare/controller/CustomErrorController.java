package cn.lemwood.fileshare.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.web.servlet.error.ErrorController;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;

import javax.servlet.RequestDispatcher;
import javax.servlet.http.HttpServletRequest;

/**
 * 错误页面控制器
 * 处理404等错误页面的显示
 */
@Controller
public class CustomErrorController implements ErrorController {
    
    private static final Logger logger = LoggerFactory.getLogger(CustomErrorController.class);
    
    @RequestMapping("/error")
    public String handleError(HttpServletRequest request, Model model) {
        // 获取错误状态码
        Object status = request.getAttribute(RequestDispatcher.ERROR_STATUS_CODE);
        
        if (status != null) {
            int statusCode = Integer.parseInt(status.toString());
            
            // 记录错误信息
            String requestUri = (String) request.getAttribute(RequestDispatcher.ERROR_REQUEST_URI);
            String errorMessage = (String) request.getAttribute(RequestDispatcher.ERROR_MESSAGE);
            
            logger.warn("错误页面访问 - 状态码: {}, URI: {}, 消息: {}", statusCode, requestUri, errorMessage);
            
            // 根据状态码返回不同的错误页面
            if (statusCode == HttpStatus.NOT_FOUND.value()) {
                // 对于404错误，返回自定义的404页面
                return "forward:/404.html";
            } else if (statusCode == HttpStatus.INTERNAL_SERVER_ERROR.value()) {
                // 对于500错误，可以返回500页面（如果需要的话）
                model.addAttribute("errorCode", statusCode);
                model.addAttribute("errorMessage", "服务器内部错误");
                return "forward:/404.html"; // 暂时也使用404页面
            } else if (statusCode == HttpStatus.FORBIDDEN.value()) {
                // 对于403错误
                model.addAttribute("errorCode", statusCode);
                model.addAttribute("errorMessage", "访问被拒绝");
                return "forward:/404.html"; // 暂时也使用404页面
            }
        }
        
        // 默认返回404页面
        return "forward:/404.html";
    }
    
    /**
     * 直接访问404页面的处理
     */
    @RequestMapping("/404")
    public String notFound() {
        return "forward:/404.html";
    }
    
    /**
     * 处理首页路由
     */
    @RequestMapping("/")
    public String home() {
        return "forward:/index.html";
    }
    
    /**
     * 处理index路由
     */
    @RequestMapping("/index")
    public String index() {
        return "forward:/index.html";
    }
}