package com.airoubo.chessrounds.entity;

import com.airoubo.chessrounds.enums.RoundStatus;
import com.fasterxml.jackson.annotation.JsonFormat;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 回合实体类
 * 
 * @author AiRoubo
 * @version 1.0.0
 */
@Entity
@Table(name = "rounds", indexes = {
    @Index(name = "idx_round_code", columnList = "round_code"),
    @Index(name = "idx_creator_id", columnList = "creator_id"),
    @Index(name = "idx_status", columnList = "status"),
    @Index(name = "idx_created_at", columnList = "created_at")
})
public class Round extends BaseEntity {
    
    @Column(name = "round_code", nullable = false, unique = true, length = 32)
    private String roundCode;
    
    @Column(name = "creator_id", nullable = false)
    private Long creatorId;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "creator_id", insertable = false, updatable = false)
    private User creator;
    
    @Column(name = "game_type", length = 50)
    private String gameType = "mahjong";
    
    @Column(name = "multiplier", precision = 10, scale = 2)
    private BigDecimal multiplier = BigDecimal.ONE;
    
    @Column(name = "has_table")
    private Boolean hasTable = false;
    
    @Column(name = "table_user_id")
    private Long tableUserId;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "table_user_id", insertable = false, updatable = false)
    private User tableUser;
    
    @Column(name = "max_participants")
    private Integer maxParticipants = 4;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "status")
    private RoundStatus status = RoundStatus.WAITING;
    
    @Column(name = "start_time")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime startTime;
    
    @Column(name = "end_time")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime endTime;
    
    @Column(name = "total_amount", precision = 15, scale = 2)
    private BigDecimal totalAmount = BigDecimal.ZERO;
    
    @Column(name = "round_count")
    private Integer roundCount = 0;
    
    @Column(name = "ai_analysis", columnDefinition = "TEXT")
    private String aiAnalysis;
    
    // Getters and Setters
    public String getRoundCode() {
        return roundCode;
    }
    
    public void setRoundCode(String roundCode) {
        this.roundCode = roundCode;
    }
    
    public Long getCreatorId() {
        return creatorId;
    }
    
    public void setCreatorId(Long creatorId) {
        this.creatorId = creatorId;
    }
    
    public User getCreator() {
        return creator;
    }
    
    public void setCreator(User creator) {
        this.creator = creator;
    }
    
    public String getGameType() {
        return gameType;
    }
    
    public void setGameType(String gameType) {
        this.gameType = gameType;
    }
    
    public BigDecimal getMultiplier() {
        return multiplier;
    }
    
    public void setMultiplier(BigDecimal multiplier) {
        this.multiplier = multiplier;
    }
    
    public Boolean getHasTable() {
        return hasTable;
    }
    
    public void setHasTable(Boolean hasTable) {
        this.hasTable = hasTable;
    }
    
    public Long getTableUserId() {
        return tableUserId;
    }
    
    public void setTableUserId(Long tableUserId) {
        this.tableUserId = tableUserId;
    }
    
    public User getTableUser() {
        return tableUser;
    }
    
    public void setTableUser(User tableUser) {
        this.tableUser = tableUser;
    }
    
    public Integer getMaxParticipants() {
        return maxParticipants;
    }
    
    public void setMaxParticipants(Integer maxParticipants) {
        this.maxParticipants = maxParticipants;
    }
    
    public RoundStatus getStatus() {
        return status;
    }
    
    public void setStatus(RoundStatus status) {
        this.status = status;
    }
    
    public LocalDateTime getStartTime() {
        return startTime;
    }
    
    public void setStartTime(LocalDateTime startTime) {
        this.startTime = startTime;
    }
    
    public LocalDateTime getEndTime() {
        return endTime;
    }
    
    public void setEndTime(LocalDateTime endTime) {
        this.endTime = endTime;
    }
    
    public BigDecimal getTotalAmount() {
        return totalAmount;
    }
    
    public void setTotalAmount(BigDecimal totalAmount) {
        this.totalAmount = totalAmount;
    }
    
    public Integer getRoundCount() {
        return roundCount;
    }
    
    public void setRoundCount(Integer roundCount) {
        this.roundCount = roundCount;
    }
    
    public String getAiAnalysis() {
        return aiAnalysis;
    }
    
    public void setAiAnalysis(String aiAnalysis) {
        this.aiAnalysis = aiAnalysis;
    }
}