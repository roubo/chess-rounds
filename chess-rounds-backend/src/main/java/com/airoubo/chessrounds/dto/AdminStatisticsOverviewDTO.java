package com.airoubo.chessrounds.dto;

import java.math.BigDecimal;

/**
 * 管理员统计概览DTO
 */
public class AdminStatisticsOverviewDTO {
    
    private Long totalUsers;
    private Long activeUsers;
    private Long totalRounds;
    private BigDecimal totalAmount;
    private BigDecimal todayAmount;
    private BigDecimal weekAmount;
    private BigDecimal monthAmount;
    
    public AdminStatisticsOverviewDTO() {}
    
    public Long getTotalUsers() {
        return totalUsers;
    }
    
    public void setTotalUsers(Long totalUsers) {
        this.totalUsers = totalUsers;
    }
    
    public Long getActiveUsers() {
        return activeUsers;
    }
    
    public void setActiveUsers(Long activeUsers) {
        this.activeUsers = activeUsers;
    }
    
    public Long getTotalRounds() {
        return totalRounds;
    }
    
    public void setTotalRounds(Long totalRounds) {
        this.totalRounds = totalRounds;
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
    
    public BigDecimal getWeekAmount() {
        return weekAmount;
    }
    
    public void setWeekAmount(BigDecimal weekAmount) {
        this.weekAmount = weekAmount;
    }
    
    public BigDecimal getMonthAmount() {
        return monthAmount;
    }
    
    public void setMonthAmount(BigDecimal monthAmount) {
        this.monthAmount = monthAmount;
    }
}