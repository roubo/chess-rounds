package com.airoubo.chessrounds.security;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 管理员权限注解
 * 用于标记需要管理员权限（用户ID为1）才能访问的接口
 * 
 * @author AiRoubo
 * @version 1.0.0
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface AdminOnly {
    
    /**
     * 权限描述信息
     * @return 权限描述
     */
    String value() default "需要管理员权限";
}