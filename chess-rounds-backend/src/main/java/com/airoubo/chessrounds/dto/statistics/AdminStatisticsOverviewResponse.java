package com.airoubo.chessrounds.dto.statistics;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.io.Serializable;
import java.time.Instant;
import java.util.Map;

/**
 * 管理员统计总览响应DTO
 * 
 * @author AiRoubo
 * @version 1.0.0
 */
public class AdminStatisticsOverviewResponse implements Serializable {
    
    private static final long serialVersionUID = 1L;
    
    /**
     * 回合统计数据
     */
    @JsonProperty("round_statistics")
    private Map<String, RoundStatisticsDTO> roundStatistics;
    
    /**
     * 用户统计数据
     */
    @JsonProperty("user_statistics")
    private UserStatisticsDTO userStatistics;
    
    /**
     * 财务统计数据
     */
    @JsonProperty("financial_statistics")
    private FinancialStatisticsDTO financialStatistics;
    
    /**
     * 统计时间戳
     */
    @JsonProperty("timestamp")
    private Instant timestamp;
    
    public AdminStatisticsOverviewResponse() {
    }
    
    public AdminStatisticsOverviewResponse(Map<String, RoundStatisticsDTO> roundStatistics,
                                         UserStatisticsDTO userStatistics,
                                         FinancialStatisticsDTO financialStatistics,
                                         Instant timestamp) {
        this.roundStatistics = roundStatistics;
        this.userStatistics = userStatistics;
        this.financialStatistics = financialStatistics;
        this.timestamp = timestamp;
    }
    
    public Map<String, RoundStatisticsDTO> getRoundStatistics() {
        return roundStatistics;
    }
    
    public void setRoundStatistics(Map<String, RoundStatisticsDTO> roundStatistics) {
        this.roundStatistics = roundStatistics;
    }
    
    public UserStatisticsDTO getUserStatistics() {
        return userStatistics;
    }
    
    public void setUserStatistics(UserStatisticsDTO userStatistics) {
        this.userStatistics = userStatistics;
    }
    
    public FinancialStatisticsDTO getFinancialStatistics() {
        return financialStatistics;
    }
    
    public void setFinancialStatistics(FinancialStatisticsDTO financialStatistics) {
        this.financialStatistics = financialStatistics;
    }
    
    public Instant getTimestamp() {
        return timestamp;
    }
    
    public void setTimestamp(Instant timestamp) {
        this.timestamp = timestamp;
    }
}