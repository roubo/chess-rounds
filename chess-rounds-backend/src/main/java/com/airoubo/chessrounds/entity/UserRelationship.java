package com.airoubo.chessrounds.entity;

import com.airoubo.chessrounds.enums.RelationshipType;
import com.fasterxml.jackson.annotation.JsonFormat;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 用户关系实体类
 * 
 * @author AiRoubo
 * @version 1.0.0
 */
@Entity
@Table(name = "user_relationships", 
    uniqueConstraints = {
        @UniqueConstraint(name = "uk_user_relationship", columnNames = {"user_id", "target_user_id", "relationship_type"})
    },
    indexes = {
        @Index(name = "idx_user_id", columnList = "user_id"),
        @Index(name = "idx_target_user", columnList = "target_user_id"),
        @Index(name = "idx_relationship_type", columnList = "relationship_type")
    }
)
public class UserRelationship extends BaseEntity {
    
    @Column(name = "user_id", nullable = false)
    private Long userId;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", insertable = false, updatable = false)
    private User user;
    
    @Column(name = "target_user_id", nullable = false)
    private Long targetUserId;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "target_user_id", insertable = false, updatable = false)
    private User targetUser;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "relationship_type", nullable = false)
    private RelationshipType relationshipType;
    
    @Column(name = "total_rounds")
    private Integer totalRounds = 0;
    
    @Column(name = "win_rounds")
    private Integer winRounds = 0;
    
    @Column(name = "total_amount", precision = 15, scale = 2)
    private BigDecimal totalAmount = BigDecimal.ZERO;
    
    @Column(name = "win_amount", precision = 15, scale = 2)
    private BigDecimal winAmount = BigDecimal.ZERO;
    
    @Column(name = "last_play_time")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime lastPlayTime;
    
    // Getters and Setters
    public Long getUserId() {
        return userId;
    }
    
    public void setUserId(Long userId) {
        this.userId = userId;
    }
    
    public User getUser() {
        return user;
    }
    
    public void setUser(User user) {
        this.user = user;
    }
    
    public Long getTargetUserId() {
        return targetUserId;
    }
    
    public void setTargetUserId(Long targetUserId) {
        this.targetUserId = targetUserId;
    }
    
    public User getTargetUser() {
        return targetUser;
    }
    
    public void setTargetUser(User targetUser) {
        this.targetUser = targetUser;
    }
    
    public RelationshipType getRelationshipType() {
        return relationshipType;
    }
    
    public void setRelationshipType(RelationshipType relationshipType) {
        this.relationshipType = relationshipType;
    }
    
    public Integer getTotalRounds() {
        return totalRounds;
    }
    
    public void setTotalRounds(Integer totalRounds) {
        this.totalRounds = totalRounds;
    }
    
    public Integer getWinRounds() {
        return winRounds;
    }
    
    public void setWinRounds(Integer winRounds) {
        this.winRounds = winRounds;
    }
    
    public BigDecimal getTotalAmount() {
        return totalAmount;
    }
    
    public void setTotalAmount(BigDecimal totalAmount) {
        this.totalAmount = totalAmount;
    }
    
    public BigDecimal getWinAmount() {
        return winAmount;
    }
    
    public void setWinAmount(BigDecimal winAmount) {
        this.winAmount = winAmount;
    }
    
    public LocalDateTime getLastPlayTime() {
        return lastPlayTime;
    }
    
    public void setLastPlayTime(LocalDateTime lastPlayTime) {
        this.lastPlayTime = lastPlayTime;
    }
}