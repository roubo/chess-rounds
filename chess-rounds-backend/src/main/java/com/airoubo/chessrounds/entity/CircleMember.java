package com.airoubo.chessrounds.entity;

import com.fasterxml.jackson.annotation.JsonFormat;

import jakarta.persistence.*;
import java.time.LocalDateTime;

/**
 * 圈子成员实体类
 * 
 * @author AiRoubo
 * @version 1.0.0
 */
@Entity
@Table(name = "circle_members", indexes = {
    @Index(name = "idx_circle_id", columnList = "circle_id"),
    @Index(name = "idx_user_id", columnList = "user_id"),
    @Index(name = "idx_role", columnList = "role"),
    @Index(name = "idx_status", columnList = "status"),
    @Index(name = "idx_joined_at", columnList = "joined_at")
}, uniqueConstraints = {
    @UniqueConstraint(name = "uk_circle_user", columnNames = {"circle_id", "user_id"})
})
public class CircleMember extends BaseEntity {
    
    @Column(name = "circle_id", nullable = false)
    private Long circleId;
    
    @Column(name = "user_id", nullable = false)
    private Long userId;
    
    @Column(name = "role")
    private Integer role = 0; // 0-普通成员，1-管理员，2-创建者
    
    @Column(name = "status")
    private Integer status = 1; // 0-已退出，1-正常
    
    @Column(name = "joined_at")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime joinedAt;
    
    @Column(name = "left_at")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime leftAt;
    
    @Column(name = "nickname", length = 100)
    private String nickname; // 在圈子中的昵称
    
    @Column(name = "avatar_url", length = 500)
    private String avatarUrl; // 在圈子中的头像
    
    // 关联实体
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "circle_id", insertable = false, updatable = false)
    private Circle circle;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", insertable = false, updatable = false)
    private User user;
    
    // Getters and Setters
    public Long getCircleId() {
        return circleId;
    }
    
    public void setCircleId(Long circleId) {
        this.circleId = circleId;
    }
    
    public Long getUserId() {
        return userId;
    }
    
    public void setUserId(Long userId) {
        this.userId = userId;
    }
    
    public Integer getRole() {
        return role;
    }
    
    public void setRole(Integer role) {
        this.role = role;
    }
    
    public Integer getStatus() {
        return status;
    }
    
    public void setStatus(Integer status) {
        this.status = status;
    }
    
    public LocalDateTime getJoinedAt() {
        return joinedAt;
    }
    
    public void setJoinedAt(LocalDateTime joinedAt) {
        this.joinedAt = joinedAt;
    }
    
    public LocalDateTime getLeftAt() {
        return leftAt;
    }
    
    public void setLeftAt(LocalDateTime leftAt) {
        this.leftAt = leftAt;
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
    
    public Circle getCircle() {
        return circle;
    }
    
    public void setCircle(Circle circle) {
        this.circle = circle;
    }
    
    public User getUser() {
        return user;
    }
    
    public void setUser(User user) {
        this.user = user;
    }
}