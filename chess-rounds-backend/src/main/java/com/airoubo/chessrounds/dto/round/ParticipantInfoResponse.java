package com.airoubo.chessrounds.dto.round;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.airoubo.chessrounds.dto.user.UserInfoResponse;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 参与者信息响应DTO
 * 
 * @author AiRoubo
 * @version 1.0.0
 */
public class ParticipantInfoResponse implements Serializable {
    
    private static final long serialVersionUID = 1L;
    
    /**
     * 参与者ID
     */
    @JsonProperty("participant_id")
    private Long participantId;
    
    /**
     * 用户信息
     */
    @JsonProperty("user_info")
    private UserInfoResponse userInfo;
    
    /**
     * 参与角色
     */
    @JsonProperty("role")
    private String role;
    
    /**
     * 座位号
     */
    @JsonProperty("seat_number")
    private Integer seatNumber;
    
    /**
     * 当前金额
     */
    @JsonProperty("current_amount")
    private BigDecimal currentAmount;
    
    /**
     * 总投入金额
     */
    @JsonProperty("total_bet_amount")
    private BigDecimal totalBetAmount;
    
    /**
     * 总赢得金额
     */
    @JsonProperty("total_win_amount")
    private BigDecimal totalWinAmount;
    
    /**
     * 是否活跃
     */
    @JsonProperty("is_active")
    private Boolean isActive;
    
    /**
     * 是否在线
     */
    @JsonProperty("is_online")
    private Boolean isOnline;
    
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
     * 胜负记录
     */
    @JsonProperty("win_count")
    private Integer winCount;
    
    /**
     * 总局数
     */
    @JsonProperty("total_games")
    private Integer totalGames;
    
    /**
     * 胜率
     */
    @JsonProperty("win_rate")
    private Double winRate;
    
    public ParticipantInfoResponse() {
    }
    
    // Getter and Setter methods
    public Long getParticipantId() {
        return participantId;
    }
    
    public void setParticipantId(Long participantId) {
        this.participantId = participantId;
    }
    
    public UserInfoResponse getUserInfo() {
        return userInfo;
    }
    
    public void setUserInfo(UserInfoResponse userInfo) {
        this.userInfo = userInfo;
    }
    
    public String getRole() {
        return role;
    }
    
    public void setRole(String role) {
        this.role = role;
    }
    
    public Integer getSeatNumber() {
        return seatNumber;
    }
    
    public void setSeatNumber(Integer seatNumber) {
        this.seatNumber = seatNumber;
    }
    
    public BigDecimal getCurrentAmount() {
        return currentAmount;
    }
    
    public void setCurrentAmount(BigDecimal currentAmount) {
        this.currentAmount = currentAmount;
    }
    
    public BigDecimal getTotalBetAmount() {
        return totalBetAmount;
    }
    
    public void setTotalBetAmount(BigDecimal totalBetAmount) {
        this.totalBetAmount = totalBetAmount;
    }
    
    public BigDecimal getTotalWinAmount() {
        return totalWinAmount;
    }
    
    public void setTotalWinAmount(BigDecimal totalWinAmount) {
        this.totalWinAmount = totalWinAmount;
    }
    
    public Boolean getIsActive() {
        return isActive;
    }
    
    public void setIsActive(Boolean isActive) {
        this.isActive = isActive;
    }
    
    public Boolean getIsOnline() {
        return isOnline;
    }
    
    public void setIsOnline(Boolean isOnline) {
        this.isOnline = isOnline;
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
    
    public Integer getWinCount() {
        return winCount;
    }
    
    public void setWinCount(Integer winCount) {
        this.winCount = winCount;
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
}