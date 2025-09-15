package com.airoubo.chessrounds.dto.statistics;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.io.Serializable;

/**
 * 用户统计DTO
 * 
 * @author AiRoubo
 * @version 1.0.0
 */
public class UserStatisticsDTO implements Serializable {
    
    private static final long serialVersionUID = 1L;
    
    /**
     * 总用户数
     */
    @JsonProperty("total_users")
    private Integer totalUsers;
    
    /**
     * 活跃用户数
     */
    @JsonProperty("active_users")
    private Integer activeUsers;
    
    /**
     * 非活跃用户数
     */
    @JsonProperty("inactive_users")
    private Integer inactiveUsers;
    
    /**
     * 今日新增用户数
     */
    @JsonProperty("new_users_today")
    private Integer newUsersToday;
    
    /**
     * 本周新增用户数
     */
    @JsonProperty("new_users_this_week")
    private Integer newUsersThisWeek;
    
    public UserStatisticsDTO() {
    }
    
    public UserStatisticsDTO(Integer totalUsers, Integer activeUsers, Integer inactiveUsers,
                           Integer newUsersToday, Integer newUsersThisWeek) {
        this.totalUsers = totalUsers;
        this.activeUsers = activeUsers;
        this.inactiveUsers = inactiveUsers;
        this.newUsersToday = newUsersToday;
        this.newUsersThisWeek = newUsersThisWeek;
    }
    
    public Integer getTotalUsers() {
        return totalUsers;
    }
    
    public void setTotalUsers(Integer totalUsers) {
        this.totalUsers = totalUsers;
    }
    
    public Integer getActiveUsers() {
        return activeUsers;
    }
    
    public void setActiveUsers(Integer activeUsers) {
        this.activeUsers = activeUsers;
    }
    
    public Integer getInactiveUsers() {
        return inactiveUsers;
    }
    
    public void setInactiveUsers(Integer inactiveUsers) {
        this.inactiveUsers = inactiveUsers;
    }
    
    public Integer getNewUsersToday() {
        return newUsersToday;
    }
    
    public void setNewUsersToday(Integer newUsersToday) {
        this.newUsersToday = newUsersToday;
    }
    
    public Integer getNewUsersThisWeek() {
        return newUsersThisWeek;
    }
    
    public void setNewUsersThisWeek(Integer newUsersThisWeek) {
        this.newUsersThisWeek = newUsersThisWeek;
    }
}