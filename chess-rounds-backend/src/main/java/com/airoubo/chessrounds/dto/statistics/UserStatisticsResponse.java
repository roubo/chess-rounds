package com.airoubo.chessrounds.dto.statistics;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.airoubo.chessrounds.dto.user.UserInfoResponse;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * 用户统计响应DTO
 * 
 * @author AiRoubo
 * @version 1.0.0
 */
public class UserStatisticsResponse implements Serializable {
    
    private static final long serialVersionUID = 1L;
    
    /**
     * 统计ID
     */
    @JsonProperty("stat_id")
    private Long statId;
    
    /**
     * 用户信息
     */
    @JsonProperty("user_info")
    private UserInfoResponse userInfo;
    
    /**
     * 统计日期
     */
    @JsonProperty("stat_date")
    private LocalDate statDate;
    
    /**
     * 统计类型
     */
    @JsonProperty("stat_type")
    private String statType;
    
    /**
     * 总回合数
     */
    @JsonProperty("total_rounds")
    private Integer totalRounds;
    
    /**
     * 胜利回合数
     */
    @JsonProperty("win_rounds")
    private Integer winRounds;
    
    /**
     * 失败回合数
     */
    @JsonProperty("lose_rounds")
    private Integer loseRounds;
    
    /**
     * 胜率
     */
    @JsonProperty("win_rate")
    private Double winRate;
    
    /**
     * 总金额
     */
    @JsonProperty("total_amount")
    private BigDecimal totalAmount;
    
    /**
     * 胜利金额
     */
    @JsonProperty("win_amount")
    private BigDecimal winAmount;
    
    /**
     * 失败金额
     */
    @JsonProperty("lose_amount")
    private BigDecimal loseAmount;
    
    /**
     * 净收益
     */
    @JsonProperty("net_profit")
    private BigDecimal netProfit;
    
    /**
     * 最大单次胜利
     */
    @JsonProperty("max_single_win")
    private BigDecimal maxSingleWin;
    
    /**
     * 最大单次失败
     */
    @JsonProperty("max_single_lose")
    private BigDecimal maxSingleLose;
    
    /**
     * 平均每回合金额
     */
    @JsonProperty("avg_amount_per_round")
    private BigDecimal avgAmountPerRound;
    
    /**
     * 连胜次数
     */
    @JsonProperty("consecutive_wins")
    private Integer consecutiveWins;
    
    /**
     * 连败次数
     */
    @JsonProperty("consecutive_losses")
    private Integer consecutiveLosses;
    
    /**
     * 排名
     */
    @JsonProperty("ranking")
    private Integer ranking;
    
    /**
     * 总排名数
     */
    @JsonProperty("total_rankings")
    private Integer totalRankings;
    
    public UserStatisticsResponse() {
    }
    
    // Getter and Setter methods
    public Long getStatId() {
        return statId;
    }
    
    public void setStatId(Long statId) {
        this.statId = statId;
    }
    
    public UserInfoResponse getUserInfo() {
        return userInfo;
    }
    
    public void setUserInfo(UserInfoResponse userInfo) {
        this.userInfo = userInfo;
    }
    
    public LocalDate getStatDate() {
        return statDate;
    }
    
    public void setStatDate(LocalDate statDate) {
        this.statDate = statDate;
    }
    
    public String getStatType() {
        return statType;
    }
    
    public void setStatType(String statType) {
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
    
    public Integer getLoseRounds() {
        return loseRounds;
    }
    
    public void setLoseRounds(Integer loseRounds) {
        this.loseRounds = loseRounds;
    }
    
    public Double getWinRate() {
        return winRate;
    }
    
    public void setWinRate(Double winRate) {
        this.winRate = winRate;
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
    
    public BigDecimal getLoseAmount() {
        return loseAmount;
    }
    
    public void setLoseAmount(BigDecimal loseAmount) {
        this.loseAmount = loseAmount;
    }
    
    public BigDecimal getNetProfit() {
        return netProfit;
    }
    
    public void setNetProfit(BigDecimal netProfit) {
        this.netProfit = netProfit;
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
    
    public BigDecimal getAvgAmountPerRound() {
        return avgAmountPerRound;
    }
    
    public void setAvgAmountPerRound(BigDecimal avgAmountPerRound) {
        this.avgAmountPerRound = avgAmountPerRound;
    }
    
    public Integer getConsecutiveWins() {
        return consecutiveWins;
    }
    
    public void setConsecutiveWins(Integer consecutiveWins) {
        this.consecutiveWins = consecutiveWins;
    }
    
    public Integer getConsecutiveLosses() {
        return consecutiveLosses;
    }
    
    public void setConsecutiveLosses(Integer consecutiveLosses) {
        this.consecutiveLosses = consecutiveLosses;
    }
    
    public Integer getRanking() {
        return ranking;
    }
    
    public void setRanking(Integer ranking) {
        this.ranking = ranking;
    }
    
    public Integer getTotalRankings() {
        return totalRankings;
    }
    
    public void setTotalRankings(Integer totalRankings) {
        this.totalRankings = totalRankings;
    }
}