package com.airoubo.chessrounds.dto.statistics;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.io.Serializable;
import java.math.BigDecimal;

/**
 * 财务统计DTO
 * 
 * @author AiRoubo
 * @version 1.0.0
 */
public class FinancialStatisticsDTO implements Serializable {
    
    private static final long serialVersionUID = 1L;
    
    /**
     * 总流水金额
     */
    @JsonProperty("total_amount")
    private BigDecimal totalAmount;
    
    /**
     * 今日流水金额
     */
    @JsonProperty("today_amount")
    private BigDecimal todayAmount;
    
    /**
     * 本周流水金额
     */
    @JsonProperty("this_week_amount")
    private BigDecimal thisWeekAmount;
    
    /**
     * 本月流水金额
     */
    @JsonProperty("this_month_amount")
    private BigDecimal thisMonthAmount;
    
    /**
     * 平均每回合流水
     */
    @JsonProperty("average_per_round")
    private BigDecimal averagePerRound;
    
    /**
     * 最高单回合流水
     */
    @JsonProperty("max_round_amount")
    private BigDecimal maxRoundAmount;
    
    public FinancialStatisticsDTO() {
    }
    
    public FinancialStatisticsDTO(BigDecimal totalAmount, BigDecimal todayAmount,
                                BigDecimal thisWeekAmount, BigDecimal thisMonthAmount,
                                BigDecimal averagePerRound, BigDecimal maxRoundAmount) {
        this.totalAmount = totalAmount;
        this.todayAmount = todayAmount;
        this.thisWeekAmount = thisWeekAmount;
        this.thisMonthAmount = thisMonthAmount;
        this.averagePerRound = averagePerRound;
        this.maxRoundAmount = maxRoundAmount;
    }
    
    public BigDecimal getTotalAmount() {
        return totalAmount;
    }
    
    public void setTotalAmount(BigDecimal totalAmount) {
        this.totalAmount = totalAmount;
    }
    
    public BigDecimal getTodayAmount() {
        return todayAmount;
    }
    
    public void setTodayAmount(BigDecimal todayAmount) {
        this.todayAmount = todayAmount;
    }
    
    public BigDecimal getThisWeekAmount() {
        return thisWeekAmount;
    }
    
    public void setThisWeekAmount(BigDecimal thisWeekAmount) {
        this.thisWeekAmount = thisWeekAmount;
    }
    
    public BigDecimal getThisMonthAmount() {
        return thisMonthAmount;
    }
    
    public void setThisMonthAmount(BigDecimal thisMonthAmount) {
        this.thisMonthAmount = thisMonthAmount;
    }
    
    public BigDecimal getAveragePerRound() {
        return averagePerRound;
    }
    
    public void setAveragePerRound(BigDecimal averagePerRound) {
        this.averagePerRound = averagePerRound;
    }
    
    public BigDecimal getMaxRoundAmount() {
        return maxRoundAmount;
    }
    
    public void setMaxRoundAmount(BigDecimal maxRoundAmount) {
        this.maxRoundAmount = maxRoundAmount;
    }
}