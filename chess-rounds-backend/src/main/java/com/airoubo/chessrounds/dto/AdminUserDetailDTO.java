package com.airoubo.chessrounds.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 管理员用户详情DTO
 */
public class AdminUserDetailDTO {
    
    private Long userId;
    private String nickname;
    private BigDecimal totalAmount;
    private Long roundCount;
    private LocalDateTime lastActiveAt;
    
    public AdminUserDetailDTO() {}
    
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
    
    public BigDecimal getTotalAmount() {
        return totalAmount;
    }
    
    public void setTotalAmount(BigDecimal totalAmount) {
        this.totalAmount = totalAmount;
    }
    
    public Long getRoundCount() {
        return roundCount;
    }
    
    public void setRoundCount(Long roundCount) {
        this.roundCount = roundCount;
    }
    
    public LocalDateTime getLastActiveAt() {
        return lastActiveAt;
    }
    
    public void setLastActiveAt(LocalDateTime lastActiveAt) {
        this.lastActiveAt = lastActiveAt;
    }
}