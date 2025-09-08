package com.airoubo.chessrounds.controller;

import com.airoubo.chessrounds.dto.user.UpdateProfileRequest;
import com.airoubo.chessrounds.dto.user.UserInfoResponse;
import com.airoubo.chessrounds.dto.user.UserLoginRequest;
import com.airoubo.chessrounds.dto.user.UserLoginResponse;
import com.airoubo.chessrounds.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpServletRequest;

import java.time.LocalDateTime;
import java.util.Optional;

/**
 * 用户控制器
 * 
 * @author AiRoubo
 * @version 1.0.0
 */
@RestController
@RequestMapping("/users")
@CrossOrigin(origins = "*")
public class UserController {
    
    @Autowired
    private UserService userService;
    
    /**
     * 微信登录
     * 
     * @param loginRequest 登录请求
     * @return 登录响应
     */
    @PostMapping("/login")
    public ResponseEntity<UserLoginResponse> login(@RequestBody UserLoginRequest loginRequest) {
        UserLoginResponse response = userService.login(loginRequest);
        return ResponseEntity.ok(response);
    }
    
    /**
     * 获取用户信息
     * 
     * @param userId 用户ID
     * @return 用户信息
     */
    @GetMapping("/{userId}")
    public ResponseEntity<UserInfoResponse> getUserById(@PathVariable Long userId) {
        Optional<UserInfoResponse> user = userService.getUserById(userId);
        return user.map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }
    

    
    /**
     * 搜索用户（按昵称）
     * 
     * @param nickname 昵称关键字
     * @param pageable 分页参数
     * @return 用户列表
     */
    @GetMapping("/search")
    public ResponseEntity<Page<UserInfoResponse>> searchUsers(
            @RequestParam String nickname,
            Pageable pageable) {
        Page<UserInfoResponse> users = userService.searchUsers(nickname, pageable);
        return ResponseEntity.ok(users);
    }
    
    /**
     * 获取活跃用户列表
     * 
     * @param since 时间范围
     * @param pageable 分页参数
     * @return 活跃用户列表
     */
    @GetMapping("/active")
    public ResponseEntity<Page<UserInfoResponse>> getActiveUsers(
            @RequestParam(required = false) LocalDateTime since,
            Pageable pageable) {
        Page<UserInfoResponse> users = userService.getActiveUsers(since, pageable);
        return ResponseEntity.ok(users);
    }
    
    /**
     * 统计活跃用户数量
     * 
     * @param since 时间范围
     * @return 活跃用户数量
     */
    @GetMapping("/active/count")
    public ResponseEntity<Long> countActiveUsers(
            @RequestParam(required = false) LocalDateTime since) {
        Long count = userService.countActiveUsers(since);
        return ResponseEntity.ok(count);
    }
    
    /**
     * 获取用户统计信息
     * 
     * @param userId 用户ID
     * @return 用户统计信息
     */
    @GetMapping("/{userId}/statistics")
    public ResponseEntity<UserService.UserStatistics> getUserStatistics(@PathVariable Long userId) {
        UserService.UserStatistics statistics = userService.getUserStatistics(userId);
        return ResponseEntity.ok(statistics);
    }
    
    /**
     * 获取当前用户信息
     * 
     * @param request HTTP请求对象（从中获取用户ID）
     * @return 用户信息
     */
    @GetMapping("/profile")
    public ResponseEntity<UserInfoResponse> getCurrentUserProfile(HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        if (userId == null) {
            return ResponseEntity.status(403).build();
        }
        Optional<UserInfoResponse> user = userService.getUserById(userId);
        return user.map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }
    
    /**
     * 更新当前用户信息
     * 
     * @param request HTTP请求对象（从中获取用户ID）
     * @param updateRequest 更新请求
     * @return 更新后的用户信息
     */
    @PutMapping("/profile")
    public ResponseEntity<UserInfoResponse> updateCurrentUserProfile(
            HttpServletRequest request,
            @RequestBody UpdateProfileRequest updateRequest) {
        Long userId = (Long) request.getAttribute("userId");
        if (userId == null) {
            return ResponseEntity.status(403).build();
        }
        UserInfoResponse updatedUser = userService.updateUserProfile(userId, updateRequest);
        return ResponseEntity.ok(updatedUser);
    }

}