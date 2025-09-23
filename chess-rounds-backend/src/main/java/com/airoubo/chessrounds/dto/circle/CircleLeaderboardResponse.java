package com.airoubo.chessrounds.dto.circle;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 圈子排行榜响应DTO
 * 
 * @author AiRoubo
 * @version 1.0.0
 */
public class CircleLeaderboardResponse implements Serializable {
    
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
     * 排名
     */
    @JsonProperty("rank")
    private Integer rank;
    
    /**
     * 积分
     */
    @JsonProperty("score")
    private Integer score;
    
    /**
     * 胜场数
     */
    @JsonProperty("wins")
    private Integer wins;
    
    /**
     * 负场数
     */
    @JsonProperty("losses")
    private Integer losses;
    
    /**
     * 平局数
     */
    @JsonProperty("draws")
    private Integer draws;
    
    /**
     * 总场数
     */
    @JsonProperty("total_games")
    private Integer totalGames;
    
    /**
     * 胜率
     */
    @JsonProperty("win_rate")
    private Double winRate;
    
    /**
     * 最后更新时间
     */
    @JsonProperty("last_updated")
    private LocalDateTime lastUpdated;
    
    /**
     * 排名变化（相比上次）
     */
    @JsonProperty("rank_change")
    private Integer rankChange;
    
    /**
     * 是否是当前用户
     */
    @JsonProperty("is_current_user")
    private Boolean isCurrentUser;
    
    public CircleLeaderboardResponse() {
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
    
    public Integer getRank() {
        return rank;
    }
    
    public void setRank(Integer rank) {
        this.rank = rank;
    }
    
    public Integer getScore() {
        return score;
    }
    
    public void setScore(Integer score) {
        this.score = score;
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
    
    public Integer getTotalGames() {
        return totalGames;
    }
    
    public void setTotalGames(Integer totalGames) {
        this.totalGames = totalGames;
    }
    
    public Double getWinRate() {
        return winRate;
    }
    
    public void setWinRate(Double winRate) {
        this.winRate = winRate;
    }
    
    public LocalDateTime getLastUpdated() {
        return lastUpdated;
    }
    
    public void setLastUpdated(LocalDateTime lastUpdated) {
        this.lastUpdated = lastUpdated;
    }
    
    public Integer getRankChange() {
        return rankChange;
    }
    
    public void setRankChange(Integer rankChange) {
        this.rankChange = rankChange;
    }
    
    public Boolean getIsCurrentUser() {
        return isCurrentUser;
    }
    
    public void setIsCurrentUser(Boolean isCurrentUser) {
        this.isCurrentUser = isCurrentUser;
    }
}