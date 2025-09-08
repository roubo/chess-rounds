package com.airoubo.chessrounds.dto.user;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 用户登录响应DTO
 * 
 * @author AiRoubo
 * @version 1.0.0
 */
public class UserLoginResponse implements Serializable {
    
    private static final long serialVersionUID = 1L;
    
    /**
     * 用户ID
     */
    @JsonProperty("user_id")
    private Long userId;
    
    /**
     * 访问令牌
     */
    @JsonProperty("access_token")
    private String accessToken;
    
    /**
     * 刷新令牌
     */
    @JsonProperty("refresh_token")
    private String refreshToken;
    
    /**
     * 令牌类型
     */
    @JsonProperty("token_type")
    private String tokenType = "Bearer";
    
    /**
     * 令牌过期时间（秒）
     */
    @JsonProperty("expires_in")
    private Long expiresIn;
    
    /**
     * 用户信息
     */
    @JsonProperty("user_info")
    private UserInfoResponse userInfo;
    
    /**
     * 是否为新用户
     */
    @JsonProperty("is_new_user")
    private Boolean isNewUser;
    
    /**
     * 登录时间
     */
    @JsonProperty("login_time")
    private LocalDateTime loginTime;
    
    public UserLoginResponse() {
    }
    
    public UserLoginResponse(Long userId, String accessToken, String refreshToken, Long expiresIn, UserInfoResponse userInfo, Boolean isNewUser) {
        this.userId = userId;
        this.accessToken = accessToken;
        this.refreshToken = refreshToken;
        this.expiresIn = expiresIn;
        this.userInfo = userInfo;
        this.isNewUser = isNewUser;
        this.loginTime = LocalDateTime.now();
    }
    
    // Getter and Setter methods
    public Long getUserId() {
        return userId;
    }
    
    public void setUserId(Long userId) {
        this.userId = userId;
    }
    
    public String getAccessToken() {
        return accessToken;
    }
    
    public void setAccessToken(String accessToken) {
        this.accessToken = accessToken;
    }
    
    public String getRefreshToken() {
        return refreshToken;
    }
    
    public void setRefreshToken(String refreshToken) {
        this.refreshToken = refreshToken;
    }
    
    public String getTokenType() {
        return tokenType;
    }
    
    public void setTokenType(String tokenType) {
        this.tokenType = tokenType;
    }
    
    public Long getExpiresIn() {
        return expiresIn;
    }
    
    public void setExpiresIn(Long expiresIn) {
        this.expiresIn = expiresIn;
    }
    
    public UserInfoResponse getUserInfo() {
        return userInfo;
    }
    
    public void setUserInfo(UserInfoResponse userInfo) {
        this.userInfo = userInfo;
    }
    
    public Boolean getIsNewUser() {
        return isNewUser;
    }
    
    public void setIsNewUser(Boolean isNewUser) {
        this.isNewUser = isNewUser;
    }
    
    public LocalDateTime getLoginTime() {
        return loginTime;
    }
    
    public void setLoginTime(LocalDateTime loginTime) {
        this.loginTime = loginTime;
    }
}