package com.airoubo.chessrounds.entity;

import com.fasterxml.jackson.annotation.JsonFormat;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 圈子排行榜实体类
 * 
 * @author AiRoubo
 * @version 1.0.0
 */
@Entity
@Table(name = "circle_leaderboards", indexes = {
    @Index(name = "idx_circle_id", columnList = "circle_id"),
    @Index(name = "idx_user_id", columnList = "user_id"),
    @Index(name = "idx_ranking", columnList = "ranking"),
    @Index(name = "idx_score", columnList = "score"),
    @Index(name = "idx_updated_at", columnList = "updated_at")
}, uniqueConstraints = {
    @UniqueConstraint(name = "uk_circle_user_leaderboard", columnNames = {"circle_id", "user_id"})
})
public class CircleLeaderboard extends BaseEntity {
    
    @Column(name = "circle_id", nullable = false)
    private Long circleId;
    
    @Column(name = "user_id", nullable = false)
    private Long userId;
    
    @Column(name = "ranking")
    private Integer ranking; // 排名
    
    @Column(name = "score", precision = 10, scale = 2)
    private BigDecimal score = BigDecimal.ZERO; // 积分
    
    @Column(name = "total_games")
    private Integer totalGames = 0; // 总对局数
    
    @Column(name = "wins")
    private Integer wins = 0; // 胜局数
    
    @Column(name = "losses")
    private Integer losses = 0; // 负局数
    
    @Column(name = "draws")
    private Integer draws = 0; // 平局数
    
    @Column(name = "win_rate", precision = 5, scale = 2)
    private BigDecimal winRate = BigDecimal.ZERO; // 胜率（百分比格式，如72.00表示72%）
    
    @Column(name = "consecutive_wins")
    private Integer consecutiveWins = 0; // 连胜数
    
    @Column(name = "max_consecutive_wins")
    private Integer maxConsecutiveWins = 0; // 最大连胜数
    
    @Column(name = "last_game_at")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime lastGameAt; // 最后对局时间
    
    @Column(name = "season", length = 20)
    private String season; // 赛季标识
    
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
    
    public Integer getRanking() {
        return ranking;
    }
    
    public void setRanking(Integer ranking) {
        this.ranking = ranking;
    }
    
    public BigDecimal getScore() {
        return score;
    }
    
    public void setScore(BigDecimal score) {
        this.score = score;
    }
    
    public Integer getTotalGames() {
        return totalGames;
    }
    
    public void setTotalGames(Integer totalGames) {
        this.totalGames = totalGames;
    }
    
    public Integer getWins() {
        return wins;
    }
    
    public void setWins(Integer wins) {
        this.wins = wins;
    }
    
    public Integer getLosses() {
        return losses;
    }
    
    public void setLosses(Integer losses) {
        this.losses = losses;
    }
    
    public Integer getDraws() {
        return draws;
    }
    
    public void setDraws(Integer draws) {
        this.draws = draws;
    }
    
    public BigDecimal getWinRate() {
        return winRate;
    }
    
    public void setWinRate(BigDecimal winRate) {
        this.winRate = winRate;
    }
    
    public Integer getConsecutiveWins() {
        return consecutiveWins;
    }
    
    public void setConsecutiveWins(Integer consecutiveWins) {
        this.consecutiveWins = consecutiveWins;
    }
    
    public Integer getMaxConsecutiveWins() {
        return maxConsecutiveWins;
    }
    
    public void setMaxConsecutiveWins(Integer maxConsecutiveWins) {
        this.maxConsecutiveWins = maxConsecutiveWins;
    }
    
    public LocalDateTime getLastGameAt() {
        return lastGameAt;
    }
    
    public void setLastGameAt(LocalDateTime lastGameAt) {
        this.lastGameAt = lastGameAt;
    }
    
    public String getSeason() {
        return season;
    }
    
    public void setSeason(String season) {
        this.season = season;
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