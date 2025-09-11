package com.airoubo.chessrounds.service;

import com.airoubo.chessrounds.dto.user.UpdateProfileRequest;
import com.airoubo.chessrounds.dto.user.UserInfoResponse;
import com.airoubo.chessrounds.dto.user.UserLoginRequest;
import com.airoubo.chessrounds.dto.user.UserLoginResponse;
import com.airoubo.chessrounds.entity.User;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * 用户服务接口
 * 
 * @author AiRoubo
 * @version 1.0.0
 */
public interface UserService {
    
    /**
     * 用户登录
     * 
     * @param loginRequest 登录请求
     * @return 登录响应
     */
    UserLoginResponse login(UserLoginRequest loginRequest);
    
    /**
     * 刷新令牌
     * 
     * @param refreshToken 刷新令牌
     * @return 新的访问令牌
     */
    String refreshToken(String refreshToken);
    
    /**
     * 用户登出
     * 
     * @param userId 用户ID
     */
    void logout(Long userId);
    
    /**
     * 根据用户ID获取用户信息
     * 
     * @param userId 用户ID
     * @return 用户信息
     */
    Optional<UserInfoResponse> getUserById(Long userId);
    
    /**
     * 根据OpenID获取用户信息
     * 
     * @param openid OpenID
     * @return 用户信息
     */
    Optional<UserInfoResponse> getUserByOpenid(String openid);
    
    /**
     * 根据UnionID获取用户信息
     * 
     * @param unionid UnionID
     * @return 用户信息
     */
    Optional<UserInfoResponse> getUserByUnionid(String unionid);
    
    /**
     * 更新用户信息
     * 
     * @param userId 用户ID
     * @param userInfo 用户信息
     * @return 更新后的用户信息
     */
    UserInfoResponse updateUserInfo(Long userId, UserInfoResponse userInfo);
    
    /**
     * 更新用户资料
     * 
     * @param userId 用户ID
     * @param updateRequest 更新请求
     * @return 更新后的用户信息
     */
    UserInfoResponse updateUserProfile(Long userId, UpdateProfileRequest updateRequest);
    
    /**
     * 更新用户状态
     * 
     * @param userId 用户ID
     * @param status 用户状态
     */
    void updateUserStatus(Long userId, String status);
    
    /**
     * 更新用户最后登录时间
     * 
     * @param userId 用户ID
     * @param lastLoginTime 最后登录时间
     */
    void updateLastLoginTime(Long userId, LocalDateTime lastLoginTime);
    
    /**
     * 搜索用户（按昵称模糊查询）
     * 
     * @param keyword 关键字
     * @param pageable 分页参数
     * @return 用户列表
     */
    Page<UserInfoResponse> searchUsers(String keyword, Pageable pageable);
    
    /**
     * 获取活跃用户列表
     * 
     * @param since 时间范围
     * @param pageable 分页参数
     * @return 活跃用户列表
     */
    Page<UserInfoResponse> getActiveUsers(LocalDateTime since, Pageable pageable);
    
    /**
     * 获取用户统计信息
     * 
     * @param userId 用户ID
     * @return 统计信息
     */
    UserStatistics getUserStatistics(Long userId);
    
    /**
     * 检查用户是否存在
     * 
     * @param openid OpenID
     * @return 是否存在
     */
    boolean existsByOpenid(String openid);
    
    /**
     * 检查UnionID是否存在
     * 
     * @param unionid UnionID
     * @return 是否存在
     */
    boolean existsByUnionid(String unionid);
    
    /**
     * 统计活跃用户数量
     * 
     * @param since 时间范围
     * @return 活跃用户数量
     */
    Long countActiveUsers(LocalDateTime since);
    
    /**
     * 获取用户实体（内部使用）
     * 
     * @param userId 用户ID
     * @return 用户实体
     */
    Optional<User> getUserEntity(Long userId);
    
    /**
     * 保存用户实体（内部使用）
     * 
     * @param user 用户实体
     * @return 保存后的用户实体
     */
    User saveUser(User user);
    
    /**
     * 用户统计信息内部类
     */
    public static class UserStatistics implements Serializable {
        @JsonProperty("totalRounds")
        private Long totalRounds;
        
        @JsonProperty("winRounds")
        private Long winRounds;
        
        @JsonProperty("loseRounds")
        private Long loseRounds;
        
        @JsonProperty("drawRounds")
        private Long drawRounds;
        
        @JsonProperty("totalAmount")
        private Long totalAmount;
        
        @JsonProperty("winAmount")
        private Long winAmount;
        
        @JsonProperty("winRate")
        private Double winRate;
        
        public UserStatistics() {
        }
        
        public UserStatistics(Long totalRounds, Long winRounds, Long loseRounds, Long drawRounds, Long totalAmount, Long winAmount) {
            this.totalRounds = totalRounds;
            this.winRounds = winRounds;
            this.loseRounds = loseRounds;
            this.drawRounds = drawRounds;
            this.totalAmount = totalAmount;
            this.winAmount = winAmount;
            this.winRate = totalRounds > 0 ? (double) winRounds / totalRounds : 0.0;
        }
        
        // Getter and Setter methods
        public Long getTotalRounds() {
            return totalRounds;
        }
        
        public void setTotalRounds(Long totalRounds) {
            this.totalRounds = totalRounds;
        }
        
        public Long getWinRounds() {
            return winRounds;
        }
        
        public void setWinRounds(Long winRounds) {
            this.winRounds = winRounds;
        }
        
        public Long getLoseRounds() {
            return loseRounds;
        }
        
        public void setLoseRounds(Long loseRounds) {
            this.loseRounds = loseRounds;
        }
        
        public Long getDrawRounds() {
            return drawRounds;
        }
        
        public void setDrawRounds(Long drawRounds) {
            this.drawRounds = drawRounds;
        }
        
        public Long getTotalAmount() {
            return totalAmount;
        }
        
        public void setTotalAmount(Long totalAmount) {
            this.totalAmount = totalAmount;
        }
        
        public Long getWinAmount() {
            return winAmount;
        }
        
        public void setWinAmount(Long winAmount) {
            this.winAmount = winAmount;
        }
        
        public Double getWinRate() {
            return winRate;
        }
        
        public void setWinRate(Double winRate) {
            this.winRate = winRate;
        }
    }
}