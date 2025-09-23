package com.airoubo.chessrounds.dto.circle;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 圈子成员响应DTO
 * 
 * @author AiRoubo
 * @version 1.0.0
 */
public class CircleMemberResponse implements Serializable {
    
    private static final long serialVersionUID = 1L;
    
    /**
     * 用户ID
     */
    @JsonProperty("user_id")
    private Long userId;
    
    /**
     * 用户昵称
     */
    @JsonProperty("nickname")
    private String nickname;
    
    /**
     * 用户头像URL
     */
    @JsonProperty("avatar_url")
    private String avatarUrl;
    
    /**
     * 在圈子中的昵称
     */
    @JsonProperty("circle_nickname")
    private String circleNickname;
    
    /**
     * 在圈子中的头像URL
     */
    @JsonProperty("circle_avatar_url")
    private String circleAvatarUrl;
    
    /**
     * 在圈子中的角色
     */
    @JsonProperty("role")
    private String role;
    
    /**
     * 加入时间
     */
    @JsonProperty("joined_at")
    private LocalDateTime joinedAt;
    
    /**
     * 最后活动时间
     */
    @JsonProperty("last_activity_time")
    private LocalDateTime lastActivityTime;
    
    /**
     * 是否在线
     */
    @JsonProperty("is_online")
    private Boolean isOnline;
    
    public CircleMemberResponse() {
    }
    
    public Long getUserId() {
        return userId;
    }
    
    public void setUserId(Long userId) {
        this.userId = userId;
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
    
    public String getCircleNickname() {
        return circleNickname;
    }
    
    public void setCircleNickname(String circleNickname) {
        this.circleNickname = circleNickname;
    }
    
    public String getCircleAvatarUrl() {
        return circleAvatarUrl;
    }
    
    public void setCircleAvatarUrl(String circleAvatarUrl) {
        this.circleAvatarUrl = circleAvatarUrl;
    }
    
    public String getRole() {
        return role;
    }
    
    public void setRole(String role) {
        this.role = role;
    }
    
    public LocalDateTime getJoinedAt() {
        return joinedAt;
    }
    
    public void setJoinedAt(LocalDateTime joinedAt) {
        this.joinedAt = joinedAt;
    }
    
    public LocalDateTime getLastActivityTime() {
        return lastActivityTime;
    }
    
    public void setLastActivityTime(LocalDateTime lastActivityTime) {
        this.lastActivityTime = lastActivityTime;
    }
    
    public Boolean getIsOnline() {
        return isOnline;
    }
    
    public void setIsOnline(Boolean isOnline) {
        this.isOnline = isOnline;
    }
}