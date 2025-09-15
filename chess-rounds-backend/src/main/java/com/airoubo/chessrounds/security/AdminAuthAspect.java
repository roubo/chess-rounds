package com.airoubo.chessrounds.security;

import com.airoubo.chessrounds.util.JwtUtil;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import org.springframework.web.server.ResponseStatusException;

import jakarta.servlet.http.HttpServletRequest;

/**
 * 管理员权限验证切面
 * 拦截带有@AdminOnly注解的方法，验证用户是否为管理员（用户ID为1）
 * 
 * @author AiRoubo
 * @version 1.0.0
 */
@Aspect
@Component
public class AdminAuthAspect {
    
    @Autowired
    private JwtUtil jwtUtil;
    
    /**
     * 管理员权限验证切面方法
     * 
     * @param joinPoint 连接点
     * @param adminOnly 管理员权限注解
     * @return 方法执行结果
     * @throws Throwable 异常
     */
    @Around("@annotation(adminOnly)")
    public Object checkAdminPermission(ProceedingJoinPoint joinPoint, AdminOnly adminOnly) throws Throwable {
        // 获取当前请求
        ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        if (attributes == null) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "无法获取请求上下文");
        }
        
        HttpServletRequest request = attributes.getRequest();
        
        // 获取Authorization头
        String authHeader = request.getHeader("Authorization");
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "缺少有效的认证令牌");
        }
        
        // 提取JWT令牌
        String token = authHeader.substring(7);
        
        try {
            // 验证令牌并获取用户ID
            Long userId = jwtUtil.getUserIdFromToken(token);
            
            // 检查是否为管理员用户（ID为1）
            if (userId == null || !userId.equals(1L)) {
                throw new ResponseStatusException(HttpStatus.FORBIDDEN, 
                    "访问被拒绝：" + adminOnly.value() + "，当前用户ID: " + userId);
            }
            
            // 权限验证通过，继续执行原方法
            return joinPoint.proceed();
            
        } catch (Exception e) {
            if (e instanceof ResponseStatusException) {
                throw e;
            }
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "令牌验证失败: " + e.getMessage());
        }
    }
}