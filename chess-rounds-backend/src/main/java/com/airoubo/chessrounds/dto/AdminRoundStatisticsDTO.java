package com.airoubo.chessrounds.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 管理员回合统计DTO
 */
public class AdminRoundStatisticsDTO {
    
    private Long roundId;
    private String roundName;
    private Long participantCount;
    private BigDecimal totalAmount;
    private LocalDateTime createdAt;
    
    public AdminRoundStatisticsDTO() {}
    
    public Long getRoundId() {
        return roundId;
    }
    
    public void setRoundId(Long roundId) {
        this.roundId = roundId;
    }
    
    public String getRoundName() {
        return roundName;
    }
    
    public void setRoundName(String roundName) {
        this.roundName = roundName;
    }
    
    public Long getParticipantCount() {
        return participantCount;
    }
    
    public void setParticipantCount(Long participantCount) {
        this.participantCount = participantCount;
    }
    
    public BigDecimal getTotalAmount() {
        return totalAmount;
    }
    
    public void setTotalAmount(BigDecimal totalAmount) {
        this.totalAmount = totalAmount;
    }
    
    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
    
    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}