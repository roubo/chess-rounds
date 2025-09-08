package com.airoubo.chessrounds.entity;

import com.airoubo.chessrounds.enums.ParticipantRole;
import com.fasterxml.jackson.annotation.JsonFormat;

import jakarta.persistence.*;
import java.time.LocalDateTime;

/**
 * 参与者实体类
 * 
 * @author AiRoubo
 * @version 1.0.0
 */
@Entity
@Table(name = "participants", 
    uniqueConstraints = {
        @UniqueConstraint(name = "uk_round_user", columnNames = {"round_id", "user_id"})
    },
    indexes = {
        @Index(name = "idx_round_id", columnList = "round_id"),
        @Index(name = "idx_user_id", columnList = "user_id"),
        @Index(name = "idx_role", columnList = "role"),
        @Index(name = "idx_joined_at", columnList = "joined_at")
    }
)
public class Participant extends BaseEntity {
    
    @Column(name = "round_id", nullable = false)
    private Long roundId;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "round_id", insertable = false, updatable = false)
    private Round round;
    
    @Column(name = "user_id", nullable = false)
    private Long userId;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", insertable = false, updatable = false)
    private User user;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "role", nullable = false)
    private ParticipantRole role;
    
    @Column(name = "seat_number")
    private Integer seatNumber;
    
    @Column(name = "joined_at")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime joinedAt;
    
    @Column(name = "left_at")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime leftAt;
    
    @Column(name = "is_active")
    private Boolean isActive = true;
    
    // Getters and Setters
    public Long getRoundId() {
        return roundId;
    }
    
    public void setRoundId(Long roundId) {
        this.roundId = roundId;
    }
    
    public Round getRound() {
        return round;
    }
    
    public void setRound(Round round) {
        this.round = round;
    }
    
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
    
    public ParticipantRole getRole() {
        return role;
    }
    
    public void setRole(ParticipantRole role) {
        this.role = role;
    }
    
    public Integer getSeatNumber() {
        return seatNumber;
    }
    
    public void setSeatNumber(Integer seatNumber) {
        this.seatNumber = seatNumber;
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
    
    public Boolean getIsActive() {
        return isActive;
    }
    
    public void setIsActive(Boolean isActive) {
        this.isActive = isActive;
    }
}