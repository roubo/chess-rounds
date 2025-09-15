package com.airoubo.chessrounds.dto.statistics;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.Instant;

/**
 * 用户详细信息DTO
 * 
 * @author AiRoubo
 * @version 1.0.0
 */
public class UserDetailDTO implements Serializable {
    
    private static final long serialVersionUID = 1L;
    
    /**
     * 用户ID
     */
    @JsonProperty("user_id")
    private Long userId;
    
    /**
     * 用户名
     */
    @JsonProperty("username")
    private String username;
    
    /**
     * 昵称
     */
    @JsonProperty("nickname")
    private String nickname;
    
    /**
     * 头像URL
     */
    @JsonProperty("avatar_url")
    private String avatarUrl;
    
    /**
     * 注册时间
     */
    @JsonProperty("created_at")
    private Instant createdAt;
    
    /**
     * 最后登录时间
     */
    @JsonProperty("last_login_at")
    private Instant lastLoginAt;
    
    /**
     * 参与回合数
     */
    @JsonProperty("rounds_participated")
    private Integer roundsParticipated;
    
    /**
     * 总流水金额
     */
    @JsonProperty("total_amount")
    private BigDecimal totalAmount;
    
    /**
     * 是否活跃用户
     */
    @JsonProperty("is_active")
    private Boolean isActive;
    
    public UserDetailDTO() {
    }
    
    public UserDetailDTO(Long userId, String username, String nickname, String avatarUrl,
                        Instant createdAt, Instant lastLoginAt, Integer roundsParticipated,
                        BigDecimal totalAmount, Boolean isActive) {
        this.userId = userId;
        this.username = username;
        this.nickname = nickname;
        this.avatarUrl = avatarUrl;
        this.createdAt = createdAt;
        this.lastLoginAt = lastLoginAt;
        this.roundsParticipated = roundsParticipated;
        this.totalAmount = totalAmount;
        this.isActive = isActive;
    }
    
    public Long getUserId() {
        return userId;
    }
    
    public void setUserId(Long userId) {
        this.userId = userId;
    }
    
    public String getUsername() {
        return username;
    }
    
    public void setUsername(String username) {
        this.username = username;
    }
    
    public String getNickname() {
        return nickname;
    }
    
    public void setNickname(String nickname) {
        this.nickname = nickname;
    }
    
    public String getAvatarUrl() {
        return avatarUrl;
    }
    
    public void setAvatarUrl(String avatarUrl) {
        this.avatarUrl = avatarUrl;
    }
    
    public Instant getCreatedAt() {
        return createdAt;
    }
    
    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }
    
    public Instant getLastLoginAt() {
        return lastLoginAt;
    }
    
    public void setLastLoginAt(Instant lastLoginAt) {
        this.lastLoginAt = lastLoginAt;
    }
    
    public Integer getRoundsParticipated() {
        return roundsParticipated;
    }
    
    public void setRoundsParticipated(Integer roundsParticipated) {
        this.roundsParticipated = roundsParticipated;
    }
    
    public BigDecimal getTotalAmount() {
        return totalAmount;
    }
    
    public void setTotalAmount(BigDecimal totalAmount) {
        this.totalAmount = totalAmount;
    }
    
    public Boolean getIsActive() {
        return isActive;
    }
    
    public void setIsActive(Boolean isActive) {
        this.isActive = isActive;
    }
}