package com.airoubo.chessrounds.entity;

import com.airoubo.chessrounds.enums.StatType;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * 用户统计实体类
 * 
 * @author AiRoubo
 * @version 1.0.0
 */
@Entity
@Table(name = "user_statistics", 
    uniqueConstraints = {
        @UniqueConstraint(name = "uk_user_stat", columnNames = {"user_id", "stat_date", "stat_type"})
    },
    indexes = {
        @Index(name = "idx_user_id", columnList = "user_id"),
        @Index(name = "idx_stat_date", columnList = "stat_date"),
        @Index(name = "idx_stat_type", columnList = "stat_type")
    }
)
public class UserStatistics extends BaseEntity {
    
    @Column(name = "user_id", nullable = false)
    private Long userId;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", insertable = false, updatable = false)
    private User user;
    
    @Column(name = "stat_date", nullable = false)
    private LocalDate statDate;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "stat_type", nullable = false)
    private StatType statType;
    
    @Column(name = "total_rounds")
    private Integer totalRounds = 0;
    
    @Column(name = "win_rounds")
    private Integer winRounds = 0;
    
    @Column(name = "total_amount", precision = 15, scale = 2)
    private BigDecimal totalAmount = BigDecimal.ZERO;
    
    @Column(name = "win_amount", precision = 15, scale = 2)
    private BigDecimal winAmount = BigDecimal.ZERO;
    
    @Column(name = "max_single_win", precision = 15, scale = 2)
    private BigDecimal maxSingleWin = BigDecimal.ZERO;
    
    @Column(name = "max_single_lose", precision = 15, scale = 2)
    private BigDecimal maxSingleLose = BigDecimal.ZERO;
    
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
    
    public LocalDate getStatDate() {
        return statDate;
    }
    
    public void setStatDate(LocalDate statDate) {
        this.statDate = statDate;
    }
    
    public StatType getStatType() {
        return statType;
    }
    
    public void setStatType(StatType statType) {
        this.statType = statType;
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
    
    public BigDecimal getMaxSingleWin() {
        return maxSingleWin;
    }
    
    public void setMaxSingleWin(BigDecimal maxSingleWin) {
        this.maxSingleWin = maxSingleWin;
    }
    
    public BigDecimal getMaxSingleLose() {
        return maxSingleLose;
    }
    
    public void setMaxSingleLose(BigDecimal maxSingleLose) {
        this.maxSingleLose = maxSingleLose;
    }
}