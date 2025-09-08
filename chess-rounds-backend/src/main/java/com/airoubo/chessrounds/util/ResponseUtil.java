package com.airoubo.chessrounds.util;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

/**
 * 响应工具类
 * 
 * @author AiRoubo
 * @version 1.0.0
 */
public class ResponseUtil {
    
    /**
     * 成功响应
     * 
     * @param data 数据
     * @param <T> 数据类型
     * @return 响应实体
     */
    public static <T> ResponseEntity<ApiResponse<T>> success(T data) {
        return ResponseEntity.ok(new ApiResponse<>(true, "操作成功", data, null));
    }
    
    /**
     * 成功响应（带消息）
     * 
     * @param message 消息
     * @param data 数据
     * @param <T> 数据类型
     * @return 响应实体
     */
    public static <T> ResponseEntity<ApiResponse<T>> success(String message, T data) {
        return ResponseEntity.ok(new ApiResponse<>(true, message, data, null));
    }
    
    /**
     * 失败响应
     * 
     * @param message 错误消息
     * @param <T> 数据类型
     * @return 响应实体
     */
    public static <T> ResponseEntity<ApiResponse<T>> error(String message) {
        return ResponseEntity.badRequest().body(new ApiResponse<>(false, message, null, null));
    }
    
    /**
     * 失败响应（带状态码）
     * 
     * @param status HTTP状态码
     * @param message 错误消息
     * @param <T> 数据类型
     * @return 响应实体
     */
    public static <T> ResponseEntity<ApiResponse<T>> error(HttpStatus status, String message) {
        return ResponseEntity.status(status).body(new ApiResponse<>(false, message, null, null));
    }
    
    /**
     * 失败响应（带错误详情）
     * 
     * @param message 错误消息
     * @param error 错误详情
     * @param <T> 数据类型
     * @return 响应实体
     */
    public static <T> ResponseEntity<ApiResponse<T>> error(String message, Object error) {
        return ResponseEntity.badRequest().body(new ApiResponse<>(false, message, null, error));
    }
    
    /**
     * API响应类
     * 
     * @param <T> 数据类型
     */
    public static class ApiResponse<T> {
        private boolean success;
        private String message;
        private T data;
        private Object error;
        private long timestamp;
        
        public ApiResponse(boolean success, String message, T data, Object error) {
            this.success = success;
            this.message = message;
            this.data = data;
            this.error = error;
            this.timestamp = System.currentTimeMillis();
        }
        
        // Getters and Setters
        public boolean isSuccess() {
            return success;
        }
        
        public void setSuccess(boolean success) {
            this.success = success;
        }
        
        public String getMessage() {
            return message;
        }
        
        public void setMessage(String message) {
            this.message = message;
        }
        
        public T getData() {
            return data;
        }
        
        public void setData(T data) {
            this.data = data;
        }
        
        public Object getError() {
            return error;
        }
        
        public void setError(Object error) {
            this.error = error;
        }
        
        public long getTimestamp() {
            return timestamp;
        }
        
        public void setTimestamp(long timestamp) {
            this.timestamp = timestamp;
        }
    }
}