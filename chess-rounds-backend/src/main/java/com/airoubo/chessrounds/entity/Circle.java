package com.airoubo.chessrounds.entity;

import com.fasterxml.jackson.annotation.JsonFormat;

import jakarta.persistence.*;
import java.time.LocalDateTime;

/**
 * 圈子实体类
 * 
 * @author AiRoubo
 * @version 1.0.0
 */
@Entity
@Table(name = "circles", indexes = {
    @Index(name = "idx_creator_id", columnList = "creator_id"),
    @Index(name = "idx_status", columnList = "status"),
    @Index(name = "idx_created_at", columnList = "created_at"),
    @Index(name = "idx_circle_code", columnList = "circle_code")
})
public class Circle extends BaseEntity {
    
    @Column(name = "name", nullable = false, length = 100)
    private String name;
    
    @Column(name = "description", length = 500)
    private String description;
    
    @Column(name = "creator_id", nullable = false)
    private Long creatorId;
    
    @Column(name = "circle_code", nullable = false, unique = true, length = 20)
    private String circleCode;
    
    @Column(name = "join_code", nullable = false, unique = true, length = 5)
    private String joinCode;
    
    @Column(name = "max_members")
    private Integer maxMembers = 100; // 最大成员数，默认100
    
    @Column(name = "current_members")
    private Integer currentMembers = 1; // 当前成员数，默认1（创建者）
    
    @Column(name = "is_public")
    private Boolean isPublic = true; // 是否公开，默认公开
    
    @Column(name = "status")
    private Integer status = 1; // 0-禁用，1-正常
    
    @Column(name = "avatar_url", length = 500)
    private String avatarUrl;
    
    @Column(name = "last_activity_at")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime lastActivityAt;
    
    // Getters and Setters
    public String getName() {
        return name;
    }
    
    public void setName(String name) {
        this.name = name;
    }
    
    public String getDescription() {
        return description;
    }
    
    public void setDescription(String description) {
        this.description = description;
    }
    
    public Long getCreatorId() {
        return creatorId;
    }
    
    public void setCreatorId(Long creatorId) {
        this.creatorId = creatorId;
    }
    
    public String getCircleCode() {
        return circleCode;
    }
    
    public void setCircleCode(String circleCode) {
        this.circleCode = circleCode;
    }
    
    public String getJoinCode() {
        return joinCode;
    }
    
    public void setJoinCode(String joinCode) {
        this.joinCode = joinCode;
    }
    
    public Integer getMaxMembers() {
        return maxMembers;
    }
    
    public void setMaxMembers(Integer maxMembers) {
        this.maxMembers = maxMembers;
    }
    
    public Integer getCurrentMembers() {
        return currentMembers;
    }
    
    public void setCurrentMembers(Integer currentMembers) {
        this.currentMembers = currentMembers;
    }
    
    public Boolean getIsPublic() {
        return isPublic;
    }
    
    public void setIsPublic(Boolean isPublic) {
        this.isPublic = isPublic;
    }
    
    public Integer getStatus() {
        return status;
    }
    
    public void setStatus(Integer status) {
        this.status = status;
    }
    
    public String getAvatarUrl() {
        return avatarUrl;
    }
    
    public void setAvatarUrl(String avatarUrl) {
        this.avatarUrl = avatarUrl;
    }
    
    public LocalDateTime getLastActivityAt() {
        return lastActivityAt;
    }
    
    public void setLastActivityAt(LocalDateTime lastActivityAt) {
        this.lastActivityAt = lastActivityAt;
    }
}