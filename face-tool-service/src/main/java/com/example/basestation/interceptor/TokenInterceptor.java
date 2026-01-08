package com.example.basestation.interceptor;

import com.example.basestation.config.Result;
import com.example.basestation.service.TokenService;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.servlet.HandlerInterceptor;

/**
 * Token验证拦截器
 */
@Slf4j
@Component
public class TokenInterceptor implements HandlerInterceptor {
    
    @Autowired
    private TokenService tokenService;
    
    @Autowired
    private ObjectMapper objectMapper;
    
    // 不需要Token验证的路径
    private static final String[] EXCLUDE_PATHS = {
        "/api/token/apply",      // Token申请
        "/api/token/list",       // Token列表
        "/api/token/revoke",     // Token撤销
        "/api/test/",            // 测试接口（本地测试使用）
        "/error"                 // 错误页面
    };
    
    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        String requestPath = request.getRequestURI();
        
        // 检查是否在排除列表中
        for (String excludePath : EXCLUDE_PATHS) {
            if (requestPath.startsWith(excludePath)) {
                return true;
            }
        }
        
        // 获取Token（支持Header和参数两种方式）
        String token = getTokenFromRequest(request);
        
        if (!StringUtils.hasText(token)) {
            log.warn("请求缺少Token: path={}, ip={}", requestPath, getClientIp(request));
            writeErrorResponse(response, 401, "缺少Token，请在请求头中添加 Authorization: Bearer {token} 或在参数中添加 token");
            return false;
        }
        
        // 验证Token
        if (!tokenService.validateToken(token)) {
            log.warn("Token验证失败: token={}, path={}, ip={}", 
                token.substring(0, Math.min(20, token.length())), requestPath, getClientIp(request));
            writeErrorResponse(response, 401, "Token无效或已过期，请重新申请");
            return false;
        }
        
        // 刷新Token使用时间
        tokenService.refreshTokenUsage(token);
        
        return true;
    }
    
    /**
     * 从请求中获取Token
     * 支持两种方式：
     * 1. Header: Authorization: Bearer {token}
     * 2. 参数: ?token={token}
     */
    private String getTokenFromRequest(HttpServletRequest request) {
        // 方式1: 从Header获取
        String authHeader = request.getHeader("Authorization");
        if (StringUtils.hasText(authHeader) && authHeader.startsWith("Bearer ")) {
            return authHeader.substring(7);
        }
        
        // 方式2: 从参数获取
        String tokenParam = request.getParameter("token");
        if (StringUtils.hasText(tokenParam)) {
            return tokenParam;
        }
        
        return null;
    }
    
    /**
     * 获取客户端IP
     */
    private String getClientIp(HttpServletRequest request) {
        String ip = request.getHeader("X-Forwarded-For");
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("X-Real-IP");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getRemoteAddr();
        }
        return ip;
    }
    
    /**
     * 写入错误响应
     */
    private void writeErrorResponse(HttpServletResponse response, int code, String message) throws Exception {
        response.setStatus(HttpServletResponse.SC_OK); // 统一返回200，错误信息在body中
        response.setContentType("application/json;charset=UTF-8");
        
        Result<?> result = Result.error(code, message);
        String json = objectMapper.writeValueAsString(result);
        response.getWriter().write(json);
        response.getWriter().flush();
    }
}

