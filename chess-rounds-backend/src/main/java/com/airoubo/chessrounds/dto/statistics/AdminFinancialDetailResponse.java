package com.airoubo.chessrounds.dto.statistics;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.Instant;

/**
 * 管理员财务详细信息响应DTO
 * 
 * @author AiRoubo
 * @version 1.0.0
 */
public class AdminFinancialDetailResponse implements Serializable {
    
    private static final long serialVersionUID = 1L;
    
    /**
     * 总流水金额（绝对值）
     */
    @JsonProperty("total_amount_abs")
    private BigDecimal totalAmountAbs;
    
    /**
     * 总收入金额
     */
    @JsonProperty("total_income")
    private BigDecimal totalIncome;
    
    /**
     * 总支出金额
     */
    @JsonProperty("total_expense")
    private BigDecimal totalExpense;
    
    /**
     * 净收益
     */
    @JsonProperty("net_profit")
    private BigDecimal netProfit;
    
    /**
     * 已结束回合数量
     */
    @JsonProperty("finished_rounds_count")
    private Integer finishedRoundsCount;
    
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
    
    /**
     * 最低单回合流水
     */
    @JsonProperty("min_round_amount")
    private BigDecimal minRoundAmount;
    
    /**
     * 统计时间戳
     */
    @JsonProperty("timestamp")
    private Instant timestamp;
    
    public AdminFinancialDetailResponse() {
    }
    
    public AdminFinancialDetailResponse(BigDecimal totalAmountAbs, BigDecimal totalIncome,
                                      BigDecimal totalExpense, BigDecimal netProfit,
                                      Integer finishedRoundsCount, BigDecimal averagePerRound,
                                      BigDecimal maxRoundAmount, BigDecimal minRoundAmount,
                                      Instant timestamp) {
        this.totalAmountAbs = totalAmountAbs;
        this.totalIncome = totalIncome;
        this.totalExpense = totalExpense;
        this.netProfit = netProfit;
        this.finishedRoundsCount = finishedRoundsCount;
        this.averagePerRound = averagePerRound;
        this.maxRoundAmount = maxRoundAmount;
        this.minRoundAmount = minRoundAmount;
        this.timestamp = timestamp;
    }
    
    public BigDecimal getTotalAmountAbs() {
        return totalAmountAbs;
    }
    
    public void setTotalAmountAbs(BigDecimal totalAmountAbs) {
        this.totalAmountAbs = totalAmountAbs;
    }
    
    public BigDecimal getTotalIncome() {
        return totalIncome;
    }
    
    public void setTotalIncome(BigDecimal totalIncome) {
        this.totalIncome = totalIncome;
    }
    
    public BigDecimal getTotalExpense() {
        return totalExpense;
    }
    
    public void setTotalExpense(BigDecimal totalExpense) {
        this.totalExpense = totalExpense;
    }
    
    public BigDecimal getNetProfit() {
        return netProfit;
    }
    
    public void setNetProfit(BigDecimal netProfit) {
        this.netProfit = netProfit;
    }
    
    public Integer getFinishedRoundsCount() {
        return finishedRoundsCount;
    }
    
    public void setFinishedRoundsCount(Integer finishedRoundsCount) {
        this.finishedRoundsCount = finishedRoundsCount;
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
    
    public BigDecimal getMinRoundAmount() {
        return minRoundAmount;
    }
    
    public void setMinRoundAmount(BigDecimal minRoundAmount) {
        this.minRoundAmount = minRoundAmount;
    }
    
    public Instant getTimestamp() {
        return timestamp;
    }
    
    public void setTimestamp(Instant timestamp) {
        this.timestamp = timestamp;
    }
}